package net.civmc.namelayer.velocity.config;

import com.rabbitmq.client.ConnectionFactory;
import java.util.Objects;

public record RabbitMqConfig(String user, String password, String host, int port) {

    public RabbitMqConfig {
        user = requireNonBlank(user, "user");
        password = password == null ? "" : password;
        host = requireNonBlank(host, "host");
        if (port <= 0) {
            throw new IllegalArgumentException("port must be positive");
        }
    }

    public ConnectionFactory connectionFactory() {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setHost(host);
        factory.setPort(port);
        return factory;
    }

    private static String requireNonBlank(final String value, final String fieldName) {
        Objects.requireNonNull(value, fieldName);
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
