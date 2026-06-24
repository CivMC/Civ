package net.civmc.namelayer.velocity.rabbitmq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.civmc.namelayer.sync.NameLayerInvalidationMessage;
import net.civmc.namelayer.sync.NameLayerRabbitMqTopology;
import org.slf4j.Logger;

public final class NameLayerInvalidationPublisher implements AutoCloseable {

    private static final long PUBLISH_CONFIRM_TIMEOUT_MILLIS = 5_000L;
    private static final long RECONNECT_DELAY_SECONDS = 5L;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final ConnectionFactory connectionFactory;
    private final ProxyServer proxyServer;
    private final Object plugin;
    private final Logger logger;
    private Connection connection;
    private Channel channel;

    public NameLayerInvalidationPublisher(
        final ConnectionFactory connectionFactory,
        final ProxyServer proxyServer,
        final Object plugin,
        final Logger logger
    ) {
        this.connectionFactory = connectionFactory;
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.logger = logger;
    }

    public boolean start() {
        return connect();
    }

    private synchronized boolean connect() {
        try {
            final Connection newConnection = connectionFactory.newConnection("namelayer-velocity-invalidations");
            connection = newConnection;
            final Channel newChannel = newConnection.createChannel();
            channel = newChannel;
            channel.exchangeDeclare(
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE,
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE_TYPE,
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE_DURABLE
            );
            channel.confirmSelect();
            logger.info("NameLayer RabbitMQ invalidation publisher connected");
            return true;
        } catch (ConnectException ex) {
            logger.warn("Retrying RabbitMQ connection");
            proxyServer.getScheduler().buildTask(plugin, this::connect).delay(RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS).schedule();
            return false;
        } catch (final IOException | TimeoutException exception) {
            logger.error("Failed to connect NameLayer RabbitMQ invalidation publisher", exception);
            return false;
        }
    }

    public synchronized boolean publish(final String database, final NameLayerInvalidationMessage invalidation) {
        Objects.requireNonNull(invalidation, "invalidation");
        if (channel == null || !channel.isOpen()) {
            logger.error("NameLayer invalidation publisher is not connected");
            return false;
        }
        try {
            final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType(NameLayerRabbitMqTopology.CONTENT_TYPE_JSON)
                .deliveryMode(2)
                .build();
            channel.basicPublish(
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE,
                database,
                properties,
                GSON.toJson(invalidation).getBytes(StandardCharsets.UTF_8)
            );
            return channel.waitForConfirms(PUBLISH_CONFIRM_TIMEOUT_MILLIS);
        } catch (final IOException | InterruptedException | TimeoutException exception) {
            logger.error("Failed to publish NameLayer invalidation", exception);
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    @Override
    public synchronized void close() {
        closeChannel();
        closeConnection();
    }

    private void closeChannel() {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (final IOException | TimeoutException exception) {
            logger.warn("Failed to close NameLayer invalidation channel", exception);
        } finally {
            channel = null;
        }
    }

    private void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (final IOException exception) {
            logger.warn("Failed to close NameLayer invalidation connection", exception);
        } finally {
            connection = null;
        }
    }
}
