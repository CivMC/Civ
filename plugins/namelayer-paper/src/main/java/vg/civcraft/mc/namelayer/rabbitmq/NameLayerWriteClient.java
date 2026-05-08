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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.civmc.namelayer.sync.NameLayerRabbitMqTopology;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public final class NameLayerWriteClient implements AutoCloseable {

    private static final long RESPONSE_TIMEOUT_SECONDS = 15L;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final ConnectionFactory connectionFactory;
    private final Logger logger;
    private final Map<UUID, CompletableFuture<NameLayerWriteResponse>> pendingResponses = new ConcurrentHashMap<>();
    private Connection connection;
    private Channel channel;
    private final JavaPlugin plugin;
    private volatile boolean closing;
    private volatile boolean reconnectScheduled;

    private String queue;

    public NameLayerWriteClient(
        final ConnectionFactory connectionFactory,
        final Logger logger, JavaPlugin plugin
    ) {
        this.connectionFactory = connectionFactory;
        this.logger = logger;
        this.plugin = plugin;
    }

    public boolean start() {
        closing = false;
        return connect();
    }

    public boolean connect() {
        try {
            connection = connectionFactory.newConnection("namelayer-paper-writes");
            connection.addShutdownListener(cause -> scheduleReconnect());
            channel = connection.createChannel();
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", 12000);
            AMQP.Queue.DeclareOk ok = channel.queueDeclare("", false, true, true, args);
            this.queue = ok.getQueue();
            final DeliverCallback deliverCallback = (consumerTag, delivery) -> handleResponse(delivery.getBody());
            channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
            });
            return true;
        } catch (final IOException | TimeoutException exception) {
            logger.log(Level.SEVERE, "Failed to connect NameLayer RabbitMQ write client", exception);
            close();
            return false;
        }
    }

    private void scheduleReconnect() {
        if (closing || reconnectScheduled || !plugin.isEnabled()) {
            return;
        }
        reconnectScheduled = true;
        NameLayerPlugin.recordRabbitMqReconnect();
        logger.log(Level.WARNING,
            "NameLayer RabbitMQ write client connection closed; reconnecting after 5 seconds");
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (closing || !plugin.isEnabled()) {
                reconnectScheduled = false;
                return;
            }
            closeChannel();
            closeChannel();
            final boolean connected = connect();
            reconnectScheduled = false;
            if (!connected) {
                scheduleReconnect();
            }
        }, 20L * 5L);
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
                .replyTo(queue)
                .deliveryMode(2)
                .build();
            synchronized (this) {
                channel.basicPublish(
                    "",
                    NameLayerRabbitMqTopology.WRITE_REQUEST_QUEUE,
                    properties,
                    GSON.toJson(request).getBytes(StandardCharsets.UTF_8)
                );
            }
        } catch (final IOException exception) {
            pendingResponses.remove(request.requestId());
            responseFuture.completeExceptionally(exception);
        }
        return responseFuture;
    }

    private void handleResponse(final byte[] body) {
        final NameLayerWriteResponse response;
        try {
            response = GSON.fromJson(new String(body, StandardCharsets.UTF_8), NameLayerWriteResponse.class);
        } catch (final JsonParseException exception) {
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
