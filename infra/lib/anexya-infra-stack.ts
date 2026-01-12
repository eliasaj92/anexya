import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecsPatterns from 'aws-cdk-lib/aws-ecs-patterns';
import * as logs from 'aws-cdk-lib/aws-logs';
import * as cloudwatch from 'aws-cdk-lib/aws-cloudwatch';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as kms from 'aws-cdk-lib/aws-kms';

export class AnexyaInfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const kmsKeyArnParam = new cdk.CfnParameter(this, 'KmsKeyArn', {
      type: 'String',
      default: '',
      description: 'Optional KMS key ARN used by the API for referenceCode encryption. Leave blank to disable.',
    });

    const ecrRepoNameParam = new cdk.CfnParameter(this, 'EcrRepositoryName', {
      type: 'String',
      default: 'anexya-api-app',
      description: 'Name of an existing ECR repository that holds the API image (created by AnexyaEcrStack).',
    });

    const vpc = new ec2.Vpc(this, 'Vpc', {
      maxAzs: 2,
      natGateways: 1,
    });

    const cluster = new ecs.Cluster(this, 'Cluster', {
      vpc,
      containerInsightsV2: ecs.ContainerInsights.ENABLED,
    });

    const logGroup = new logs.LogGroup(this, 'ApiLogGroup', {
      retention: logs.RetentionDays.ONE_MONTH,
    });

    const repository = ecr.Repository.fromRepositoryName(this, 'ApiRepository', ecrRepoNameParam.valueAsString);

    const dbCredentials = new rds.DatabaseSecret(this, 'DbCredentials', {
      username: 'appuser',
    });

    const rdsKmsKey = new kms.Key(this, 'RdsKmsKey', {
      enableKeyRotation: true,
      removalPolicy: cdk.RemovalPolicy.RETAIN,
      description: 'Customer-managed KMS key for RDS encryption at rest',
    });

    const db = new rds.DatabaseInstance(this, 'TagreadsDb', {
      engine: rds.DatabaseInstanceEngine.mysql({ version: rds.MysqlEngineVersion.VER_8_0_40 }),
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      credentials: rds.Credentials.fromSecret(dbCredentials),
      allocatedStorage: 20,
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      multiAz: false,
      publiclyAccessible: false,
      deletionProtection: true,
      backupRetention: cdk.Duration.days(1),
      databaseName: 'tagreads',
      storageEncryptionKey: rdsKmsKey,
    });

    const loadBalancedService = new ecsPatterns.ApplicationLoadBalancedFargateService(this, 'FargateService', {
      cluster,
      cpu: 512,
      memoryLimitMiB: 1024,
      desiredCount: 2,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      listenerPort: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      publicLoadBalancer: true,
      enableExecuteCommand: true,
      taskImageOptions: {
        containerName: 'anexya-api',
        image: ecs.ContainerImage.fromEcrRepository(repository, 'latest'),
        containerPort: 8080,
        logDriver: ecs.LogDrivers.awsLogs({
          logGroup,
          streamPrefix: 'anexya-api'
        }),
        environment: {
          JAVA_TOOL_OPTIONS: '-XX:+UseZGC',
          MYSQL_HOST: db.instanceEndpoint.hostname,
          MYSQL_PORT: db.instanceEndpoint.port.toString(),
          MYSQL_DB: 'tagreads',
          APP_KMS_KEY_ID: kmsKeyArnParam.valueAsString,
          SPRING_PROFILES_ACTIVE: 'mysql,aws',
        },
        secrets: {
          MYSQL_USER: ecs.Secret.fromSecretsManager(dbCredentials, 'username'),
          MYSQL_PASSWORD: ecs.Secret.fromSecretsManager(dbCredentials, 'password'),
        },
      },
    });

    db.connections.allowDefaultPortFrom(loadBalancedService.service.connections, 'Allow ECS tasks to access RDS');

    loadBalancedService.targetGroup.configureHealthCheck({
      path: '/actuator/health',
      healthyHttpCodes: '200-299',
    });

    const scaling = loadBalancedService.service.autoScaleTaskCount({
      minCapacity: 2,
      maxCapacity: 4,
    });

    scaling.scaleOnCpuUtilization('CpuScaling', {
      targetUtilizationPercent: 70,
    });

    // Alarms: application log ERRORs and ALB target 5XX
    const appErrorMetric = new cloudwatch.Metric({
      namespace: 'Anexya/Api',
      metricName: 'ApplicationErrors',
      period: cdk.Duration.minutes(5),
      statistic: 'sum',
    });

    new logs.MetricFilter(this, 'ApiErrorMetricFilter', {
      logGroup,
      metricNamespace: appErrorMetric.namespace!,
      metricName: appErrorMetric.metricName,
      filterPattern: logs.FilterPattern.anyTerm('ERROR', 'Exception'),
      metricValue: '1',
    });

    new cloudwatch.Alarm(this, 'ApiErrorAlarm', {
      metric: appErrorMetric,
      evaluationPeriods: 1,
      threshold: 1,
      datapointsToAlarm: 1,
      treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
      alarmDescription: 'Triggers on any ERROR/Exception in application logs within 5 minutes',
    });

    new cloudwatch.CfnAlarm(this, 'ApiErrorAnomalyAlarm', {
      comparisonOperator: 'GreaterThanUpperThreshold',
      evaluationPeriods: 3,
      datapointsToAlarm: 3,
      treatMissingData: 'notBreaching',
      metrics: [
        {
          id: 'm1',
          metricStat: {
            metric: {
              namespace: appErrorMetric.namespace!,
              metricName: appErrorMetric.metricName,
            },
            period: appErrorMetric.period.toSeconds(),
            stat: appErrorMetric.statistic!,
          },
        },
        {
          id: 'ad1',
          expression: 'ANOMALY_DETECTION_BAND(m1, 2)',
        },
      ],
      thresholdMetricId: 'ad1',
      alarmDescription: 'Triggers when ERROR rate breaches anomaly band (adaptive baseline)',
    });

    const target5xxMetric = loadBalancedService.loadBalancer.metricHttpCodeTarget(elbv2.HttpCodeTarget.TARGET_5XX_COUNT, {
      period: cdk.Duration.minutes(5),
      statistic: 'sum',
    });

    new cloudwatch.Alarm(this, 'Alb5xxAlarm', {
      metric: target5xxMetric,
      evaluationPeriods: 1,
      threshold: 1,
      datapointsToAlarm: 1,
      treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
      alarmDescription: 'Triggers on any ALB target 5xx responses in 5 minutes',
    });

    const targetResponseTimeP90 = loadBalancedService.loadBalancer.metricTargetResponseTime({
      statistic: 'p90',
      period: cdk.Duration.minutes(5),
    });

    new cloudwatch.Alarm(this, 'AlbLatencyP90Alarm', {
      metric: targetResponseTimeP90,
      evaluationPeriods: 1,
      threshold: 1.5, // seconds
      datapointsToAlarm: 1,
      treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
      alarmDescription: 'Triggers when ALB target P90 latency exceeds 1.5s over 5 minutes',
    });

    // Optional: grant task role permissions to use the provided KMS key
    const hasKmsKey = new cdk.CfnCondition(this, 'HasKmsKey', {
      expression: cdk.Fn.conditionNot(cdk.Fn.conditionEquals(kmsKeyArnParam.valueAsString, '')),
    });

    const kmsPolicy = new iam.Policy(this, 'TaskKmsPolicy', {
      statements: [
        new iam.PolicyStatement({
          actions: ['kms:Encrypt', 'kms:Decrypt', 'kms:DescribeKey', 'kms:GenerateDataKey*'],
          resources: [kmsKeyArnParam.valueAsString],
        }),
      ],
    });
    (kmsPolicy.node.defaultChild as iam.CfnPolicy).cfnOptions.condition = hasKmsKey;
    kmsPolicy.attachToRole(loadBalancedService.taskDefinition.taskRole);

    new cdk.CfnOutput(this, 'LoadBalancerDNS', {
      value: loadBalancedService.loadBalancer.loadBalancerDnsName,
    });

    new cdk.CfnOutput(this, 'EcrRepositoryUri', {
      value: repository.repositoryUri,
    });

    new cdk.CfnOutput(this, 'RdsEndpoint', {
      value: db.instanceEndpoint.hostname,
    });
  }
}
