package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.ChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.ChunkMetaFactory;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedStorageEngine;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto.AutoBlockChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto.AutoStorageEngine;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto.SerializableDataObject;
import vg.civcraft.mc.civmodcore.world.locations.global.CMCWorldDAO;
import vg.civcraft.mc.civmodcore.world.locations.global.GlobalLocationTracker;
import vg.civcraft.mc.civmodcore.world.locations.global.GlobalTrackableDAO;
import vg.civcraft.mc.civmodcore.world.locations.global.LocationTrackable;

public class ChunkMetaAPI {

	private static Map<String, APIView> existingViews = new HashMap<>();

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
	public static <T extends BlockBasedChunkMeta<D, S>, D extends BlockDataObject<D>, S extends BlockBasedStorageEngine<D>> BlockBasedChunkMetaView<T, D, S> registerBlockBasedPlugin(
			JavaPlugin plugin, Supplier<T> emptyChunkCreator, S storageEngine, boolean allowAccessUnloaded) {
		return registerBlockBasedPlugin(plugin, plugin.getName(), emptyChunkCreator, storageEngine, allowAccessUnloaded);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BlockBasedChunkMeta<D, S>, D extends BlockDataObject<D>, S extends BlockBasedStorageEngine<D>> BlockBasedChunkMetaView<T, D, S> registerBlockBasedPlugin(
			JavaPlugin plugin, String identifier, Supplier<T> emptyChunkCreator, S storageEngine, boolean allowAccessUnloaded) {
		if (existingViews.containsKey(plugin.getName())) {
			ChunkMetaView<T> chunkMetaView = (ChunkMetaView<T>) existingViews.get(plugin.getName());
			return (BlockBasedChunkMetaView<T, D, S>) chunkMetaView;
		}
		GlobalChunkMetaManager globalManager = CivModCorePlugin.getInstance().getChunkMetaManager();
		if (globalManager == null) {
			plugin.getLogger().log(Level.SEVERE, "Could not start chunk meta data, manager was null");
			return null;
		}
		CMCWorldDAO chunkDAO = globalManager.getChunkDAO();
		short id = chunkDAO.getOrCreatePluginID(identifier);
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
				emptyChunkCreator, storageEngine, storageEngine.stayLoaded(), allowAccessUnloaded);
		ChunkMetaViewTracker.getInstance().put(view, id);
		existingViews.put(identifier, view);
		return view;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BlockBasedChunkMeta<D, S>, D extends SerializableDataObject<D>, S extends AutoStorageEngine<D>> BlockBasedChunkMetaView<T, D, S> registerAutoBlockBasedPlugin(
			JavaPlugin plugin, String identifier, S storageEngine, boolean allowAccessUnloaded) {
		return (BlockBasedChunkMetaView<T, D, S>) registerBlockBasedPlugin(plugin, identifier, () -> new AutoBlockChunkMeta<D>(storageEngine),storageEngine, allowAccessUnloaded);
	}
	
	public static <T extends LocationTrackable> SingleBlockAPIView<T> registerSingleTrackingPlugin(JavaPlugin plugin, GlobalTrackableDAO<T> dao) {
		GlobalChunkMetaManager globalManager = CivModCorePlugin.getInstance().getChunkMetaManager();
		if (globalManager == null) {
			plugin.getLogger().log(Level.SEVERE, "Could not start chunk meta data, manager was null");
			return null;
		}
		CMCWorldDAO chunkDAO = globalManager.getChunkDAO();
		short id = chunkDAO.getOrCreatePluginID(plugin);
		if (id == -1) {
			plugin.getLogger().log(Level.SEVERE, "Could not init single block meta data, could not retrieve plugin id from db");
			return null;
		}
		GlobalLocationTracker<T> tracker = new GlobalLocationTracker<>(dao);
		SingleBlockAPIView<T> view = new SingleBlockAPIView<>(plugin, id, tracker);
		existingViews.put(plugin.getName(), view);
		ChunkMetaViewTracker.getInstance().put(view);
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
			APIView view = existingViews.get(key);
			if (view != null) {
				view.disable();
			}
		}
	}

	private ChunkMetaAPI() {
	}

}
