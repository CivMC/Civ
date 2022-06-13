FROM itzg/minecraft-server

# https://github.com/itzg/docker-minecraft-server/blob/master/README.md#versions
ENV VERSION=1.18.2

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
# https://github.com/itzg/docker-minecraft-server#replacing-variables-inside-configs
ENV REPLACE_ENV_DURING_SYNC=TRUE
ENV REPLACE_ENV_VARIABLE_PREFIX=CIV

ENV CIV_WHITELIST=false
