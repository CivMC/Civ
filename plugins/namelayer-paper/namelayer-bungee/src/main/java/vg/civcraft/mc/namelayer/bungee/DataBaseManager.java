package vg.civcraft.mc.namelayer.bungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBaseManager {

	private Logger logger;
	private Database db;
	private NameLayerBungee plugin = NameLayerBungee.getInstance();
	
	private static final String addPlayer = "call addplayertotable(?, ?)"; // order player name, uuid 
	private static final String getPlayerFromUUID = "select player from Name_player where uuid=?";
	private static final String getUUIDfromPlayer = "select uuid from Name_player where player=?";
	
	public DataBaseManager() {
		this.logger = plugin.getLogger();
		String user = ConfigManager.getUser();
		String password = ConfigManager.getPassword();
		int port = ConfigManager.getPort();
		String host = ConfigManager.getHost();
		String database = ConfigManager.getDB();
		int poolsize = ConfigManager.getPoolsize();
		long connectionTimeout = ConfigManager.getConnectionTimeout();
		long idleTimeout = ConfigManager.getIdleTimeout();
		long maxLifetime = ConfigManager.getMaxLifetime();
		db = new Database(logger, user, password, host, port, database,
				poolsize, connectionTimeout, idleTimeout, maxLifetime);
		try {
			db.available();
		} catch (Exception e){
			logger.log(Level.SEVERE, "Unable to prepare the database pool!");
		}
	}
	
	public void addPlayer(String playername, UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement addPlayer = connection.prepareStatement(DataBaseManager.addPlayer);) {
			addPlayer.setString(1, playername);
			addPlayer.setString(2, uuid.toString());
			addPlayer.execute();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to add player {0} - {1} ", new Object[] {playername, uuid});
			logger.log(Level.WARNING, "Failed to add player", e);
		}
	}
	
	// returns null if no playername was found
	public String getCurrentName(UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement getPlayerfromUUID = connection.prepareStatement(DataBaseManager.getPlayerFromUUID);) {
			getPlayerfromUUID.setString(1, uuid.toString());
			try (ResultSet set = getPlayerfromUUID.executeQuery();) {
				if (!set.next()) return null;
				String playername = set.getString("player");
				return playername;
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Failed to get current name for " + uuid, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to prepare query to get current name for " + uuid, e);
		}
		return null;
	}
	
	// returns null if no uuid was found
	public UUID getUUID(String playername){
		try (Connection connection = db.getConnection();
				PreparedStatement getUUIDfromPlayer = connection.prepareStatement(DataBaseManager.getUUIDfromPlayer);) {
			getUUIDfromPlayer.setString(1, playername);
			try (ResultSet set = getUUIDfromPlayer.executeQuery();) {
				if (!set.next() || set.wasNull()) return null;
				String uuid = set.getString("uuid");
				return UUID.fromString(uuid);
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Failed to get uuid for " + playername, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to prepare query to get uuid for " + playername, e);
		}
		return null;
	}
}
