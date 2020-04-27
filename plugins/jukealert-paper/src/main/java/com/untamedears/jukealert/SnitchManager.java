package com.untamedears.jukealert;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchQTEntry;

import vg.civcraft.mc.civmodcore.locations.SparseQuadTree;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.SingleBlockAPIView;

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

	public void addSnitchToQuadTree(Snitch snitch) {
		SparseQuadTree<SnitchQTEntry> quadTree = getQuadTreeFor(snitch.getLocation());
		for (SnitchQTEntry qt : snitch.getFieldManager().getQTEntries()) {
			quadTree.add(qt);
		}
	}

	private SparseQuadTree<SnitchQTEntry> getQuadTreeFor(Location loc) {
		return quadTreesByWorld.computeIfAbsent(loc.getWorld().getUID(), u -> new SparseQuadTree<>(1));
	}

	/**
	 * Removes the given snitch from the QtBox field tracking and the per chunk
	 * block data tracking.
	 * 
	 * Removal from culling timers has to be done outside this call
	 * 
	 * @param snitch Snitch to remove
	 */
	public void removeSnitch(Snitch snitch) {
		api.remove(snitch);
		SparseQuadTree<SnitchQTEntry> quadTree = getQuadTreeFor(snitch.getLocation());
		for (SnitchQTEntry qt : snitch.getFieldManager().getQTEntries()) {
			quadTree.remove(qt);
		}
	}

	public Set<Snitch> getSnitchesCovering(Location location) {
		return getQuadTreeFor(location).find(location.getBlockX(), location.getBlockZ(), true).stream().map(SnitchQTEntry::getSnitch)
				.filter(s -> s.getFieldManager().isInside(location)).collect(Collectors.toSet());
	}

}
