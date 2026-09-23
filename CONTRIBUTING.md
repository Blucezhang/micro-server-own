# Contributing to micro-server-own

Thank you for considering a contribution. This repository combines Java microservices and a Vben Admin 5 frontend; focused, verifiable changes are much easier to review than broad rewrites.

## Before you start

- Use [Issues](https://github.com/Blucezhang/micro-server-own/issues) for reproducible defects and scoped feature proposals.
- Use [Discussions](https://github.com/Blucezhang/micro-server-own/discussions) for questions and design trade-offs once Discussions is enabled.
- Do not report security-sensitive details in a public issue. Ask the maintainer for a private reporting channel without posting exploit details.
- Read the root [README](README.md), especially the deployment and verification boundaries.

## Local setup

Backend changes require JDK 17. The frontend requires Node.js 22.18+ or 24.12+.

```bash
JAVA_HOME=<jdk17> ./mvnw -B -ntp clean verify
git diff --check
```

For the Vben frontend:

```bash
cd micro-server-own-web-vben
npx -y pnpm@11.16.0 --filter @vben/web-antd run typecheck
npx -y pnpm@11.16.0 --filter @vben/web-antd run build
```

## Contribution workflow

1. Open or find an issue that states the problem and acceptance conditions.
2. Create a branch from the current `master` baseline.
3. Keep the change limited to the relevant service, frontend route, configuration, or document.
4. Update direct callers, validation, examples, and documentation when changing a contract.
5. Run proportionate verification and include the command plus concise result in the pull request.
6. Submit a pull request using the template; explain any skipped check and remaining risk.

## Review expectations

- Preserve existing user changes and avoid drive-by formatting or dependency upgrades.
- Never commit credentials, production endpoints, real payment data, or personal information.
- Prefer explicit tests or manual evidence for behavior changes.
- Treat Outbox, Inbox, RocketMQ, payment, logistics, and production deployment claims carefully: a local test is not end-to-end integration proof.

## Commit messages

Use concise imperative messages. A conventional prefix is welcome when it improves scanning, for example `feat(order): add shipment validation` or `docs: clarify local setup`.
