package net.civmc.heliodor.heliodor.vein;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BreakListener implements Listener {

    private final List<Location> minedBlocksCache = new ArrayList<>();

    private final Plugin plugin;
    private final NamespacedKey oreLocationsKey;
    private final VeinCache dao;

    public BreakListener(Plugin plugin, VeinCache dao) {
        this.plugin = plugin;
        this.oreLocationsKey = new NamespacedKey(plugin, "ore_locations");
        this.dao = dao;
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (this.minedBlocksCache.contains(block.getLocation())) {
            return;
        }
        World world = block.getWorld();
        List<VeinCache.CachedVein> veins = this.dao.getVeinsInRadius(world.getName(), block.getX(), block.getY(), block.getZ());
        if (veins.isEmpty()) {
            return;
        }

        this.minedBlocksCache.add(block.getLocation());

        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        List<BlockFace> validFaces = new ArrayList<>(faces.length);
        for (BlockFace face : faces) {
            if (Tag.BASE_STONE_OVERWORLD.isTagged(block.getRelative(face).getType())) {
                validFaces.add(face);
            }
        }
        if (validFaces.isEmpty()) {
            return;
        }
        BlockFace face = validFaces.get(ThreadLocalRandom.current().nextInt(validFaces.size()));
        Block oreBlock = block.getRelative(face);

        oreBlock.setType(Material.RAW_IRON_BLOCK);
        event.getPlayer().sendMessage(Component.text("You found a chunk of meteoric iron", NamedTextColor.GRAY));

        PersistentDataContainer chunkPdc = block.getChunk().getPersistentDataContainer();
        int[] ints = chunkPdc.get(oreLocationsKey, PersistentDataType.INTEGER_ARRAY);
        IntList list = ints == null ? new IntArrayList() : new IntArrayList(ints);
        list.add()

        chunkPdc.set(oreLocationsKey, PersistentDataType.INTEGER_ARRAY, list.toIntArray());
    }
}
