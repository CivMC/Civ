package net.civmc.heliodor.vein.listener;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.civmc.heliodor.vein.VeinCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VeinBreakListener implements Listener {

    private final List<Location> minedBlocksCache = new ArrayList<>();

    private final NamespacedKey oreLocationsKey;
    private final VeinCache dao;

    public VeinBreakListener(NamespacedKey oreLocationsKey, VeinCache dao) {
        this.oreLocationsKey = oreLocationsKey;
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
        event.getPlayer().sendMessage(Component.text("You found a chunk of meteoric iron", NamedTextColor.GRAY, TextDecoration.ITALIC));

        PersistentDataContainer chunkPdc = block.getChunk().getPersistentDataContainer();
        int[] ints = chunkPdc.get(oreLocationsKey, PersistentDataType.INTEGER_ARRAY);
        IntList list = ints == null ? new IntArrayList() : new IntArrayList(ints);
        list.add(oreBlock.getX());
        list.add(oreBlock.getY());
        list.add(oreBlock.getZ());

        chunkPdc.set(oreLocationsKey, PersistentDataType.INTEGER_ARRAY, list.toIntArray());
    }
}
