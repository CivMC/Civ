package vg.civcraft.mc.civmodcore.dao;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

/**
 * This is a data class representing a set of credentials used for connecting to databases.
 *
 * @author Protonull
 */
public class DatabaseCredentials implements ConfigurationSerializable {

	private final String user;
	private final String pass;
	private final String host;
	private final int port;
	private final String driver;
	private final String database;
	private final int poolSize;
	private final long connectionTimeout;
	private final long idleTimeout;
	private final long maxLifetime;

	public DatabaseCredentials(String user, String pass, String host, int port, String driver, String database,
							   int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this.user = user;
		this.pass = pass;
		this.host = host;
		this.port = port;
		this.driver = driver;
		this.database = database;
		this.poolSize = poolSize;
		this.connectionTimeout = connectionTimeout;
		this.idleTimeout = idleTimeout;
		this.maxLifetime = maxLifetime;
	}

	public final String getUsername() {
		return this.user;
	}

	public final String getPassword() {
		return this.pass;
	}

	public final String getHostname() {
		return this.host;
	}

	public final int getPort() {
		return this.port;
	}

	public final String getDriver() {
		return this.driver;
	}

	public final String getDatabase() {
		return this.database;
	}

	public final int getPoolSize() {
		return this.poolSize;
	}

	public final long getConnectionTimeout() {
		return this.connectionTimeout;
	}

	public final long getIdleTimeout() {
		return this.idleTimeout;
	}

	public final long getMaxLifetime() {
		return this.maxLifetime;
	}

	@Override
	public final Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put("username", this.user);
		data.put("password", this.pass);
		data.put("hostname", this.host);
		data.put("port", this.port);
		data.put("database", this.database);
		data.put("poolSize", this.poolSize);
		data.put("connectionTimeout", this.connectionTimeout);
		data.put("idleTimeout", this.idleTimeout);
		data.put("maxLifetime", this.maxLifetime);
		return data;
	}

	// This must be kept compatible with ManagedDatasource's deserialization
	public static DatabaseCredentials deserialize(Map<String, Object> data) {
		if (MapUtils.isEmpty(data)) {
			return null;
		}
		String user = MoreMapUtils.attemptGet(data, "root", "username", "user");
		String pass = MoreMapUtils.attemptGet(data, "", "password", "pass");
		String host = MoreMapUtils.attemptGet(data, "localhost", "hostname", "host");
		int port = MoreMapUtils.attemptGet(data, NumberConversions::toInt, 3306, "port");
		String driver = MoreMapUtils.attemptGet(data, "mysql", "driver");
		String database = MoreMapUtils.attemptGet(data, "minecraft", "database", "dbname", "db");
		int poolSize = MoreMapUtils.attemptGet(data, NumberConversions::toInt, 10, "poolSize", "poolsize");
		long connectionTimeout = MoreMapUtils.attemptGet(data, NumberConversions::toLong, 10_000L,
				"connectionTimeout", "connection_timeout");
		long idleTimeout = MoreMapUtils.attemptGet(data, NumberConversions::toLong, 600_000L,
				"idleTimeout", "idle_timeout");
		long maxLifetime = MoreMapUtils.attemptGet(data, NumberConversions::toLong, 7_200_000L,
				"maxLifetime", "max_lifetime");
		return new DatabaseCredentials(user, pass, host, port, driver, database,
				poolSize, connectionTimeout, idleTimeout, maxLifetime);
	}

}
