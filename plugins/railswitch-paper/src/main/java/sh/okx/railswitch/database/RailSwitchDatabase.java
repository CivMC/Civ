package sh.okx.railswitch.database;

import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.dao.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RailSwitchDatabase {
  //private String switchTable;
  private String destTable;

  private ConnectionPool pool;
  private Logger log;

  private String setPlayerDestination;
  private String getPlayerDestination;

  public RailSwitchDatabase(String host, int port, String db, String user, String password, String prefix, Logger logger) {
    log = logger;
    pool = new ConnectionPool(logger, user, password, host, port, db, 10, 1000, 600000, 7200000);

    destTable = prefix + "dest";
    createTable();
    loadStatements();
  }

  private void createTable() {
    try (Connection connection = pool.getConnection()) {
      connection.createStatement().executeUpdate(
          "CREATE TABLE IF NOT EXISTS `" + destTable + "` (" +
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
        destTable);
    getPlayerDestination = String.format(
        "SELECT `dest` FROM `%1$s` WHERE `uuid`=?",
        destTable);
  }

  public void setPlayerDestination(Player player, String destination) {
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(setPlayerDestination);
      statement.setString(1, player.getUniqueId().toString());
      statement.setString(2, destination);

      statement.executeUpdate();
    } catch (SQLException e) {
      log.log(Level.SEVERE, "Could not set destination", e);
    }
  }

  public String getPlayerDestination(Player player) {
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(getPlayerDestination);
      statement.setString(1, player.getUniqueId().toString());

      ResultSet resultSet = statement.executeQuery();
      if (!resultSet.next()) {
        return null;
      }
      return resultSet.getString("dest");
    } catch (SQLException e) {
      log.log(Level.SEVERE, "Could not get destination", e);
      return null;
    }
  }
}
