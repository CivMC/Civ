package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class ChunkMetaListener implements Listener {

	private GlobalChunkMetaManager manager;
	// unloading is offloaded to another thread at this level, because it requires
	// inserting the ChunkCoord into the unloading queue, which requires the
	// executing thread to acquire the lock on the unloading queue. During unloading
	// queue cleanup this monitor may be unavailable for a longer period of time due
	// to the sync db writes executed as a part of it, so the thread inserting
	// unloaded chunks may have to wait for the unloading queue cleanup to finish.
	// We don't want to block the main thread, so we use a different one for this
	private Thread unloadConsumer;
	private Queue<Chunk> unloadQueue;

	public ChunkMetaListener(GlobalChunkMetaManager manager) {
		this.manager = manager;
		this.unloadQueue = new LinkedBlockingQueue<>();
		unloadConsumer = new Thread(() -> {
			while (true) {
				synchronized (unloadQueue) {
					while (unloadQueue.isEmpty()) {
						try {
							unloadQueue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// usually it'd be bad to exit the monitor block here, but due to only having
					// one consumer and wanting to avoid the problem described above, we need to do
					// this
					Chunk chunk = unloadQueue.poll();
					if (chunk == null) {
						// should never happen, but eh
						continue;
					}
					manager.unloadChunkData(chunk);
				}
			}
		});
		unloadConsumer.start();
	}

	// LOWEST = we go first
	@EventHandler(priority = EventPriority.LOWEST)
	public void chunkLoad(ChunkLoadEvent e) {
		manager.loadChunkData(e.getChunk());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void chunkUnload(ChunkUnloadEvent e) {
		synchronized (unloadQueue) {
			unloadQueue.add(e.getChunk());
			unloadQueue.notifyAll();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void worldLoad(WorldLoadEvent e) {
		manager.registerWorld(e.getWorld());
	}

}
