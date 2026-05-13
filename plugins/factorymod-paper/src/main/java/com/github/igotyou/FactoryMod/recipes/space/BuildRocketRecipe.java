package com.github.igotyou.FactoryMod.recipes.space;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class BuildRocketRecipe extends InputRecipe {

    private static final int WIDTH = 11;
    private static final int HEIGHT = 24;
    private static final int DEPTH = 12;
    private static final DustOptions RED_DUST = new DustOptions(Color.RED, 1.0f);

    private int timesRun = 0;
    private BukkitTask task;

    private final Clipboard clipboard;

    public BuildRocketRecipe(final String identifier, final String name, final int productionTime, final ItemMap input) {
        super(identifier, name, productionTime, input);

        File file = FactoryMod.getInstance().getDataFile("rocket.schem");
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            this.clipboard = reader.read();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
        return List.of();
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.DAYLIGHT_DETECTOR;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        if (!isEmpty(fccf)) {
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
        }
        if (!isEmpty(fccf)) {
            return false;
        }

        final Block northWestOrigin = getNorthWestOrigin(fccf);
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();

        for (BlockVector3 position : region) {
            final BlockState block = clipboard.getBlock(position);
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final Block target = northWestOrigin.getRelative(relative.getX(), relative.getY(), relative.getZ());

            final BlockData data = Bukkit.createBlockData(block.getAsString());

            target.setBlockData(data, false);
        }
        return true;
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
