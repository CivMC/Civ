package net.civmc.namelayer.velocity.rabbitmq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
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
    private volatile boolean closing;
    private volatile boolean reconnectScheduled;
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
        closing = false;
        if (connect()) {
            return true;
        }
        scheduleReconnect(null);
        return true;
    }

    private synchronized boolean connect() {
        try {
            final Connection newConnection = connectionFactory.newConnection("namelayer-velocity-invalidations");
            newConnection.addShutdownListener(cause -> scheduleReconnect(newConnection));
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
        } catch (final IOException | TimeoutException exception) {
            logger.error("Failed to connect NameLayer RabbitMQ invalidation publisher", exception);
            closeResources();
            return false;
        }
    }

    private synchronized void scheduleReconnect(final Connection closedConnection) {
        if (closedConnection != null && closedConnection != connection) {
            return;
        }
        if (closing || reconnectScheduled) {
            return;
        }
        reconnectScheduled = true;
        logger.warn("NameLayer RabbitMQ invalidation publisher disconnected; reconnecting in {} seconds", RECONNECT_DELAY_SECONDS);
        proxyServer.getScheduler().buildTask(plugin, () -> {
            if (closing) {
                reconnectScheduled = false;
                return;
            }
            closeResources();
            final boolean connected = connect();
            reconnectScheduled = false;
            if (!connected) {
                scheduleReconnect(null);
            }
        }).delay(RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS).schedule();
    }

    public synchronized boolean publish(final String database, final NameLayerInvalidationMessage invalidation) {
        Objects.requireNonNull(invalidation, "invalidation");
        if (channel == null || !channel.isOpen()) {
            logger.error("NameLayer invalidation publisher is not connected");
            scheduleReconnect(null);
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
            scheduleReconnect(null);
            return false;
        }
    }

    @Override
    public synchronized void close() {
        closing = true;
        closeResources();
    }

    private void closeResources() {
        closeChannel();
        closeConnection();
    }

    private void closeChannel() {
        if (channel == null) {
            return;
        }
        if (!channel.isOpen()) {
            channel = null;
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
        if (!connection.isOpen()) {
            connection = null;
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
