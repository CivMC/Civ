package com.devotedmc.ExilePearl.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import com.devotedmc.ExilePearl.config.PearlConfig;

import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.civmodcore.dao.ConnectionPool;

class MySqlStorage implements PluginStorage {

	private static final Integer DATABASE_VERSION = 1;

	private final PearlFactory pearlFactory;
	private final PearlLogger logger;
	private final PearlConfig config;

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
	public MySqlStorage(final PearlFactory pearlFactory, final PearlLogger logger, final PearlConfig config) {
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
				config.getMySqlName(), 
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
			migratePrisonPearl();
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
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ExilePearls"); ) {
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				try {
					UUID playerId = UUID.fromString(resultSet.getString("uid"));
					UUID killerId = UUID.fromString(resultSet.getString("killer_id"));
					int pearlId = resultSet.getInt("pearl_id");
					World world = Bukkit.getWorld(resultSet.getString("world"));
					int x = resultSet.getInt("x");
					int y = resultSet.getInt("y");
					int z = resultSet.getInt("z");
					int health = resultSet.getInt("health");
					Date pearledOn = new Date(resultSet.getLong("pearled_on"));
					boolean freedOffline = resultSet.getBoolean("freed_offline");

					if (world == null) {
						logger.log(Level.WARNING, "Failed to load world for pearl %s", playerId.toString());
						continue;
					}
					Location loc = new Location(world, x, y, z);

					ExilePearl pearl = pearlFactory.createExilePearl(playerId, killerId, pearlId, loc);
					pearl.setHealth(health);
					pearl.setPearledOn(pearledOn);
					pearl.setFreedOffline(freedOffline);
					pearl.enableStorage();
					pearls.add(pearl);
				} catch (Exception ex) {
					logger.log(Level.WARNING, "Failed to load pearl record: %s", resultSet.toString());
					ex.printStackTrace();
				}
			}

		} catch (SQLException ex) {
			logger.log(Level.SEVERE, "An error occurred when loading pearls.");
		}

		logger.log("Loaded %d exile pearls.", pearls.size());

		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");

		try (Connection connection = db.getConnection();
				PreparedStatement ps = connection.prepareStatement("INSERT INTO exilepearls VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"); ) {

			Location l = pearl.getLocation();

			ps.setString(1, pearl.getPlayerId().toString());
			ps.setString(2, pearl.getKillerUniqueId().toString());
			ps.setInt(3, pearl.getPearlId());
			ps.setString(4, l.getWorld().getName());
			ps.setInt(5, l.getBlockX());
			ps.setInt(6, l.getBlockY());
			ps.setInt(7, l.getBlockZ());
			ps.setInt(8, pearl.getHealth());
			ps.setLong(9, pearl.getPearledOn().getTime());
			ps.setBoolean(10, pearl.getFreedOffline());
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
		}
		catch (SQLException ex) {
			logFailedPearlOperation(ex, pearl, "delete record");
		}
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {
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
	public void pearlUpdateHealth(ExilePearl pearl) {
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
	public void pearlUpdateFreedOffline(ExilePearl pearl) {
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

	/**
	 * Migrate prison pearl data
	 */
	private void migratePrisonPearl() {
		logger.log(Level.WARNING, "Attempting to perform PrisonPearl data migration.");

		int migrated = 0;
		int failed = 0;

		try {
			// Check if the table exists
			try (Connection connection = db.getConnection();) {
				DatabaseMetaData dbm = db.getConnection().getMetaData();

				ResultSet tables = dbm.getTables(null, null, "prisonpearls", null);
				if (!tables.next()) {
					logger.log(Level.WARNING, "No PrisonPearl data was found.");
					return;
				}
			} catch(Exception ex) {
				logger.log(Level.WARNING, "Failed to retrieve PrisonPearl table.");
				return;
			}

			try (Connection connection = db.getConnection();
					PreparedStatement getAllPearls = connection.prepareStatement("SELECT * FROM PrisonPearls;"); ) {
				ResultSet set = getAllPearls.executeQuery();
				while (set.next()) {
					try {
						String w = set.getString("world");
						World world = Bukkit.getWorld(w);
						int x = set.getInt("x");
						int y = set.getInt("y");
						int z = set.getInt("z");
						UUID playerId = UUID.fromString(set.getString("uuid"));
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

						ExilePearl pearl = pearlFactory.createExilePearl(playerId, killerId, pearlId, loc);
						pearl.setPearledOn(pearledOn);
						pearl.setHealth(config.getPearlHealthMaxValue() / 2); // set health to half max health
						pearl.enableStorage();
						pearlInsert(pearl);

						migrated++;
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
				PreparedStatement stmt = connection.prepareStatement("INSERT INTO ExilePearlPlugin(setting, value) VALUES(?, ?) " + 
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
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM ExilePearlPlugin where setting = ?;");) {
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
