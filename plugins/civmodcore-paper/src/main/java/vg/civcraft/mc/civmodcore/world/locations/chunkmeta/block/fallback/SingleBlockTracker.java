package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.fallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockDataObject;

public class SingleBlockTracker<D extends BlockDataObject<D>> {

	private Map<XZWCoord, Map<Location, D>> blocks;

	public SingleBlockTracker() {
		blocks = new HashMap<>();
	}

	public void putBlock(D data, short worldID) {
		Map<Location, D> existing = blocks.computeIfAbsent(toCoord(data.getLocation(), worldID), s -> new HashMap<>());
		existing.put(data.getLocation(), data);
	}

	public D getBlock(Location location, short worldID) {
		Map<Location, D> existing = blocks.get(toCoord(location, worldID));
		if (existing == null) {
			return null;
		}
		return existing.get(location);
	}

	public D removeBlock(Location location, short worldID) {
		Map<Location, D> existing = blocks.get(toCoord(location, worldID));
		if (existing == null) {
			return null;
		}
		return existing.remove(location);
	}

	public Collection<D> getAllForChunkAndRemove(XZWCoord chunk) {
		Map<Location, D> removed = blocks.remove(chunk);
		if (removed == null) {
			return Collections.emptyList();
		}
		return removed.values();
	}

	private static XZWCoord toCoord(Location location, short worldID) {
		return new XZWCoord(BlockBasedChunkMeta.toChunkCoord(location.getBlockX()),
				BlockBasedChunkMeta.toChunkCoord(location.getBlockZ()), worldID);
	}

	public Collection<D> getAll() {
		List<D> result = new ArrayList<>();
		for (Map<Location, D> chunkMap : blocks.values()) {
			for (D data : chunkMap.values()) {
				result.add(data);
			}
		}
		return result;
	}

}
