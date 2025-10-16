package sh.okx.railswitch.switches;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Tag;
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
import sh.okx.railswitch.RailSwitchPlugin;
import sh.okx.railswitch.glue.CitadelGlue;
import sh.okx.railswitch.storage.RailSwitchRecord;
import sh.okx.railswitch.storage.RailSwitchStorage;
import sh.okx.railswitch.settings.SettingsManager;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Switch listener that implements switch functionality.
 */
public class SwitchListener implements Listener {

    public static final String WILDCARD = "*";

    public static final CitadelGlue CITADEL_GLUE = new CitadelGlue(RailSwitchPlugin.getPlugin(RailSwitchPlugin.class));

    /**
     * Event handler for rail switches. Will determine if a switch exists at the target location, and if so will process
     * it accordingly, allowing it to trigger or not trigger depending on the rider's set destination, the listed
     * destinations on the switch, and the switch type.
     *
     * @param event The block redstone event to base the switch's existence on.
     */
    @EventHandler
    public void onSwitchTrigger(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        // Block must be a detector rail being triggered
        if (!WorldUtils.isValidBlock(block)
            || block.getType() != Material.DETECTOR_RAIL
            || event.getNewCurrent() != 15) {
            return;
        }
        // Attempt to resolve configuration from persistent storage or from a sign
        Block above = block.getRelative(BlockFace.UP);
        Sign sign = null;
        if (Tag.SIGNS.isTagged(above.getType())
            && above.getState() instanceof Sign) {
            sign = (Sign) above.getState();
        }
        RailSwitchStorage storage = RailSwitchPlugin.getPlugin(RailSwitchPlugin.class).getRailSwitchStorage();
        SwitchType type = null;
        List<String> positiveDestinations = new ArrayList<>();
        List<String> negativeDestinations = new ArrayList<>();
        if (storage != null) {
            RailSwitchRecord record = storage.get(block).orElse(null);
            if (record != null) {
                type = SwitchType.find(record.getHeader());
                parseDestinations(record.getLines(), positiveDestinations, negativeDestinations);
            }
        }
        if (type == null && sign != null) {
            String[] signLines = sign.getLines();
            type = SwitchType.find(signLines[0]);
            if (type != null) {
                for (int i = 1; i < signLines.length; i++) {
                    addDestination(signLines[i], positiveDestinations, negativeDestinations);
                }
            }
        }
        if (type == null) {
            return;
        }
        // Check that a player is triggering the switch
        // NOTE: The event doesn't provide the information and so the next best thing is searching for a
        //       player who is nearby and riding a minecart.
        Player player = null;
        {
            double searchDistance = Double.MAX_VALUE;
            for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 3, 3, 3)) {
                if (!(entity instanceof Player)) {
                    continue;
                }
                Entity vehicle = entity.getVehicle();
                // TODO: This should be abstracted into CivModCore
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
        // If Citadel is enabled, check that the sign and the rail are on the same group
        if (sign != null && CITADEL_GLUE.isSafeToUse()) {
            if (!CITADEL_GLUE.doSignAndRailHaveSameReinforcement(above, block)) {
                return;
            }
        }
        // Determine whether a player has a destination that matches one of the destinations
        // listed on the switch signs, or match if there's a wildcard.
        boolean matched = false;
        String setDest = SettingsManager.getDestination(player);
        if (!Strings.isNullOrEmpty(setDest)) {
            String[] playerDestinations = setDest.split(" ");
            matcher:
            for (String playerDestination : playerDestinations) {
                if (Strings.isNullOrEmpty(playerDestination)) {
                    continue;
                }
                if (playerDestination.equals(WILDCARD)) {
                    matched = true;
                    break;
                }
                if (containsIgnoreCase(negativeDestinations, playerDestination)) {
                    continue;
                }
                for (String switchDestination : positiveDestinations) {
                    if (Strings.isNullOrEmpty(switchDestination)) {
                        continue;
                    }
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
        }
    }

    private void parseDestinations(Iterable<String> values, List<String> positive, List<String> negative) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addDestination(value, positive, negative);
        }
    }

    private void addDestination(String value, List<String> positive, List<String> negative) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (trimmed.startsWith("!")) {
            String neg = trimmed.substring(1).trim();
            if (!Strings.isNullOrEmpty(neg)) {
                negative.add(neg);
            }
        } else {
            positive.add(trimmed);
        }
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        for (String value : values) {
            if (value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

}
