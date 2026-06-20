package net.civmc.heliodor.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.civmc.heliodor.ChunkPos;
import net.civmc.heliodor.vein.data.MeteoricIronVeinConfig;
import net.civmc.heliodor.vein.data.Vein;
import net.civmc.heliodor.vein.data.VerticalBlockPos;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;

public class VeinSpawner {

    private final Logger logger;
    private final Plugin plugin;
    private final VeinDao dao;
    private final VeinCache cache;

    private final MeteoricIronVeinConfig meteoriteVeinConfig;
    private final NamespacedKey nextMeteorSpawnAtKey;
    private final NamespacedKey nextMeteorForecastStartKey;
    private final NamespacedKey announcedMeteorVeinIdKey;
    private boolean publicSpawnInProgress;

    public VeinSpawner(Plugin plugin, VeinDao dao, VeinCache cache, MeteoricIronVeinConfig meteoriteVeinConfig) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dao = dao;
        this.cache = cache;
        this.meteoriteVeinConfig = meteoriteVeinConfig;
        this.nextMeteorSpawnAtKey = new NamespacedKey(plugin, "next_meteor_spawn_at");
        this.nextMeteorForecastStartKey = new NamespacedKey(plugin, "next_meteor_forecast_start");
        this.announcedMeteorVeinIdKey = new NamespacedKey(plugin, "announced_meteor_vein_id");
    }

    public void start() {
        if (meteoriteVeinConfig.publicAnnouncementEnabled()) {
            Bukkit.getScheduler().runTaskTimer(plugin, this::tryPublicSpawn, 20 * 60, 20 * 60);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::trySpawns, 20 * 60, 20 * 60);
        }
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

    public Vein trySpawnMeteoricIron() {
        if (Bukkit.isPrimaryThread()) {
            return null;
        }

        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        List<VerticalBlockPos> positions = meteoriteVeinConfig.positions();
        VerticalBlockPos position = positions.get(ThreadLocalRandom.current().nextInt(positions.size()));

        int x;
        int z;

        while (true) {
            x = ThreadLocalRandom.current().nextInt(-meteoriteVeinConfig.maxPositionRadius(), meteoriteVeinConfig.maxPositionRadius() + 1);
            z = ThreadLocalRandom.current().nextInt(-meteoriteVeinConfig.maxPositionRadius(), meteoriteVeinConfig.maxPositionRadius() + 1);

            if (x * x + z * z < meteoriteVeinConfig.minPositionRadius() * meteoriteVeinConfig.minPositionRadius()) {
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
            return null;
        }

        int inaccuracy = meteoriteVeinConfig.config().inaccuracy();

        int offsetX;
        int offsetY;
        int offsetZ;
        do {
            offsetX = ThreadLocalRandom.current().nextInt(-inaccuracy, inaccuracy + 1);
            offsetY = ThreadLocalRandom.current().nextInt(-inaccuracy, inaccuracy + 1);
            offsetZ = ThreadLocalRandom.current().nextInt(-inaccuracy, inaccuracy + 1);
        } while (NumberConversions.square(offsetX) + NumberConversions.square(offsetY) + NumberConversions.square(offsetZ) > NumberConversions.square(meteoriteVeinConfig.config().highDistance() - meteoriteVeinConfig.config().spawnRadius()));

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

    public MeteorStatus getAnnouncedMeteorStatus() {
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null) {
            return MeteorStatus.noSignal();
        }
        Integer veinId = world.getPersistentDataContainer().get(announcedMeteorVeinIdKey, PersistentDataType.INTEGER);
        if (veinId == null) {
            return MeteorStatus.noSignal();
        }
        Vein vein = cache.getVeinById(veinId);
        if (vein == null || vein.oresRemaining() < vein.ores() * 0.5) {
            return MeteorStatus.noSignal();
        }
        return new MeteorStatus(true, vein.world(), vein.x() + vein.offsetX(), vein.y() + vein.offsetY(),
            vein.z() + vein.offsetZ());
    }

    public ForecastWindow getForecastWindow() {
        if (!meteoriteVeinConfig.publicAnnouncementEnabled()) {
            return null;
        }
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null) {
            return null;
        }
        Long spawnAt = world.getPersistentDataContainer().get(nextMeteorSpawnAtKey, PersistentDataType.LONG);
        if (spawnAt == null) {
            spawnAt = scheduleNextPublicSpawn(world);
        }
        long windowMillis = Math.max(1L, meteoriteVeinConfig.forecastWindowMinutes()) * 60_000L;
        Long forecastStart = world.getPersistentDataContainer().get(nextMeteorForecastStartKey, PersistentDataType.LONG);
        if (forecastStart == null) {
            forecastStart = chooseForecastStart(world, spawnAt, windowMillis);
        }
        return new ForecastWindow(forecastStart, forecastStart + windowMillis);
    }

    public Long getNextPublicSpawnAt() {
        if (!meteoriteVeinConfig.publicAnnouncementEnabled()) {
            return null;
        }
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null) {
            return null;
        }
        Long spawnAt = world.getPersistentDataContainer().get(nextMeteorSpawnAtKey, PersistentDataType.LONG);
        if (spawnAt == null) {
            spawnAt = scheduleNextPublicSpawn(world);
        }
        return spawnAt;
    }

    public void forcePublicSpawn(Consumer<Vein> callback) {
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null || !canStartPublicSpawn()) {
            callback.accept(null);
            return;
        }

        startPublicSpawn(world, callback);
    }

    private void tryPublicSpawn() {
        World world = Bukkit.getWorld(meteoriteVeinConfig.config().world());
        if (world == null || !canStartPublicSpawn()) {
            return;
        }
        PersistentDataContainer pdc = world.getPersistentDataContainer();
        Long spawnAt = pdc.get(nextMeteorSpawnAtKey, PersistentDataType.LONG);
        if (spawnAt == null) {
            scheduleNextPublicSpawn(world);
            return;
        }
        if (spawnAt > System.currentTimeMillis()) {
            return;
        }
        startPublicSpawn(world, vein -> {
        });
    }

    private boolean canStartPublicSpawn() {
        return !publicSpawnInProgress && meteoriteVeinConfig.publicAnnouncementEnabled() && checkValidMeteoricIronConfig();
    }

    private void startPublicSpawn(World world, Consumer<Vein> callback) {
        publicSpawnInProgress = true;
        Integer previousVeinId = world.getPersistentDataContainer().get(announcedMeteorVeinIdKey,
            PersistentDataType.INTEGER);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Vein spawnedVein = trySpawnMeteoricIron();
            if (spawnedVein != null && previousVeinId != null) {
                cache.expireVein(previousVeinId);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                publicSpawnInProgress = false;
                if (spawnedVein == null) {
                    callback.accept(null);
                    return;
                }
                world.getPersistentDataContainer().set(announcedMeteorVeinIdKey, PersistentDataType.INTEGER,
                    spawnedVein.id());
                announceMeteor(spawnedVein);
                scheduleNextPublicSpawn(world);
                callback.accept(spawnedVein);
            });
        });
    }

    private long scheduleNextPublicSpawn(World world) {
        long minDelayMillis = 60 * 60_000L;
        long maxDelayMillis = Math.max(minDelayMillis,
            Math.max(1L, meteoriteVeinConfig.maxPublicDelayMinutes()) * 60_000L);
        long spawnAt = System.currentTimeMillis()
            + ThreadLocalRandom.current().nextLong(minDelayMillis, maxDelayMillis + 1L);
        world.getPersistentDataContainer().set(nextMeteorSpawnAtKey, PersistentDataType.LONG, spawnAt);
        chooseForecastStart(world, spawnAt, Math.max(1L, meteoriteVeinConfig.forecastWindowMinutes()) * 60_000L);
        return spawnAt;
    }

    private long chooseForecastStart(World world, long spawnAt, long windowMillis) {
        long offsetMillis = ThreadLocalRandom.current().nextLong(windowMillis + 1L);
        long forecastStart = spawnAt - offsetMillis;
        world.getPersistentDataContainer().set(nextMeteorForecastStartKey, PersistentDataType.LONG, forecastStart);
        return forecastStart;
    }

    private void announceMeteor(Vein vein) {
        Bukkit.broadcast(Component.text("A bright flash in the sky appears near " + (vein.x() + vein.offsetX())
            + " " + (vein.y() + vein.offsetY()) + " " + (vein.z() + vein.offsetZ())
            + "...", NamedTextColor.GOLD).hoverEvent(Component.text("Use /meteor to check this later")));
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
                            world.removePluginChunkTicket(ticket.x(), ticket.z(), plugin);
                        }
                        return null;
                    }
                }
            }
        } catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Adding chunk tickets to determine location", ex);
            for (ChunkPos ticket : tickets) {
                world.removePluginChunkTicket(ticket.x(), ticket.z(), plugin);
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
                    world.removePluginChunkTicket(ticket.x(), ticket.z(), plugin);
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

    private record MeteoritePos(int blocks, int y) {

    }

    public record MeteorStatus(boolean active, String world, int x, int y, int z) {

        public static MeteorStatus noSignal() {
            return new MeteorStatus(false, null, 0, 0, 0);
        }
    }

    public record ForecastWindow(long startMillis, long endMillis) {

    }
}
