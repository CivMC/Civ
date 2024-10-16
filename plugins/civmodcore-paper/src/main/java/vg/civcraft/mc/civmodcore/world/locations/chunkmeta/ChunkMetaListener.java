package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaViewTracker;
import vg.civcraft.mc.civmodcore.world.locations.global.WorldIDManager;

public class ChunkMetaListener implements Listener {

	private final GlobalChunkMetaManager manager;
	private final ChunkMetaViewTracker viewTracker;
	// unloading is offloaded to another thread at this level, because it requires
	// inserting the ChunkCoord into the unloading queue, which requires the
	// executing thread to acquire the lock on the unloading queue. During unloading
	// queue cleanup this monitor may be unavailable for a longer period of time due
	// to the sync db writes executed as a part of it, so the thread inserting
	// unloaded chunks may have to wait for the unloading queue cleanup to finish.
	// We don't want to block the main thread, so we use a different one for this
	private Thread unloadConsumer;
	private final LinkedBlockingQueue<Chunk> unloadQueue;

	public ChunkMetaListener(GlobalChunkMetaManager manager, ChunkMetaViewTracker viewTracker) {
		this.manager = manager;
		this.viewTracker = viewTracker;
		this.unloadQueue = new LinkedBlockingQueue<>();
		unloadConsumer = new Thread(() -> {
			while (true) {
				try {
					Chunk chunk = unloadQueue.take();
					manager.unloadChunkData(chunk);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "CivModCore chunk unload handler");
		unloadConsumer.start();
	}

	// LOWEST = we go first
	@EventHandler(priority = EventPriority.LOWEST)
	public void chunkLoad(ChunkLoadEvent e) {
		manager.loadChunkData(e.getChunk());
		viewTracker.applyToAllSingleBlockViews(s -> s.handleChunkLoad(e.getChunk()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void chunkUnload(ChunkUnloadEvent e) {
		unloadQueue.add(e.getChunk());
		viewTracker.applyToAllSingleBlockViews(s -> s.handleChunkUnload(e.getChunk()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void worldLoad(WorldLoadEvent e) {
		WorldIDManager idManager = CivModCorePlugin.getInstance().getWorldIdManager();
		if (!idManager.registerWorld(e.getWorld())) {
			CivModCorePlugin.getInstance().getLogger().severe("Failed to initialize world tracking");
			return;
		}
		CivModCorePlugin.getInstance().getChunkMetaManager().registerWorld(idManager.getInternalWorldId(e.getWorld()),
				e.getWorld());
	}

}
