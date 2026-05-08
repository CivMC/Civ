package net.civmc.namelayer.velocity.rabbitmq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.civmc.namelayer.sync.NameLayerRabbitMqTopology;
import net.civmc.namelayer.sync.NameLayerWriteFailureCode;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import net.civmc.namelayer.velocity.write.NameLayerWriteCoordinator;
import org.slf4j.Logger;

public final class NameLayerWriteRequestConsumer implements AutoCloseable {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final long RECONNECT_DELAY_SECONDS = 5L;

    private final ConnectionFactory connectionFactory;
    private final NameLayerWriteCoordinator coordinator;
    private final ProxyServer proxyServer;
    private final Object plugin;
    private final Logger logger;
    private Connection connection;
    private Channel channel;

    public NameLayerWriteRequestConsumer(
        final ConnectionFactory connectionFactory,
        final NameLayerWriteCoordinator coordinator,
        final ProxyServer proxyServer,
        final Object plugin,
        final Logger logger
    ) {
        this.connectionFactory = connectionFactory;
        this.coordinator = coordinator;
        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.logger = logger;
    }

    public boolean start() {
        return connect();
    }

    private synchronized boolean connect() {
        try {
            final Connection newConnection = connectionFactory.newConnection("namelayer-velocity-writes");
            connection = newConnection;
            channel = newConnection.createChannel();
            channel.queueDeclare(
                NameLayerRabbitMqTopology.WRITE_REQUEST_QUEUE,
                NameLayerRabbitMqTopology.WRITE_REQUEST_QUEUE_DURABLE,
                false,
                false,
                null
            );
            channel.basicQos(1);
            final DeliverCallback deliverCallback = (consumerTag, delivery) -> handleDelivery(delivery.getBody(), delivery.getProperties(), delivery.getEnvelope().getDeliveryTag());
            channel.basicConsume(NameLayerRabbitMqTopology.WRITE_REQUEST_QUEUE, false, deliverCallback, consumerTag -> {
            });
            logger.info("NameLayer RabbitMQ write request consumer connected");
            return true;
        } catch (ConnectException ex) {
            logger.warn("Retrying RabbitMQ connection");
            proxyServer.getScheduler().buildTask(plugin, this::connect).delay(RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS).schedule();
            return false;
        } catch (final IOException | TimeoutException exception) {
            logger.error("Failed to connect NameLayer RabbitMQ write request consumer", exception);
            return false;
        }
    }

    private void handleDelivery(final byte[] body, final AMQP.BasicProperties properties, final long deliveryTag) throws IOException {
        NameLayerWriteRequest request = null;
        NameLayerWriteResponse response;
        try {
            request = GSON.fromJson(new String(body, StandardCharsets.UTF_8), NameLayerWriteRequest.class);
            response = coordinator.handle(request);
        } catch (final JsonParseException exception) {
            logger.warn("Rejecting malformed NameLayer write request", exception);
            response = malformedRequestResponse(properties);
        } catch (final RuntimeException exception) {
            logger.error("NameLayer write request failed unexpectedly", exception);
            response = failureResponse(request, properties, NameLayerWriteFailureCode.WRITE_FAILED, "Unexpected write failure");
        }

        if (response != null) {
            publishResponse(properties, request, response);
        }
        channel.basicAck(deliveryTag, false);
    }

    private NameLayerWriteResponse malformedRequestResponse(final AMQP.BasicProperties properties) {
        final UUID requestId = parseCorrelationId(properties);
        if (requestId == null) {
            return null;
        }
        return NameLayerWriteResponse.failure(requestId, NameLayerWriteFailureCode.INVALID_REQUEST, "Malformed write request");
    }

    private NameLayerWriteResponse failureResponse(
        final NameLayerWriteRequest request,
        final AMQP.BasicProperties properties,
        final NameLayerWriteFailureCode failureCode,
        final String message
    ) {
        final UUID requestId = request == null ? parseCorrelationId(properties) : request.requestId();
        if (requestId == null) {
            return null;
        }
        return NameLayerWriteResponse.failure(requestId, failureCode, message);
    }

    private void publishResponse(
        final AMQP.BasicProperties requestProperties,
        final NameLayerWriteRequest request,
        final NameLayerWriteResponse response
    ) throws IOException {
        final String replyQueue = responseQueue(requestProperties, request);
        if (replyQueue == null) {
            logger.warn("Dropping NameLayer write response {} because no reply queue was provided", response.requestId());
            return;
        }
        final AMQP.BasicProperties responseProperties = new AMQP.BasicProperties.Builder()
            .contentType(NameLayerRabbitMqTopology.CONTENT_TYPE_JSON)
            .correlationId(response.requestId().toString())
            .deliveryMode(1)
            .build();
        channel.basicPublish("", replyQueue, responseProperties, GSON.toJson(response).getBytes(StandardCharsets.UTF_8));
    }

    private String responseQueue(final AMQP.BasicProperties properties, final NameLayerWriteRequest request) {
        if (properties != null && properties.getReplyTo() != null && !properties.getReplyTo().isBlank()) {
            return properties.getReplyTo();
        }
        if (request == null) {
            return null;
        }
        return NameLayerRabbitMqTopology.WRITE_RESPONSE_QUEUE_PREFIX + request.originServerId();
    }

    private UUID parseCorrelationId(final AMQP.BasicProperties properties) {
        if (properties == null || properties.getCorrelationId() == null || properties.getCorrelationId().isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(properties.getCorrelationId());
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    private void closeChannel() {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (final IOException | TimeoutException exception) {
            logger.warn("Failed to close NameLayer RabbitMQ channel", exception);
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
            logger.warn("Failed to close NameLayer RabbitMQ connection", exception);
        } finally {
            connection = null;
        }
    }

    @Override
    public void close() {
        closeChannel();
        closeConnection();
    }
}
