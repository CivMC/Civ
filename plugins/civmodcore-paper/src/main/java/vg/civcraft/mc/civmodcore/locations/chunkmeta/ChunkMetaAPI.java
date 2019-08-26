package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class ChunkMetaAPI {

	private ChunkMetaAPI() {
	}

	private static Map<String, ChunkMetaView<? extends ChunkMeta>> existingViews = new HashMap<>();

	/**
	 * Access method to the entire chunk metadata API. You can use this method once
	 * per plugin to obtain an access object for metadata created by your plugin
	 * 
	 * @param <T>            Metadata class for chunks
	 * @param plugin         Plugin to get instance for
	 * @param chunkMetaClass Class object for metadata class
	 * @return Access object for the given plugins meta data or null if something
	 *         went wrong
	 */
	public static <T extends ChunkMeta> ChunkMetaView<T> registerPlugin(JavaPlugin plugin, Class<T> chunkMetaClass) {
		return registerPluginInternal(plugin, chunkMetaClass, (id, man) -> new ChunkMetaView<T>(plugin, id, man));
	}

	private static <T extends ChunkMeta> ChunkMetaView<T> registerPluginInternal(JavaPlugin plugin,
			Class<T> chunkMetaClass,
			BiFunction<Integer, GlobalChunkMetaManager, ? extends ChunkMetaView<T>> metaViewSupplier) {
		if (existingViews.containsKey(plugin.getName())) {
			@SuppressWarnings("unchecked")
			ChunkMetaView<T> chunkMetaView = (ChunkMetaView<T>) existingViews.get(plugin.getName());
			return chunkMetaView;
		}
		GlobalChunkMetaManager globalManager = CivModCorePlugin.getInstance().getChunkMetaManager();
		if (globalManager == null) {
			return null;
		}
		ChunkDAO chunkDAO = globalManager.getChunkDAO();
		int id = chunkDAO.getOrCreatePluginID(plugin);
		if (id == -1) {
			return null;
		}
		ChunkMetaFactory metaFactory = globalManager.getChunkMetaFactory();
		if (!metaFactory.registerPlugin(plugin.getName(), id, chunkMetaClass)) {
			return null;
		}
		ChunkMetaView<T> view = metaViewSupplier.apply(id, globalManager);
		existingViews.put(plugin.getName(), view);
		return view;
	}

	/**
	 * Allows creating instances of BlockBasedChunkMetaView, which is most likely
	 * what you want if your ChunkMeta is block based
	 * 
	 * @param <T>               BlockBasedChunkMeta subclass
	 * @param <D>               BlockDataObject subclass
	 * @param plugin            Your plugin
	 * @param chunkMetaClass    BlockBasedChunkMeta subclass class object
	 * @param emptyChunkCreator Lambda supplying new empty instances of your
	 *                          BlockBasedChunkMeta class
	 * @return API access object for block based chunk metadata
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BlockBasedChunkMeta<D>, D extends BlockDataObject> BlockBasedChunkMetaView<T, D> registerBlockBasedPlugin(
			JavaPlugin plugin, Class<T> chunkMetaClass, Supplier<T> emptyChunkCreator) {
		return (BlockBasedChunkMetaView<T, D>) registerPluginInternal(plugin, chunkMetaClass,
				(id, man) -> new BlockBasedChunkMetaView<T, D>(plugin, id, man) {

					@Override
					public T getEmptyNewChunkCache() {
						return emptyChunkCreator.get();
					}
				});
	}

	/**
	 * Shuts down all active views and saves them to the database. Should only be
	 * called on server shutdown
	 */
	public static void saveAll() {
		// copy keys so we can iterate safely
		List<String> keys = new LinkedList<>(existingViews.keySet());
		for (String key : keys) {
			ChunkMetaView<? extends ChunkMeta> view = existingViews.get(key);
			if (view != null) {
				view.disable();
			}
		}
	}

	static void removePlugin(JavaPlugin plugin) {
		existingViews.remove(plugin.getName());
	}

}
