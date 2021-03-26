package vg.civcraft.mc.namelayer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class AssociationList {
	private ManagedDatasource db;
	private Logger logger;
	
	private static final String addPlayer = "call addplayertotable(?, ?)"; // order player name, uuid
	private static final String getUUIDfromPlayer = "select uuid from Name_player where player=?"; 
	private static final String getPlayerfromUUID = "select player from Name_player where uuid=?";
	private static final String changePlayerName = "update Name_player set player=? where uuid=?";
	private static final String getAllPlayerInfo = "select * from Name_player";
	
	public AssociationList(Logger logger, ManagedDatasource db){
		this.db = db;
		this.logger = logger;
	}

	public void registerMigrations(){
		// creates the player table
		// Where uuid and host names will be stored
		db.registerMigration(-1, false, 
				"CREATE TABLE IF NOT EXISTS `Name_player` (" + 
					"`uuid` varchar(40) NOT NULL," +
					"`player` varchar(40) NOT NULL,"
					+ "UNIQUE KEY `uuid_player_combo` (`uuid`, `player`));",
				// this creates the table needed for when a player changes there name to a prexisting name before joining the server
				"create table if not exists playercountnames ("
					+ "player varchar(40) not null,"
					+ "amount int(10) not null,"
					+ "primary key (player));");

		db.registerMigration(0, false, 
				"drop procedure if exists addplayertotable",
				"create definer=current_user procedure addplayertotable("
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
		// For future migrations, check the max migrations that is combination of here and
		// GroupManagerDao!
	}

	/**
	 * returns null if no uuid was found
	 * @param playername the player's name
	 * @return the UUID of the player, or null
	 */
	public UUID getUUID(String playername){
		try (Connection connection = db.getConnection();
				PreparedStatement getUUIDfromPlayer = connection.prepareStatement(AssociationList.getUUIDfromPlayer);) {
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

	/**
	 *  returns null if no playername was found
	 * @param uuid get the current server's name for this UUId
	 * @return the player's name if found
	 */
	public String getCurrentName(UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement getPlayerfromUUID = connection.prepareStatement(AssociationList.getPlayerfromUUID);) {
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
				PreparedStatement addPlayer = connection.prepareStatement(AssociationList.addPlayer);) {
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
				PreparedStatement changePlayerName = connection.prepareStatement(AssociationList.changePlayerName);) {
			changePlayerName.setString(1, newName);
			changePlayerName.setString(2, uuid.toString());
			changePlayerName.execute();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to change player name mapping {0} <==> {1}, due to {2}", 
					new Object[] {newName, uuid, e.getMessage()});
			logger.log(Level.WARNING, "Change player failure: ", e);
			return; // don't add on failure
		}
	}
	
	/**
	 * This method returns all player info in the table.  It is used mainly
	 * by NameAPI class to prepopulate the maps.  
	 * As such PlayerMappingInfo.nameMapping will return Map&lt;String, UUID&gt;
	 * while PlayerMappingInfo.uuidMapping will return Map&lt;UUID, String&gt;
	 *
	 * @return the player mapping info is possible
	 */
	public PlayerMappingInfo getAllPlayerInfo(){
		Map<String, UUID> nameMapping = new HashMap<String, UUID>();
		Map<UUID, String> uuidMapping = new HashMap<UUID, String>();
		try (Connection connection = db.getConnection();
				PreparedStatement getAllPlayerInfo = connection.prepareStatement(AssociationList.getAllPlayerInfo);
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
