package com.devotedmc.ExilePearl.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.config.Document;
import com.devotedmc.ExilePearl.config.MySqlConfig;

import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.civmodcore.dao.ConnectionPool;

class MySqlStorage implements PluginStorage {

	private static final Integer DATABASE_VERSION = 1;

	private final PearlFactory pearlFactory;
	private final PearlLogger logger;
	private final MySqlConfig config;

	private ConnectionPool db;
	private boolean isConnected = false;

	private static final String createPluginTable = "create table if not exists exilepearlplugin( " + 
			"setting varchar(255) NOT NULL," +
			"value varchar(255) NOT NULL," +
			"PRIMARY KEY (setting));";
	
	private static final String createPearlTable = "create table if not exists exilepearls( " +
			"uid varchar(36) not null," +
			"killer_id varchar(36) not null," +
			"pearl_id int not null," +
			"ptype int not null," +
			"world varchar(36) not null," +
			"x int not null," +
			"y int not null," +
			"z int not null," +
			"health int not null," +
			"pearled_on long not null," +
			"freed_offline bool," +
			"PRIMARY KEY (uid));";

	/**
	 * Creates a new MySqlStorage instance
	 * @param pearlFactory The pearl factory
	 */
	public MySqlStorage(final PearlFactory pearlFactory, final PearlLogger logger, final MySqlConfig config) {
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		Guard.ArgumentNotNull(logger, "logger");
		Guard.ArgumentNotNull(config, "config");

		this.pearlFactory = pearlFactory;
		this.logger = logger;
		this.config = config;
	}

	@Override
	public boolean connect() {
		db = new ConnectionPool(logger.getPluginLogger(), 
				config.getMySqlUsername(), 
				config.getMySqlPassword(), 
				config.getMySqlHost(), 
				config.getMySqlPort(), 
				config.getMySqlDatabaseName(), 
				config.getMySqlPoolSize(), 
				config.getMySqlConnectionTimeout(), 
				config.getMySqlIdleTimeout(), 
				config.getMySqlMaxLifetime());

		isConnected = true;
		setupDatabase();
		return true;
	}

	@Override
	public void disconnect() {
		isConnected = false;
		try {
			db.close();
		} catch (SQLException e) {
		}
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Sets up the database
	 */
	private void setupDatabase() {

		try (Connection connection = db.getConnection();) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(createPluginTable)) {
				preparedStatement.execute();
			} catch (SQLException ex) {
				logger.log(Level.SEVERE, "Failed to create the plugin table.");
			}
			
			try (PreparedStatement preparedStatement = connection.prepareStatement(createPearlTable)) {
				preparedStatement.execute();
			} catch (SQLException ex) {
				logger.log(Level.SEVERE, "Failed to create the pearl table.");
			}
		} catch (SQLException ex) {
			logger.log(Level.SEVERE, "Failed to setup the database");
		}

		if (isFirstRun()) {
			logger.log(Level.WARNING, "ExilePearl is running for the first time.");
			if (config.getMigratePrisonPearl()) {
				migratePrisonPearl();
			}
			setHasRun();
		} else if (getDatabaseVersion() < DATABASE_VERSION) {
			// This is a placeholder for any future upgrade methods
		}

		updateDatabaseVersion();
	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();

		logger.log("Loading ExilePearl pearls.");

		try (Connection connection = db.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM exilepearls"); ) {
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData meta = resultSet.getMetaData();
			while (resultSet.next()) {
				try {
					// Translate the SQL data into a document
					Document doc = new Document();
					for (int i = 1; i <= meta.getColumnCount(); i++) {
		                String key = meta.getColumnName(i);
		                Object value = resultSet.getObject(key);
		                doc.put(key, value);
		            }
					
					// Translate the date and location to the expected format
					doc.append("pearled_on", new Date(Long.parseLong(doc.getString("pearled_on"))));
					doc.append("location", new Document("world", doc.getString("world"))
							.append("x", doc.getInteger("x"))
							.append("y", doc.getInteger("y"))
							.append("z", doc.getInteger("z")));
					doc.append("type", doc.getInteger("ptype", 0));
					
					pearls.add(pearlFactory.createExilePearl(doc.getUUID("uid"), doc));
				} catch (Exception ex) {
					logger.log(Level.WARNING, "Failed to load pearl record: %s", resultSet.toString());
					ex.printStackTrace();
				}
			}

		} catch (SQLException ex) {
			logger.log(Level.SEVERE, "An error occurred when loading pearls.");
		}

		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("INSERT INTO exilepearls VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"); ) {

			Location l = pearl.getLocation();

			ps.setString(1, pearl.getPlayerId().toString());
			ps.setString(2, pearl.getKillerId().toString());
			ps.setInt(3, pearl.getPearlId());
			ps.setInt(4, pearl.getPearlType().toInt());
			ps.setString(5, l.getWorld().getName());
			ps.setInt(6, l.getBlockX());
			ps.setInt(7, l.getBlockY());
			ps.setInt(8, l.getBlockZ());
			ps.setInt(9, pearl.getHealth());
			ps.setLong(10, pearl.getPearledOn().getTime());
			ps.setBoolean(11, pearl.getFreedOffline());
			ps.executeUpdate();

		} catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "insert record");
		}
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("DELETE FROM exilepearls WHERE uid = ?"); ) {
			ps.setString(1, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "delete record");
		}
	}

	@Override
	public void updatePearlLocation(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("UPDATE exilepearls SET world = ?, x = ?, y = ?, z = ? WHERE uid = ?"); ) {

			Location l = pearl.getLocation();
			ps.setString(1, l.getWorld().getName());
			ps.setInt(2, l.getBlockX());
			ps.setInt(3, l.getBlockY());
			ps.setInt(4, l.getBlockZ());
			ps.setString(5, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "update 'location'");
		}
	}

	@Override
	public void updatePearlHealth(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("UPDATE exilepearls SET health = ? WHERE uid = ?"); ) {

			ps.setInt(1, pearl.getHealth());
			ps.setString(2, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "update 'health'");
		}
	}

	@Override
	public void updatePearlFreedOffline(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("UPDATE exilepearls SET freed_offline = ? WHERE uid = ?"); ) {

			ps.setBoolean(1, pearl.getFreedOffline());
			ps.setString(2, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "update 'freed offline'");
		}
	}

	@Override
	public void updatePearlType(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("UPDATE exilepearls SET ptype = ? WHERE uid = ?"); ) {

			ps.setInt(1, pearl.getPearlType().toInt());
			ps.setString(2, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "update 'freed offline'");
		}
	}

	/**
	 * Migrate prison pearl data
	 */
	private void migratePrisonPearl() {
		logger.log(Level.WARNING, "Attempting to perform PrisonPearl data migration.");
		
		ConnectionPool migrateDb = new ConnectionPool(logger.getPluginLogger(), 
				config.getMySqlUsername(), 
				config.getMySqlPassword(), 
				config.getMySqlHost(), 
				config.getMySqlPort(), 
				config.getMySqlMigrateDatabaseName(), 
				config.getMySqlPoolSize(), 
				config.getMySqlConnectionTimeout(), 
				config.getMySqlIdleTimeout(), 
				config.getMySqlMaxLifetime());

		int migrated = 0;
		int failed = 0;

		try {
			// Check if the table exists
			try (Connection connection = migrateDb.getConnection();) {
				DatabaseMetaData dbm = migrateDb.getConnection().getMetaData();

				ResultSet tables = dbm.getTables(null, null, "PrisonPearls", null);
				if (!tables.next()) {
					logger.log(Level.WARNING, "No PrisonPearl data was found.");
					return;
				}
			} catch(Exception ex) {
				logger.log(Level.WARNING, "Failed to retrieve PrisonPearl table.");
				return;
			}

			try (Connection connection = migrateDb.getConnection();
					PreparedStatement getAllPearls = connection.prepareStatement("SELECT * FROM PrisonPearls;"); ) {
				ResultSet set = getAllPearls.executeQuery();
				while (set.next()) {
					try {
						UUID playerId = UUID.fromString(set.getString("uuid"));
						
						String w = set.getString("world");
						World world = Bukkit.getWorld(w);
						int x = set.getInt("x");
						int y = set.getInt("y");
						int z = set.getInt("z");
						int pearlId = set.getInt("uq");
						UUID killerId = null;
						String killerUUIDAsString = set.getString("killer");
						if (killerUUIDAsString == null) {
							killerId = UUID.randomUUID();
						}
						else {
							killerId = UUID.fromString(killerUUIDAsString);
						}
						long imprisonTime = set.getLong("pearlTime");
						if (imprisonTime == 0) {
							imprisonTime = new Date().getTime();
						}
						Date pearledOn = new Date(imprisonTime);
						Location loc = new Location(world, x, y, z);

						Document doc = new Document("killer_id", killerId)
								.append("pearl_id", pearlId)
								.append("location", loc)
								.append("pearled_on", pearledOn);

						ExilePearl pearl = pearlFactory.createdMigratedPearl(playerId, doc);
						if (pearl != null) {
							pearlInsert(pearl);
							migrated++;
						} else {
							failed++;
						}

					} catch(SQLException ex) {
						failed++;
						logger.log(Level.SEVERE, "Failed to migrate PisonPearl pearl.");
					}
				}
			} catch(SQLException ex) {
				logger.log(Level.SEVERE, "An error occurred when migrating PrisonPearl data.");
				return;
			}
		} catch(Exception ex) {
			logger.log(Level.SEVERE, "An error occurred when migrating PrisonPearl data.");
			return;
		}

		logger.log(Level.INFO, "PrisonPearl data migration complete. Migrated %d and %d failed.", migrated, failed);
	}

	/**
	 * Gets the database version
	 * @return The database version
	 */
	private int getDatabaseVersion() {
		int version = DATABASE_VERSION;
		String versionStr = getPluginSetting("db_version");

		if (versionStr == null) {
			version = Integer.parseInt(versionStr);
		}

		return version;
	}

	/**
	 * Updates the database version
	 */
	private void updateDatabaseVersion() {
		setPluginSetting("db_version", DATABASE_VERSION.toString());
	}

	private boolean isFirstRun() {
		return getPluginSetting("first_run") == null;
	}

	private void setHasRun() {
		setPluginSetting("first_run", "1");
	}

	private void setPluginSetting(final String setting, final String value) {
		try (Connection connection = db.getConnection();
				PreparedStatement stmt = connection.prepareStatement("INSERT INTO exilepearlplugin(setting, value) VALUES(?, ?) " + 
						"ON DUPLICATE KEY UPDATE value = ?;");) {
			stmt.setString(1, setting);
			stmt.setString(2, value);
			stmt.setString(3, value);
			stmt.execute();
		}
		catch (SQLException ex) {
			logger.log(Level.SEVERE, "Failed to update the plugin setting %s.", setting);
			ex.printStackTrace();
		}
	}

	private String getPluginSetting(final String setting) {
		String value = null;

		try (Connection connection = db.getConnection();
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM exilepearlplugin where setting = ?;");) {
			stmt.setString(1, setting);

			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				value = resultSet.getString("value");
			}
		}
		catch (SQLException ex) {
			logger.log(Level.SEVERE, "Failed to update the plugin setting %s.", setting);
			ex.printStackTrace();
		}
		return value;
	}

	private void logFailedPearlOperation(Exception ex, ExilePearl pearl, String action) {
		logger.log(Level.SEVERE, "Failed to %s pearl for player %s.", action, pearl.getPlayerName());
	}
}
