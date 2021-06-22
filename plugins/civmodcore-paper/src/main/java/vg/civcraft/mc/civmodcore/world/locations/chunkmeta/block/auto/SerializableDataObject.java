package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto;

import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockDataObject;

public abstract class SerializableDataObject <D extends SerializableDataObject<D>> extends BlockDataObject<D> {
	
	public SerializableDataObject(Location location, boolean isNew) {
		super(location, isNew);
	}

	public abstract String serialize();

}
