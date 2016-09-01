package vg.civcraft.mc.namelayer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssociationList {
	private Database db;
	private Logger logger;

	public AssociationList(Logger logger, Database db){
		this.db = db;
		this.logger = logger;

		if (db != null) {
			genTables();
			initializeProcedures();
			initializeStatements();
		}
	}

	public void genTables(){
		// creates the player table
		// Where uuid and host names will be stored
		try (Connection connection = db.getConnection();
				Statement statement = connection.createStatement();) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `Name_player` (" + 
					"`uuid` varchar(40) NOT NULL," +
					"`player` varchar(40) NOT NULL,"
					+ "UNIQUE KEY `uuid_player_combo` (`uuid`, `player`));");
	
			// this creates the table needed for when a player changes there name to a prexisting name before joining the server
			statement.executeUpdate("create table if not exists playercountnames ("
					+ "player varchar(40) not null,"
					+ "amount int(10) not null,"
					+ "primary key (player));");
		} catch (SQLException se) {
			logger.log(Level.SEVERE, "Failed to generate tables for the association listener", se);
		}
	}

	private String addPlayer;
	private String getUUIDfromPlayer;
	private String getPlayerfromUUID;
	private String changePlayerName;
	private String getAllPlayerInfo;

	public void initializeStatements(){
		addPlayer = "call addplayertotable(?, ?)"; // order player name, uuid 
		getUUIDfromPlayer = "select uuid from Name_player " +
				"where player=?";
		getPlayerfromUUID = "select player from Name_player " +
				"where uuid=?";
		changePlayerName = "delete from Name_player " +
				"where uuid=?";
		getAllPlayerInfo = "select * from Name_player";
	}

	public void initializeProcedures(){
		// TODO: lock this behind version update so its not run every time.
		try (Connection connection = db.getConnection();
				Statement statement = connection.createStatement();) {
		
			statement.executeUpdate("drop procedure if exists addplayertotable");
			statement.executeUpdate("create definer=current_user procedure addplayertotable("
					+ "in pl varchar(40), in uu varchar(40)) sql security invoker begin "
					+ ""
					+ "declare account varchar(40);"
					+ "declare nameamount int(10);"
					+ ""
					+ "set @@SESSION.max_sp_recursion_depth = 30;"
					+ ""
					+ "set nameamount=0;"
					+ "set nameamount=(select count(*) from Name_player p where p.uuid=uu);"
					+ ""
					+ "if (nameamount < 1) then"
					+ "		setName: loop"
					+ "		set account =(select uuid from Name_player p where p.player=pl);"
					+ "		if (account not like uu) then"
					+ ""
					+ "				if (nameamount > 0) then"
					+ "					set pl = (select concat(SUBSTRING(pl, 1, length(pl)-1)));"
					+ "				end if;"
					+ ""
					+ "			insert ignore into playercountnames (player, amount) values (pl, 0);"
					+ ""
					+ "			update playercountnames set amount = nameamount+1 where player=pl;"
					+ ""
					+ "			set nameamount=(select amount from playercountnames where player=pl);"
					+ ""
					+ "			set pl = (select concat (pl,nameamount));"
					+ ""
					+ "			set account =(select uuid from Name_player p where p.player=pl);"
					+ ""
					+ "			if (account not like uu) then"
					+ "				iterate setName;"
					+ "			end if;"
					+ "		else"
					+ "			insert ignore into Name_player (player, uuid) values (pl, uu);"
					+ "			leave SetName;"
					+ "		end if;"
					+ "END LOOP setName;"
					+ "end if;"
					+ "end");
		} catch (SQLException se) {
			logger.log(Level.SEVERE, "Failed to drop and create addplayertotable procedure", se);
		}
	}

	// returns null if no uuid was found
	public UUID getUUID(String playername){
		try (Connection connection = db.getConnection();
				PreparedStatement getUUIDfromPlayer = connection.prepareStatement(this.getUUIDfromPlayer);) {
			getUUIDfromPlayer.setString(1, playername);
			try (ResultSet set = getUUIDfromPlayer.executeQuery();) {
				if (!set.next() || set.wasNull()) return null;
				String uuid = set.getString("uuid");
				return UUID.fromString(uuid);
			} catch (SQLException se) {
				logger.log(Level.WARNING, "Failed to get UUID for playername " + playername, se);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to set up query to get UUID for playername " + playername, e);
		}
		return null;
	}

	// returns null if no playername was found
	public String getCurrentName(UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement getPlayerfromUUID = connection.prepareStatement(this.getPlayerfromUUID);) {
			getPlayerfromUUID.setString(1, uuid.toString());
			try (ResultSet set = getPlayerfromUUID.executeQuery();) {
				if (!set.next()) return null;
				String playername = set.getString("player");
				return playername;
			} catch (SQLException se) {
				logger.log(Level.WARNING, "Failed to get current player name for UUID " + uuid, se);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to set up query to get current player name for UUID " + uuid, e);
		}
		return null;
	}

	public void addPlayer(String playername, UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement addPlayer = connection.prepareStatement(this.addPlayer);) {
			addPlayer.setString(1, playername);
			addPlayer.setString(2, uuid.toString());
			addPlayer.execute();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to add new player mapping {0} <==> {1}, due to {2}", 
					new Object[] {playername, uuid, e.getMessage()});
			logger.log(Level.WARNING, "Add new player failure: ", e);
		}
	}

	public void changePlayer(String newName, UUID uuid) {
		try (Connection connection = db.getConnection();
				PreparedStatement changePlayerName = connection.prepareStatement(this.changePlayerName);) {
			changePlayerName.setString(1, uuid.toString());
			changePlayerName.execute();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to change player name mapping {0} <==> {1}, due to {2}", 
					new Object[] {newName, uuid, e.getMessage()});
			logger.log(Level.WARNING, "Change player failure: ", e);
			return; // don't add on failure
		}
		// wait to close prior connection and such before add player with new name.
		addPlayer(newName, uuid);
	}
	
	/**
	 * This method returns all player info in the table.  It is used mainly
	 * by NameAPI class to prepopulate the maps.  
	 * As such PlayerMappingInfo.nameMapping will return Map<String, UUID> 
	 * while PlayerMappingInfo.uuidMapping will return Map<UUID, String>
	 */
	public PlayerMappingInfo getAllPlayerInfo(){
		Map<String, UUID> nameMapping = new HashMap<String, UUID>();
		Map<UUID, String> uuidMapping = new HashMap<UUID, String>();
		try (Connection connection = db.getConnection();
				PreparedStatement getAllPlayerInfo = connection.prepareStatement(this.getAllPlayerInfo);
				ResultSet set = getAllPlayerInfo.executeQuery();) {
			while (set.next()){
				UUID uuid = UUID.fromString(set.getString("uuid"));
				String playername = set.getString("player");
				nameMapping.put(playername, uuid);
				uuidMapping.put(uuid, playername);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to get all player info", e);
		}
		return new PlayerMappingInfo(nameMapping, uuidMapping);
	}

	public static class PlayerMappingInfo {
		public final Map<String, UUID> nameMapping;
		public final Map<UUID, String> uuidMapping;
		public PlayerMappingInfo(Map<String, UUID> nameMap, Map<UUID, String> uuidMap) {
			this.nameMapping = nameMap;
			this.uuidMapping = uuidMap;
		}
	}
}
