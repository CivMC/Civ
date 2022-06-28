package com.untamedears.jukealert;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchQTEntry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.world.locations.SparseQuadTree;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.SingleBlockAPIView;
import vg.civcraft.mc.namelayer.group.Group;

public class SnitchManager {

	private SingleBlockAPIView<Snitch> api;
	private Map<UUID, SparseQuadTree<SnitchQTEntry>> quadTreesByWorld;

	public SnitchManager(SingleBlockAPIView<Snitch> api) {
		this.api = api;
		this.quadTreesByWorld = new TreeMap<>();
	}

	public void shutDown() {
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

}
