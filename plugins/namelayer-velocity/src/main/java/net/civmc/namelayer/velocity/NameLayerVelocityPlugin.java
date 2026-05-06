package net.civmc.namelayer.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Path;
import java.util.HashSet;
import net.civmc.namelayer.velocity.config.NameLayerVelocityConfig;
import net.civmc.namelayer.velocity.rabbitmq.NameLayerInvalidationPublisher;
import net.civmc.namelayer.velocity.rabbitmq.NameLayerWriteRequestConsumer;
import net.civmc.namelayer.velocity.write.NameLayerDatabaseMigrator;
import net.civmc.namelayer.velocity.write.NameLayerWriteCoordinator;
import org.slf4j.Logger;

@Plugin(
    id = "namelayer",
    name = "NameLayer",
    version = "1.0.0",
    authors = {"Okx"}
)
public final class NameLayerVelocityPlugin {

    private final Logger logger;
    private final Path dataDirectory;
    private HikariDataSource dataSource;
    private NameLayerInvalidationPublisher invalidationPublisher;
    private NameLayerWriteRequestConsumer writeRequestConsumer;

    @Inject
    public NameLayerVelocityPlugin(final Logger logger, @DataDirectory final Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        final NameLayerVelocityConfig config = NameLayerVelocityConfig.load(dataDirectory, logger, getClass());
        if (config == null) {
            logger.error("NameLayer Velocity failed to load configuration");
            return;
        }

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        dataSource = config.database().createDataSource();
        if (!NameLayerDatabaseMigrator.migrate(dataSource, logger, new HashSet<>(config.serverDatabases().values()))) {
            return;
        }
        invalidationPublisher = new NameLayerInvalidationPublisher(config.rabbitMq().connectionFactory(), logger);
        if (!invalidationPublisher.start()) {
            logger.error("NameLayer Velocity failed to start RabbitMQ invalidation publisher");
            return;
        }
        final NameLayerWriteCoordinator coordinator = new NameLayerWriteCoordinator(dataSource, invalidationPublisher, logger, config.serverDatabases());
        writeRequestConsumer = new NameLayerWriteRequestConsumer(config.rabbitMq().connectionFactory(), coordinator, logger);
        if (!writeRequestConsumer.start()) {
            logger.error("NameLayer Velocity failed to start RabbitMQ write request consumer");
        }
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        if (writeRequestConsumer != null) {
            writeRequestConsumer.close();
        }
        if (invalidationPublisher != null) {
            invalidationPublisher.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
