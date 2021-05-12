package vg.civcraft.mc.civmodcore.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author caucow ( https://github.com/caucow )
 */
// Maybe some day this class can be made more generic and accept an ExecutorService in a constructor or something.
// For now it works. Ree.
public class SkinCache {

	private final Plugin plugin;
	// TODO: If an easier way to limit the number of threads spawned by Bukkit's scheduler exists, use it.
	// This is dirty. It feels bad.
	private final ExecutorService executor;
	private final BukkitTask watchdog;
	private Thread watchdogThread;
	/**
	 * Caching of PlayerProfiles, should only be accessed directly through
	 * getIfPresent or other non-blocking means. Use futureCache to trigger
	 * loads.
	 */
	private LoadingCache<UUID, SkinData> skinCache;
	/**
	 * Implementation for "notifying" code waiting on a skin to be loaded.
	 * Futures returned are processed on this HeadCache's ExecutorService (for
	 * now unless a better way to limit Bukkit scheduler thread spawning arises)
	 */
	private LoadingCache<UUID, CompletableFuture<SkinData>> futureCache;

	/**
	 * Creates a new SkinCache and initializes its ExecutorService and watchdog bukkit task.
	 *
	 * Always call {@link #shutdown()} before discarding this object (ex if replacing a SkinCache or disabling a plugin)
	 * @param plugin Plugin owning the cache, used to schedule Bukkit sync tasks.
	 * @param downloadThreads Number of threads to use to handle async loading.
	 */
	public SkinCache(Plugin plugin, int downloadThreads) {
		this.plugin = plugin;

		this.skinCache = CacheBuilder.newBuilder()
				.expireAfterWrite(6, TimeUnit.HOURS)
				.build(new CacheLoader<UUID, SkinData>() {
					@Override
					public SkinData load(UUID uuid) throws Exception {
						PlayerProfile profile = Bukkit.createProfile(uuid);
						profile.complete(true);
						if (profile.hasTextures()) {
							return new SkinData(profile);
						}
						throw new Exception("Could not complete() PlayerProfile for " + uuid + " (rate limited or profile doesn't exist)");
					}
				});
		this.futureCache = CacheBuilder.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.build(new CacheLoader<UUID, CompletableFuture<SkinData>>() {
					@Override
					public CompletableFuture<SkinData> load(UUID uuid) throws Exception {
						return CompletableFuture.supplyAsync(() -> {
							try {
								return skinCache.get(uuid);
							} catch (Exception e) {
								return new SkinData(Bukkit.createProfile(uuid));
							}
						}, executor);
					}
				});

		this.executor = Executors.newFixedThreadPool(downloadThreads);
		// because I don't trust plugins to clean up on disable so might as well
		// have something screeching in the logs. not that many plugins try to
		// be reloadable
		this.watchdog = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			watchdogThread = Thread.currentThread();
			do {
				try {
					SkinCache.this.executor.awaitTermination(60, TimeUnit.SECONDS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					shutdown();
					return;
				}
				if (!plugin.isEnabled()) {
					break;
				}
			} while (!SkinCache.this.executor.isShutdown());
			if (SkinCache.this.executor.isShutdown()) {
				return;
			}
			try {
				SkinCache.this.executor.awaitTermination(60, TimeUnit.SECONDS);
				if (!SkinCache.this.executor.isShutdown()) {
					plugin.getLogger().log(Level.WARNING, "Plugin did not shutdown() HeadCache while disabling!");
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			shutdown();
		});
	}

	/**
	 * Shut down the ExecutorService and watchdog task, and cancel any remaining async loads.
	 */
	public void shutdown() {
		watchdog.cancel();
		executor.shutdownNow();
		if (watchdogThread != null) {
			watchdogThread.interrupt();
		}
	}

	/**
	 * @param playerId UUID of player to get head for
	 * @param placeholderSupplier If there is not already a skin available for the given player, use this supplier to
	 *                            generate a placeholder item while the skin for the skull item is loaded.
	 * @param notifyAvailable If a skin must be downloaded or retrieved from the server's usercache, this will be used
	 *                        as a callback when the skin has been loaded and a skull item created.
	 * @return A bare player skull item, or a placeholder if the skin for the provided player ID is not loaded yet.
	 */
	public ItemStack getHeadItem(final UUID playerId, Supplier<ItemStack> placeholderSupplier, final Consumer<ItemStack> notifyAvailable) {
		SkinData skin = skinCache.getIfPresent(playerId);
		if (skin != null) {
			return createHeadItem(skin);
		}
		CompletableFuture<SkinData> metaFuture = futureCache.getUnchecked(playerId);
		metaFuture.thenAccept((asyncMeta) -> {
			if (plugin.isEnabled()) {
				ItemStack headItem = createHeadItem(asyncMeta);
				Bukkit.getScheduler().runTask(plugin, () -> notifyAvailable.accept(headItem));
			}
		});
		return placeholderSupplier.get();
	}

	private ItemStack createHeadItem(SkinData skin) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		item.setItemMeta(skin.headMeta.clone());
		return item;
	}

	private static class SkinData {

		public final PlayerProfile profile;
		public final SkullMeta headMeta;

		public SkinData(PlayerProfile profile) {
			this.profile = profile;
			this.headMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
			headMeta.setPlayerProfile(profile);
		}

	}
}
