package vg.civcraft.mc.namelayer.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record NameLayerRabbitMqConfig(
    boolean enabled,
    String serverId,
    String user,
    String password,
    String host,
    int port,
    boolean freshnessCheckEnabled,
    long freshnessCheckIntervalSeconds,
    long freshnessCheckJitterSeconds,
    long freshnessCheckStaleGraceSeconds
) {

    public NameLayerRabbitMqConfig {
        if (enabled) {
            user = requireNonBlank(user, "user");
            host = requireNonBlank(host, "host");
            if (port <= 0) {
                throw new IllegalArgumentException("port must be positive");
            }
            if (freshnessCheckIntervalSeconds <= 0L) {
                throw new IllegalArgumentException("freshnessCheck.intervalSeconds must be positive");
            }
            if (freshnessCheckJitterSeconds < 0L) {
                throw new IllegalArgumentException("freshnessCheck.jitterSeconds must not be negative");
            }
            if (freshnessCheckStaleGraceSeconds < 0L) {
                throw new IllegalArgumentException("freshnessCheck.staleGraceSeconds must not be negative");
            }
        }
        password = password == null ? "" : password;
    }

    public static NameLayerRabbitMqConfig from(final ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("section is null");
        }
        final ConfigurationSection freshnessCheck = section.getConfigurationSection("freshnessCheck");
        return new NameLayerRabbitMqConfig(
            section.getBoolean("enabled", false),
            section.getString("serverId", "paper"),
            section.getString("user", "guest"),
            section.getString("password", "guest"),
            section.getString("host", "localhost"),
            section.getInt("port", 5672),
            freshnessCheck == null || freshnessCheck.getBoolean("enabled", true),
            freshnessCheck == null ? 300L : freshnessCheck.getLong("intervalSeconds", 300L),
            freshnessCheck == null ? 30L : freshnessCheck.getLong("jitterSeconds", 30L),
            freshnessCheck == null ? 300L : freshnessCheck.getLong("staleGraceSeconds", 300L)
        );
    }

    public ConnectionFactory connectionFactory() {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(user);
        factory.setPassword(password);
        factory.setHost(host);
        factory.setPort(port);
        factory.setAutomaticRecoveryEnabled(false);
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
