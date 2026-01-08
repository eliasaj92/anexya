import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecsPatterns from 'aws-cdk-lib/aws-ecs-patterns';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';

export class AnexyaInfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, 'Vpc', {
      maxAzs: 2,
      natGateways: 1,
    });

    const cluster = new ecs.Cluster(this, 'Cluster', {
      vpc,
      containerInsightsV2: ecs.ContainerInsights.ENABLED,
    });

    const repository = new ecr.Repository(this, 'ApiRepository', {
      repositoryName: 'anexya-api',
    });

    const dbCredentials = new rds.DatabaseSecret(this, 'DbCredentials', {
      username: 'appuser',
    });

    const db = new rds.DatabaseInstance(this, 'TasksDb', {
      engine: rds.DatabaseInstanceEngine.mysql({ version: rds.MysqlEngineVersion.VER_8_0_36 }),
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      credentials: rds.Credentials.fromSecret(dbCredentials),
      allocatedStorage: 20,
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      multiAz: false,
      publiclyAccessible: false,
      deletionProtection: false,
      databaseName: 'tasks',
    });

    const loadBalancedService = new ecsPatterns.ApplicationLoadBalancedFargateService(this, 'FargateService', {
      cluster,
      cpu: 512,
      memoryLimitMiB: 1024,
      desiredCount: 1,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      listenerPort: 80,
      publicLoadBalancer: true,
      taskImageOptions: {
        containerName: 'anexya-api',
        image: ecs.ContainerImage.fromEcrRepository(repository, 'latest'),
        containerPort: 8080,
        environment: {
          JAVA_TOOL_OPTIONS: '-XX:+UseZGC',
          MYSQL_HOST: db.instanceEndpoint.hostname,
          MYSQL_PORT: db.instanceEndpoint.port.toString(),
          MYSQL_DB: 'tagreads',
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
      minCapacity: 1,
      maxCapacity: 4,
    });

    scaling.scaleOnCpuUtilization('CpuScaling', {
      targetUtilizationPercent: 70,
    });

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
