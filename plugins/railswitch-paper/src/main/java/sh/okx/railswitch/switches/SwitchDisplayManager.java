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
    private final Map<UUID, PlayerDisplays> activeDisplays;
    private BukkitTask ticker;

    public SwitchDisplayManager(RailSwitchPlugin plugin) {
        this.plugin = plugin;
        this.activeDisplays = new HashMap<>();
    }

    public void start() {
        if (ticker != null) return;
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this, 20L, 20L);
    }

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
        PlayerDisplays displays = activeDisplays.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerDisplays());
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

        List<DisplayTarget> results = new ArrayList<>(2);

        for (CurveContext context : locateCurve(detector)) {
            BlockFace[] exits = {getExitDirection(context.off_shape, context.incoming), getExitDirection(context.on_shape, context.incoming)};

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


        if (results.isEmpty()) {
            Component fallback = combineComponents(positiveText, negativeText);
            if (fallback == null) return List.of();
            Location fallbackLocation = detector.getLocation().add(0.5D, DISPLAY_HEIGHT, 0.5D);
            results.add(new DisplayTarget(fallbackLocation, fallback));
        }
        return results;
    }

    public BlockFace getExitDirection(Rail.Shape shape, BlockFace incoming) {
        return switch (shape) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH, EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> null;
            case NORTH_EAST -> switch (incoming) {
                case NORTH -> BlockFace.EAST;
                case EAST -> BlockFace.NORTH;
                case WEST -> BlockFace.WEST;
                case SOUTH -> BlockFace.SOUTH;
                default -> null;
            };
            case NORTH_WEST -> switch (incoming) {
                case NORTH -> BlockFace.WEST;
                case WEST -> BlockFace.NORTH;
                case EAST -> BlockFace.EAST;
                case SOUTH -> BlockFace.SOUTH;
                default -> null;
            };
            case SOUTH_EAST -> switch (incoming) {
                case EAST -> BlockFace.SOUTH;
                case WEST -> BlockFace.WEST;
                case NORTH -> BlockFace.NORTH;
                case SOUTH -> BlockFace.EAST;

                default -> null;
            };
            case SOUTH_WEST -> switch (incoming) {
                case SOUTH -> BlockFace.WEST;
                case WEST -> BlockFace.SOUTH;
                case NORTH -> BlockFace.NORTH;
                case EAST -> BlockFace.EAST;
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
                        leg.getOppositeFace(),
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


    public Rail.Shape getExpectedRailShape(Block railBlock, boolean isPowered) {
        if (!(railBlock.getBlockData() instanceof Rail)) return null;

        Location loc = railBlock.getLocation();
        World world = railBlock.getWorld();

        boolean north = isRail(world.getBlockAt(loc.clone().add(0, 0, -1)));
        boolean south = isRail(world.getBlockAt(loc.clone().add(0, 0, 1)));
        boolean west  = isRail(world.getBlockAt(loc.clone().add(-1, 0, 0)));
        boolean east  = isRail(world.getBlockAt(loc.clone().add(1, 0, 0)));

        boolean northSouth = north || south;
        boolean eastWest   = east  || west;

        Rail.Shape shape = null;

        // Step 1: straight-only logic
        if (northSouth && !eastWest) shape = Rail.Shape.NORTH_SOUTH;
        else if (eastWest && !northSouth) shape = Rail.Shape.EAST_WEST;

        // Step 2: check for curves if allowed
        boolean se = south && east && !north && !west;
        boolean sw = south && west && !north && !east;
        boolean nw = north && west && !south && !east;
        boolean ne = north && east && !south && !west;

        if (se) shape = Rail.Shape.SOUTH_EAST;
        else if (sw) shape = Rail.Shape.SOUTH_WEST;
        else if (nw) shape = Rail.Shape.NORTH_WEST;
        else if (ne) shape = Rail.Shape.NORTH_EAST;

        // Step 3: if still ambiguous, use redstone toggle priority
        if (shape == null) {
            List<Rail.Shape> order = isPowered
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

        // Step 4: check for ascending slopes if straight
        if (shape == Rail.Shape.NORTH_SOUTH) {
            if (isRail(world.getBlockAt(loc.clone().add(0, 1, -1)))) shape = Rail.Shape.ASCENDING_NORTH;
            else if (isRail(world.getBlockAt(loc.clone().add(0, 1, 1)))) shape = Rail.Shape.ASCENDING_SOUTH;
        } else if (shape == Rail.Shape.EAST_WEST) {
            if (isRail(world.getBlockAt(loc.clone().add(1, 1, 0)))) shape = Rail.Shape.ASCENDING_EAST;
            else if (isRail(world.getBlockAt(loc.clone().add(-1, 1, 0)))) shape = Rail.Shape.ASCENDING_WEST;
        }

        // Fallback
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

    private boolean isCurvedShape(Rail.Shape shape) {
        switch (shape) {
            case NORTH_EAST:
            case NORTH_WEST:
            case SOUTH_EAST:
            case SOUTH_WEST:
                return true;
            default:
                return false;
        }
    }

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

    // True if rails at A and B are connected through the shared face (with 1-block vertical tolerance)
    private static boolean railsConnect(Block a, Block b) {
        if (!(a.getBlockData() instanceof Rail ra) || !(b.getBlockData() instanceof Rail rb)) return false;

        BlockFace face = directionTo(a, b);
        if (face == null || face == BlockFace.UP || face == BlockFace.DOWN) return false;

        int dy = b.getY() - a.getY();
        if (Math.abs(dy) > 1) return false;

        boolean aOk = Arrays.asList(connectedFaces(ra.getShape())).contains(face);
        boolean bOk = Arrays.asList(connectedFaces(rb.getShape())).contains(face.getOppositeFace());
        return aOk && bOk;
    }


    // Move from 'current' to the next rail that actually connects (ignores 'previous')
    @Nullable
    private Block nextRailFollowing(Block current, @Nullable Block previous) {
        for (BlockFace f : HORIZ) {
            Block n = neighborRail(current, f);  // neighborRail already tolerates ±1Y
            if (n == null) continue;
            if (previous != null && n.equals(previous)) continue;
            if (railsConnect(current, n)) return n;
        }
        return null;
    }

    private static final class CurveContext {
        final Block curve;
        final BlockFace incoming;
        final Rail.Shape off_shape;
        final Rail.Shape on_shape;


        CurveContext(Block curve, BlockFace incoming, Rail.Shape off_shape, Rail.Shape on_shape) {
            this.curve = curve;
            this.incoming = incoming;
            this.off_shape = off_shape;
            this.on_shape = on_shape;
        }
    }

    private static BlockFace opposite(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case EAST -> BlockFace.WEST;
            case WEST -> BlockFace.EAST;
            case UP -> BlockFace.DOWN;
            case DOWN -> BlockFace.UP;
            default -> face.getOppositeFace();
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


    private static final class DisplayTarget {
        private final Location location;
        private final Component text;

        private DisplayTarget(Location location, Component text) {
            this.location = location.clone();
            this.text = text;
        }

        public Location getLocation() { return location.clone(); }
        public Component getText() { return text; }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof DisplayTarget)) return false;
            DisplayTarget that = (DisplayTarget) other;
            UUID w1 = this.location.getWorld() != null ? this.location.getWorld().getUID() : null;
            UUID w2 = that.location.getWorld() != null ? that.location.getWorld().getUID() : null;
            return Objects.equals(w1, w2)
                && location.getBlockX() == that.location.getBlockX()
                && location.getBlockY() == that.location.getBlockY()
                && location.getBlockZ() == that.location.getBlockZ()
                && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            UUID w = location.getWorld() != null ? location.getWorld().getUID() : null;
            return Objects.hash(w, location.getBlockX(), location.getBlockY(), location.getBlockZ(), text);
        }
    }

    private final class PlayerDisplays {
        private final Map<RailSwitchKey, DisplayEntry> displays = new HashMap<>();

        void sync(Map<RailSwitchKey, List<DisplayTarget>> targets, Player player) {
            displays.entrySet().removeIf(entry -> {
                if (!targets.containsKey(entry.getKey())) {
                    entry.getValue().destroy(player);
                    return true;
                }
                return false;
            });

            for (Map.Entry<RailSwitchKey, List<DisplayTarget>> entry : targets.entrySet()) {
                DisplayEntry existing = displays.get(entry.getKey());
                if (existing != null && existing.matches(entry.getValue())) continue;

                if (existing != null) existing.destroy(player);
                DisplayEntry replacement = DisplayEntry.spawn(plugin, player, entry.getValue());
                if (replacement != null) {
                    displays.put(entry.getKey(), replacement);
                }
            }
        }

        void clear(@Nullable Player player) {
            for (DisplayEntry entry : displays.values()) {
                entry.destroy(player);
            }
            displays.clear();
        }
    }

    private static final class DisplayEntry {
        private final RailSwitchPlugin plugin;
        private final List<DisplayTarget> targets;
        private final List<TextDisplay> entities;

        private DisplayEntry(RailSwitchPlugin plugin, List<DisplayTarget> targets, List<TextDisplay> entities) {
            this.plugin = plugin;
            this.targets = targets;
            this.entities = entities;
        }

        static DisplayEntry spawn(RailSwitchPlugin plugin, Player player, List<DisplayTarget> targets) {
            if (targets.isEmpty()) return null;
            List<TextDisplay> spawned = new ArrayList<>(targets.size());
            for (DisplayTarget target : targets) {
                Location spawnLocation = target.getLocation();
                TextDisplay entity = spawnDisplay(plugin, player, spawnLocation, target.getText());
                if (entity != null) spawned.add(entity);
            }
            if (spawned.isEmpty()) return null;
            return new DisplayEntry(plugin, targets, spawned);
        }

        boolean matches(List<DisplayTarget> other) {
            if (targets.size() != other.size()) return false;
            for (int i = 0; i < targets.size(); i++) {
                if (!targets.get(i).equals(other.get(i))) return false;
            }
            return true;
        }

        void destroy(@Nullable Player player) {
            for (TextDisplay entity : entities) {
                if (entity == null || !entity.isValid()) continue;
                if (player != null) player.hideEntity(plugin, entity);
                entity.remove();
            }
            entities.clear();
        }

        private static TextDisplay spawnDisplay(RailSwitchPlugin plugin, Player player, Location location, Component text) {
            return location.getWorld().spawn(location, TextDisplay.class, display -> {
                display.text(text);
                display.setBillboard(Display.Billboard.CENTER);
                display.setShadowed(false);
                display.setGravity(false);
                display.setPersistent(false);
                display.setSeeThrough(true);
                display.setViewRange((float) plugin.getSwitchConfiguration().getDisplayRange() + 2.0F);
                display.setTeleportDuration(0);
                display.setVisibleByDefault(false);
                display.setAlignment(TextDisplay.TextAlignment.CENTER);
                player.showEntity(plugin, display);
            });
        }
    }

}
