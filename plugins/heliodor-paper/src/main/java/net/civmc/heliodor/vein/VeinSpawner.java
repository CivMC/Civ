package net.civmc.heliodor.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.civmc.heliodor.vein.data.MeteoricIronVeinConfig;
import net.civmc.heliodor.vein.data.Vein;
import net.civmc.heliodor.vein.data.VerticalBlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;

public class VeinSpawner {

    private final Logger logger;
    private final Plugin plugin;
    private final VeinDao dao;
    private final VeinCache cache;

    private final MeteoricIronVeinConfig meteoriteVeinConfig;

    public VeinSpawner(Plugin plugin, VeinDao dao, VeinCache cache, MeteoricIronVeinConfig meteoriteVeinConfig) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dao = dao;
        this.cache = cache;
        this.meteoriteVeinConfig = meteoriteVeinConfig;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::trySpawns, 20 * 60, 20 * 60);
    }

    private void trySpawns() {
        for (int i = 0; i < 10; i++) {
            boolean attemptedSpawn = false;
            Map<String, Boolean> spawnableTypes = dao.getSpawnableTypes(
                Map.of(MeteoricIronVeinConfig.TYPE_NAME, meteoriteVeinConfig.config().frequencyMinutes()),
                Map.of(MeteoricIronVeinConfig.TYPE_NAME, meteoriteVeinConfig.config().maxSpawns()));
            if (spawnableTypes.getOrDefault(MeteoricIronVeinConfig.TYPE_NAME, true)) {
                if (checkValidMeteoricIronConfig()) {
                    attemptedSpawn = true;
                    logger.info("Attempting meteorite vein spawn");
                    trySpawnMeteoricIron();
                }
            }
            if (!attemptedSpawn) {
                break;
            }
        }
    }

    public boolean checkValidMeteoricIronConfig() {
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null) {
            return false;
        }
        List<VerticalBlockPos> positions = meteoriteVeinConfig.positions();
        if (positions.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean trySpawnMeteoricIron() {
        if (Bukkit.isPrimaryThread()) {
            return false;
        }

        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        List<VerticalBlockPos> positions = meteoriteVeinConfig.positions();
        VerticalBlockPos position = positions.get(ThreadLocalRandom.current().nextInt(positions.size()));

        int x;
        int z;

        while (true) {
            x = ThreadLocalRandom.current().nextInt(-meteoriteVeinConfig.maxPositionRadius(), meteoriteVeinConfig.maxPositionRadius() + 1);
            z = ThreadLocalRandom.current().nextInt(-meteoriteVeinConfig.maxPositionRadius(), meteoriteVeinConfig.maxPositionRadius() + 1);

            if (Math.abs(x) < meteoriteVeinConfig.minPositionRadius() || Math.abs(z) < meteoriteVeinConfig.minPositionRadius()) {
                continue;
            }

            if (x * x + z * z <= meteoriteVeinConfig.maxPositionRadius() * meteoriteVeinConfig.maxPositionRadius()) {
                break;
            }
        }

        x += position.x();
        z += position.z();

        int bury = ThreadLocalRandom.current().nextInt(meteoriteVeinConfig.maxBury() + 1);
        int radius = meteoriteVeinConfig.config().spawnRadius();
        MeteoritePos mpos = getMeteoricVeinPositionAndBlocks(world, x, z, bury, radius);
        if (mpos == null || mpos.blocks() < meteoriteVeinConfig.config().minBlocks()) {
            return false;
        }

        int inaccuracy = meteoriteVeinConfig.config().inaccuracy();

        int offsetX;
        int offsetY;
        int offsetZ;
        do {
            offsetX = ThreadLocalRandom.current().nextInt(-inaccuracy, inaccuracy + 1);
            offsetY = ThreadLocalRandom.current().nextInt(-inaccuracy, inaccuracy + 1);
            offsetZ = ThreadLocalRandom.current().nextInt(-inaccuracy, inaccuracy + 1);
        } while(NumberConversions.square(offsetX) + NumberConversions.square(offsetY) + NumberConversions.square(offsetZ) > NumberConversions.square(meteoriteVeinConfig.config().highDistance() - meteoriteVeinConfig.config().spawnRadius()));

        int ores = ThreadLocalRandom.current().nextInt(meteoriteVeinConfig.config().minOre(), meteoriteVeinConfig.config().maxOre() + 1);
        Vein vein = new Vein(
            -1,
            MeteoricIronVeinConfig.TYPE_NAME,
            System.currentTimeMillis(),
            world.getName(),
            radius,
            x,
            mpos.y(),
            z,
            offsetX,
            offsetY,
            offsetZ,
            mpos.blocks(),
            false,
            ores,
            ores
        );

        logger.info("Meteorite vein submitted to database: " + vein);
        return cache.addVein(vein);
    }

    private MeteoritePos getMeteoricVeinPositionAndBlocks(World world, int x, int z, int bury, int radius) {
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
                        return null;
                    }
                }
            }
        } catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Adding chunk tickets to determine location", ex);
            for (ChunkPos ticket : tickets) {
                world.removePluginChunkTicket(ticket.x, ticket.z, plugin);
            }
            return null;
        }

        AtomicReference<MeteoritePos> blocksAtomic = new AtomicReference<>();
        CountDownLatch blocksReady = new CountDownLatch(1);
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                int blocks = 0;
                Block spawnBlock = world.getHighestBlockAt(x, z);
                if (spawnBlock.getY() <= world.getMinHeight()) {
                    logger.log(Level.INFO, "Failed spawn attempt at x = " + x + ", z = " + z);
                    return;
                }
                while (!Tag.BASE_STONE_OVERWORLD.isTagged(spawnBlock.getType())) {
                    spawnBlock = spawnBlock.getRelative(BlockFace.DOWN);
                    if (spawnBlock.getY() <= world.getMinHeight()) {
                        logger.log(Level.INFO, "Failed spawn attempt at x = " + x + ", z = " + z);
                        return;
                    }
                }
                spawnBlock = spawnBlock.getRelative(BlockFace.DOWN, bury + radius);
                if (spawnBlock.getY() <= world.getMinHeight() + radius) {
                    logger.log(Level.INFO, "Failed spawn attempt at x = " + x + ", z = " + z);
                    return;
                }

                for (int sx = spawnBlock.getX() - radius; sx <= spawnBlock.getX() + radius; sx++) {
                    for (int sy = spawnBlock.getY() - radius; sy <= spawnBlock.getY() + radius; sy++) {
                        for (int sz = spawnBlock.getZ() - radius; sz < spawnBlock.getZ() + radius; sz++) {
                            Location spawn = new Location(world, sx, sy, sz);
                            if (spawnBlock.getLocation().distanceSquared(spawn.getBlock().getLocation()) <= radius * radius) {
                                if (Tag.BASE_STONE_OVERWORLD.isTagged(spawn.getBlock().getType())) {
                                    blocks++;
                                }
                            }
                        }
                    }
                }

                blocksAtomic.set(new MeteoritePos(blocks, spawnBlock.getY()));
            } finally {
                for (ChunkPos ticket : tickets) {
                    world.removePluginChunkTicket(ticket.x, ticket.z, plugin);
                }
                blocksReady.countDown();
            }
        });

        try {
            blocksReady.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return blocksAtomic.get();

    }

    private record ChunkPos(int x, int z) {

    }

    private record MeteoritePos(int blocks, int y) {

    }
}
