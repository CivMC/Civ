package vg.civcraft.mc.civmodcore.world.locations.files;

import org.bukkit.World;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class FileCacheSerializer<D extends TableBasedDataObject> {

	public abstract void serialize(DataOutputStream stream, D object) throws IOException;

	public abstract D deserialize(DataInputStream stream, World world, int chunkXOffset, int chunkZOffset) throws IOException;
}
