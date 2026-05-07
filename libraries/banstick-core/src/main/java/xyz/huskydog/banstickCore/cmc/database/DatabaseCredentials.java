package xyz.huskydog.banstickCore.cmc.database;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.huskydog.banstickCore.cmc.utils.MoreMapUtils;
import xyz.huskydog.banstickCore.cmc.utils.NumberUtils;

/**
 * This is a data class representing a set of credentials used for connecting to databases.
 *
 * @author Protonull
 */
public record DatabaseCredentials(String username,
                                  String password,
                                  String host,
                                  int port,
                                  String driver,
                                  String database,
                                  int poolSize,
                                  long connectionTimeout,
                                  long idleTimeout,
                                  long maxLifetime) {

    @NotNull
    public Map<String, Object> serialize() {
        final var data = new HashMap<String, Object>(10);
        data.put("username", this.username);
        data.put("password", this.password);
        data.put("hostname", this.host);
        data.put("port", this.port);
        data.put("driver", this.driver);
        data.put("database", this.database);
        data.put("poolSize", this.poolSize);
        data.put("connectionTimeout", this.connectionTimeout);
        data.put("idleTimeout", this.idleTimeout);
        data.put("maxLifetime", this.maxLifetime);
        return data;
    }

    @Nullable
    public static DatabaseCredentials deserialize(@NotNull final Map<String, Object> data) {
        if (MapUtils.isEmpty(data)) {
            return null;
        }
        return new DatabaseCredentials(
            MoreMapUtils.attemptGet(data, "root",
                "username", "user"), // keys
            MoreMapUtils.attemptGet(data, "",
                "password", "pass"), // keys
            MoreMapUtils.attemptGet(data, "localhost",
                "hostname", "host"), // keys
            MoreMapUtils.attemptGet(data, NumberUtils::toInt, 3306,
                "port"), // keys
            MoreMapUtils.attemptGet(data, "mysql",
                "driver"), // keys
            MoreMapUtils.attemptGet(data, "minecraft",
                "database", "dbname", "db"), // keys
            MoreMapUtils.attemptGet(data, NumberUtils::toInt, 10,
                "poolSize", "poolsize"), // keys
            MoreMapUtils.attemptGet(data, NumberUtils::toLong, 10_000L,
                "connectionTimeout", "connection_timeout"), // keys
            MoreMapUtils.attemptGet(data, NumberUtils::toLong, 600_000L,
                "idleTimeout", "idle_timeout"), // keys
            MoreMapUtils.attemptGet(data, NumberUtils::toLong, 7_200_000L,
                "maxLifetime", "max_lifetime")); // keys
    }

}
