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
    
    public DetectorRailActivateListener(RailSwitch plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onRailSwitch(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (block == null
                || block.getType() != Material.DETECTOR_RAIL
                || event.getNewCurrent() != 15) {
            return;
        }
        
        Block above = block.getRelative(BlockFace.UP);
        if (!(above.getState() instanceof Sign)) {
            return;
        }
        
        String[] lines = ((Sign) above.getState()).getLines();
        if (!(lines[0].equalsIgnoreCase(NORMAL_SWITCH_TYPE)
                || lines[0].equalsIgnoreCase(INVERTED_SWITCH_TYPE))) {
            return;
        }
        
        if (Bukkit.getPluginManager().isPluginEnabled("Citadel")
                && !isSameReinforcementGroup(block.getLocation(), above.getLocation())) {
            return;
        }
        
        Player player = findNearestPlayerInMinecart(block.getLocation());
        if (player == null) {
            return;
        }
        
        Optional<String> destination = plugin.getDatabase().getPlayerDestination(player);
        
        boolean matched = destination.map(d -> isSignMatchDestination(lines, d)).orElse(false);
        
        if (lines[0].equalsIgnoreCase(NORMAL_SWITCH_TYPE) && !matched) {
            event.setNewCurrent(0);
        } else if (lines[0].equalsIgnoreCase(INVERTED_SWITCH_TYPE) && matched) {
            event.setNewCurrent(0);
        }
    }
    
    private boolean isSignMatchDestination(String[] lines, String destination) {
        for (String line : lines) {
            if ("*".equals(line) || destination.equalsIgnoreCase(line)) {
                return true;
            }
            
            // Use spaces as an "or" operator, allowing for more advanced routing.
            // We preserve the check above for backwards compatibility.
            String[] destinations = destination.split(" ");
            for (String dest : destinations) {
                if (dest.equalsIgnoreCase(line)) {
                    return true;
                }
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
