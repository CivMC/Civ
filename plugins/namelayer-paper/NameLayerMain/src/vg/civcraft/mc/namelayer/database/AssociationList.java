package vg.civcraft.mc.namelayer.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.namelayer.NameLayerPlugin;


public class AssociationList {
	private Database db;

	public AssociationList(Database db){
		this.db = db;
		if (db.isConnected()){
			genTables();
			initializeProcedures();
			initializeStatements();
		}
	}

	public void genTables(){
		// creates the player table
		// Where uuid and host names will be stored
		db.execute("CREATE TABLE IF NOT EXISTS `Name_player` (" + 
				"`uuid` varchar(40) NOT NULL," +
				"`player` varchar(40) NOT NULL,"
				+ "UNIQUE KEY `uuid_player_combo` (`uuid`, `player`));");

		// this creates the table needed for when a player changes there name to a prexisting name before joining the server
		db.execute("create table if not exists playercountnames ("
				+ "player varchar(40) not null,"
				+ "amount int(10) not null,"
				+ "primary key (player));");
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
		db.execute("drop procedure if exists addplayertotable");
		db.execute("create definer=current_user procedure addplayertotable("
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
	}

	// returns null if no uuid was found
	public UUID getUUID(String playername){
		NameLayerPlugin.reconnectAndReintializeStatements();
		PreparedStatement getUUIDfromPlayer = db.prepareStatement(this.getUUIDfromPlayer);
		try {
			getUUIDfromPlayer.setString(1, playername);
			ResultSet set = getUUIDfromPlayer.executeQuery();
			if (!set.next() || set.wasNull()) return null;
			String uuid = set.getString("uuid");
			return UUID.fromString(uuid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// returns null if no playername was found
	public String getCurrentName(UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
		PreparedStatement getPlayerfromUUID = db.prepareStatement(this.getPlayerfromUUID);
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

	public void addPlayer(String playername, UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
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

	public void changePlayer(String newName, UUID uuid) {
		NameLayerPlugin.reconnectAndReintializeStatements();
		PreparedStatement changePlayerName = db.prepareStatement(this.changePlayerName);
		try {
			changePlayerName.setString(1, uuid.toString());
			changePlayerName.execute();
			addPlayer(newName, uuid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * This method returns all player info in the table.  It is used mainly
	 * by NameAPI class to prepopulate the maps.  
	 * As such Object[0] will return Map<String, UUID> while Object[1]
	 * will return Map<UUID, String>
	 */
	public PlayerMappingInfo getAllPlayerInfo(){
		NameLayerPlugin.reconnectAndReintializeStatements();
		PreparedStatement getAllPlayerInfo = db.prepareStatement(this.getAllPlayerInfo);
		Map<String, UUID> nameMapping = new HashMap<String, UUID>();
		Map<UUID, String> uuidMapping = new HashMap<UUID, String>();
		try {
			ResultSet set = getAllPlayerInfo.executeQuery();
			while (set.next()){
				UUID uuid = UUID.fromString(set.getString("uuid"));
				String playername = set.getString("player");
				nameMapping.put(playername, uuid);
				uuidMapping.put(uuid, playername);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
