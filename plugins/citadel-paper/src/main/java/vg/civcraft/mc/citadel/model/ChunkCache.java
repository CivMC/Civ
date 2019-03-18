package vg.civcraft.mc.citadel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Location;

public class ChunkCache {

	private class Coords implements Comparable<Coords> {

		private int x;
		private int y;
		private int z;

		Coords(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		Coords(Location loc) {
			this.x = loc.getBlockX();
			this.y = loc.getBlockY();
			this.z = loc.getBlockZ();
		}

		public int compareTo(Coords coords) {
			// y first because we have the most variety here
			if (coords.y != y) {
				return Integer.compare(y, coords.y);
			}
			if (coords.x != x) {
				return Integer.compare(x, coords.x);
			}
			if (coords.z != z) {
				return Integer.compare(z, coords.z);
			}
			return 0;
		}

		public boolean equals(Object o) {
			Coords coords = (Coords) o;
			return coords.y == y && coords.x == x && coords.z == z;
		}
	}

	private final ChunkCoord chunkPair;
	private Map<Coords, Reinforcement> reinforcements;
	private List<Reinforcement> deletedReinforcements;
	private boolean isDirty;

	private final int worldID;

	public ChunkCache(ChunkCoord chunkPair, Collection<Reinforcement> reins, int worldID) {
		this.reinforcements = new TreeMap<>();
		for (Reinforcement rein : reins) {
			rein.setOwningCache(this);
			reinforcements.put(new Coords(rein.getLocation()), rein);
		}
		this.chunkPair = chunkPair;
		this.worldID = worldID;
		this.isDirty = false;
	}

	/**
	 * Gets all existing reinforcements within this chunk
	 * 
	 * @return All reinforcements
	 */
	public Collection<Reinforcement> getAll() {
		List<Reinforcement> reins = new ArrayList<>();
		reins.addAll(reinforcements.values());
		return reins;
	}

	/**
	 * Used when dumping all reinforcements to the database. Returns not only the
	 * existing reinforcements, but also the ones deleted and not yet removed from
	 * the database
	 * 
	 * @return All reinforcements possibly needing to be persisted to the database
	 */
	public Collection<Reinforcement> getAllAndCleanUp() {
		List<Reinforcement> reins = new ArrayList<>();
		if (deletedReinforcements != null) {
			reins.addAll(deletedReinforcements);
			deletedReinforcements.clear();
		}
		reins.addAll(reinforcements.values());
		return reins;
	}

	public ChunkCoord getChunkPair() {
		return chunkPair;
	}

	public Reinforcement getReinforcement(int x, int y, int z) {
		return reinforcements.get(new Coords(x, y, z));
	}

	public int getWorldID() {
		return worldID;
	}

	public void insertReinforcement(Reinforcement rein) {
		Coords key = new Coords(rein.getLocation());
		if (reinforcements.containsKey(key)) {
			throw new IllegalStateException(
					"Trying to insert reinforcement at " + rein.getLocation().toString() + ", but one already existed");
		}
		rein.setOwningCache(this);
		reinforcements.put(key, rein);
		if (rein.isDirty) {
			this.isDirty = true;
		}
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void removeReinforcement(Reinforcement rein) {
		Coords key = new Coords(rein.getLocation());
		Reinforcement removed = reinforcements.remove(key);
		if (removed != rein) {
			throw new IllegalStateException("Removed wrong reinforcement at " + rein.getLocation().toString());
		}
		if (deletedReinforcements == null) {
			deletedReinforcements = new LinkedList<>();
		}
		deletedReinforcements.add(rein);
	}

	public void setDirty(boolean dirty) {
		this.isDirty = dirty;
	}
}
