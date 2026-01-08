# Anexya Infra (AWS CDK v2, TypeScript)

Provisioning for the Anexya API: VPC, ECS cluster, ECR repo, and an ALB-backed Fargate service expecting the API container on port 8080 with `/actuator/health` as the health check.

## Prereqs
- Node 18+
- AWS CLI configured (account/region)

## Install & build
```bash
npm install
npm run build
```

## CDK commands
```bash
npm run synth   # cdk synth
npm run diff    # cdk diff
npm run deploy  # cdk deploy AnexyaInfraStack
```

## Deployment flow
1) Build & push the API image to ECR (repo `anexya-api` created by the stack).
2) Deploy the stack so ECS pulls the latest tag.

## Parameters
- `KmsKeyArn` (optional): ARN of the KMS key used by the API to encrypt `referenceCode`. When set, the task role is granted Encrypt/Decrypt permissions and the value is passed to the container as `APP_KMS_KEY_ID`.

## Observability (AWS)
- **Logs**: Fargate task uses the `awslogs` driver with a dedicated CloudWatch Log Group. Stdout/stderr are shipped automatically; retention set to 1 month.
- **Alarms**:
	- Application log alarm on any `ERROR`/`Exception` (5-minute window) via a metric filter on the log group.
	- ALB target 5xx alarm (triggers on any 5xx from the target in 5 minutes).
- **Notes**: Alarms are created without actions; attach SNS/Slack/pager subs in the console or extend the CDK stack.

## Stack outputs
- `LoadBalancerDNS` — public ALB DNS for the service.
- `EcrRepositoryUri` — URI to tag/push the API image.

## Structure
- `bin/anexya-infra.ts` — CDK app entry
- `lib/anexya-infra-stack.ts` — main stack definition
- `cdk.json`, `tsconfig.json`, `package.json` — config and scripts
