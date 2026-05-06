package vg.civcraft.mc.namelayer.rabbitmq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.civmc.namelayer.sync.NameLayerInvalidationMessage;
import net.civmc.namelayer.sync.NameLayerRabbitMqTopology;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;
import vg.civcraft.mc.namelayer.group.DefaultGroupHandler;

public final class NameLayerInvalidationConsumer implements AutoCloseable {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final ConnectionFactory connectionFactory;
    private final String serverId;
    private final Logger logger;
    private final JavaPlugin plugin;
    private volatile boolean closing;
    private volatile boolean reconnectScheduled;
    private Connection connection;
    private Channel channel;

    public NameLayerInvalidationConsumer(
        final ConnectionFactory connectionFactory,
        final String serverId,
        final Logger logger,
        final JavaPlugin plugin
    ) {
        this.connectionFactory = connectionFactory;
        this.serverId = serverId;
        this.logger = logger;
        this.plugin = plugin;
    }

    public boolean start() {
        closing = false;
        return connect(false);
    }

    private boolean connect(final boolean fullResyncBeforeConsume) {
        try {
            if (fullResyncBeforeConsume) {
                GroupManager.fullResyncCache();
            }
            connection = connectionFactory.newConnection("namelayer-paper-invalidations-" + serverId);
            connection.addShutdownListener(cause -> scheduleReconnect());
            channel = connection.createChannel();
            channel.exchangeDeclare(
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE,
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE_TYPE,
                NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE_DURABLE
            );
            AMQP.Queue.DeclareOk declare = channel.queueDeclare();
            String queueName = declare.getQueue();
            channel.queueBind(queueName, NameLayerRabbitMqTopology.INVALIDATION_EXCHANGE, "");
            channel.basicQos(1);
            final DeliverCallback deliverCallback = (consumerTag, delivery) -> handleDelivery(delivery.getBody(), delivery.getEnvelope().getDeliveryTag());
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
            logger.log(Level.INFO, "NameLayer consuming invalidations from " + queueName);
            return true;
        } catch (final IOException | TimeoutException exception) {
            logger.log(Level.SEVERE, "Failed to start NameLayer RabbitMQ invalidation consumer", exception);
            closeResources();
            return false;
        } catch (final RuntimeException exception) {
            logger.log(Level.SEVERE, "Failed to resync NameLayer before RabbitMQ reconnect", exception);
            closeResources();
            return false;
        }
    }

    private void scheduleReconnect() {
        if (closing || reconnectScheduled || !plugin.isEnabled()) {
            return;
        }
        reconnectScheduled = true;
        NameLayerPlugin.recordRabbitMqReconnect();
        logger.log(Level.WARNING, "NameLayer RabbitMQ invalidation connection closed; reconnecting after full resync");
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (closing || !plugin.isEnabled()) {
                reconnectScheduled = false;
                return;
            }
            closeResources();
            final boolean connected = connect(true);
            reconnectScheduled = false;
            if (!connected) {
                scheduleReconnect();
            }
        }, 20L * 5L);
    }

    private void handleDelivery(final byte[] body, final long deliveryTag) throws IOException {
        try {
            final NameLayerInvalidationMessage invalidation = GSON.fromJson(
                new String(body, StandardCharsets.UTF_8),
                NameLayerInvalidationMessage.class
            );
            if (applyInvalidation(invalidation)) {
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (final JsonParseException exception) {
            logger.log(Level.WARNING, "Rejecting malformed NameLayer invalidation", exception);
            channel.basicReject(deliveryTag, false);
        } catch (final RuntimeException exception) {
            logger.log(Level.WARNING, "Failed to apply NameLayer invalidation; requeueing", exception);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private boolean applyInvalidation(final NameLayerInvalidationMessage invalidation) {
        if (invalidation.requiresFullResync()) {
            logger.log(Level.INFO, "Applying NameLayer full-resync invalidation");
            GroupManager.fullResyncCache();
            return true;
        }
        logger.log(Level.INFO, "Applying NameLayer targeted invalidation for "
            + invalidation.affectedGroupIds().size() + " groups, "
            + invalidation.affectedDefaultGroupPlayers().size() + " default groups, "
            + invalidation.affectedAutoAcceptPlayers().size() + " auto-accept entries");
        if (!invalidation.affectedGroupIds().isEmpty()
            && !GroupManager.reloadGroupsById(new ArrayList<>(invalidation.affectedGroupIds()))) {
            return false;
        }
        final DefaultGroupHandler defaultGroupHandler = NameLayerPlugin.getDefaultGroupHandler();
        if (defaultGroupHandler != null) {
            for (final UUID playerUuid : invalidation.affectedDefaultGroupPlayers()) {
                defaultGroupHandler.reload(playerUuid);
            }
        }
        final AutoAcceptHandler autoAcceptHandler = NameLayerPlugin.getAutoAcceptHandler();
        if (autoAcceptHandler != null) {
            for (final UUID playerUuid : invalidation.affectedAutoAcceptPlayers()) {
                autoAcceptHandler.reload(
                    playerUuid,
                    NameLayerPlugin.getNameLayerReadDao().isAutoAcceptEnabled(playerUuid)
                );
            }
        }
        return true;
    }

    @Override
    public void close() {
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
        try {
            channel.close();
        } catch (final IOException | TimeoutException exception) {
            logger.log(Level.WARNING, "Failed to close NameLayer RabbitMQ channel", exception);
        }
    }

    private void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (final IOException exception) {
            logger.log(Level.WARNING, "Failed to close NameLayer RabbitMQ connection", exception);
        }
    }
}
