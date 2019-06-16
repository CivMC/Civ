package sh.okx.railswitch.listener;

import com.google.common.base.CharMatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import sh.okx.railswitch.RailSwitch;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class DetectorRailUseListener implements Listener {
  /*private Map<Player, Location> creatingSwitch = Collections.synchronizedMap(new WeakHashMap<>());
  private final RailSwitch plugin;

  public DetectorRailUseListener(RailSwitch plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void on(PlayerInteractEvent e) {
    if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) {
      return;
    }
    Block block = e.getClickedBlock();
    if (block.getType() != Material.DETECTOR_RAIL) {
      return;
    }

    Player player = e.getPlayer();
    if (Bukkit.getPluginManager().isPluginEnabled("Citadel")) {
      Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(block);
      if (!(reinforcement instanceof PlayerReinforcement)) {
        return;
      }
      PlayerReinforcement playerReinforcement = (PlayerReinforcement) reinforcement;

      // check CREATE_RAIL_SWITCH permission
      if (!NameAPI.getGroupManager().hasAccess(playerReinforcement.getGroup(), player.getUniqueId(),
          PermissionType.getPermission("CREATE_RAIL_SWITCH"))) {
        return;
      }
    }

    player.sendMessage(ChatColor.GOLD + "Type a destination name or 'none' for no destination");
    long start = System.currentTimeMillis();
    String destination = plugin.getDatabase().getDestination(block.getLocation());
    if (plugin.isTimings()) {
      plugin.getLogger().info("Get rail destination took" + (System.currentTimeMillis()-start) + "ms");
    }
    if (destination != null) {
      player.sendMessage(ChatColor.GOLD + "The current destination is: " + destination);
    }
    creatingSwitch.put(player, block.getLocation());
  }

  @EventHandler
  public void on(AsyncPlayerChatEvent e) {
    Player player = e.getPlayer();
    Location location = creatingSwitch.remove(player);
    if (location == null) {
      return;
    }
    e.setCancelled(true);

    String message = e.getMessage();
    if (!plugin.isValidDestination(message)) {
      player.sendMessage(ChatColor.RED + "Destination names cannot be more than 40 characters and may only use alphanumerical characters and ASCII symbols");
      player.sendMessage(ChatColor.RED + "Rail switch creation cancelled.");
      return;
    }

    long start = System.currentTimeMillis();
    plugin.getDatabase().setDestination(location, message);
    if (plugin.isTimings()) {
      plugin.getLogger().info("Set rail destination took " + (System.currentTimeMillis()-start) + "ms");
    }
    if (message.equalsIgnoreCase("none")) {
      player.sendMessage(ChatColor.GREEN + "Unset rail destination");
    } else {
      player.sendMessage(ChatColor.GREEN + "Set detector rail destination to: " + message);
    }
  }*/
}
