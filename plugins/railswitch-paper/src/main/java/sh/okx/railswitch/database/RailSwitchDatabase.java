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

  // takes a uuid and location and returns a row if and only if that uuid
  // has their destination set to the same destination as that location
  //private String checkDestination;
  //private String getDestination;
  //private String setSwitchDestination;
  private String setPlayerDestination;
  //private String removeSwitchDestination;
  private String getPlayerDestination;

  public RailSwitchDatabase(String host, int port, String db, String user, String password, String prefix, Logger logger) {
    log = logger;
    pool = new ConnectionPool(logger, user, password, host, port, db, 10, 1000, 600000, 7200000);

    //switchTable = prefix + "switch";
    destTable = prefix + "dest";
    createTable();
    //loadSwitches();
    loadStatements();
  }

  private void createTable() {
    try (Connection connection = pool.getConnection()) {
      /*connection.createStatement().executeUpdate(
          "CREATE TABLE IF NOT EXISTS `" + switchTable + "` (" +
              "`world` varchar(36) NOT NULL," +
              "`x` int(10) NOT NULL," +
              "`y` int(10) NOT NULL," +
              "`z` int(10) NOT NULL," +
              "`dest` varchar(40) NOT NULL," +
              "PRIMARY KEY (`world`, `x`, `y`, `z`))");*/
      connection.createStatement().executeUpdate(
          "CREATE TABLE IF NOT EXISTS `" + destTable + "` (" +
              "`uuid` VARCHAR(36) NOT NULL," +
              "`dest` VARCHAR(40) NOT NULL," +
              "PRIMARY KEY (`uuid`))");
    } catch (SQLException e) {
      log.log(Level.SEVERE, "Could not create tables", e);
    }
  }

  /*private void loadSwitches() {
    ResultSet resultSet = getResultSet("SELECT * FROM `" + switchTable + "`");
    try {
      while (resultSet.next()) {
        String world = resultSet.getString("world");
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        int z = resultSet.getInt("z");
        String dest = resultSet.getString("dest");
        switches.put(new Location(Bukkit.getWorld(world), x, y, z), dest);
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Could not get result set from SQL statement!", e);
    }
  }*/

  private void loadStatements() {
    /*checkDestination = String.format(
        "SELECT `%1$s`.`uuid` FROM `%2$s` JOIN `%1$s` ON `%2$s`.`dest`=`%1$s`.`dest` WHERE `x`=? AND `y`=? AND `z`=? AND `world`=? AND `uuid`=? LIMIT 1",
        destTable, switchTable);*/
    /*getDestination = String.format(
        "SELECT `dest` FROM `%1$s` WHERE `x`=? AND `y`=? AND `z`=? AND `world`=? LIMIT 1",
        switchTable);
    setSwitchDestination = String.format(
        "REPLACE INTO `%1$s` VALUES (?, ?, ?, ?, ?)",
        switchTable);*/
    setPlayerDestination = String.format(
        "REPLACE INTO `%1$s` VALUES (?, ?)",
        destTable);
    /*removeSwitchDestination = String.format(
        "DELETE FROM `%s` WHERE `world`=? AND `x`=? AND `y`=? AND `z`=?",
        switchTable);*/
    getPlayerDestination = String.format(
        "SELECT `dest` FROM `%1$s` WHERE `uuid`=?",
        destTable);
  }

  /*public boolean isActivateRail(Player player, Location location) {
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(checkDestination);
      statement.setInt(1, location.getBlockX());
      statement.setInt(2, location.getBlockY());
      statement.setInt(3, location.getBlockZ());
      statement.setString(4, location.getWorld().getUID().toString());
      statement.setString(5, player.getUniqueId().toString());

      ResultSet resultSet = statement.executeQuery();
      return resultSet.next();
    } catch (SQLException e) {
      log.log(Level.SEVERE, "Could not check activate rail", e);
      return false;
    }
  }

  public String getDestination(Location location) {
    try (Connection connection = pool.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(getDestination);
      statement.setInt(1, location.getBlockX());
      statement.setInt(2, location.getBlockY());
      statement.setInt(3, location.getBlockZ());
      statement.setString(4, location.getWorld().getUID().toString());

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

  public void setDestination(Location location, String destination) {
    try (Connection connection = pool.getConnection()) {
      boolean none = destination.equalsIgnoreCase("none");
      PreparedStatement statement = connection.prepareStatement(none ? removeSwitchDestination : setSwitchDestination);
      statement.setString(1, location.getWorld().getUID().toString());
      statement.setInt(2, location.getBlockX());
      statement.setInt(3, location.getBlockY());
      statement.setInt(4, location.getBlockZ());
      if (!none) {
        statement.setString(5, destination);
      }

      statement.executeUpdate();
    } catch (SQLException e) {
      log.log(Level.SEVERE, "Could not set destination", e);
    }
  }*/

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
