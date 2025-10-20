package sh.okx.railswitch.switches;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
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
public final class SwitchListener implements Listener {

    public static final String WILDCARD = "*";

    private final RailSwitchPlugin plugin;
    private final CitadelGlue citadelGlue;

    public SwitchListener(RailSwitchPlugin plugin, CitadelGlue citadelGlue) {
        this.plugin = plugin;
        this.citadelGlue = citadelGlue;
    }

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
        SwitchType type = null;
        List<String> positiveDestinations = new ArrayList<>();
        List<String> negativeDestinations = new ArrayList<>();
        if (sign != null) {
            // Use the front side (you can also check BOTH if needed)
            List<Component> lines = sign.getSide(Side.FRONT).lines();

            // Convert Adventure Components to plain text
            String[] signLines = lines.stream()
                .map(line -> PlainTextComponentSerializer.plainText().serialize(line))
                .toArray(String[]::new);

            type = SwitchType.find(signLines[0]);

            switch (type) {
                case NORMAL -> {
                    Arrays.stream(signLines)
                        .skip(1)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(positiveDestinations::add);                }
                case INVERTED -> {
                    Arrays.stream(signLines)
                        .skip(1)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(negativeDestinations::add);
                }
            }
        }

        if (type == null) {
            RailSwitchStorage storage = plugin.getRailSwitchStorage();
            if (storage != null) {
                storage.get(block).ifPresent(record ->
                    DestinationLists.splitDestinations(record.getLines(), positiveDestinations, negativeDestinations)
                );
            }
        }

        if (positiveDestinations.isEmpty() && negativeDestinations.isEmpty()) {return;}

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
                // Check if player is riding a minecart (potential candidate for CivModCore utility)
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
        if (sign != null && citadelGlue != null && citadelGlue.isSafeToUse()) {
            if (!citadelGlue.doSignAndRailHaveSameReinforcement(above, block)) {
                return;
            }
        }
        // --- resolve player on cart (leave your existing player-finding code as-is above) ---
// (we're already past the Citadel check)

// From here, branch based on whether config came from a sign (type != null)
// or storage (type == null).

        if (type == null) {
            // STORAGE-BACKED: choose output by which list matches
            boolean matchedPositive = false;
            boolean matchedNegative = false;

            String setDest = SettingsManager.getDestination(player);
            if (!Strings.isNullOrEmpty(setDest)) {
                String[] playerDestinations = setDest.split(" ");
                outer:
                for (String playerDestination : playerDestinations) {
                    if (Strings.isNullOrEmpty(playerDestination)) {
                        continue;
                    }
                    // Negative match takes precedence
                    if (DestinationLists.containsIgnoreCase(negativeDestinations, playerDestination)) {
                        matchedNegative = true;
                    }
                    // Player wildcard => treat as positive match
                    if (playerDestination.equals(WILDCARD)) {
                        matchedPositive = true;
                    }
                    // Positive match (including switch-side wildcard)
                    for (String switchDestination : positiveDestinations) {
                        if (Strings.isNullOrEmpty(switchDestination)) {
                            continue;
                        }
                        if (switchDestination.equals(WILDCARD)
                            || playerDestination.equalsIgnoreCase(switchDestination)) {
                            matchedPositive = true;
                            break outer;
                        }
                    }
                }
            }


            // Apply power based on match source
            if (matchedNegative) {
                event.setNewCurrent(0);   // INVERTED behavior: OFF
                return;
            }
            if (matchedPositive) {
                event.setNewCurrent(15);  // NORMAL behavior: ON
                return;
            }
            // No match for storage-backed => OFF
            event.setNewCurrent(0);
            return;
        }

// SIGN-BACKED: preserve your legacy behavior exactly
        boolean matched = false;
        {
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
                    if (DestinationLists.containsIgnoreCase(negativeDestinations, playerDestination)) {
                        continue; // negative list excludes this destination
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
        }

        switch (type) {
            case NORMAL -> event.setNewCurrent(matched ? 15 : 0);
            case INVERTED -> event.setNewCurrent(matched ? 0 : 15);
        }
    }

}
