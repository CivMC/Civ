# Pin to java17
FROM itzg/minecraft-server@sha256:491e37fa63ffef22c9ac1f6ec2fb4cc793735799cd91bafa1e5790aa39e9b313

# https://github.com/itzg/docker-minecraft-server/blob/master/README.md#versions
ENV VERSION=1.20.4

# https://github.com/itzg/docker-minecraft-server#optional-plugins-mods-and-config-attach-points
COPY plugins /plugins
ENV REMOVE_OLD_MODS=TRUE

# https://github.com/itzg/docker-minecraft-server#optional-plugins-mods-and-config-attach-points
COPY config /config
ENV COPY_CONFIG_DEST=/data
ENV SYNC_SKIP_NEWER_IN_DESTINATION=false

# https://github.com/itzg/docker-minecraft-server#running-a-paper-server
ENV TYPE=PAPER
ENV EULA=TRUE
ENV USE_AIKAR_FLAGS=TRUE
# https://github.com/itzg/docker-minecraft-server#replacing-variables-inside-configs
ENV REPLACE_ENV_DURING_SYNC=TRUE
ENV REPLACE_ENV_VARIABLE_PREFIX=CIV

ENV CIV_WHITELIST=false
