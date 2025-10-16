package sh.okx.railswitch.switches;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.okx.railswitch.RailSwitchPlugin;
import sh.okx.railswitch.storage.RailSwitchKey;
import sh.okx.railswitch.storage.RailSwitchRecord;
import sh.okx.railswitch.storage.RailSwitchStorage;

/**
 * Tracks players holding the configuration tool and renders per-player text displays showing switch destinations.
 */
public final class SwitchDisplayManager implements Listener, Runnable {

    private static final double DISPLAY_HEIGHT = 0.5D;

    // 6-way adjacency for rail walking and directional inference.
    private static final BlockFace[] HORIZ = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final RailSwitchPlugin plugin;
    private final Map<UUID, PlayerDisplays> activeDisplays = new HashMap<>();
    private BukkitTask ticker;

    public SwitchDisplayManager(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the display manager, scheduling the update task.
     */
    public void start() {
        if (ticker != null) return;
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this, 20L, 20L);
    }

    /**
     * Shuts down the display manager, canceling the update task and clearing displays.
     */
    public void shutdown() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        for (Map.Entry<UUID, PlayerDisplays> entry : activeDisplays.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                entry.getValue().clear(player);
            } else {
                entry.getValue().clear(null);
            }
        }
        activeDisplays.clear();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerDisplays displays = activeDisplays.remove(event.getPlayer().getUniqueId());
        if (displays != null) {
            displays.clear(event.getPlayer());
        }
    }

    private void updatePlayer(Player player) {
        if (!player.isOnline()) {
            clearPlayer(player);
            return;
        }
        if (!isHoldingConfigurationTool(player)) {
            clearPlayer(player);
            return;
        }
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage == null || storage.isEmpty()) {
            clearPlayer(player);
            return;
        }

        double range = plugin.getSwitchConfiguration().getDisplayRange();
        double rangeSquared = range * range;
        Map<RailSwitchKey, List<DisplayTarget>> targets = new HashMap<>();
        Location playerLocation = player.getLocation();

        for (RailSwitchRecord record : storage.values()) {
            Location recordLocation = record.toLocation();
            if (recordLocation == null || recordLocation.getWorld() == null) continue;
            if (!recordLocation.getWorld().equals(playerLocation.getWorld())) continue;

            Location center = recordLocation.clone().add(0.5D, 0.0D, 0.5D);
            if (center.distanceSquared(playerLocation) > rangeSquared) continue;

            List<DisplayTarget> recordTargets = computeTargets(record);
            if (recordTargets.isEmpty()) continue;

            targets.put(record.toKey(), recordTargets);
        }

        if (targets.isEmpty()) {
            clearPlayer(player);
            return;
        }
        PlayerDisplays displays = activeDisplays.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerDisplays(plugin));
        displays.sync(targets, player);
    }

    private void clearPlayer(Player player) {
        PlayerDisplays displays = activeDisplays.remove(player.getUniqueId());
        if (displays != null) {
            // Force-null so destroy() won't call hideEntity on an offline player
            displays.clear(null);
        }
    }

    private boolean isHoldingConfigurationTool(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        Material tool = plugin.getSwitchConfiguration().getToolMaterial();
        if (tool == null) return false;
        return main.getType() == tool || off.getType() == tool;
    }

    private List<DisplayTarget> computeTargets(RailSwitchRecord record) {
        Location location = record.toLocation();
        if (location == null || location.getWorld() == null) return List.of();

        Block detector = location.getBlock();

        // Split configured destinations into + / - for preview (no live power logic).
        List<String> positiveNames = new ArrayList<>();
        List<String> negativeNames = new ArrayList<>();
        splitDestinations(record.getLines(), positiveNames, negativeNames);

        List<Component> positiveText = positiveNames.isEmpty() ? null : buildText(positiveNames, NamedTextColor.GREEN);
        List<Component> negativeText = negativeNames.isEmpty() ? null : buildText(negativeNames, NamedTextColor.RED);

        List<DisplayTarget> results = createDisplayTargetsForCurves(detector, positiveText, negativeText);

        if (results.isEmpty()) {
            Component fallback = combineComponents(positiveText, negativeText);
            DisplayTarget fallbackTarget = createFallbackDisplayTarget(detector, fallback);
            if (fallbackTarget != null) {
                results.add(fallbackTarget);
            }
        }
        return results;
    }

    /**
     * Determines the exit direction for a curved rail based on its shape and the incoming direction.
     * This simulates how Minecraft's rail mechanics route minecarts through curves.
     *
     * @param shape The shape of the curved rail
     * @param incoming The direction the minecart is coming from
     * @return The exit direction, or null if not applicable (straight rails or invalid cases)
     */
    public BlockFace getExitDirection(Rail.Shape shape, BlockFace incoming) {
        return switch (shape) {
            // Straight rails don't have exit directions for curves
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH, EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> null;
            case NORTH_EAST -> switch (incoming) {
                case NORTH, WEST -> BlockFace.EAST;
                case EAST, SOUTH -> BlockFace.NORTH;
                default -> null;
            };
            case NORTH_WEST -> switch (incoming) {
                case NORTH, EAST -> BlockFace.WEST;
                case WEST, SOUTH -> BlockFace.NORTH;
                default -> null;
            };
            case SOUTH_EAST -> switch (incoming) {
                case EAST, NORTH -> BlockFace.SOUTH;
                case WEST, SOUTH -> BlockFace.EAST;
                default -> null;
            };
            case SOUTH_WEST -> switch (incoming) {
                case SOUTH, EAST -> BlockFace.WEST;
                case WEST, NORTH -> BlockFace.SOUTH;
                default -> null;
            };
        };
    }


    private List<Component> buildText(List<String> lines, NamedTextColor color) {
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            if (Strings.isNullOrEmpty(line)) continue;
            components.add(Component.text(line, color));
        }
        if (components.isEmpty()) {
            components.add(Component.text("No stations configured", color));
        }
        return components;
    }

    private void splitDestinations(List<String> lines, List<String> positives, List<String> negatives) {
        if (lines == null) return;
        for (String raw : lines) {
            if (Strings.isNullOrEmpty(raw)) continue;
            String value = raw.trim();
            if (value.isEmpty()) continue;
            if (value.startsWith("!")) {
                String neg = value.substring(1).trim();
                if (!neg.isEmpty()) negatives.add(neg);
            } else {
                positives.add(value);
            }
        }
    }

    /**
     * Locates curved rails connected to the detector rail that could form a switch junction.
     * Examines adjacent rails to find curves that might route minecarts differently based on power state.
     *
     * @param detector The detector rail block
     * @return A list of curve contexts representing potential switch curves
     */
    @NotNull
    private List<CurveContext> locateCurve(Block detector) {
        if (!(detector.getBlockData() instanceof Rail detectorRail)) return List.of();

        List<CurveContext> contexts = new ArrayList<>();

        for (BlockFace leg : connectedFaces(detectorRail.getShape())) {
            Block prev = neighborRail(detector, leg.getOppositeFace());
            if (prev != null) {
                if (!(prev.getBlockData() instanceof Rail targetRail)) return List.of();

                if (isCurvedShape(targetRail.getShape())) {
                    contexts.add(new CurveContext(
                        prev,
                        leg,
                        getExpectedRailShape(prev, false),
                        getExpectedRailShape(prev, true)
                    ));
                }
            }
        }

        return contexts;
    }


    private Block firstRailAfter(Block curve, BlockFace exit) {
        return neighborRail(curve, exit);
    }

    private Location computeDisplayLocation(Block target, BlockFace exit) {
        Location location = target.getLocation().add(0.5D, DISPLAY_HEIGHT, 0.5D);
        location.add(exit.getModX() * 0.25D, exit.getModY() * 0.25D, exit.getModZ() * 0.25D);
        return location;
    }


    /**
     * Determines the expected rail shape for a given block based on surrounding rails and power state.
     * This replicates Minecraft's rail placement logic to predict how rails behave in junctions.
     *
     * @param railBlock The rail block to analyze
     * @param isPowered Whether the rail is powered (affects curve priority)
     * @return The expected rail shape, or NORTH_SOUTH as fallback
     */
    public Rail.Shape getExpectedRailShape(Block railBlock, boolean isPowered) {
        if (!(railBlock.getBlockData() instanceof Rail)) return null;

        Location loc = railBlock.getLocation();
        World world = railBlock.getWorld();

        // Check adjacent blocks for connected rails
        boolean north = isRail(world.getBlockAt(loc.clone().add(0, 0, -1)));
        boolean south = isRail(world.getBlockAt(loc.clone().add(0, 0, 1)));
        boolean west  = isRail(world.getBlockAt(loc.clone().add(-1, 0, 0)));
        boolean east  = isRail(world.getBlockAt(loc.clone().add(1, 0, 0)));

        boolean northSouth = north || south;
        boolean eastWest   = east  || west;

        Rail.Shape shape = null;

        // Step 1: Determine if it's a straight rail
        if (northSouth && !eastWest) shape = Rail.Shape.NORTH_SOUTH;
        else if (eastWest && !northSouth) shape = Rail.Shape.EAST_WEST;

        // Step 2: Check for perfect curve matches (only one direction pair connected)
        boolean se = south && east && !north && !west;
        boolean sw = south && west && !north && !east;
        boolean nw = north && west && !south && !east;
        boolean ne = north && east && !south && !west;

        if (se) shape = Rail.Shape.SOUTH_EAST;
        else if (sw) shape = Rail.Shape.SOUTH_WEST;
        else if (nw) shape = Rail.Shape.NORTH_WEST;
        else if (ne) shape = Rail.Shape.NORTH_EAST;

        // Step 3: If ambiguous, use redstone-powered priority order to choose a curve
        if (shape == null) {
            List<Rail.Shape> order = !isPowered
                ? List.of(Rail.Shape.SOUTH_EAST, Rail.Shape.SOUTH_WEST, Rail.Shape.NORTH_EAST, Rail.Shape.NORTH_WEST)
                : List.of(Rail.Shape.NORTH_WEST, Rail.Shape.NORTH_EAST, Rail.Shape.SOUTH_WEST, Rail.Shape.SOUTH_EAST);

            for (Rail.Shape candidate : order) {
                Set<BlockFace> req = getFacesForCurve(candidate);
                if (req.contains(BlockFace.NORTH) && !north) continue;
                if (req.contains(BlockFace.SOUTH) && !south) continue;
                if (req.contains(BlockFace.EAST)  && !east)  continue;
                if (req.contains(BlockFace.WEST)  && !west)  continue;
                shape = candidate;
                break;
            }
        }

        // Step 4: Check for ascending slopes on straight rails
        if (shape == Rail.Shape.NORTH_SOUTH) {
            if (isRail(world.getBlockAt(loc.clone().add(0, 1, -1)))) shape = Rail.Shape.ASCENDING_NORTH;
            else if (isRail(world.getBlockAt(loc.clone().add(0, 1, 1)))) shape = Rail.Shape.ASCENDING_SOUTH;
        } else if (shape == Rail.Shape.EAST_WEST) {
            if (isRail(world.getBlockAt(loc.clone().add(1, 1, 0)))) shape = Rail.Shape.ASCENDING_EAST;
            else if (isRail(world.getBlockAt(loc.clone().add(-1, 1, 0)))) shape = Rail.Shape.ASCENDING_WEST;
        }

        // Fallback to straight north-south if nothing determined
        return shape != null ? shape : Rail.Shape.NORTH_SOUTH;
    }


    private boolean isRail(Block block) {
        return block.getBlockData() instanceof Rail;
    }

    private Set<BlockFace> getFacesForCurve(Rail.Shape shape) {
        return switch (shape) {
            case NORTH_EAST -> Set.of(BlockFace.NORTH, BlockFace.EAST);
            case NORTH_WEST -> Set.of(BlockFace.NORTH, BlockFace.WEST);
            case SOUTH_EAST -> Set.of(BlockFace.SOUTH, BlockFace.EAST);
            case SOUTH_WEST -> Set.of(BlockFace.SOUTH, BlockFace.WEST);
            default -> Set.of();
        };
    }


    private Component combineComponents(List<Component> first, List<Component> second) {
        List<Component> components = new ArrayList<>();
        if (first != null) components.addAll(first);
        if (second != null) components.addAll(second);

        if (components.isEmpty()) {
            return null;
        }
        return Component.join(JoinConfiguration.separator(Component.newline()), components);
    }

    /**
     * Creates display targets for curved rail switches by determining exit directions and positions.
     *
     * @param detector The detector rail block
     * @param positiveText The text component for positive destinations
     * @param negativeText The text component for negative destinations
     * @return A list of display targets for the curves
     */
    private List<DisplayTarget> createDisplayTargetsForCurves(Block detector, List<Component> positiveText, List<Component> negativeText) {
        List<DisplayTarget> results = new ArrayList<>();
        for (CurveContext context : locateCurve(detector)) {
            BlockFace[] exits = {getExitDirection(context.off_shape, context.incoming), getExitDirection(context.on_shape, context.incoming)};

            System.out.println("Incoming: " + context.incoming);
            System.out.println("Curve: " + context.off_shape + " -> " + context.on_shape + " (" + Arrays.toString(exits) + ")");


            BlockFace exit1 = null, exit2 = null;
            for (BlockFace face : exits) {
                if (exit1 == null) exit1 = face;
                else exit2 = face;
            }
            if (exit1 != null && exit2 != null) {
                Block block1 = firstRailAfter(context.curve, exit1);
                Block block2 = firstRailAfter(context.curve, exit2);
                if (block1 != null && block2 != null) {
                    if (negativeText != null) {
                        Location marker = computeDisplayLocation(block1, exit1);
                        Component text = Component.join(JoinConfiguration.separator(Component.newline()), negativeText);
                        results.add(new DisplayTarget(marker, text));
                    }
                    if (positiveText != null) {
                        Location marker = computeDisplayLocation(block2, exit2);
                        Component text = Component.join(JoinConfiguration.separator(Component.newline()), positiveText);
                        results.add(new DisplayTarget(marker, text));
                    }
                }
            }
        }
        return results;
    }

    /**
     * Creates a fallback display target when no curve-based targets are available.
     *
     * @param detector The detector rail block
     * @param combinedText The combined text component for all destinations
     * @return A single display target, or null if no text
     */
    private DisplayTarget createFallbackDisplayTarget(Block detector, Component combinedText) {
        if (combinedText == null) {
            return null;
        }
        Location fallbackLocation = detector.getLocation().add(0.5D, DISPLAY_HEIGHT, 0.5D);
        return new DisplayTarget(fallbackLocation, combinedText);
    }

    private boolean isCurvedShape(Rail.Shape shape) {
        return switch (shape) {
            case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST -> true;
            default -> false;
        };
    }

    /**
     * Returns the possible connection faces for a given rail shape.
     * This defines which directions a rail can connect to adjacent rails.
     *
     * @param s The rail shape
     * @return Array of block faces that this rail shape can connect to
     */
    private static BlockFace[] connectedFaces(Rail.Shape s) {
        return switch (s) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH};
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> new BlockFace[]{BlockFace.EAST, BlockFace.WEST};
            case NORTH_EAST -> new BlockFace[]{BlockFace.NORTH, BlockFace.EAST};
            case NORTH_WEST -> new BlockFace[]{BlockFace.NORTH, BlockFace.WEST};
            case SOUTH_EAST -> new BlockFace[]{BlockFace.SOUTH, BlockFace.EAST};
            case SOUTH_WEST -> new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST};
            default -> new BlockFace[0];
        };
    }

    // Get neighboring rail with vertical tolerance (-1/0/+1)
    @Nullable
    private Block neighborRail(Block base, BlockFace horiz) {
        Block b = base.getRelative(horiz);
        if (Tag.RAILS.isTagged(b.getType())) return b;
        Block up = b.getRelative(BlockFace.UP);
        if (Tag.RAILS.isTagged(up.getType())) return up;
        Block dn = b.getRelative(BlockFace.DOWN);
        if (Tag.RAILS.isTagged(dn.getType())) return dn;
        return null;
    }

    @Nullable
    private static BlockFace directionTo(Block from, Block to) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dz = Integer.compare(to.getZ(), from.getZ());
        if (dx != 0 && dz != 0) {
            return null;
        }
        if (dx > 0) {
            return BlockFace.EAST;
        }
        if (dx < 0) {
            return BlockFace.WEST;
        }
        if (dz > 0) {
            return BlockFace.SOUTH;
        }
        if (dz < 0) {
            return BlockFace.NORTH;
        }
        int dy = Integer.compare(to.getY(), from.getY());
        if (dy > 0) {
            return BlockFace.UP;
        }
        if (dy < 0) {
            return BlockFace.DOWN;
        }
        return null;
    }








}
