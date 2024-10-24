package net.civmc.heliodor.heliodor.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

public class VeinSpawner {

    private final Logger logger;
    private final Plugin plugin;
    private final VeinDao dao;
    private final VeinCache cache;

    private final MeteoriteVeinConfig meteoriteVeinConfig;

    public VeinSpawner(Plugin plugin, VeinDao dao, VeinCache cache, MeteoriteVeinConfig meteoriteVeinConfig) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dao = dao;
        this.cache = cache;
        this.meteoriteVeinConfig = meteoriteVeinConfig;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::trySpawns, 20 * 60, 20 * 60);
    }

    private void trySpawns() {
        for (int i = 0; i < 10; i++) {
            boolean attemptedSpawn = false;
            Map<String, Boolean> spawnableTypes = dao.getSpawnableTypes(
                Map.of(MeteoriteVeinConfig.TYPE_NAME, meteoriteVeinConfig.config().frequencyMinutes()),
                Map.of(MeteoriteVeinConfig.TYPE_NAME, meteoriteVeinConfig.config().maxSpawns()));
            if (spawnableTypes.getOrDefault(MeteoriteVeinConfig.TYPE_NAME, true)) {
                attemptedSpawn = true;
                logger.info("Attempting meteorite vein spawn");
                trySpawnMeteorite();
            }
            if (!attemptedSpawn) {
                break;
            }
        }
    }

    private void trySpawnMeteorite() {
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null) {
            return;
        }
        List<Location> positions = meteoriteVeinConfig.positions();
        Location position = positions.get(ThreadLocalRandom.current().nextInt(positions.size())).clone();

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

        int bury = ThreadLocalRandom.current().nextInt(meteoriteVeinConfig.maxBury());
        int radius = meteoriteVeinConfig.config().spawnRadius();
        MeteoritePos mpos = getMeteoricVeinPositionAndBlocks(world, position.getBlockX(), position.getBlockZ(), bury, radius);
        if (mpos == null || mpos.blocks() < meteoriteVeinConfig.config().minBlocks()) {
            return;
        }

        int inaccuracy = meteoriteVeinConfig.config().inaccuracy();
        Vein vein = new Vein(
            MeteoriteVeinConfig.TYPE_NAME,
            System.currentTimeMillis(),
            world.getName(),
            radius,
            position.getBlockX(),
            mpos.y(),
            position.getBlockZ(),
            ThreadLocalRandom.current().nextInt(inaccuracy),
            ThreadLocalRandom.current().nextInt(inaccuracy),
            ThreadLocalRandom.current().nextInt(inaccuracy),
            mpos.blocks(),
            0,
            false,
            ThreadLocalRandom.current().nextInt(meteoriteVeinConfig.config().minOre(), meteoriteVeinConfig.config().maxOre() + 1)
        );

        logger.info("Meteorite vein submitted to database: " + vein);
        cache.addVein(vein);
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
                    return;
                }
                while (!Tag.BASE_STONE_OVERWORLD.isTagged(spawnBlock.getType())) {
                    spawnBlock = spawnBlock.getRelative(BlockFace.DOWN);
                    if (spawnBlock.getY() <= world.getMinHeight()) {
                        return;
                    }
                }
                spawnBlock = spawnBlock.getRelative(BlockFace.DOWN, bury + radius);
                if (spawnBlock.getY() <= world.getMinHeight() + radius) {
                    return;
                }

                for (int sx = spawnBlock.getX() - radius; sx <= spawnBlock.getX() + radius; sx++) {
                    for (int sy = spawnBlock.getY() - radius; sy <= spawnBlock.getY() + radius; sy++) {
                        for (int sz = spawnBlock.getZ() - radius; sz < spawnBlock.getZ() + radius; sz++) {
                            Location spawn = new Location(world, sx, sy, sz);
                            if (spawn.distanceSquared(spawn.getBlock().getLocation()) <= radius * radius) {
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
