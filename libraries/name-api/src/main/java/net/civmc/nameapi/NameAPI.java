package net.civmc.nameapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NameAPI {

    private DataSource db;
    private Logger logger;

    private static final String addPlayer = "call addplayertotable(?, ?)"; // order player name, uuid
    private static final String getUUIDfromPlayer = "select uuid from Name_player where player=?";
    private static final String getPlayerfromUUID = "select player from Name_player where uuid=?";
    private static final String changePlayerName = "REPLACE INTO Name_player (player, uuid) VALUES (?, ?)";
    private static final String getAllPlayerInfo = "select * from Name_player WHERE id > ?";

    public NameAPI(Logger logger, DataSource source) {
        this.logger = logger;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.db = source;
    }

    public void migrate() {
        Migrator migrator = new Migrator();
        // creates the player table
        // Where uuid and host names will be stored
        migrator.registerMigration("renamer", 0,
            "CREATE TABLE IF NOT EXISTS `Name_player` (" +
                "`uuid` varchar(40) NOT NULL," +
                "`player` varchar(40) NOT NULL,"
                + "UNIQUE KEY `uuid_player_combo` (`uuid`, `player`));",
            // this creates the table needed for when a player changes there name to a prexisting name before joining the server
            "create table if not exists playercountnames ("
                + "player varchar(40) not null,"
                + "amount int(10) not null,"
                + "primary key (player));");

        migrator.registerMigration("renamer", 1,
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

        migrator.registerMigration("renamer", 2,
            "ALTER TABLE Name_player ADD COLUMN id SERIAL",
            "ALTER TABLE Name_player DROP INDEX `uuid_player_combo`",
            "ALTER TABLE Name_player ADD UNIQUE INDEX on_uuid(uuid)");

        try {
            migrator.migrate(db.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get player's UUID based on their name
     *
     * @param playername the player's name
     * @return the UUID of the player, or null
     */
    public @Nullable UUID getUUID(String playername) {
        try (Connection connection = db.getConnection();
             PreparedStatement getUUIDfromPlayer = connection.prepareStatement(NameAPI.getUUIDfromPlayer);) {
            getUUIDfromPlayer.setString(1, playername);
            try (ResultSet set = getUUIDfromPlayer.executeQuery();) {
                if (!set.next() || set.wasNull()) return null;
                String uuid = set.getString("uuid");
                return UUID.fromString(uuid);
            } catch (SQLException se) {
                logger.warn("Failed to get UUID for playername " + playername, se);
            }
        } catch (SQLException e) {
            logger.warn("Failed to set up query to get UUID for playername " + playername, e);
        }
        return null;
    }

    /**
     * Get player's current name based on UUID
     *
     * @param uuid get the current server's name for this UUId
     * @return the player's name if found
     */
    public @Nullable String getCurrentName(UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement getPlayerfromUUID = connection.prepareStatement(NameAPI.getPlayerfromUUID);) {
            getPlayerfromUUID.setString(1, uuid.toString());
            try (ResultSet set = getPlayerfromUUID.executeQuery();) {
                if (!set.next()) return null;
                String playername = set.getString("player");
                return playername;
            } catch (SQLException se) {
                logger.warn("Failed to get current player name for UUID " + uuid, se);
            }
        } catch (SQLException e) {
            logger.warn("Failed to set up query to get current player name for UUID " + uuid, e);
        }
        return null;
    }

    /**
     * Adds a player to the database.  If the player already exists, does nothing.
     *
     * @param playername the player's name
     * @param uuid       the player's UUID
     */
    public void addPlayer(String playername, UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement addPlayer = connection.prepareStatement(NameAPI.addPlayer);) {
            addPlayer.setString(1, playername);
            addPlayer.setString(2, uuid.toString());
            addPlayer.execute();
        } catch (SQLException e) {
            logger.warn("Failed to add new player mapping {0} <==> {1}, due to {2}",
                new Object[]{playername, uuid, e.getMessage()});
            logger.warn("Add new player failure: ", e);
        }
    }

    /**
     * Changes player's name in the database
     *
     * @param newName the player's new name
     * @param uuid    the player's UUID
     */
    public void changePlayer(String newName, UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement changePlayerName = connection.prepareStatement(NameAPI.changePlayerName);) {
            changePlayerName.setString(1, newName);
            changePlayerName.setString(2, uuid.toString());
            changePlayerName.execute();
        } catch (SQLException e) {
            logger.warn("Failed to change player name mapping {0} <==> {1}, due to {2}",
                new Object[]{newName, uuid, e.getMessage()});
            logger.warn("Change player failure: ", e);
            return; // don't add on failure
        }
    }

    /**
     * This method returns all player info in the table.  It is used mainly
     * by NameAPI class to prepopulate the maps.
     * As such PlayerMappingInfo.nameMapping will return Map&lt;String, UUID&gt;
     * while PlayerMappingInfo.uuidMapping will return Map&lt;UUID, String&gt;
     *
     * @param highestKnown only includes rows with a higher id than this. Use 0 to get all player mappings.
     * @return the player mapping info
     */
    public PlayerMappingInfo getAllPlayerInfo(long highestKnown) {
        if (highestKnown < 0) {
            throw new IllegalArgumentException("highestKnown < 0");
        }

        Map<String, UUID> nameMapping = new HashMap<String, UUID>();
        Map<UUID, String> uuidMapping = new HashMap<UUID, String>();
        long highest = highestKnown;
        try (Connection connection = db.getConnection()) {
            PreparedStatement getAllPlayerInfo = connection.prepareStatement(NameAPI.getAllPlayerInfo);
            getAllPlayerInfo.setLong(1, highestKnown);
            ResultSet set = getAllPlayerInfo.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid"));
                String playername = set.getString("player");
                nameMapping.put(playername, uuid);
                uuidMapping.put(uuid, playername);
                int id = set.getInt("id");
                if (id > highest) {
                    highest = id;
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to get all player info", e);
        }
        return new PlayerMappingInfo(nameMapping, uuidMapping, highest);
    }

    public record PlayerMappingInfo(Map<String, UUID> nameMapping, Map<UUID, String> uuidMapping, long highest) {

    }
}
