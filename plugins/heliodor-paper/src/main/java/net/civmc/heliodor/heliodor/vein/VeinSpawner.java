package net.civmc.heliodor.heliodor.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

public class VeinSpawner {

    private final Plugin plugin;

    public VeinSpawner(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean getEligibleMeteoricIronVeinBlocks(World world, int x, int z, int bury, int radius, Consumer<Integer> blocksConsumer) {
        List<ChunkPos> tickets = new ArrayList<>();

        try {
            for (int chunkX = Math.floorDiv(x - radius, 16); chunkX <= Math.floorDiv(x + radius, 16); chunkX++) {
                for (int chunkZ = Math.floorDiv(z - radius, 16); chunkZ <= Math.floorDiv(z + radius, 16); chunkZ++) {
                    if (world.addPluginChunkTicket(chunkX, chunkZ, plugin)) {
                        tickets.add(new ChunkPos(chunkX, chunkZ));
                    } else {
                        for (ChunkPos ticket : tickets) {
                            world.removePluginChunkTicket(ticket.x, ticket.z, plugin);
                        }
                        return false;
                    }
                }
            }
        } catch (RuntimeException ex) {
            plugin.getLogger().log(Level.WARNING, "Adding chunk tickets to determine location", ex);
            for (ChunkPos ticket : tickets) {
                world.removePluginChunkTicket(ticket.x, ticket.z, plugin);
            }
            return false;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                int blocks = 0;
                Block spawnBlock = world.getHighestBlockAt(x, z);
                while (Tag.BASE_STONE_OVERWORLD.isTagged(spawnBlock.getType())) {
                    spawnBlock = spawnBlock.getRelative(BlockFace.DOWN);
                }
                spawnBlock = spawnBlock.getRelative(BlockFace.DOWN, bury + radius);

                for (int sx = spawnBlock.getX() - radius; sx <= spawnBlock.getX() + radius; sx++) {
                    for (int sy = spawnBlock.getY() - radius; sy <= spawnBlock.getY() + radius; sy++) {
                        for (int sz = spawnBlock.getZ() - radius; sz <+ spawnBlock.getZ() + radius; sz++) {
                            Location spawn = new Location(world, sx, sy, sz);
                            if (spawn.distanceSquared(spawn.getBlock().getLocation()) <= radius * radius) {
                                if (Tag.BASE_STONE_OVERWORLD.isTagged(spawn.getBlock().getType())) {
                                    blocks++;
                                }
                            }
                        }
                    }
                }

                blocksConsumer.accept(blocks);
            } catch (RuntimeException ex) {
                blocksConsumer.accept(0);
                throw ex;
            } finally {
                for (ChunkPos ticket : tickets) {
                    world.removePluginChunkTicket(ticket.x, ticket.z, plugin);
                }
            }
        });

        return true;

    }

    private record ChunkPos(int x, int z) {

    }
}
