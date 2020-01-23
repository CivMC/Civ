package sh.okx.railswitch.database;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.entity.Player;

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

public class RailSwitchDatabase {
    private static final String DESTINATION_TABLE = "rsw_dest";
    
    private ConnectionPool pool;
    private Logger log;
    
    private String setPlayerDestination;
    private String getPlayerDestination;
    private String removePlayerDestination;
    private ExecutorService service = Executors.newSingleThreadExecutor();
    
    private final LoadingCache<Player, Optional<String>> destinations = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<Player, Optional<String>>() {
                @Override
                public Optional<String> load(Player key) throws Exception {
                    try (Connection connection = pool.getConnection()) {
                        PreparedStatement statement = connection.prepareStatement(getPlayerDestination);
                        statement.setString(1, key.getUniqueId().toString());
                        
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            return Optional.of(resultSet.getString("dest"));
                        } else {
                            return Optional.empty();
                        }
                    }
                }
            });
    
    public RailSwitchDatabase(ConnectionPool pool, Logger logger) {
        log = logger;
        this.pool = pool;
        
        createTable();
        loadStatements();
    }
    
    private void createTable() {
        try (Connection connection = pool.getConnection()) {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `" + DESTINATION_TABLE + "` (" +
                            "`uuid` VARCHAR(36) NOT NULL," +
                            "`dest` VARCHAR(40) NOT NULL," +
                            "PRIMARY KEY (`uuid`))");
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Could not create tables", e);
        }
    }
    
    private void loadStatements() {
        setPlayerDestination = String.format(
                "REPLACE INTO `%1$s` VALUES (?, ?)",
                DESTINATION_TABLE);
        getPlayerDestination = String.format(
                "SELECT `dest` FROM `%1$s` WHERE `uuid`=?",
                DESTINATION_TABLE);
        removePlayerDestination = String.format(
                "DELETE FROM `%1$s` WHERE `uuid`=?",
                DESTINATION_TABLE);
    }
    
    public void setPlayerDestination(Player player, String destination) {
        destinations.put(player, Optional.of(destination));
        service.execute(() -> {
            try (Connection connection = pool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(setPlayerDestination);
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, destination);
                
                statement.executeUpdate();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Could not set destination", e);
            }
        });
    }
    
    public void removePlayerDestination(Player player) {
        destinations.put(player, Optional.empty());
        service.execute(() -> {
            try (Connection connection = pool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(removePlayerDestination);
                statement.setString(1, player.getUniqueId().toString());
                
                statement.executeUpdate();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Could not remove destination", e);
            }
        });
    }
    
    public Optional<String> getPlayerDestination(Player player) {
        try {
            return destinations.get(player);
        } catch (ExecutionException e) {
            log.log(Level.SEVERE, "Could not get destination", e);
            return Optional.empty();
        }
    }
}
