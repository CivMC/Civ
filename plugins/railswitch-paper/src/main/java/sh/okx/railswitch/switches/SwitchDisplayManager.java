package sh.okx.railswitch.switches;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.okx.railswitch.RailSwitchPlugin;
import sh.okx.railswitch.settings.SettingsManager;
import sh.okx.railswitch.config.SwitchPluginConfiguration;
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
        if (!isHoldingConfigurationTool(player) || !SettingsManager.isVisualsEnabled(player)) {
            clearPlayer(player);
            return;
        }
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage == null || storage.isEmpty()) {
            clearPlayer(player);
            return;
        }

        SwitchPluginConfiguration configuration = plugin.getSwitchConfiguration();
        if (configuration == null) {
            clearPlayer(player);
            return;
        }

        double range = configuration.getDisplayRange();
        double rangeSquared = range * range;
        Map<RailSwitchKey, List<DisplayTarget>> targets = new HashMap<>();
        Location playerLocation = player.getLocation();

        for (RailSwitchRecord record : storage.values()) {
            Location recordLocation = record.toLocation();
            if (recordLocation == null || recordLocation.getWorld() == null) continue;
            if (!recordLocation.getWorld().equals(playerLocation.getWorld())) continue;

            Location center = recordLocation.clone().add(0.5D, 0.0D, 0.5D);
            if (center.distanceSquared(playerLocation) > rangeSquared) continue;

            List<DisplayTarget> recordTargets = computeTargets(record, player);
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
        if (player == null) {
            return;
        }
        PlayerDisplays displays = activeDisplays.remove(player.getUniqueId());
        if (displays != null) {
            // Force-null so destroy() won't call hideEntity on an offline player
            displays.clear(null);
        }
    }

    private boolean isHoldingConfigurationTool(Player player) {
        if (plugin.getSwitchConfiguration() == null) {
            return false;
        }
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        SwitchPluginConfiguration configuration = plugin.getSwitchConfiguration();
        if (configuration == null) {
            return false;
        }
        Material tool = configuration.getToolMaterial();
        if (tool == null) return false;
        return main.getType() == tool || off.getType() == tool;
    }

    private List<DisplayTarget> computeTargets(RailSwitchRecord record, Player viewer) {
        Location location = record.toLocation();
        if (location == null || location.getWorld() == null) return List.of();

        Block detector = location.getBlock();

        // Split configured destinations into + / - for preview (no live power logic).
        List<String> positiveNames = new ArrayList<>();
        List<String> negativeNames = new ArrayList<>();
        DestinationLists.splitDestinations(record.getLines(), positiveNames, negativeNames);

        List<Component> positiveText = positiveNames.isEmpty() ? null : buildText(positiveNames, NamedTextColor.GREEN);
        List<Component> negativeText = negativeNames.isEmpty() ? null : buildText(negativeNames, NamedTextColor.RED);

        List<DisplayTarget> results = new ArrayList<>();
        if (SettingsManager.isPredictionEnabled(viewer)) {
            results = createDisplayTargetsForCurves(detector, positiveText, negativeText);
        }

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

                    ExpectedShapes expected = getExpectedRailShapes(prev);

                    if (expected == null) continue;
                    contexts.add(new CurveContext(
                        prev,
                        leg,
                        expected.unpowered,
                        expected.powered
                    ));
                }
            }
        }

        return contexts;
    }

    private Location computeDisplayLocation(Block target, BlockFace exit) {
        Location location = target.getLocation().add(0.5D, DISPLAY_HEIGHT, 0.5D);
        location.add(exit.getModX() * 0.25D, exit.getModY() * 0.25D, exit.getModZ() * 0.25D);
        return location;
    }

    enum Dir { NORTH, SOUTH, WEST, EAST;
        Dir opposite() { return switch (this) {
            case NORTH -> SOUTH; case SOUTH -> NORTH; case WEST -> EAST; case EAST -> WEST; }; }
    }

    private static Set<Dir> connectionsOf(Rail.Shape s) {
        return switch (s) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> EnumSet.of(Dir.NORTH, Dir.SOUTH);
            case EAST_WEST,   ASCENDING_EAST,  ASCENDING_WEST  -> EnumSet.of(Dir.EAST,  Dir.WEST);
            case NORTH_EAST -> EnumSet.of(Dir.NORTH, Dir.EAST);
            case NORTH_WEST -> EnumSet.of(Dir.NORTH, Dir.WEST);
            case SOUTH_EAST -> EnumSet.of(Dir.SOUTH, Dir.EAST);
            case SOUTH_WEST -> EnumSet.of(Dir.SOUTH, Dir.WEST);
        };
    }
    private static Rail.Shape ascendingToward(Dir toward) {
        return switch (toward) {
            case NORTH -> Rail.Shape.ASCENDING_NORTH;
            case SOUTH -> Rail.Shape.ASCENDING_SOUTH;
            case EAST  -> Rail.Shape.ASCENDING_EAST;
            case WEST  -> Rail.Shape.ASCENDING_WEST;
        };
    }

    record RailConnection(boolean anyRail, boolean openEnd) {}

    private RailConnection checkConnection(World world, Location base, Dir dir) {
        int dx = (dir == Dir.EAST ? 1 : dir == Dir.WEST ? -1 : 0);
        int dz = (dir == Dir.SOUTH ? 1 : dir == Dir.NORTH ? -1 : 0);
        int x = base.getBlockX(), y = base.getBlockY(), z = base.getBlockZ();

        boolean anyRail = false;
        boolean openEnd = false;

        for (int ny : new int[]{y, y + 1, y - 1}) {
            Block b = world.getBlockAt(x + dx, ny, z + dz);
            BlockData bd = b.getBlockData();
            if (!(bd instanceof Rail r)) continue;

            anyRail = true; // found at least one rail

            Rail.Shape shape = r.getShape();
            if (!connectionsOf(shape).contains(dir.opposite())) continue;

            int dy = ny - y;
            if (dy == 0) {
                openEnd = true;
            } else if (dy == 1) {
                openEnd = shape == ascendingToward(dir.opposite());
            } else if (dy == -1) {
                openEnd = shape == ascendingToward(dir);
            }

            if (openEnd) break; // good enough
        }

        return new RailConnection(anyRail, openEnd);
    }

    public record CurveContext(Block curve, BlockFace incoming, Rail.Shape unpoweredShape, Rail.Shape poweredShape){}
    public record ExpectedShapes(Rail.Shape powered, Rail.Shape unpowered) {}

    public ExpectedShapes getExpectedRailShapes(Block railBlock) {
        if (!(railBlock.getBlockData() instanceof Rail r)) return null;

        final World world = railBlock.getWorld();
        final Location loc = railBlock.getLocation();
        final Rail.Shape current = r.getShape();
        final boolean forbidCurves = railBlock.getType() != Material.RAIL; // vanilla: only plain rails curve

        // --- snapshot neighbors (Mojang's bl..bl10) ---
        RailConnection north = checkConnection(world, loc, Dir.NORTH);
        RailConnection south = checkConnection(world, loc, Dir.SOUTH);
        RailConnection west  = checkConnection(world, loc, Dir.WEST);
        RailConnection east  = checkConnection(world, loc, Dir.EAST);

        if (north.anyRail() && south.anyRail() && east.anyRail() && west.anyRail()) {
            return new ExpectedShapes(current, current);
        }

        // Connection flags for shape logic:
        boolean bl  = north.openEnd();
        boolean bl2 = south.openEnd();
        boolean bl3 = west.openEnd();
        boolean bl4 = east.openEnd();

        final boolean bl5 = bl  || bl2; // any N/S
        final boolean bl6 = bl3 || bl4; // any W/E

        final boolean bl7  = bl2 && bl4; // S & E
        final boolean bl8  = bl2 && bl3; // S & W
        final boolean bl9  = bl  && bl4; // N & E
        final boolean bl10 = bl  && bl3; // N & W

        // rails ABOVE forward neighbors (for ascending)
        final boolean nUp = isRailAbove(world, loc, 0, -1);
        final boolean sUp = isRailAbove(world, loc, 0,  1);
        final boolean eUp = isRailAbove(world, loc,  1, 0);
        final boolean wUp = isRailAbove(world, loc, -1, 0);

        // --- immediate resolution (straight / explicit curve) ---
        Rail.Shape shape = null;
        if (bl5 && !bl6) shape = Rail.Shape.NORTH_SOUTH;
        if (bl6 && !bl5) shape = Rail.Shape.EAST_WEST;

        if (!forbidCurves) { // explicit corner cases
            if (bl7  && !bl && !bl3) shape = Rail.Shape.SOUTH_EAST;
            if (bl8  && !bl && !bl4) shape = Rail.Shape.SOUTH_WEST;
            if (bl10 && !bl2 && !bl4) shape = Rail.Shape.NORTH_WEST;
            if (bl9  && !bl2 && !bl3) shape = Rail.Shape.NORTH_EAST;
        }

        // If shape chosen, apply ascending (only for straights) and return same for both states
        if (shape != null) {
            shape = withAscending(shape, nUp, sUp, eUp, wUp);
            return new ExpectedShapes(shape, shape);
        }

        // --- ambiguous: choose per-state base with vanilla priorities ---
        Rail.Shape baseOn;
        Rail.Shape baseOff;

        if (bl5 && bl6) {                 // crossroads → keep current (no redstone effect)
            baseOn = baseOff = current;
        } else if (bl5) {
            baseOn = baseOff = Rail.Shape.NORTH_SOUTH;
        } else if (bl6) {
            baseOn = baseOff = Rail.Shape.EAST_WEST;
        } else {                          // no neighbors → keep current
            baseOn = baseOff = current;
        }

        if (!forbidCurves) {
            // powered priority
            if (bl7)  baseOn = Rail.Shape.SOUTH_EAST;
            if (bl8)  baseOn = Rail.Shape.SOUTH_WEST;
            if (bl9)  baseOn = Rail.Shape.NORTH_EAST;
            if (bl10) baseOn = Rail.Shape.NORTH_WEST;

            // unpowered priority
            if (bl10) baseOff = Rail.Shape.NORTH_WEST;
            if (bl9)  baseOff = Rail.Shape.NORTH_EAST;
            if (bl8)  baseOff = Rail.Shape.SOUTH_WEST;
            if (bl7)  baseOff = Rail.Shape.SOUTH_EAST;
        }

        // --- ascending after base chosen (only affects straight bases) ---
        Rail.Shape powered   = withAscending(baseOn,  nUp, sUp, eUp, wUp);
        Rail.Shape unpowered = withAscending(baseOff, nUp, sUp, eUp, wUp);

        return new ExpectedShapes(powered, unpowered);
    }

    private Rail.Shape withAscending(Rail.Shape base, boolean nUp, boolean sUp, boolean eUp, boolean wUp) {
        if (base == Rail.Shape.NORTH_SOUTH) {
            if (nUp) return Rail.Shape.ASCENDING_NORTH;
            if (sUp) return Rail.Shape.ASCENDING_SOUTH;
        } else if (base == Rail.Shape.EAST_WEST) {
            if (eUp) return Rail.Shape.ASCENDING_EAST;
            if (wUp) return Rail.Shape.ASCENDING_WEST;
        }
        return base;
    }


    private boolean isRailAbove(World world, Location base, int dx, int dz) {
        Block b = world.getBlockAt(base.getBlockX() + dx, base.getBlockY() + 1, base.getBlockZ() + dz);
        return b.getBlockData() instanceof Rail;
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
            System.out.println("Curve: " + context.curve() + " incoming: " + context.incoming());
            System.out.println("Unpowered: " + context.unpoweredShape() + " Powered: " + context.poweredShape());

            BlockFace exit1 = getExitDirection(context.unpoweredShape(), context.incoming());
            BlockFace exit2 = getExitDirection(context.poweredShape(), context.incoming());

            if (exit1 != null && exit2 != null) {
                Block block1 = context.curve().getRelative(exit1);
                Block block2 = context.curve().getRelative(exit2);
                if (exit1 == exit2) {
                    Location marker = computeDisplayLocation(block1, exit1);
                    results.add(new DisplayTarget(marker, combineComponents(positiveText, negativeText)));
                } else {
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
