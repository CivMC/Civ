package vg.civcraft.mc.civmodcore.dao;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

/**
 * Plugins should replace their custom Database handlers with an instance of ManagedDatasource.
 * 
 * See the {@link #ManagedDatasource} constructor for details on how to use the ManagedDatasource.
 * 
 * To convert existing plugins, do the following:
 *
 * <ol><li> Take existing database version code and refactor it.
 * <ol><li> Any CREATE, UPDATE, ALTER, or similar statements, convert
 * to a List of Strings and pass them into ManagedDatasource as a Migration using
 * {@link #registerMigration(int, boolean, String...)}</li>
 * <li>Find your prepared statements. Convert the string resources as static final in your plugin's DAO layer.</li>
 * <li>Remove any "is database alive" check code. It's not needed.</li>
 * <li>Remove any version management code that remains</li>
 * <li>DO react to the results of the {@link #updateDatabase} call.
 * <ol><li> If false is returned, terminate your plugin. <li>
 * <li>If false is returned and your plugin is critical to a host of other plugins, terminate the server.</li>
 * <li>If an Exception is thrown, I strongly recommend you consider it a "false" return value and react 
 * accordingly.</li></ol></li>
 * <li>Note: Create a "first" migration at index -1 that ignores errors and copies
 * any "current" migration state data from the db_version or equivalent table into the <code>managed_plugin_data</code>
 * table.</li></ol></li>
 * <li>Throughout your plugin, ensure that PreparedStatements are "created" new each request and against a newly
 * retrieved Connection (using {@link #getConnection()} of this class). Don't worry about PrepareStatements. The driver
 * will manage caching them efficiently.</li>
 * <li>Make sure you release Connections using {@link Connection#close()} as soon as you can 
 * (when done with them).
 * <ol><li>Don't hold on to Connections.</li>
 * <li>Close them.</li>
 * <li>Use "try-with-resources" where-ever possible so that they are auto-closed.</li></ol></li>
 * <li>If you have loops to insert a bunch of similar records, convert
 * it to a batch. Find instructions in {@link #ManagedDatasource}.</li>
 * <li>If you have special needs like atomic
 * multi-statement, do all your work on a single Connection and return it to a clean state when you are done. (turn
 * auto-commit back on, ensure all transactions are committed, etc.)</li></ol>
 *
 * That should cover most cases. Note that points 2 and 3 are critical. Point 1 is required. Point 4 and 5 are highly
 * recommended.
 * 
 * @author ProgrammerDan (refactored by Protonull)
 */
public class ManagedDatasource implements ConfigurationSerializable {

	private static final Logger LOGGER = Bukkit.getLogger();

	private static final String CREATE_MIGRATIONS_TABLE = "CREATE TABLE IF NOT EXISTS managed_plugin_data ("
			+ "managed_id BIGINT NOT NULL AUTO_INCREMENT, " + "plugin_name VARCHAR(120) NOT NULL, "
			+ "management_began TIMESTAMP NOT NULL DEFAULT NOW(), " + "current_migration_number INT NOT NULL, "
			+ "last_migration TIMESTAMP, " + "CONSTRAINT pk_managed_plugin_data PRIMARY KEY (managed_id), "
			+ "CONSTRAINT uniq_managed_plugin UNIQUE (plugin_name), "
			+ "INDEX idx_managed_plugin USING BTREE (plugin_name)" + ");";

	private static final String CREATE_LOCK_TABLE = "CREATE TABLE IF NOT EXISTS managed_plugin_locks ("
			+ "plugin_name VARCHAR(120) NOT NULL, " + "lock_time TIMESTAMP NOT NULL DEFAULT NOW(), "
			+ "CONSTRAINT pk_managed_plugin_locks PRIMARY KEY (plugin_name)" + ");";

	private static final String CHECK_LAST_MIGRATION = "SELECT current_migration_number FROM managed_plugin_data "
			+ "WHERE plugin_name = ?;";

	private static final String RECORD_MIGRATION = "INSERT INTO managed_plugin_data "
			+ "(plugin_name, current_migration_number, last_migration) "
			+ "VALUES (?, ?, NOW()) ON DUPLICATE KEY UPDATE plugin_name = VALUES(plugin_name), "
			+ "current_migration_number = VALUES(current_migration_number), "
			+ "last_migration = VALUES(last_migration);";

	private static final String CLEANUP_LOCK_TABLE = "DELETE FROM managed_plugin_locks "
			+ "WHERE lock_time <= TIMESTAMPADD(HOUR, -8, NOW());";

	private static final String ACQUIRE_LOCK = "INSERT IGNORE INTO managed_plugin_locks (plugin_name) VALUES (?);";

	private static final String RELEASE_LOCK = "DELETE FROM managed_plugin_locks WHERE plugin_name = ?;";

	private static final long MAX_WAIT_FOR_LOCK = 600000L;

	private static final long WAIT_PERIOD = 500L;

	private final Plugin plugin;
	private final DatabaseCredentials credentials;
	private final ConnectionPool connections;
	private final ExecutorService postExecutor;
	private final TreeMap<Integer, Migration> migrations;
	private int firstMigration;
	private int lastMigration;

	/**
	 * See {@link #ManagedDatasource(ACivMod, String, String, String, int, String, String, int, long, long, long)} for
	 * more details.
	 *
	 * @param plugin The civ plugin whose database is being managed.
	 * @param user The SQL user to connect as.
	 * @param pass The SQL user's password.
	 * @param host The hostname of the database.
	 * @param port The port to connect via.
	 * @param database The specific database to create and modify tables in.
	 * @param poolSize The maximum size of the connection pool (under 10 recommended)
	 * @param connectionTimeout The longest a query can run until it times out (1-5 seconds recommended)
	 * @param idleTimeout The longest a connection can sit idle before recycling (10 minutes recommended, check dbms)
	 * @param maxLifetime The longest a connection can exist in total. (2 hours recommended, check dbms)
	 */
	public ManagedDatasource(ACivMod plugin, String user, String pass, String host, int port, String database,
							 int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this(plugin, user, pass, host, port, "mysql", database, poolSize, connectionTimeout, idleTimeout, maxLifetime);
	}

	/**
	 * Create a new ManagedDatasource.
	 *
	 * After creating, a plugin should register its migrations, which are numbered "sets" of queries that ensure that
	 * the database has the required tables, procedures, etc, for your plugin to function correctly. Migrations will be
	 * iterated through in ascending order during first setup, and whenever newer migrations have been added relative
	 * to your current setup. Think of them like patches, that each one builds off and modifies what came before it, so
	 * it's recommended that your migrations be non-destructive of existing data. Though writing good migrations does
	 * not absolve you of needing to back up your data... when was the last time you backed up? You should probably run
	 * a back up.
	 *
	 * Use {@link #registerMigration(int, boolean, Callable, String...)} to add a new migration.
	 *
	 * When you are done adding, call {@link #updateDatabase()} which gets a lock on migrating for this plugin, then
	 * checks if any migrations need to be applied, and applies as needed.
	 *
	 * Now, your database connection pool will be ready to use!
	 *
	 * Don't worry about "pre-preparing" statements. Just use the following pattern:
	 *
	 * {@code
	 *   try (Connection connection = myManagedDatasource.getConnection();
	 *   		PreparedStatement statement = connection.prepareStatement("SELECT * FROM sample;");) {
	 *   	// code that uses that -^ statement
	 *   }
	 *   catch (SQLException exception) {
	 *   	// code that alerts on failure
	 *   }
	 * }
	 *
	 * Or similar w/ normal Statements. This is a try-with-resources block, and it ensures that once the query is
	 * complete (even if it errors!) all resources are "closed". In the case of the connection pool, this just returns
	 * the connection back to the connection pool for use elsewhere.
	 *
	 * If you want to batch, just use a PreparedStatement as illustrated above, and use {@code .addBatch();} on it
	 * after adding each set of parameters. When you are done, call {@code .executeBatch();} and all the statements
	 * will be executed in order. Be sure to watch for errors or warnings and of course read the PreparedStatement API
	 * docs for any further questions.
	 *
	 * @param plugin The plugin whose database is being managed.
	 * @param user The SQL user to connect as.
	 * @param pass The SQL user's password.
	 * @param host The hostname of the database.
	 * @param port The port to connect via.
	 * @param driver The jdbc driver to use to connect to the database.
	 * @param database The specific database to create and modify tables in.
	 * @param poolSize The maximum size of the connection pool (under 10 recommended)
	 * @param connectionTimeout The longest a query can run until it times out (1-5 seconds recommended)
	 * @param idleTimeout The longest a connection can sit idle before recycling (10 minutes recommended, check dbms)
	 * @param maxLifetime The longest a connection can exist in total. (2 hours recommended, check dbms)
	 */
	public ManagedDatasource(ACivMod plugin, String user, String pass, String host, int port, String driver,
							 String database, int poolSize, long connectionTimeout, long idleTimeout,
							 long maxLifetime) {
		this(plugin, new DatabaseCredentials(user, pass, host, port, driver, database,
				poolSize, connectionTimeout, idleTimeout, maxLifetime));
	}

	/**
	 * Create a new ManagedDatasource.
	 *
	 * @param plugin The plugin whose database is being managed.
	 * @param credentials The credentials to connect to the database with.
	 */
	public ManagedDatasource(ACivMod plugin, DatabaseCredentials credentials) {
		Preconditions.checkArgument(plugin != null && plugin.isEnabled());
		Preconditions.checkArgument(credentials != null);
		this.plugin = plugin;
		this.credentials = credentials;
		LOGGER.info(String.format("Connecting to %s@%s:%s using %s",credentials.getDatabase(),
				credentials.getHostname(), credentials.getPort(), credentials.getUsername()));
		this.connections = new ConnectionPool(credentials);
		this.postExecutor = Executors.newSingleThreadExecutor();
		this.migrations = new TreeMap<>();
		this.firstMigration = Integer.MAX_VALUE;
		this.lastMigration = Integer.MIN_VALUE;
		try (Connection connection = connections.getConnection();) {
			try (Statement statement = connection.createStatement();) {
				statement.executeUpdate(ManagedDatasource.CREATE_MIGRATIONS_TABLE);
			}
			try (Statement statement = connection.createStatement();) {
				statement.executeUpdate(ManagedDatasource.CREATE_LOCK_TABLE);
			}
		}
		catch (SQLException ignored) {
			LOGGER.severe("Failed to prepare migrations table or register this plugin to it.");
			LOGGER.log(Level.SEVERE, "Assuming you provided proper database credentials this is most likely happening, because " +
					"your mysql install is outdated. We recommend using MariaDB or at least the latest mysql version", ignored);
		}
	}

	/**
	 * Use this to register a migration. After all migrations have been registered, call {@link #updateDatabase()}.
	 *
	 * This is <i>not</i> checked for completeness or accuracy.
	 *
	 * @param id The migration ID -- 0, 1, 2 etc, must be unique.
	 * @param ignoreErrors Indicates if errors in this migration should be ignored.
	 * @param queries The queries to run, in sequence.
	 */
	public void registerMigration(int id, boolean ignoreErrors, String... queries) {
		registerMigration(id, ignoreErrors, null, queries);
	}

	/**
	 * Use this to register a migration. After all migrations have been registered, call {@link #updateDatabase()}.
	 * 
	 * This is <i>not</i> checked for completeness or accuracy.
	 * 
	 * @param id The migration ID -- 0, 1, 2 etc, must be unique.
	 * @param ignoreErrors Indicates if errors in this migration should be ignored.
	 * @param callback An optional callback that'll run after the migration has completed.
	 * @param queries The queries to run, in sequence.
	 */
	public void registerMigration(int id, boolean ignoreErrors, Callable<Boolean> callback, String... queries) {
		this.migrations.put(id, new Migration(ignoreErrors, callback, queries));
		if (id > lastMigration) {
			lastMigration = id;
		}
		if (id < firstMigration) {
			firstMigration = id;
		}
	}

	/**
	 * This method should be called by your plugin after all migrations have been registered. It applies the migrations
	 * if necessary in a "multi-tenant" safe way via a soft-lock. Locks have a maximum duration currently set to 8
	 * hours, but realistically they will be very short. For multi-tenant updates all servers should gracefully wait in
	 * line.
	 *
	 * 1. Attempts to get a lock for migrations for this plugin. 2. If unsuccessful, periodically check the lock for
	 * release. a. Once released, restart at 1. b. If Timeout occurs, return "false". 3. If successful, check for
	 * current update level. a. If no record exists, start with first migration, and apply in sequence from first to
	 * last, updating the migration management table along the way b. If a record exists, read which migration was
	 * completed last i. If identical to "highest" registered migration level, do nothing. ii. If less then "highest"
	 * registered migration level, get the tailset of migrations "after" the last completed level, and run. 4. If no
	 * errors occurred, or this migration has errors marked ignored, return true. 5. If errors, return false. 6. In
	 * either case, release the lock.
	 * 
	 * @return As described in the algorithm above, returns true if no errors or all ignored; or false if unable to
	 *         start migration in a timely fashion or errors occurred.
	 */
	public boolean updateDatabase() {
		try {
			checkWaitLock();
		}
		catch (SQLException exception) {
			LOGGER.log(Level.SEVERE, "An uncorrectable SQL error was encountered!", exception);
			return false;
		}
		catch (TimeoutException exception) {
			LOGGER.log(Level.SEVERE, "Unable to acquire a lock!", exception);
			return false;
		}
		// Now check update level, etc.
		int currentLevel = migrations.firstKey() - 1;
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(CHECK_LAST_MIGRATION);) {
			statement.setString(1, plugin.getName());
			try (ResultSet set = statement.executeQuery();) {
				if (set.next()) {
					currentLevel = set.getInt(1);
				} // else we aren't tracked yet!
			}
		}
		catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Unable to check last migration!", e);
			releaseLock();
			return false;
		}
		NavigableMap<Integer, Migration> newApply = migrations.tailMap(currentLevel, false);
		try {
			if (newApply.size() > 0) {
				LOGGER.info(String.format("%s database is behind, %s migrations found",
						plugin.getName(), newApply.size()));
				if (doMigrations(newApply)) {
					LOGGER.info(plugin.getName() + " fully migrated.");
				}
				else {
					LOGGER.warning(plugin.getName() + " failed to apply updates.");
					return false;
				}
			}
			else {
				LOGGER.info(plugin.getName() + " database is up to date.");
			}
			return true;
		}
		catch (Exception exception) {
			LOGGER.warning(plugin.getName() + " failed to apply updates for some reason...");
			LOGGER.log(Level.WARNING, "Full exception: ", exception);
			return false;
		}
		finally {
			releaseLock();
		}
	}

	private boolean doMigrations(NavigableMap<Integer, Migration> migrations) {
		try {
			for (Integer id : migrations.keySet()) {
				LOGGER.info("Migration " +  id + " ] Applying");
				Migration migration = migrations.get(id);
				if (migration == null) {
					continue; // huh?
				}
				if (doMigration(id, migration.migrations, migration.ignoreErrors, migration.postMigration)) {
					LOGGER.info("Migration " +  id + " ] Successful");
					try (Connection connection = getConnection();
						 PreparedStatement statement = connection.prepareStatement(RECORD_MIGRATION);) {
						statement.setString(1, plugin.getName());
						statement.setInt(2, id);
						if (statement.executeUpdate() < 1) {
							LOGGER.warning("Might not have recorded migration " + id + " occurrence successfully.");
						}
					}
					catch (SQLException exception) {
						LOGGER.warning("Failed to record migration " + id + " occurrence successfully.");
						LOGGER.log(Level.SEVERE, "Full Error: ", exception);
						return false;
					}
				}
				else {
					LOGGER.info("Migration " +  id + " ] Failed");
					return false;
				}
			}
			return true;
		}
		catch (Exception exception) {
			LOGGER.log(Level.SEVERE, "Unexpected failure during migrations", exception);
			return false;
		}
	}

	private boolean doMigration(Integer migration, List<String> queries, boolean ignoreErrors, Callable<Boolean> post) {
		try (Connection connection = getConnection();) {
			for (String query : queries) {
				try (Statement statement = connection.createStatement();) {
					statement.executeUpdate(query);
					if (!ignoreErrors) { // if we ignore errors we totally ignore warnings.
						SQLWarning warning = statement.getWarnings();
						while (warning != null) {
							LOGGER.warning("Migration " + migration + " ] Warning: " + warning.getMessage());
							// TODO: add verbose check
							warning = warning.getNextWarning();
						}
					}
				}
				catch (SQLException exception) {
					if (ignoreErrors) {
						LOGGER.warning("Migration " + migration + " ] Ignoring error: " + exception.getMessage());
					}
					else {
						throw exception;
					}
				}
			}
		}
		catch (SQLException exception) {
			if (ignoreErrors) {
				LOGGER.warning("Migration " + migration + " ] Ignoring error: " + exception.getMessage());
			}
			else {
				LOGGER.warning("Migration " + migration + " ] Failed migration: " + exception.getMessage());
				LOGGER.log(Level.SEVERE, "Full Error: ", exception);
				return false;
			}
		}
		if (post != null) {
			Future<Boolean> doing = postExecutor.submit(post);
			try {
				if (doing.get()) {
					LOGGER.info("Migration " + migration + " ] Post Call Complete");
				}
				else {
					if (ignoreErrors) {
						LOGGER.warning("Migration " + migration + " ] Post Call indicated failure; ignored.");
					}
					else {
						LOGGER.severe("Migration " + migration + " ] Post Call failed!");
						return false;
					}
				}
			}
			catch (Exception exception) {
				if (ignoreErrors) {
					LOGGER.warning("Migration " + migration + " ] Post Call indicated failure; ignored: " +
							exception.getMessage());
				}
				else {
					LOGGER.severe("Migration " + migration + " ] Post Call failed!");
					LOGGER.log(Level.SEVERE, "Full Error: ", exception);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if this plugin is already managed by the ManagedDatasource infrastructure or not.
	 *
	 * @return Returns true if the plugin has an entry in the migrations table; false for any other outcome.
	 */
	public boolean isManaged() {
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(CHECK_LAST_MIGRATION);) {
			statement.setString(1, plugin.getName());
			try (ResultSet set = statement.executeQuery();) {
				return set.next();
			}
		}
		catch (SQLException e) {
			return false;
		}
	}

	/**
	 * This attempts to acquire a lock every WAIT_PERIOD milliseconds, up to MAX_WAIT_FOR_LOCK milliseconds.
	 * 
	 * If max wait is exhausted, throws a TimeoutException.
	 * 
	 * If a <i>real</i> error (not failure to insert) is encountered, stops trying and throws that error.
	 * 
	 * Otherwise, returns true when lock is acquired.
	 * 
	 * @return true when lock is acquired, or exception otherwise
	 * @throws TimeoutException
	 *             If lock isn't acquired by max wait time.
	 * @throws SQLException
	 *             If an exception is encountered
	 */
	private boolean checkWaitLock() throws TimeoutException, SQLException {
		/* First, cleanup old locks if any */
		try (Connection connection = getConnection(); Statement cleanup = connection.createStatement();) {
			cleanup.executeUpdate(CLEANUP_LOCK_TABLE);
		}
		catch (SQLException exception) {
			LOGGER.severe("Unable to cleanup old locks, error encountered!");
			throw exception;
		}
		/* Now get our own lock */
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < MAX_WAIT_FOR_LOCK) {
			try (Connection connection = getConnection();
				 PreparedStatement tryAcquire = connection.prepareStatement(ACQUIRE_LOCK);) {
				tryAcquire.setString(1, plugin.getName());
				int hasLock = tryAcquire.executeUpdate();
				if (hasLock > 0) {
					LOGGER.info("Lock acquired, proceeding.");
					return true;
				}
			}
			catch (SQLException failToAcquire) {
				LOGGER.severe("Unable to acquire a lock, error encountered!");
				// let the exception continue so we return right away; only errors we'd encounter here are terminal.
				throw failToAcquire;
			}
			if (System.currentTimeMillis() - start > MAX_WAIT_FOR_LOCK) {
				break;
			}
			try {
				Thread.sleep(WAIT_PERIOD);
			}
			catch (InterruptedException ignored) {
				// Someone wants us to check right away.
			}
		}
		throw new TimeoutException("We were unable to acquire a lock in the time allowed");
	}

	private void releaseLock() {
		try (Connection connection = getConnection();
				PreparedStatement release = connection.prepareStatement(RELEASE_LOCK);) {
			release.setString(1, plugin.getName());
			int releaseLock = release.executeUpdate();
			if (releaseLock < 1) {
				LOGGER.warning("Attempted to release a lock, already released.");
			}
			else {
				LOGGER.info("Lock released.");
			}
		}
		catch (SQLException exception) {
			LOGGER.log(Level.WARNING, "Attempted to release lock; failed. This may interrupt startup for other " +
							"servers working against this database.", exception);
		}
	}

	/**
	 * Passthrough; gets a connection from the underlying ConnectionPool. Simply close() it when done.
	 * 
	 * This method _could_ briefly block while waiting for a connection. Keep this in mind.
	 * 
	 * @return Returns a connection from the pool.
	 * @throws SQLException If the pool has gone away, database is not connected, or other error has occurred.
	 */
	public Connection getConnection() throws SQLException {
		return connections.getConnection();
	}

	/**
	 * Passthrough; closes the underlying pool. Cannot be undone.
	 * 
	 * @throws SQLException Something went horribly wrong.
	 */
	public void close() throws SQLException {
		connections.close();
	}

	private static class Migration {
		public List<String> migrations;
		public boolean ignoreErrors;
		public Callable<Boolean> postMigration;
		public Migration(boolean ignoreErrors, Callable<Boolean> postMigration, String... migrations) {
			this.migrations = MoreCollectionUtils.collect(ArrayList::new, migrations);
			this.ignoreErrors = ignoreErrors;
			this.postMigration = postMigration;
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put("plugin", this.plugin.getName());
		data.putAll(this.credentials.serialize());
		return data;
	}

	public static ManagedDatasource deserialize(Map<String, Object> data) {
		if (MapUtils.isEmpty(data)) {
			LOGGER.info("Database not defined.");
			return null;
		}
		String pluginName = MoreMapUtils.attemptGet(data, "", "plugin");
		if (Strings.isNullOrEmpty(pluginName)) {
			LOGGER.warning("Config defined ManagedDatasource did not specify a plugin, which is required.");
			return null;
		}
		Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
		if (plugin == null || !plugin.isEnabled()) {
			LOGGER.warning("Config defined ManagedDatasource did not specify a loaded plugin, is it correct?");
			return null;
		}
		if (!ACivMod.class.isAssignableFrom(plugin.getClass())) {
			LOGGER.warning("ManagedDatasource only supports ACivMod plugins.");
			return null;
		}
		return new ManagedDatasource((ACivMod) plugin, Objects.requireNonNull(DatabaseCredentials.deserialize(data)));
	}

}
