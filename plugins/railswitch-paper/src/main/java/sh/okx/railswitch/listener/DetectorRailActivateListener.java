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

import java.util.Optional;

public class DetectorRailActivateListener implements Listener {

  private static final String NORMAL_SWITCH_TYPE = "[destination]";
  private static final String INVERTED_SWITCH_TYPE = "[!destination]";

  private final RailSwitch plugin;
  private long timingsStartTime = System.currentTimeMillis();

  public DetectorRailActivateListener(RailSwitch plugin) {
    this.plugin = plugin;
  }

  private void concludeTiming() {
    if (plugin.isTimings()) {
      plugin.getLogger().info("Took " + (System.currentTimeMillis() - timingsStartTime) + "ms");
    }
  }

  @EventHandler
  public void onRailSwitch(BlockRedstoneEvent event) {
    // If Citadel is not enabled, do nothing
    if (!Bukkit.getPluginManager().isPluginEnabled("Citadel")) {
      return;
    }
    // If the block is null or empty, do nothing
    Block block = event.getBlock();
    if (block == null) {
      return;
    }
    // If the block is not a detector rail, do nothing
    if (block.getType() != Material.DETECTOR_RAIL) {
      return;
    }
    // If the detector rail is not being activated, do nothing
    if (event.getNewCurrent() != 15) {
      return;
    }
    // If the block above the detector rail is not a sign, do nothing
    Block above = block.getRelative(BlockFace.UP);
    if (!(above.getState() instanceof Sign)) {
      return;
    }
    // If the sign is not a rail switch sign, do nothing
    String[] lines = ((Sign) above.getState()).getLines();
    if (!(lines[0].equalsIgnoreCase(NORMAL_SWITCH_TYPE) || lines[0].equalsIgnoreCase(INVERTED_SWITCH_TYPE))) {
      return;
    }
    // Start timings here - there's some expensive calls coming up
    timingsStartTime = System.currentTimeMillis();
    // If detector rail and the sign are not on the same reinforcement group, do nothing
    if (!isSameReinforcementGroup(block.getLocation(), above.getLocation())) {
      concludeTiming();
      return;
    }
    // If the player riding the cart cannot be found, do nothing
    Player player = findNearestPlayerInMinecart(block.getLocation());
    if (player == null) {
      concludeTiming();
      return;
    }
    // Determine if the destination is present on the sign
    Optional<String> destination = plugin.getDatabase().getPlayerDestination(player);
    boolean matched = false;
    boolean wildcard = false;
    if (destination.isPresent()) {
      for (String line : lines) {
        if (line.equals("*")) {
          matched = true;
          wildcard = true;
          break;
        }
        if (line.equalsIgnoreCase(destination.get())) {
          matched = true;
          break;
        }
      }
    }
    // Determine the behaviour of the switch by its type
    if (lines[0].equalsIgnoreCase(NORMAL_SWITCH_TYPE)) {
      if (!matched) {
        event.setNewCurrent(0);
      }
    }
    else if (lines[0].equalsIgnoreCase(INVERTED_SWITCH_TYPE)) {
      if (matched) {
        event.setNewCurrent(0);
      }
    }
    // Calculate how much time this process took
    concludeTiming();
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
