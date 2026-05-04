package net.civmc.namelayer.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Path;
import net.civmc.namelayer.velocity.config.NameLayerVelocityConfig;
import net.civmc.namelayer.velocity.rabbitmq.NameLayerWriteRequestConsumer;
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

        dataSource = config.database().createDataSource();
        final NameLayerWriteCoordinator coordinator = new NameLayerWriteCoordinator(dataSource, logger);
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
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
