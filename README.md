# Civ

This monorepo will eventually contain all civ projects and development

## Key Technologies

- [Gradle](https://gradle.org/) Is used as an entrypoint to build all projects in this repository.
- [Docker](https://www.docker.com/) Is used to containerize any deployed services,
  and provide a somewhat consistent development environment.
- [Ansible](https://www.ansible.com/) Is used to configure the target machines and for final deployment

## Developing Locally

### Plugins

### Containers
A docker compose stack is provided to help test containers built from
this repo. To start the stack, run the following commands:

1. Linux/MacOS: `./gradlew :ansible:build`  
   Windows: `.\gradlew.bat :ansible:build`
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

### Private Config
Sensitive information is stored in a private repository, and required for Ansible deployment.
If you have access to this, you can get the submodule with `git submodule init` and `git submodule update`

To use this with the local docker-compose stack,
you can use `docker compose up -f docker-compose.yml -f docker-compose.private.yml` to merge the configurations.

Hot tip: If you use different SSH keys for your Civ GitHub account, you might use an SSH alias (`git clone git@civmc.github.com:...`).
If this is the case, you can clone the submodule by setting the`GIT_SSH_COMMAND`environment variable
to `ssh -i /path/to/your/private/key` before updating the submodule.

#### Using the console
For the minecraft servers and other interactive containers, you can attach to the console to run commands:

1. run `docker ps`
2. Note the name of the container. By default, randomly generated.
3. Run `docker attach <name>`, for example: `docker attach 81dcee85c1da`

## Licencing
This project and any subprojects not otherwise containing a licence file are licenced under the MIT licence.
Individual plugins may be subject to their own licences, please check the respective plugin directories for details.
