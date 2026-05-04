package vg.civcraft.mc.namelayer.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.civmc.namelayer.sync.NameLayerInvalidationMessage;
import net.civmc.namelayer.sync.NameLayerRabbitMqTopology;
import net.civmc.namelayer.sync.NameLayerSyncCodec;
import vg.civcraft.mc.namelayer.GroupManager;

public final class NameLayerInvalidationConsumer implements AutoCloseable {

    private final ConnectionFactory connectionFactory;
    private final String serverId;
    private final Logger logger;
    private Connection connection;
    private Channel channel;

    public NameLayerInvalidationConsumer(
        final ConnectionFactory connectionFactory,
        final String serverId,
        final Logger logger
    ) {
        this.connectionFactory = connectionFactory;
        this.serverId = serverId;
        this.logger = logger;
    }

    public boolean start() {
        try {
            connection = connectionFactory.newConnection("namelayer-paper-invalidations-" + serverId);
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
            close();
            return false;
        }
    }

    private void handleDelivery(final byte[] body, final long deliveryTag) throws IOException {
        try {
            final NameLayerInvalidationMessage invalidation = NameLayerSyncCodec.decodeInvalidation(body);
            if (applyInvalidation(invalidation)) {
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (final IllegalArgumentException exception) {
            logger.log(Level.WARNING, "Rejecting malformed NameLayer invalidation", exception);
            channel.basicReject(deliveryTag, false);
        } catch (final RuntimeException exception) {
            logger.log(Level.WARNING, "Failed to apply NameLayer invalidation; requeueing", exception);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private boolean applyInvalidation(final NameLayerInvalidationMessage invalidation) {
        if (invalidation.requiresFullResync()) {
            GroupManager.fullResyncCache();
            return true;
        }
        return GroupManager.reloadGroupsById(new ArrayList<>(invalidation.affectedGroupIds()));
    }

    @Override
    public void close() {
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
