package net.civmc.namelayer.velocity.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;

public record DatabaseConfig(
    String driver,
    String host,
    int port,
    String user,
    String password,
    int poolSize,
    long connectionTimeout,
    long idleTimeout,
    long maxLifetime
) {

    public DatabaseConfig {
        driver = requireNonBlank(driver, "driver");
        host = requireNonBlank(host, "host");
        user = requireNonBlank(user, "user");
        password = password == null ? "" : password;
        if (port <= 0) {
            throw new IllegalArgumentException("port must be positive");
        }
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be positive");
        }
    }

    public HikariDataSource createDataSource() {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:" + driver + "://" + host + ":" + port);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setMaximumPoolSize(poolSize);
        config.setUsername(user);
        if (!password.isBlank()) {
            config.setPassword(password);
        }
        return new HikariDataSource(config);
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
