package sh.okx.railswitch.database;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 * RailSwitch database class
 */
public class RailSwitchDatabase {
    
    private static final String DESTINATION_TABLE = "rsw_dest";
    
    private ConnectionPool pool;
    
    private Logger logger;
    
    private String setPlayerDestination;
    
    private String getPlayerDestination;
    
    private String removePlayerDestination;
    
    private ExecutorService service = Executors.newSingleThreadExecutor();
    
    private final LoadingCache<Player, Optional<String>> destinations = CacheBuilder.newBuilder().
            maximumSize(1000).
            build(new CacheLoader<Player, Optional<String>>() {
                @Override
                public Optional<String> load(Player key) throws Exception {
                    try (Connection connection = pool.getConnection()) {
                        PreparedStatement statement = connection.prepareStatement(getPlayerDestination);
                        statement.setString(1, key.getUniqueId().toString());
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            return Optional.of(resultSet.getString("dest"));
                        }
                        else {
                            return Optional.empty();
                        }
                    }
                }
            });
    
    /**
     * Creates a new Rail Switch database manager
     *
     * @param pool The database connection to manage
     * @param logger The logger to log to
     */
    public RailSwitchDatabase(ConnectionPool pool, Logger logger) {
        this.logger = logger;
        this.pool = pool;
        createTable();
        loadStatements();
    }
    
    private void createTable() {
        try (Connection connection = this.pool.getConnection()) {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `" + DESTINATION_TABLE + "` (" +
                            "`uuid` VARCHAR(36) NOT NULL," +
                            "`dest` VARCHAR(40) NOT NULL," +
                            "PRIMARY KEY (`uuid`))");
        }
        catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Could not create tables", e);
        }
    }
    
    private void loadStatements() {
        this.setPlayerDestination = String.format(
                "REPLACE INTO `%1$s` VALUES (?, ?)",
                DESTINATION_TABLE);
        this.getPlayerDestination = String.format(
                "SELECT `dest` FROM `%1$s` WHERE `uuid`=?",
                DESTINATION_TABLE);
        this.removePlayerDestination = String.format(
                "DELETE FROM `%1$s` WHERE `uuid`=?",
                DESTINATION_TABLE);
    }
    
    /**
     * Sets a player's destination.
     *
     * @param player The player to set the destination for
     * @param destination The destination to set
     */
    public void setPlayerDestination(Player player, String destination) {
        this.destinations.put(player, Optional.of(destination));
        this.service.execute(() -> {
            try (Connection connection = this.pool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(this.setPlayerDestination);
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, destination);
                statement.executeUpdate();
            }
            catch (SQLException e) {
                this.logger.log(Level.SEVERE, "Could not set destination", e);
            }
        });
    }
    
    /**
     * Resetting a player's destination by removing their entry
     *
     * @param player The player to reset
     */
    public void removePlayerDestination(Player player) {
        this.destinations.put(player, Optional.empty());
        this.service.execute(() -> {
            try (Connection connection = this.pool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(this.removePlayerDestination);
                statement.setString(1, player.getUniqueId().toString());
                statement.executeUpdate();
            }
            catch (SQLException e) {
                this.logger.log(Level.SEVERE, "Could not remove destination", e);
            }
        });
    }
    
    /**
     * Retrieves a player's set destination
     *
     * @param player The player to get the destination of
     * @return Returns an the player's set destinations in a optional string form
     */
    public Optional<String> getPlayerDestination(Player player) {
        try {
            return this.destinations.get(player);
        }
        catch (ExecutionException exception) {
            this.logger.log(Level.SEVERE, "Could not get destination", exception);
            return Optional.empty();
        }
    }
    
}
