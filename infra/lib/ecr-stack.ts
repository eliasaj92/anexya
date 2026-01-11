import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ecr from 'aws-cdk-lib/aws-ecr';

export interface EcrStackProps extends cdk.StackProps {
  /** Optional fixed name; otherwise falls back to the Cfn parameter default. */
  repositoryName?: string;
}

export class AnexyaEcrStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: EcrStackProps) {
    super(scope, id, props);

    const repoNameParam = new cdk.CfnParameter(this, 'EcrRepositoryName', {
      type: 'String',
      default: props?.repositoryName ?? 'anexya-api-app',
      description: 'ECR repository name to hold the API image. Adjust to avoid name collisions.',
    });

    const repository = new ecr.Repository(this, 'ApiRepository', {
      repositoryName: repoNameParam.valueAsString,
      imageScanOnPush: true,
      removalPolicy: cdk.RemovalPolicy.RETAIN,
    });

    new cdk.CfnOutput(this, 'RepositoryUri', {
      value: repository.repositoryUri,
    });

    new cdk.CfnOutput(this, 'RepositoryName', {
      value: repository.repositoryName,
    });
  }
}
