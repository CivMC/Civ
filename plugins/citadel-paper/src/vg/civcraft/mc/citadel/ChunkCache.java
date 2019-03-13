package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Location;

import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class ChunkCache {

	private final ChunkPair chunkPair;
	private Map<Coords, Reinforcement> reinforcements;
	private List<Reinforcement> deletedReinforcements;
	private boolean isDirty;
	private final int worldID;

	public ChunkCache(ChunkPair chunkPair, Collection<Reinforcement> reins, int worldID) {
		this.reinforcements = new TreeMap<>();
		for (Reinforcement rein : reins) {
			reinforcements.put(new Coords(rein.getLocation()), rein);
		}
		this.chunkPair = chunkPair;
		this.worldID = worldID;
		this.isDirty = false;
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
		reinforcements.put(key, rein);
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

	public ChunkPair getChunkPair() {
		return chunkPair;
	}

	public Reinforcement getReinforcement(int x, int y, int z) {
		return reinforcements.get(new Coords(x, y, z));
	}

	public Collection<Reinforcement> getAllAndCleanUp() {
		List<Reinforcement> reins = new ArrayList<>();
		reins.addAll(deletedReinforcements);
		reins.addAll(reinforcements.values());
		deletedReinforcements.clear();
		return reins;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean dirty) {
		this.isDirty = dirty;
	}

	private class Coords implements Comparable<Coords> {

		private int x;
		private int y;
		private int z;

		Coords(Location loc) {
			this.x = loc.getBlockX();
			this.y = loc.getBlockY();
			this.z = loc.getBlockZ();
		}

		Coords(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
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
}
