package net.civmc.heliodor.vein.listener;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.vein.data.Vein;
import net.civmc.heliodor.vein.VeinCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class VeinBreakListener implements Listener {

    private final Map<ChunkPos, List<Location>> minedBlocksCache = new HashMap<>();

    private final NamespacedKey oreLocationsKey;
    private final VeinCache dao;

    public VeinBreakListener(NamespacedKey oreLocationsKey, VeinCache dao) {
        this.oreLocationsKey = oreLocationsKey;
        this.dao = dao;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!Tag.BASE_STONE_OVERWORLD.isTagged(block.getType())
            && block.getType() != Material.SMOOTH_BASALT
            && block.getType() != Material.COPPER_ORE
            && block.getType() != Material.IRON_ORE
            && block.getType() != Material.COAL_ORE
        ) {
            return;
        }
        ChunkPos chunkPos = ChunkPos.from(block.getChunk());
        List<Location> locations = this.minedBlocksCache.get(chunkPos);
        if (locations != null && locations.contains(block.getLocation())) {
            return;
        }
        World world = block.getWorld();
        List<Vein> veins = this.dao.getVeinsInRadius(world.getName(), block.getX(), block.getY(), block.getZ());
        if (veins.isEmpty()) {
            return;
        }

        this.minedBlocksCache.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(block.getLocation());

        for (Vein vein : veins) {
            float probability = this.dao.getVeinOreProbability(vein, 120);
            if (probability == 0) {
                continue;
            }
            if (ThreadLocalRandom.current().nextFloat() > probability) {
                this.dao.incrementBlocksMined(vein, 1);
                continue;
            }

            BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
            List<BlockFace> validFaces = new ArrayList<>(faces.length);
            for (BlockFace face : faces) {
                if (Tag.BASE_STONE_OVERWORLD.isTagged(block.getRelative(face).getType())) {
                    validFaces.add(face);
                }
            }
            if (validFaces.isEmpty()) {
                this.dao.incrementBlocksMined(vein, 1);
                continue;
            }
            BlockFace face = validFaces.get(ThreadLocalRandom.current().nextInt(validFaces.size()));
            Block oreBlock = block.getRelative(face);

            oreBlock.setType(Material.RAW_IRON_BLOCK);
            event.getPlayer().sendMessage(Component.text("You found a chunk of meteoric iron", NamedTextColor.GRAY, TextDecoration.ITALIC));

            PersistentDataContainer chunkPdc = oreBlock.getChunk().getPersistentDataContainer();
            int[] ints = chunkPdc.get(oreLocationsKey, PersistentDataType.INTEGER_ARRAY);
            IntList list = ints == null ? new IntArrayList() : new IntArrayList(ints);
            list.add(oreBlock.getX());
            list.add(oreBlock.getY());
            list.add(oreBlock.getZ());

            chunkPdc.set(oreLocationsKey, PersistentDataType.INTEGER_ARRAY, list.toIntArray());

            this.minedBlocksCache.computeIfAbsent(ChunkPos.from(oreBlock.getChunk()), k -> new ArrayList<>()).add(oreBlock.getLocation());
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger()
                .info("Player " + event.getPlayer().getName() + " found meteoric iron ore at " + oreBlock.getWorld().getName() + " " + oreBlock.getX() + " " + oreBlock.getY() + " " + oreBlock.getZ());
            this.dao.incrementBlocksMined(vein, 2);
            this.dao.decrementOres(vein);
        }
    }

    record ChunkPos(int x, int z) {
        public static ChunkPos from(Chunk chunk) {
            return new ChunkPos(chunk.getX(), chunk.getZ());
        }
    }
}
