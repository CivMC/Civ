package vg.civcraft.mc.namelayer.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record NameLayerRabbitMqConfig(boolean enabled, String serverId, String user, String password, String host, int port) {

    public NameLayerRabbitMqConfig {
        if (enabled) {
            serverId = requireNonBlank(serverId, "serverId");
            user = requireNonBlank(user, "user");
            host = requireNonBlank(host, "host");
            if (port <= 0) {
                throw new IllegalArgumentException("port must be positive");
            }
        }
        password = password == null ? "" : password;
    }

    public static NameLayerRabbitMqConfig from(final ConfigurationSection section) {
        if (section == null) {
            return new NameLayerRabbitMqConfig(false, "paper", "guest", "guest", "localhost", 5672);
        }
        return new NameLayerRabbitMqConfig(
            section.getBoolean("enabled", false),
            section.getString("serverId", "paper"),
            section.getString("user", "guest"),
            section.getString("password", "guest"),
            section.getString("host", "localhost"),
            section.getInt("port", 5672)
        );
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
