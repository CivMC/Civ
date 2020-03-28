package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkDAO;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMetaFactory;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedStorageEngine;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public class ChunkMetaAPI {

	private static Map<String, ChunkMetaView<?>> existingViews = new HashMap<>();

	/**
	 * Allows creating instances of BlockBasedChunkMetaView, which is most likely
	 * what you want if your ChunkMeta is block based
	 * 
	 * @param <T>               BlockBasedChunkMeta subclass
	 * @param <D>               BlockDataObject subclass
	 * @param <S>               StorageEngine subclass
	 * @param plugin            Your plugin
	 * @param emptyChunkCreator Lambda supplying new empty instances of your
	 *                          BlockBasedChunkMeta class
	 * @return API access object for block based chunk metadata
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BlockBasedChunkMeta<D, S>, D extends BlockDataObject<D>, S extends BlockBasedStorageEngine<D>> BlockBasedChunkMetaView<T, D, S> registerBlockBasedPlugin(
			JavaPlugin plugin, Supplier<T> emptyChunkCreator, S storageEngine, boolean allowAccessUnloaded) {
		if (existingViews.containsKey(plugin.getName())) {
			ChunkMetaView<T> chunkMetaView = (ChunkMetaView<T>) existingViews.get(plugin.getName());
			return (BlockBasedChunkMetaView<T, D, S>) chunkMetaView;
		}
		GlobalChunkMetaManager globalManager = CivModCorePlugin.getInstance().getChunkMetaManager();
		if (globalManager == null) {
			plugin.getLogger().log(Level.SEVERE, "Could not start chunk meta data, manager was null");
			return null;
		}
		ChunkDAO chunkDAO = globalManager.getChunkDAO();
		short id = chunkDAO.getOrCreatePluginID(plugin);
		if (id == -1) {
			plugin.getLogger().log(Level.SEVERE, "Could not init chunk meta data, could not retrieve plugin id from db");
			return null;
		}
		if (!storageEngine.stayLoaded()) {
			//if a plugin preloads all data, we don't want to do anything on chunk load/unload
			ChunkMetaFactory metaFactory = ChunkMetaFactory.getInstance();
			metaFactory.registerPlugin(plugin.getName(), id, (Supplier<ChunkMeta<?>>) (Supplier<?>) emptyChunkCreator);
		}
		BlockBasedChunkMetaView<T, D, S> view = new BlockBasedChunkMetaView<>(plugin, id, globalManager,
				emptyChunkCreator, storageEngine.stayLoaded(), allowAccessUnloaded);
		ViewTracker.getInstance().put(view, id);
		existingViews.put(plugin.getName(), view);
		return view;
	}

	static void removePlugin(JavaPlugin plugin) {
		existingViews.remove(plugin.getName());
	}

	/**
	 * Shuts down all active views and saves them to the database. Should only be
	 * called on server shutdown
	 */
	public static void saveAll() {
		// copy keys so we can iterate safely
		List<String> keys = new ArrayList<>(existingViews.keySet());
		for (String key : keys) {
			ChunkMetaView<?> view = existingViews.get(key);
			if (view != null) {
				view.disable();
			}
		}
	}

	private ChunkMetaAPI() {
	}

}
