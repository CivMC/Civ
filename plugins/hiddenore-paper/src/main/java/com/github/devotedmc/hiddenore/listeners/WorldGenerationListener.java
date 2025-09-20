package com.github.devotedmc.hiddenore.listeners;

import ca.spottedleaf.moonrise.common.util.TickThread;
import com.github.devotedmc.hiddenore.BlockConfig;
import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.HiddenOre;
import java.lang.ref.PhantomReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;
import org.spigotmc.AsyncCatcher;

/**
 * Populator to strip out blocks selectively from a world during generation.
 *
 * @author ProgrammerDan
 */
public class WorldGenerationListener implements Listener {

    Map<Material, Material> replacements = null;
    String worldName = null;
    UUID worldUUID = null;

    private final NamespacedKey processedKey;

    /**
     * When creating, pass in a config with three sub-elements. Now supports UUID reference of world.
     * <br>
     * <code>
     * world: world_name (or UUID)
     * replace:
     * IRON_ORE: STONE
     * REDSTONE_ORE: STONE
     * </code>
     * <br>
     * This should be specified per world.
     *
     * @param config The world-specific config.
     */
    public WorldGenerationListener(ConfigurationSection config) {
        if (config.contains("world")) {
            worldName = config.getString("world");
        }
        try {
            if (worldName != null) {
                World world = HiddenOre.getPlugin().getServer().getWorld(worldName);
                if (world != null) {
                    worldUUID = world.getUID();
                } else {
                    worldUUID = UUID.fromString(worldName);
                }
            }
        } catch (IllegalArgumentException iae) {
            worldUUID = null;
        }
        if (config.contains("replace")) {
            replacements = new HashMap<>();
            for (String replace : config.getConfigurationSection("replace").getKeys(false)) {
                Material rMat = Material.matchMaterial(replace.toUpperCase());
                Material wMat = Material.matchMaterial(config.getConfigurationSection("replace").getString(replace));
                if (rMat != null && wMat != null) {
                    replacements.put(rMat, wMat);
                }
            }
        }

        this.processedKey = new NamespacedKey(HiddenOre.getPlugin(), "ore_processed");
    }

    public String getWorldName() {
        return worldName;
    }

    public void clearManually(CommandSender sender, int radius) {
        Deque<IntIntPair> chunks = new ArrayDeque<>();
        World world = HiddenOre.getPlugin().getServer().getWorld(worldName);
        for (int cx = -radius; cx <= radius; cx += 16) {
            for (int cz = -radius; cz <= radius; cz += 16) {
                chunks.add(new IntIntImmutablePair(cx >> 4, cz >> 4));
            }
        }
        sender.sendMessage("Clearing " + chunks.size() + " chunks...");
        Bukkit.getScheduler().runTaskAsynchronously(HiddenOre.getPlugin(), () -> {
            int total = chunks.size();
            int processed = 0;
            int nextProgress = Math.ceilDiv(total, 100);
            while (!chunks.isEmpty()) {
                List<CompletableFuture<?>> futures = new ArrayList<>();
                for (int i = 0; i < 32; i++) {
                    IntIntPair poll = chunks.poll();
                    if (poll == null) {
                        break;
                    }
                    futures.add(world.getChunkAtAsync(poll.firstInt(), poll.secondInt()).thenAccept(chunk -> {
                        Runnable r = () -> {
                            if (chunk.getPersistentDataContainer().getOrDefault(processedKey, PersistentDataType.BOOLEAN, false)) {
                                return;
                            }
                            clear(chunk);
                            if (Config.caveOres) {
                                generateCaveOres(chunk);
                            }
                            chunk.getPersistentDataContainer().set(processedKey, PersistentDataType.BOOLEAN, true);
                        };
                        if (!Bukkit.isPrimaryThread()) {
                            HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Chunk not loaded on primary thread");
                            CountDownLatch latch = new CountDownLatch(1);
                            Bukkit.getScheduler().runTask(HiddenOre.getPlugin(), () -> {
                                r.run();
                                latch.countDown();
                            });
                            try {
                                latch.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        } else {
                            r.run();
                        }
                    }));
                }
                for (CompletableFuture<?> c : futures) {
                    try {
                        c.join();
                    } catch (RuntimeException ex) {
                        HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Processing chunk", ex);
                    }
                    processed++;
                    nextProgress--;
                    if (nextProgress == 0) {
                        String message = "Processed " + processed + "/" + total + " chunks";
                        sender.sendMessage(message);
                        Bukkit.getConsoleSender().sendMessage(message);
                        nextProgress = Math.ceilDiv(total, 100);
                    }
                }
            }
            String message = "Processed " + total + "/" + total + " chunks";
            sender.sendMessage(message);
            Bukkit.getConsoleSender().sendMessage(message);
        });
    }

    /**
     * Reviews the chunk line by line and replaces all instances of toReplace with replaceWith.
     * This is configured world to world.
     * <p>
     * Note that by contract, this is called for a single chunk but the generation can occur
     * for surrounding chunks, if they are not yet populated.
     *
     * @param event ChunkPopulateEvent covering the chunk
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void postGenerationOreClear(ChunkPopulateEvent event) {
        if (true) {
            return;
        }
        if (replacements == null || (worldName == null && worldUUID == null)) {
            return;
        }

        Chunk chunk = event.getChunk();
        if (chunk.getPersistentDataContainer().getOrDefault(processedKey, PersistentDataType.BOOLEAN, false)) {
            return;
        }


        World world = chunk.getWorld();

        if (!world.getName().equalsIgnoreCase(worldName) && !world.getUID().equals(worldUUID)) {
            return;
        }

        clear(chunk);

        if (Config.caveOres) {
            generateCaveOres(chunk);
        }
        chunk.getPersistentDataContainer().set(processedKey, PersistentDataType.BOOLEAN, true);
    }

    private void clear(Chunk chunk) {
        int rep = 0;
        try {
            int maxY = chunk.getWorld().getMaxHeight();
            // now scan the chunk for ores and remove them.
            for (int y = chunk.getWorld().getMinHeight() + 1; y < maxY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);
                        Material mat = block.getType();

                        if (replacements.containsKey(mat)) {
                            rep++;
                            block.setType(replacements.get(mat), false);
                        }
                    }
                }
            }
            if (maxY < 32) {
                HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Chunk height abnormally low: {0} at {1}, {2}",
                    new Object[]{maxY, chunk.getX(), chunk.getZ()});
            }
        } catch (Exception e) {
            HiddenOre.getPlugin().getLogger().log(Level.SEVERE, "Failed to clear ores from chunk at {0}, {1} with error {2}",
                new Object[]{chunk.getX(), chunk.getZ(), e.getMessage()});
        }

        if (rep > 0 && Config.isDebug) {
            HiddenOre.getPlugin().getLogger().log(Level.INFO, "Replaced {0} blocks at {1}, {2}", new Object[]{rep, chunk.getX(), chunk.getZ()});
        }
    }

    static BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    private void generateCaveOres(Chunk chunk) {
        UUID world = chunk.getWorld().getUID();
        int xzmax = chunk.getWorld().getMaxHeight();
        ItemStack breakItem = new ItemStack(Material.DIAMOND_PICKAXE);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getWorld().getMinHeight(); y < xzmax; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    BlockConfig bc = Config.isDropBlock(world, block.getBlockData());
                    if (bc == null) continue;
                    for (BlockFace face : faces) {
                        if (block.getRelative(face).getType().isAir()) {
                            BlockBreakListener.spoofBlockBreak(block.getLocation(), block, breakItem);
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isProcessed(Chunk chunk) {
        return chunk.getPersistentDataContainer().getOrDefault(processedKey, PersistentDataType.BOOLEAN, false);
    }
}
