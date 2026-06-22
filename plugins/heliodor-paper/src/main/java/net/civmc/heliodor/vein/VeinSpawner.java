package net.civmc.heliodor.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;

public class VeinSpawner {

    private final Logger logger;
    private final Plugin plugin;
    private final VeinDao dao;
    private final VeinCache cache;

    private final MeteoricIronVeinConfig meteoriteVeinConfig;
    private boolean publicSpawnInProgress;

    public VeinSpawner(Plugin plugin, VeinDao dao, VeinCache cache, MeteoricIronVeinConfig meteoriteVeinConfig) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dao = dao;
        this.cache = cache;
        this.meteoriteVeinConfig = meteoriteVeinConfig;
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
        Vein vein = getLatestMeteoricIronVein(world);
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
        Vein vein = getLatestMeteoricIronVein(world);
        if (vein == null) {
            return null;
        }
        long spawnAt = getNextPublicSpawnAt(vein);
        long windowMillis = Math.max(1L, meteoriteVeinConfig.forecastWindowMinutes()) * 60_000L;
        long forecastStart = getForecastStart(vein, spawnAt, windowMillis);
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
        Vein vein = getLatestMeteoricIronVein(world);
        return vein == null ? null : getNextPublicSpawnAt(vein);
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
        Vein vein = getLatestMeteoricIronVein(world);
        if (vein != null && getNextPublicSpawnAt(vein) > System.currentTimeMillis()) {
            return;
        }
        startPublicSpawn(world, ignored -> {
        });
    }

    private boolean canStartPublicSpawn() {
        return !publicSpawnInProgress && meteoriteVeinConfig.publicAnnouncementEnabled() && checkValidMeteoricIronConfig();
    }

    private void startPublicSpawn(World world, Consumer<Vein> callback) {
        publicSpawnInProgress = true;
        Vein previousVein = getLatestMeteoricIronVein(world);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final boolean expiredPrevious = previousVein == null || cache.expireVein(previousVein.id());
            Vein spawnedVein = null;
            if (expiredPrevious) {
                for (int i = 0; i < 10; i++) {
                    spawnedVein = trySpawnMeteoricIron();
                    if (spawnedVein != null) {
                        break;
                    }
                }
            }
            Vein finalSpawnedVein = spawnedVein;
            Bukkit.getScheduler().runTask(plugin, () -> {
                publicSpawnInProgress = false;
                if (finalSpawnedVein == null) {
                    callback.accept(null);
                    return;
                }
                announceMeteor(finalSpawnedVein);
                callback.accept(finalSpawnedVein);
            });
        });
    }

    public void playMeteorCrashGlobal() {
        // 1) Incoming shockwave / bass hit
        playGlobalSound(Sound.ENTITY_WARDEN_SONIC_BOOM, 4.0f, 0.60f);

        // 2) Main impact, 6 ticks later
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            playGlobalSound(Sound.ENTITY_GENERIC_EXPLODE, 10.0f, 0.50f);
        }, 6L);

        // 3) Rolling thunder aftermath, 14 ticks later
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            playGlobalSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 5.0f, 0.65f);
        }, 14L);
    }

    private void playGlobalSound(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                player.getLocation(),
                sound,
                SoundCategory.MASTER,
                volume,
                pitch
            );
        }
    }

    private Vein getLatestMeteoricIronVein(World world) {
        Vein latest = null;
        for (Vein vein : cache.getVeins()) {
            if (!MeteoricIronVeinConfig.TYPE_NAME.equals(vein.type()) || !world.getName().equals(vein.world())) {
                continue;
            }
            if (latest == null || vein.spawnedAt() > latest.spawnedAt()) {
                latest = vein;
            }
        }
        return latest;
    }

    private long getNextPublicSpawnAt(Vein vein) {
        long minDelayMillis = 60 * 60_000L;
        long maxDelayMillis = Math.max(minDelayMillis,
            Math.max(1L, meteoriteVeinConfig.maxPublicDelayMinutes()) * 60_000L);
        long delayMillis = minDelayMillis + new Random(getPublicMeteorSeed(vein,
            meteoriteVeinConfig.spawnTimeSalt()))
            .nextLong(maxDelayMillis - minDelayMillis + 1L);
        return vein.spawnedAt() + delayMillis;
    }

    private long getForecastStart(Vein vein, long spawnAt, long windowMillis) {
        long offsetMillis = new Random(getPublicMeteorSeed(vein,
            meteoriteVeinConfig.forecastWindowSalt())).nextLong(windowMillis + 1L);
        return spawnAt - offsetMillis;
    }

    private long getPublicMeteorSeed(Vein vein, long salt) {
        return vein.spawnedAt() ^ ((long) vein.id() << 32) ^ salt;
    }

    private void announceMeteor(Vein vein) {
        Bukkit.broadcast(Component.text("A bright flash in the sky appears near " + (vein.x() + vein.offsetX())
            + " " + (vein.y() + vein.offsetY()) + " " + (vein.z() + vein.offsetZ())
            + "...", NamedTextColor.GOLD).hoverEvent(Component.text("Use /meteor to check this later")));
        playMeteorCrashGlobal();
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
                if (meteoriteVeinConfig.biome() != null && !meteoriteVeinConfig.biome().equals(spawnBlock.getBiome().getKey().asString())) {
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
