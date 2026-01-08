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

## Stack outputs
- `LoadBalancerDNS` — public ALB DNS for the service.
- `EcrRepositoryUri` — URI to tag/push the API image.

## Structure
- `bin/anexya-infra.ts` — CDK app entry
- `lib/anexya-infra-stack.ts` — main stack definition
- `cdk.json`, `tsconfig.json`, `package.json` — config and scripts
