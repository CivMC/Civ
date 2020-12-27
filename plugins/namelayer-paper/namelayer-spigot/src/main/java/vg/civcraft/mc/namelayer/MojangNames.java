package vg.civcraft.mc.namelayer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.MapUtils;
import vg.civcraft.mc.namelayer.listeners.AssociationListener;

public final class MojangNames {

	private static final Map<String, UUID> PROFILES = Collections.synchronizedMap(
			new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
	private static final String PROFILES_FILE = "mojang.dat";
	private static final long SAVE_DELAY = 20 * 60; // 60 seconds' worth of ticks
	private static BukkitTask SAVE_TASK;

	public static void init(final NameLayerPlugin plugin) {
		final Path mojangFile = plugin.getResourceFile(PROFILES_FILE).toPath();
		// Load all the profiles that already exist
		Bukkit.getScheduler().runTaskAsynchronously(
				plugin, () -> load(plugin, mojangFile));
		// Set up a process of saving profiles
		SAVE_TASK = Bukkit.getScheduler().runTaskTimerAsynchronously(
				plugin, () -> save(plugin, mojangFile), SAVE_DELAY, SAVE_DELAY);
	}

	public static void reset(final NameLayerPlugin plugin) {
		if (!PROFILES.isEmpty()) {
			save(plugin, plugin.getResourceFile(PROFILES_FILE).toPath());
			PROFILES.clear();
		}
		if (SAVE_TASK != null) {
			SAVE_TASK.cancel();
			SAVE_TASK = null;
		}
	}

	private static void load(final NameLayerPlugin plugin, final Path file) {
		PROFILES.clear();
		try {
			final byte[] data = Files.readAllBytes(file);
			final NBTCompound nbt = NBTCompound.fromBytes(data);
			nbt.getKeys().forEach(key -> PROFILES.put(key, nbt.getUUID(key)));
			plugin.info("[MojangNames] Mojang profiles loaded!");
		}
		catch (final NoSuchFileException ignored) {}
		catch (final IOException exception) {
			plugin.warning("[MojangNames] Could not load Mojang profiles!", exception);
		}
	}

	private static void save(final NameLayerPlugin plugin, final Path file) {
		final NBTCompound nbt = new NBTCompound();
		PROFILES.forEach((name, uuid) -> nbt.setUUID(name, uuid)); // Ignore highlighter
		final byte[] data = NBTCompound.toBytes(nbt);
		try {
			Files.write(file, data,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE);
		}
		catch (final IOException exception) {
			plugin.warning("[MojangNames] Could not save Mojang profiles!", exception);
			return;
		}
		plugin.info("[MojangNames] Mojang profiles saved!");
	}

	/**
	 * Returns the player's uuid based on their Mojang name.
	 *
	 * @param name The player's Mojang name.
	 * @return Returns the player's uuid, or null.
	 */
	public static UUID getMojangUuid(final String name) {
		if (Strings.isNullOrEmpty(name)) {
			return null;
		}
		return PROFILES.get(name);
	}

	/**
	 * <p>Returns the player's Mojang name based on their uuid.</p>
	 *
	 * <p>Note: The name will be lowercase.</p>
	 *
	 * @param uuid The player's uuid.
	 * @return Returns the player's Mojang name, or null.
	 */
	public static String getMojangName(final UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return MapUtils.getKeyFromValue(PROFILES, uuid);
	}

	/**
	 * DO NOT USE THIS ANYWHERE OTHER THAN {@link AssociationListener#OnPlayerLogin(PlayerLoginEvent)}
	 *
	 * @param uuid The player's uuid.
	 * @param name The player's Mojang name.
	 */
	public static void declareMojangName(final UUID uuid, final String name) {
		Preconditions.checkNotNull(uuid);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		PROFILES.put(name, uuid);
	}

}
