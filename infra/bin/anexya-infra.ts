#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { AnexyaInfraStack } from '../lib/anexya-infra-stack';
import { AnexyaEcrStack } from '../lib/ecr-stack';

const app = new cdk.App();

const env = {
  account: process.env.CDK_DEFAULT_ACCOUNT ?? process.env.AWS_ACCOUNT_ID ?? process.env.AWS_ACCOUNT ?? process.env.AWS_ACCESS_KEY_ID?.split(':')[0],
  region: process.env.CDK_DEFAULT_REGION ?? process.env.AWS_REGION ?? 'us-east-1',
};

const synthesizer = new cdk.DefaultStackSynthesizer({
  qualifier: 'anexya',
});

new AnexyaEcrStack(app, 'AnexyaEcrStack', {
  env,
  synthesizer,
});

new AnexyaInfraStack(app, 'AnexyaInfraStack', {
  env,
  synthesizer,
});
