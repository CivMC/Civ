package vg.civcraft.mc.namelayer.bungee;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import vg.civcraft.mc.namelayer.database.Database;

public class DataBaseManager {

	private Database db;
	private NameLayerBungee plugin = NameLayerBungee.getInstance();
	
	public DataBaseManager() {
		String user = ConfigManager.getUser();
		String password = ConfigManager.getPassword();
		int port = ConfigManager.getPort();
		String host = ConfigManager.getHost();
		String database = ConfigManager.getDB();
		db = new Database(host, port, database, user, password, plugin.getLogger());
		if (db.connect()) {
			intializeStringStatements();
		}
	}
	
	private String addPlayer, getPlayerFromUUID;
	private void intializeStringStatements() {
		addPlayer = "call addplayertotable(?, ?)"; // order player name, uuid 
		getPlayerFromUUID = "select player from Name_player " +
				"where uuid=?";
	}
	
	public void reconnect() {
		if (!db.isConnected())
			db.connect();
	}
	
	public void addPlayer(String playername, UUID uuid){
		reconnect();
		PreparedStatement addPlayer = db.prepareStatement(this.addPlayer);
		try {
			addPlayer.setString(1, playername);
			addPlayer.setString(2, uuid.toString());
			addPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// returns null if no playername was found
	public String getCurrentName(UUID uuid){
		reconnect();
		PreparedStatement getPlayerfromUUID = db.prepareStatement(this.getPlayerFromUUID);
		try {
			getPlayerfromUUID.setString(1, uuid.toString());
			ResultSet set = getPlayerfromUUID.executeQuery();
			if (!set.next()) return null;
			String playername = set.getString("player");
			return playername;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
