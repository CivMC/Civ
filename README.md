# Civ

This monorepo will eventually contain all civ projects and development

## Developing Locally

### Plugins

### Containers
A docker compose stack is provided to help test containers built from
this repo. To start the stack, run the following commands:

1. `gradle :ansible:build`
2. `docker compose up`

Please note that this stack is NOT suitable for production use.

Container data (world, logs, etc.) are mounted at [./containers/data](./containers/data).

Optional services may be started by enabling the profile flag, e.g. `--profile <name>`

Current services and exposed ports are:

| Name     | Ports | Description         | Profile    |
|----------|-------|---------------------|------------|
| proxy    | 25565 | TCP, Minecraft      |            |
| paper    |       |                     |            |
| pvp      |       |                     |            |
| mariadb  | 3306  | TCP, Database       |            |
| postgres | 5432  | TCP, Database       |            |
| rabbitmq | 5672  | TCP, AMQP           |            |
|          | 15672 | HTTP, Management UI |            |
| grafana  | 3000  | HTTP, Grafana UI    | monitoring |

#### Using the console
For the minecraft servers and other interactive containers, you can attach to the console to run commands:

1. run `docker ps`
2. Note the name of the container. By default, randomly generated.
3. Run `docker attach <name>`, for example: `docker attach 81dcee85c1da`

## Licencing
This project and any subprojects not otherwise containing a licence file are licenced under the MIT licence.
Individual plugins may be subject to their own licences, please check the respective plugin directories for details.
