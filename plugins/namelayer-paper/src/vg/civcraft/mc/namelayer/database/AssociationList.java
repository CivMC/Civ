package vg.civcraft.mc.namelayer.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	
	private PreparedStatement addPlayer;
    private PreparedStatement getUUIDfromPlayer;
    private PreparedStatement getPlayerfromUUID;
	private PreparedStatement changePlayerName;
	
	public void initializeStatements(){
		addPlayer = db.prepareStatement("call addplayertotable(?, ?)"); // order player name, uuid 
		getUUIDfromPlayer = db.prepareStatement("select uuid from Name_player " +
				"where player=?");
		getPlayerfromUUID = db.prepareStatement("select player from Name_player " +
				"where uuid=?");

		changePlayerName = db.prepareStatement("delete from Name_player " +
				"where uuid=?");
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
	public synchronized UUID getUUID(String playername){
		NameLayerPlugin.reconnectAndReintializeStatements();
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
	public synchronized String getCurrentName(UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
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
	
	public synchronized void addPlayer(String playername, UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			addPlayer.setString(1, playername);
			addPlayer.setString(2, uuid.toString());
			addPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public synchronized void changePlayer(String newName, UUID uuid) {
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			changePlayerName.setString(1, uuid.toString());
			changePlayerName.execute();
			addPlayer(newName, uuid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
