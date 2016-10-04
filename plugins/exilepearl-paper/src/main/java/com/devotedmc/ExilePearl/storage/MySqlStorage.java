package com.devotedmc.ExilePearl.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.util.Guard;

public class MySqlStorage implements PluginStorage {

	private static final Integer DATABASE_VERSION = 1;

	private final PearlFactory pearlFactory;
	private final PearlLogger logger;
	private final PearlConfig config;

	private MySqlConnection db;
	private boolean isConnected = false;

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
		db = new MySqlConnection(config.getDbHost(), config.getDbPort(), config.getDbName(), config.getDbUsername(), config.getDbPassword(), logger);
		isConnected = db.connect();
		if (isConnected) {
			setupDatabase();
			return true;
		}
		return false;
	}

	@Override
	public void disconnect() {
		isConnected = false;
		db.close();
		db = null;
	}

	@Override
	public boolean isConnected() {
		return isConnected && db.isConnected();
	}

	/**
	 * Sets up the database
	 */
	private void setupDatabase() {

		// Create plugin table
		db.execute("create table if not exists exilepearlplugin( " + 
				"setting varchar(255) NOT NULL," +
				"value varchar(255) NOT NULL," +
				"PRIMARY KEY (setting));");

		// Create pearl table
		db.execute("create table if not exists exilepearls( " +
				"uid varchar(255) not null," +
				"killer_name varchar(255) not null," +
				"pearl_id int not null," +
				"world varchar(255) not null," +
				"x int not null," +
				"y int not null," +
				"z int not null," +
				"health int not null," +
				"pearled_on long not null," +
				"freed_offline bool," +
				"PRIMARY KEY (uid));");

		if (getDatabaseVersion() < DATABASE_VERSION) {
			// This is a placeholder for any future upgrade methods
			
		}
		updateDatabaseVersion();
	}
	
	/**
	 * Deletes all pearl records
	 */
	public void deleteAllPearls() {
		db.execute("DELETE FROM exilepearls;");
	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();

		try {
			PreparedStatement preparedStatement = db.prepareStatement("SELECT * FROM ExilePearls");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				try {
					UUID playerId = UUID.fromString(resultSet.getString("uid"));
					String killerName = resultSet.getString("killer_name");
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

					ExilePearl pearl = pearlFactory.createExilePearl(playerId, killerName, pearlId, loc);
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

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to load pearls.");
		}

		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			Location l = pearl.getLocation();
			
			PreparedStatement ps = db.prepareStatement("INSERT INTO exilepearls VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			ps.setString(1, pearl.getPlayerId().toString());
			ps.setString(2, pearl.getKillerName());
			ps.setInt(3, pearl.getPearlId());
			ps.setString(4, l.getWorld().getName());
			ps.setInt(5, l.getBlockX());
			ps.setInt(6, l.getBlockY());
			ps.setInt(7, l.getBlockZ());
			ps.setInt(8, pearl.getHealth());
			ps.setLong(9, pearl.getPearledOn().getTime());
			ps.setBoolean(10, pearl.getFreedOffline());
			ps.executeUpdate();
			
		} catch (Exception ex) {
			logFailedPearlOperation(ex, pearl, "insert record");
		}
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			PreparedStatement ps = db.prepareStatement("DELETE FROM exilepearls WHERE uid = ?");
			ps.setString(1, pearl.getPlayerId().toString());
		}
		catch (Exception ex) {
			logFailedPearlOperation(ex, pearl, "delete record");
		}
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			PreparedStatement ps = db.prepareStatement("UPDATE exilepearls SET world = ?, x = ?, y = ?, z = ? WHERE uid = ?");
			
			Location l = pearl.getLocation();
			ps.setString(1, l.getWorld().getName());
			ps.setInt(2, l.getBlockX());
			ps.setInt(3, l.getBlockY());
			ps.setInt(4, l.getBlockZ());
			ps.setString(5, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (Exception ex) {
			logFailedPearlOperation(ex, pearl, "update 'location'");
		}
	}

	@Override
	public void pearlUpdateHealth(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			PreparedStatement ps = db.prepareStatement("UPDATE exilepearls SET health = ? WHERE uid = ?");
			
			ps.setInt(1, pearl.getHealth());
			ps.setString(2, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (Exception ex) {
			logFailedPearlOperation(ex, pearl, "update 'health'");
		}
	}

	@Override
	public void pearlUpdateFreedOffline(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			PreparedStatement ps = db.prepareStatement("UPDATE exilepearls SET freed_offline = ? WHERE uid = ?");
			
			ps.setBoolean(1, pearl.getFreedOffline());
			ps.setString(2, pearl.getPlayerId().toString());
			ps.executeUpdate();
		}
		catch (Exception ex) {
			logFailedPearlOperation(ex, pearl, "update 'freed offline'");
		}
	}

	/**
	 * Gets the database version
	 * @return The database version
	 */
	private int getDatabaseVersion() {
		try {
			PreparedStatement getDBVersion = db.prepareStatement("select * from ExilePearlPlugin;");
			ResultSet resultSet = getDBVersion.executeQuery();
			if (resultSet.next()) {
				return new Integer(resultSet.getString("db_version"));
			}

		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to get the database version.");
		}
		return DATABASE_VERSION;
	}

	/**
	 * Updates the database version
	 */
	private void updateDatabaseVersion() {
		UpdatePluginSetting("db_version", DATABASE_VERSION.toString());
	}
	
	private void UpdatePluginSetting(final String setting, final String value) {
		try {
			PreparedStatement getDBVersion = db.prepareStatement("INSERT INTO ExilePearlPlugin(setting, value) VALUES(?, ?) " + 
					"ON DUPLICATE KEY UPDATE value = ?;");
			getDBVersion.setString(1, setting);
			getDBVersion.setString(2, value);
			getDBVersion.setString(3, value);
			getDBVersion.execute();
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to update the plugin setting %s.", setting);
			ex.printStackTrace();
		}
	}
	
	private void logFailedPearlOperation(Exception ex, ExilePearl pearl, String action) {
		logger.log(Level.SEVERE, "Failed to %s for the pearl for player %s.", action, pearl.getPlayerName());
	}
}
