package sh.okx.railswitch;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

/**
 * Event listener to enact the functionality of rail switches.
 */
public class DetectorRailActivateListener implements Listener {
    
    private static final String WILDCARD = "*";
    
    private final RailSwitchPlugin plugin;
    
    /**
     * Creates a new event listener, passing through the current RailSwitch plugin instance.
     *
     * @param plugin The current RailSwitch plugin instance.
     */
    public DetectorRailActivateListener(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Event handler for rail switches. Will determine if a switch exists at the target location, and if so will process
     * it accordingly, allowing it to trigger or not trigger depending on the rider's set destination, the listed
     * destinations on the switch, and the switch type.
     *
     * @param event The block redstone event to base the switch's existence on.
     */
    @EventHandler
    public void onRailSwitch(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        
        // Block must be a detector rail being triggered
        if (block == null
                || block.getType() != Material.DETECTOR_RAIL
                || event.getNewCurrent() != 15) {
            return;
        }
        
        // Check that the block above the rail is a sign
        Block above = block.getRelative(BlockFace.UP);
        if (above == null
                // Do not check for Material.SIGN as that is an item material, not a block material
                || (above.getType() != Material.SIGN_POST && above.getType() != Material.WALL_SIGN)
                || !(above.getState() instanceof Sign)) {
            return;
        }
        
        // Check that the sign has a valid switch type
        String[] lines = ((Sign) above.getState()).getLines();
        SwitchType type = SwitchType.find(lines[0]);
        if (type == null) {
            return;
        }
        
        // If Citadel is enabled, check that the sign and the rail are on the same group
        if (Bukkit.getPluginManager().isPluginEnabled("Citadel")) {
            ReinforcementManager rein = Citadel.getReinforcementManager();
            Reinforcement railReinforcement = rein.getReinforcement(block);
            Reinforcement signReinforcement = rein.getReinforcement(above);
            // Allow the switch to work if the rail and the sign are BOTH unreinforced
            // otherwise servers with established rail networks will suddenly not work
            // if Citadel is added.
            if (railReinforcement != null || signReinforcement != null) {
                if (!(railReinforcement instanceof PlayerReinforcement)) {
                    return;
                }
                if (!(signReinforcement instanceof PlayerReinforcement)) {
                    return;
                }
                if (!Objects.equals(
                        ((PlayerReinforcement) railReinforcement).getGroup(),
                        ((PlayerReinforcement) signReinforcement).getGroup())) {
                    return;
                }
            }
        }
        
        // Check that a player is triggering the switch
        // NOTE: The event doesn't provide the information and so the next best thing is searching for a
        //       player who is nearby and riding a minecart.
        Player player = null; {
            double searchDistance = Double.MAX_VALUE;
            for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 3, 3, 3)) {
                if (entity.getType() != EntityType.PLAYER
                        || !(entity instanceof Player)) {
                    continue;
                }
                Entity vehicle = entity.getVehicle();
                if (vehicle == null
                        || vehicle.getType() != EntityType.MINECART
                        || !(vehicle instanceof Minecart)) {
                    continue;
                }
                double distance = block.getLocation().distanceSquared(entity.getLocation());
                if (distance < searchDistance) {
                    searchDistance = distance;
                    player = (Player) entity;
                }
            }
        }
        if (player == null) {
            return;
        }
        
        // Determine whether a player has a destination that matches one of the destinations
        // listed on the switch signs, or match if there's a wildcard.
        boolean matched = false;
        String[] playerDestinations = plugin.getDatabase().
                getPlayerDestination(player).
                map(dest -> dest.split(" ")).
                orElse(null);
        if (playerDestinations != null && playerDestinations.length > 0) {
            String[] switchDestinations = Arrays.copyOfRange(lines, 1, lines.length);
            matcher:
            for (String playerDestination : playerDestinations) {
                if (playerDestination.equals(WILDCARD)) {
                    matched = true;
                    break;
                }
                for (String switchDestination : switchDestinations) {
                    if (switchDestination.equals(WILDCARD)
                            || playerDestination.equalsIgnoreCase(switchDestination)) {
                        matched = true;
                        break matcher;
                    }
                }
            }
        }
        
        switch (type) {
            case NORMAL:
                event.setNewCurrent(matched ? 15 : 0);
                break;
            case INVERTED:
                event.setNewCurrent(matched ? 0 : 15);
                break;
            // Gotta have this or the Devoted style checker will screech
            default:
                break;
        }
        
    }
    
}
