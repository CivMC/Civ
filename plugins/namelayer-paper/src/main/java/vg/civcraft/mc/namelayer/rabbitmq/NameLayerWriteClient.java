package vg.civcraft.mc.namelayer.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.civmc.namelayer.sync.NameLayerRabbitMqTopology;
import net.civmc.namelayer.sync.NameLayerSyncCodec;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;

public final class NameLayerWriteClient implements AutoCloseable {

    private static final long RESPONSE_TIMEOUT_SECONDS = 10L;

    private final ConnectionFactory connectionFactory;
    private final String responseQueue;
    private final Logger logger;
    private final Map<UUID, CompletableFuture<NameLayerWriteResponse>> pendingResponses = new ConcurrentHashMap<>();
    private Connection connection;
    private Channel channel;

    public NameLayerWriteClient(
        final ConnectionFactory connectionFactory,
        final String serverId,
        final Logger logger
    ) {
        this.connectionFactory = connectionFactory;
        this.responseQueue = NameLayerRabbitMqTopology.WRITE_RESPONSE_QUEUE_PREFIX + serverId;
        this.logger = logger;
    }

    public boolean start() {
        try {
            connection = connectionFactory.newConnection("namelayer-paper-writes");
            channel = connection.createChannel();
            channel.queueDeclare(responseQueue, false, true, true, null);
            final DeliverCallback deliverCallback = (consumerTag, delivery) -> handleResponse(delivery.getBody(), delivery.getProperties());
            channel.basicConsume(responseQueue, true, deliverCallback, consumerTag -> {
            });
            return true;
        } catch (final IOException | TimeoutException exception) {
            logger.log(Level.SEVERE, "Failed to start NameLayer RabbitMQ write client", exception);
            close();
            return false;
        }
    }

    public CompletableFuture<NameLayerWriteResponse> send(final NameLayerWriteRequest request) {
        final CompletableFuture<NameLayerWriteResponse> responseFuture = new CompletableFuture<>();
        if (channel == null || !channel.isOpen()) {
            responseFuture.completeExceptionally(new IllegalStateException("NameLayer write client is not connected"));
            return responseFuture;
        }
        pendingResponses.put(request.requestId(), responseFuture);
        responseFuture.orTimeout(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .whenComplete((response, error) -> pendingResponses.remove(request.requestId()));
        try {
            final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType(NameLayerRabbitMqTopology.CONTENT_TYPE_JSON)
                .correlationId(request.requestId().toString())
                .replyTo(responseQueue)
                .deliveryMode(2)
                .build();
            synchronized (this) {
                channel.basicPublish("", NameLayerRabbitMqTopology.WRITE_REQUEST_QUEUE, properties, NameLayerSyncCodec.encodeWriteRequest(request));
            }
        } catch (final IOException exception) {
            pendingResponses.remove(request.requestId());
            responseFuture.completeExceptionally(exception);
        }
        return responseFuture;
    }

    private void handleResponse(final byte[] body, final AMQP.BasicProperties properties) {
        final NameLayerWriteResponse response;
        try {
            response = NameLayerSyncCodec.decodeWriteResponse(body);
        } catch (final IllegalArgumentException exception) {
            logger.log(Level.WARNING, "Dropping malformed NameLayer write response", exception);
            return;
        }
        final UUID requestId = response.requestId();
        final CompletableFuture<NameLayerWriteResponse> responseFuture = pendingResponses.remove(requestId);
        if (responseFuture == null) {
            logger.log(Level.FINE, "Dropping unmatched NameLayer write response " + requestId);
            return;
        }
        responseFuture.complete(response);
    }

    @Override
    public void close() {
        pendingResponses.values().forEach(response -> response.completeExceptionally(new IllegalStateException("NameLayer write client closed")));
        pendingResponses.clear();
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
            logger.log(Level.WARNING, "Failed to close NameLayer write channel", exception);
        }
    }

    private void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (final IOException exception) {
            logger.log(Level.WARNING, "Failed to close NameLayer write connection", exception);
        }
    }
}
