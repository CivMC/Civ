# CivDocker

## Development
1. run `docker-compose up`
2. Connect to minecraft server at `localhost:25565`

Container data (world, logs, etc.) are mounted at [./local](./local).
The `data` directory therin is provided for local verification,
and should not be used in prod.

If changes are made to the docker build, 
you need to use `docker-compose up --build` next time you run.