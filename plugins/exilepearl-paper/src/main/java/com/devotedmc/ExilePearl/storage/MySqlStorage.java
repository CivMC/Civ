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
import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.util.Guard;

public class MySqlStorage implements PluginStorage {

	private static final Integer DATABASE_VERSION = 1;

	private final PearlFactory pearlFactory;
	private final PearlLogger logger;
	private final ExilePearlConfig config;

	private MySqlConnection db;
	private boolean isConnected = false;

	/**
	 * Creates a new MySqlStorage instance
	 * @param pearlFactory The pearl factory
	 */
	public MySqlStorage(final PearlFactory pearlFactory, final PearlLogger logger, final ExilePearlConfig config) {
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
				"killer_uid varchar(255) not null," +
				"world varchar(255) not null," +
				"x int not null," +
				"y int not null," +
				"z int not null," +
				"health double not null," +
				"pearled_on datetime not null default 0," +
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
					UUID killerId = UUID.fromString(resultSet.getString("uid"));
					World world = Bukkit.getWorld(resultSet.getString("world"));
					int x = resultSet.getInt("x");
					int y = resultSet.getInt("y");
					int z = resultSet.getInt("z");
					double health = resultSet.getDouble("health");
					Date pearledOn = resultSet.getDate("pearled_on");
					boolean freedOffline = resultSet.getBoolean("freed_offline");

					if (world == null) {
						logger.log(Level.WARNING, "Failed to load world for pearl %s", playerId.toString());
						continue;
					}
					Location loc = new Location(world, x, y, z);

					ExilePearl pearl = pearlFactory.createExilePearl(playerId, killerId, loc, health);
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
			ex.printStackTrace();
		}

		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			Location l = pearl.getLocation();
			
			PreparedStatement ps = db.prepareStatement("INSERT INTO exilepearls VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
			ps.setString(1, pearl.getUniqueId().toString());
			ps.setString(2, pearl.getKillerUniqueId().toString());
			ps.setString(3, l.getWorld().getName());
			ps.setInt(4, l.getBlockX());
			ps.setInt(5, l.getBlockY());
			ps.setInt(6, l.getBlockZ());
			ps.setDouble(7, pearl.getHealth());
			ps.setDate(8, new java.sql.Date(pearl.getPearledOn().getTime()));
			ps.setBoolean(9, pearl.getFreedOffline());
			ps.executeUpdate();
			
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to insert pearl to database for player %s.", pearl.getPlayerName());
			ex.printStackTrace();
		}
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		try {
			PreparedStatement ps = db.prepareStatement("DELETE FROM exilepearls WHERE uid = ?");
			ps.setString(1, pearl.getUniqueId().toString());
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to delete pearl from database for player %s.", pearl.getPlayerName());
			ex.printStackTrace();
		}
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		// TODO Auto-generated method stub

	}

	@Override
	public void pearlUpdateHealth(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		// TODO Auto-generated method stub

	}

	@Override
	public void pearlUpdateFreedOffline(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		// TODO Auto-generated method stub
		
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
}
