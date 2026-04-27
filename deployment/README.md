# Civ Deployment

This project is a hybrid Gradle/pyinfra deployment project that provisions and deploys services to a server.

Vendored plugins are located in `src/` because Gradle uses them to build the final plugin directories in `build/`.
Configs are located in `files/` because deployment scripts copy them directly to the target hosts.

## Prerequisites

1. Create a user on the server named `actions` with sudo privileges.
2. Create environments in GitHub settings with the following environment secrets:
   - `SSH_KNOWN_HOSTS`
   - `SSH_PRIVATE_KEY`
   - `SUDO_PASSWORD`
3. Create a repo-scoped secret named `SECRETS_YML` with the contents of `variables/secrets.yml`.
4. Create a pyinfra inventory named after the environment in `inventories/`.

## Usage

1. Build dependencies with `gradle :deployment:build`.
2. Install deploy tooling with `pip install pyinfra PyYAML`.
3. Run a deploy with `pyinfra inventories/<inventory>.py deploys/<deploy>.py --sudo --sudo-password '<password>'`.

## TODOs

- Private Config
- Mount backups location and configure params, setup

## Provisioned Layout

```
/
└── opt/
    ├── backup-and-restart.sh
    └── stacks/
        └── <stack>/
            ├── <stack>.yml
            └── ...<service-data>
```

## Deployable Services

```mermaid
graph TD;

subgraph Exposed Ports;
  port_http((80));
  port_https((443));
  port_mc((25565));
  port_vote((8192));
end;

subgraph Minecraft;
  mc_waterfall[Waterfall];
  mc_paper[Paper];
  mc_kira[Kira];
  mc_mariadb[(MariaDB)];
  mc_postgres[(Postgres)];
  mc_rabbitmq[(RabbitMQ)];

  port_mc-->mc_waterfall;
  port_vote-->mc_paper;

  mc_waterfall-->mc_paper;
  mc_waterfall-->mc_postgres;
  mc_paper-->mc_postgres;
  mc_paper-->mc_mariadb;
  mc_paper-->mc_rabbitmq;
  mc_kira-->mc_postgres;
  mc_kira-->mc_rabbitmq;
end;

subgraph Auth;
  auth_keycloak[Keycloak];
  auth_postgres[(Postgres)];

  auth_keycloak-->auth_postgres;
end;

subgraph Monitoring;
  mon_grafana[Grafana];
  mon_loki[Loki];

  mon_grafana-->mon_loki;
  mon_grafana-->mc_postgres;
  mon_grafana-->mc_mariadb;
  mon_grafana-.->auth_keycloak;
end;

subgraph Portainer;
  por_portainer[Portainer];
  por_agent[Portainer Agent];

  por_portainer-->por_agent;
  por_portainer-.->auth_keycloak;
end;

subgraph Traefik;
  tfk_traefik[Traefik];

  port_http-->tfk_traefik;
  port_https-->tfk_traefik;

  tfk_traefik-->mon_grafana;
  tfk_traefik-->por_portainer;
end;
```
