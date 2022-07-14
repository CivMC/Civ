package com.untamedears.jukealert;

import com.untamedears.jukealert.database.JukeAlertDAO;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchQTEntry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.world.locations.SparseQuadTree;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.SingleBlockAPIView;
import vg.civcraft.mc.namelayer.group.Group;

public class SnitchManager {
	private static final long SAVE_LOGS_INTERVAL_MS = 60L * 1000L; // 1 min

	private record LogItem(int internalActionId, Snitch snitch, LoggablePlayerAction action){}

	private final SingleBlockAPIView<Snitch> api;
	private final Map<UUID, SparseQuadTree<SnitchQTEntry>> quadTreesByWorld;
	private final ScheduledExecutorService scheduler;
	private final ConcurrentLinkedQueue<LogItem> logs;

	public SnitchManager(SingleBlockAPIView<Snitch> api) {
		this.api = api;
		this.quadTreesByWorld = new TreeMap<>();
		this.scheduler = Executors.newScheduledThreadPool(1);
		this.logs = new ConcurrentLinkedQueue<>();
	}

	public void enable() {
		scheduler.scheduleWithFixedDelay(() -> {
			saveLogsToDB();
		}, SAVE_LOGS_INTERVAL_MS, SAVE_LOGS_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	public void shutdown() {
		scheduler.shutdown();

		try {
			if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))
				scheduler.shutdownNow();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		JukeAlert.getInstance().getLogger().info("SnitchManager scheduler and its tasks are shutdown.");

		saveLogsToDB();

		JukeAlert.getInstance().getLogger().info("Snitch logs are saved.");

		api.disable();
	}

	public Snitch getSnitchAt(Location location) {
		return api.get(location);
	}

	public Snitch getSnitchAt(Block block) {
		return api.get(block.getLocation());
	}

	public void addSnitch(Snitch snitch) {
		api.put(snitch);
		addSnitchToQuadTree(snitch);
	}

	public void renameSnitch(Snitch snitch, String newName) {
		snitch.setName(newName);
		api.put(snitch);
	}

	public void setSnitchGroup(Snitch snitch, Group group) {
		snitch.setGroup(group);
		api.put(snitch);
	}

	public void addSnitchToQuadTree(Snitch snitch) {
		SparseQuadTree<SnitchQTEntry> quadTree = getQuadTreeFor(snitch.getLocation());
		for (SnitchQTEntry qt : snitch.getFieldManager().getQTEntries()) {
			quadTree.add(qt);
		}
	}

	private SparseQuadTree<SnitchQTEntry> getQuadTreeFor(Location loc) {
		SparseQuadTree<SnitchQTEntry> tree = quadTreesByWorld.get(loc.getWorld().getUID());
		if (tree == null) {
			JukeAlert.getInstance().getLogger().info("Quad tree for world  " + loc.getWorld().getUID() + " does not exist, creating");
			tree = new SparseQuadTree<>(1);
			quadTreesByWorld.put(loc.getWorld().getUID(), tree);
		}
		return tree;
	}

	/**
	 * Removes the given snitch from the QtBox field tracking and the per chunk
	 * block data tracking.
	 * 
	 * Removal from culling timers has to be done outside this call
	 * 
	 * @param snitch Snitch to remove
	 */
	public void removeSnitch(@Nonnull final Snitch snitch) {
		snitch.setCacheState(CacheState.DELETED);
		this.api.remove(snitch);
		final SparseQuadTree<SnitchQTEntry> quadTree = getQuadTreeFor(snitch.getLocation());
		for (final SnitchQTEntry qt : snitch.getFieldManager().getQTEntries()) {
			quadTree.remove(qt);
		}
	}

	public Set<Snitch> getSnitchesCovering(Location location) {
		Set <SnitchQTEntry> entries = getQuadTreeFor(location).find(location.getBlockX(), location.getBlockZ(), true);
		Set<Snitch> result = new HashSet<>();
		for(SnitchQTEntry qt : entries) {
			if (qt.getSnitch().getFieldManager().isInside(location)) {
				result.add(qt.getSnitch());
			}
		}
		Iterator<Snitch> iter = result.iterator();
		while (iter.hasNext()) {
			Snitch s = iter.next();
			if (!s.checkPhysicalIntegrity()) {
				iter.remove();
			}
		}
 		return result;
	}

	public void saveLog(int internalActionId, Snitch snitch, LoggablePlayerAction action) {
		logs.add(new LogItem(internalActionId, snitch, action));
	}

	private void saveLogsToDB() {
		JukeAlertDAO dao = JukeAlert.getInstance().getDAO();

		LogItem logItem;
		while ((logItem = logs.poll()) != null) {
			final int actionID = dao.insertLog(logItem.internalActionId, logItem.snitch, logItem.action.getPersistence());
			logItem.action.setID(actionID);
			logItem.action.setCacheState(ActionCacheState.NORMAL);
		}
	}
}
