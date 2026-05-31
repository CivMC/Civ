package com.github.igotyou.FactoryMod.recipes.space;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.List;
import java.util.Objects;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.flight.FlightComputer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class BuildRocketRecipe extends InputRecipe {

    private static final int WIDTH = 11;
    private static final int HEIGHT = 24;
    private static final int DEPTH = 12;
    private static final int ROCKET_LAYER_PLACE_INTERVAL_TICKS = 15;
    private static final DustOptions RED_DUST = new DustOptions(Color.RED, 1.0f);

    private int timesRun = 0;
    private BukkitTask task;

    private final Clipboard clipboard;
    private final String world;

    public BuildRocketRecipe(final String identifier, final String name, final int productionTime, final ItemMap input,
                             final String world) {
        super(identifier, name, productionTime, input);
        this.clipboard = JavaPlugin.getPlugin(ZorwethPlugin.class).getRocketClipboard();
        this.world = Objects.requireNonNull(world);
    }

    @Override
    public List<ItemStack> getInputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        if (i == null) {
            return input.getItemStackRepresentation();
        }
        return createLoredStacksForInfo(i);
    }

    @Override
    public List<ItemStack> getOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of();
    }

    @Override
    public List<String> getTextualOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of("Rocket:",
            "6 uses",
            "100 kg dry weight",
            "50 kg per player",
            "1 kg per stack",
            "5,000 m/s exhaust velocity",
            "Requires physical space");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.DAYLIGHT_DETECTOR;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        if (!JavaPlugin.getPlugin(ZorwethPlugin.class).getResearchManager().isResearchComplete()) {
            return new EffectFeasibility(false, "research is not complete");
        } else if (!fccf.getFurnace().getWorld().getName().equals(world)) {
            return new EffectFeasibility(false, "wrong world");
        } else if (!isEmpty(fccf)) {
            drawRocketArea(fccf);
            return new EffectFeasibility(false, "rocket requires clear area");
        }

        return new EffectFeasibility(true, null);
    }

    private boolean isEmpty(FurnCraftChestFactory fccf) {
        final Block northWestOrigin = getNorthWestOrigin(fccf);

        for (int width = 0; width <= WIDTH; width++) {
            for (int height = 0; height <= HEIGHT; height++) {
                for (int depth = 0; depth <= DEPTH; depth++) {
                    final Location location = northWestOrigin.getLocation().add(width, height, depth);
                    if (!location.getBlock().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean applyEffect(final Inventory inputInv, final Inventory outputInv, final FurnCraftChestFactory fccf) {
        if (!input.isContainedIn(inputInv)) {
            return false;
        } else if (!fccf.getFurnace().getWorld().getName().equals(world)) {
            return false;
        } else if (!isEmpty(fccf)) {
            return false;
        }

        final Block northWestOrigin = getNorthWestOrigin(fccf);
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();

        final int maxLayer = region.getMaximumPoint().getY() - schematicNorthWestCorner.getY();
        final int[] currentLayer = {0};
        Bukkit.getScheduler().runTaskTimer(FactoryMod.getInstance(), task -> {
            if (currentLayer[0] > maxLayer) {
                task.cancel();
                return;
            }

            Location furnace = fccf.getFurnace().getLocation();
            World world = furnace.getWorld();

            for (BlockVector3 position : region) {
                final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
                if (relative.getY() != currentLayer[0]) {
                    continue;
                }
                final BlockState block = clipboard.getBlock(position);
                final Block target = northWestOrigin.getRelative(relative.getX(), relative.getY(), relative.getZ());

                if (!target.getType().isAir()) {
                    continue;
                }

                final BlockData data = Bukkit.createBlockData(block.getAsString());

                target.setBlockData(data, false);
                if (!data.getMaterial().isAir()) {
                    target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0.5, 0.5, 0.5), 16,
                        0.25, 0.25, 0.25, 0.0, data);
                    world.playSound(target.getLocation(), Sound.BLOCK_STONE_PLACE, .8f, 1);

                }

                if (target.getType() == Material.DISPENSER && FlightComputer.isFlightComputerPosition(relative)) {
                    Dispenser dispenser = (Dispenser) target.getState(false);
                    dispenser.getPersistentDataContainer().set(FlightComputer.ROCKET_COMPUTER_KEY, PersistentDataType.BOOLEAN, true);
                    dispenser.update(false, false);
                    reinforceFlightComputer(fccf, target);
                }
            }
            currentLayer[0]++;
        }, 0, ROCKET_LAYER_PLACE_INTERVAL_TICKS);
        input.removeSafelyFrom(inputInv);
        return true;
    }

    private void reinforceFlightComputer(final FurnCraftChestFactory fccf, final Block flightComputer) {
        if (!FactoryMod.getInstance().getManager().isCitadelEnabled()) {
            return;
        }

        final Reinforcement furnaceReinforcement = ReinforcementLogic.getReinforcementAt(fccf.getFurnace().getLocation());
        if (furnaceReinforcement == null) {
            return;
        }
        FlightComputer.reinforceFlightComputer(flightComputer, furnaceReinforcement.getGroupId());
    }

    private Block getNorthWestOrigin(final FurnCraftChestFactory fccf) {
        final Block origin = getOrigin(fccf);
        final RocketDirection direction = getDirection(fccf);

        int x = 0;
        int z = 0;

        if (direction.widthFace().getModX() < 0) {
            x = -WIDTH;
        }
        if (direction.widthFace().getModZ() < 0) {
            z = -DEPTH;
        }
        if (direction.depthFace().getModX() < 0) {
            x = -WIDTH;
        }
        if (direction.depthFace().getModZ() < 0) {
            z = -DEPTH;
        }
        return origin.getRelative(x, 0, z);
    }

    private Block getOrigin(FurnCraftChestFactory fccf) {
        final Block furnace = fccf.getFurnace();
        final BlockFace furnaceFace = ((Furnace) furnace.
            getState().getBlockData()).getFacing();
        return furnace.getRelative(furnaceFace.getOppositeFace());
    }

    private RocketDirection getDirection(FurnCraftChestFactory fccf) {
        final Block furnace = fccf.getFurnace();
        final BlockFace furnaceFace = ((Furnace) furnace.
            getState().getBlockData()).getFacing();

        final BlockFace depthFace = furnaceFace.getOppositeFace();
        final BlockFace widthFace = getRightFace(depthFace);
        return new RocketDirection(widthFace, depthFace);
    }

    private void drawRocketArea(final FurnCraftChestFactory fccf) {
        this.timesRun = 0;
        if (this.task != null) {
            return;
        }
        final Block northWestOrigin = getNorthWestOrigin(fccf);
        this.task = Bukkit.getScheduler().runTaskTimer(FactoryMod.getInstance(), () -> {
            this.timesRun++;
            if (timesRun == 40) {
                task.cancel();
                task = null;
            }
            final World world = northWestOrigin.getWorld();
            for (int width = 0; width <= WIDTH; width++) {
                for (int height = 0; height <= HEIGHT; height++) {
                    for (int depth = 0; depth <= DEPTH; depth++) {
                        if (!isEdge(width, height, depth)) {
                            continue;
                        }
                        final Location location = northWestOrigin.getLocation().add(
                            getBoundaryOffset(width, WIDTH),
                            height,
                            getBoundaryOffset(depth, DEPTH)
                        );
                        world.spawnParticle(Particle.DUST, location, 1, RED_DUST);
                    }
                }
            }
        }, 0, 5);
    }

    private boolean isEdge(final int width, final int height, final int depth) {
        int boundaryCount = 0;
        if (width == 0 || width == WIDTH) {
            boundaryCount++;
        }
        if (height == 0 || height == HEIGHT) {
            boundaryCount++;
        }
        if (depth == 0 || depth == DEPTH) {
            boundaryCount++;
        }
        return boundaryCount >= 2;
    }

    private int getBoundaryOffset(final int position, final int maxPosition) {
        if (position == maxPosition) {
            return position + 1;
        }
        return position;
    }

    private BlockFace getRightFace(final BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("Direction must be horizontal");
        };
    }

    private record RocketDirection(BlockFace widthFace, BlockFace depthFace) {
    }

    @Override
    public String getTypeIdentifier() {
        return "BUILD_ROCKET";
    }
}
