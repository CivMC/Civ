# Civ

This monorepo will eventually contain all civ projects and development

## Developing Locally

### Plugins

### Containers
A docker compose stack is provided to help test containers built from
this repo. To start the stack, run the following command:
`docker compose up --build`. Please note that this stack is NOT suitable for production use.

Optional services may be started by enabling the profile flag, e.g. `--profile <name>`

Current services and exposed ports are:

| Name     | Ports     | Profile    |
|----------|-----------|------------|
| pvp      |           |            |
| mariadb  | TCP/3306  |            |
| postgres | TCP/5432  |            |
| rabbitmq | HTTP/5672 |            |
| grafana  | HTTP/3000 | monitoring |
