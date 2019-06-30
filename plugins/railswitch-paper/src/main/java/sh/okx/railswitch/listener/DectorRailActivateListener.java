package sh.okx.railswitch.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import sh.okx.railswitch.RailSwitch;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class DectorRailActivateListener implements Listener {
  private final RailSwitch plugin;

  public DectorRailActivateListener(RailSwitch plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void on(BlockRedstoneEvent e) {
    Block block = e.getBlock();
    // check if block is a detector rail
    if (block == null || block.getType() != Material.DETECTOR_RAIL || e.getNewCurrent() != 15) {
      return;
    }

    Player nearestPlayer = findNearestPlayerInMinecart(block.getLocation());
    if (nearestPlayer == null) {
      // vanilla behaviour if no player
      return;
    }

    // check if the player has the same destination as
    // the destination on the detector rail
    long start = System.currentTimeMillis();
    boolean activate = isActivateRail(nearestPlayer, block.getLocation());
    if (plugin.isTimings()) {
      plugin.getLogger().info("Took " + (System.currentTimeMillis() - start) + "ms");
    }
    if (!activate) {
      e.setNewCurrent(0);
    }
  }

  private boolean isActivateRail(Player player, Location location) {
    Block above = location.getBlock().getRelative(BlockFace.UP);
    if (!(above.getState() instanceof Sign)) {
      // true = don't change vanilla behaviour
      return true;
    }
    Sign sign = (Sign) above.getState();
    String[] lines = sign.getLines();
    if (!"[destination]".equalsIgnoreCase(lines[0])) {
      return true;
    }

    // sign and rail must be on the same reinforcement group
    // to prevent abuse
    if (Bukkit.getPluginManager().isPluginEnabled("Citadel")
            && !isSameReinforcementGroup(above.getLocation(), location)) {
      return true;
    }

    String destination = plugin.getDatabase().getPlayerDestination(player);
    // don't activate rail if a destination is not set
    if (destination == null) {
      return false;
    }

    // only activate redstone if the sign has a destination the player is set to
    // if the sign has a * and a player has a destination set, activate it
    for (int i = 1; i < 4; i++) {
      if (destination.equalsIgnoreCase(lines[i]) || lines[i].equals("*")) {
        return true;
      }
    }
    return false;
  }

  private boolean isSameReinforcementGroup(Location a, Location b) {
    ReinforcementManager rein = Citadel.getReinforcementManager();
    Reinforcement reinA = rein.getReinforcement(a);
    Reinforcement reinB = rein.getReinforcement(b);
    if (!(reinA instanceof PlayerReinforcement) || !(reinB instanceof PlayerReinforcement)) {
      return false;
    }

    PlayerReinforcement playerReinA = (PlayerReinforcement) reinA;
    PlayerReinforcement playerReinB = (PlayerReinforcement) reinB;

    return playerReinA.getGroup().equals(playerReinB.getGroup());
  }

  /**
   * @param location the centre
   * @return the nearest player riding a minecart in a 3 block radius of the centre
   */
  private Player findNearestPlayerInMinecart(Location location) {
    World world = location.getWorld();
    Player nearestPlayer = null;
    double distance = Double.MAX_VALUE;
    for (Entity entity : world.getNearbyEntities(location, 3, 3, 3)) {
      if (entity instanceof Player && entity.getVehicle() instanceof Minecart) {
        double entityDistance = location.distanceSquared(entity.getLocation());
        if (entityDistance < distance) {
          distance = entityDistance;
          nearestPlayer = (Player) entity;
        }
      }
    }
    return nearestPlayer;
  }
}
