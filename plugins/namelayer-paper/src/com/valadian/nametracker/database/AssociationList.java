package com.valadian.nametracker.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import com.valadian.nametracker.NameTrackerPlugin;

public class AssociationList {
	private Database db;
    private final String host;
    private final String dbname;
    private final String username;
    private final int port;
    private final String password;

	public AssociationList(NameTrackerPlugin plugin, FileConfiguration config_){
		host = config_.getString("sql.hostname");
		port = config_.getInt("sql.port");
		dbname = config_.getString("sql.dbname");
		username = config_.getString("sql.username");
		password = config_.getString("sql.password");
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		boolean connected = db.connect();
		if (connected){
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
	
	private PreparedStatement addPlayer;
    private PreparedStatement getUUIDfromPlayer;
    private PreparedStatement getPlayerfromUUID;
    private PreparedStatement getAllPlayerUUIDs;
	
	public void initializeStatements(){
		addPlayer = db.prepareStatement("call addplayertotable(?, ?)"); // order player name, uuid 
		getUUIDfromPlayer = db.prepareStatement("select uuid from Name_player " +
				"where player=?");
		getPlayerfromUUID = db.prepareStatement("select player from Name_player " +
				"where uuid=?");
		getAllPlayerUUIDs = db.prepareStatement("select * from Name_player");
	}
	
	public void initializeProcedures(){
		db.execute("drop procedure if exists addplayertotable");
		db.execute("create definer=current_user procedure addplayertotable("
				+ "in pl varchar(40), in uu varchar(40)) sql security invoker begin "
				+ ""
				+ "declare account varchar(40);"
				+ "declare nameamount int(10);"
				+ ""
				+ "set nameamount=0;"
				+ "set nameamount=(select count(*) from Name_player p where p.uuid=uu);"
				+ ""
				+ "if (nameamount < 1) then"
				+ "		set account =(select uuid from Name_player p where p.player=pl);"
				+ "		if (account not like uu) then"
				+ "			insert ignore into playercountnames (player, amount) values (pl, 0);"
				+ ""
				+ "			update playercountnames set amount = nameamount+1 where player=pl;"
				+ ""
				+ "			set nameamount=(select amount from playercountnames where player=pl);"
				+ ""
				+ "			insert into Name_player (player, uuid) values ((select concat (pl,nameamount)), uu);"
				+ "		else"
				+ "			insert ignore into Name_player (player, uuid) values (pl, uu);"
				+ "		end if;"
				+ "end if;"
				+ "end");
	}
	
	// returns null if no uuid was found
	public UUID getUUID(String playername){
		try {
			getUUIDfromPlayer.setString(1, playername);
			ResultSet set = getUUIDfromPlayer.executeQuery();
			if (!set.next()) return null;
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
		try {
			addPlayer.setString(1, playername);
			addPlayer.setString(2, uuid.toString());
			addPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<UUID, String> getAllUUIDSNames(){
		Map<UUID, String> uuids = new HashMap<UUID, String>();
		try {
			ResultSet set = getAllPlayerUUIDs.executeQuery();
			while (set.next())
				uuids.put(UUID.fromString(set.getString("uuid")), "player");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uuids;
	}
}
