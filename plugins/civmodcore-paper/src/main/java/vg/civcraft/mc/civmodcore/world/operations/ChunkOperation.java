package vg.civcraft.mc.civmodcore.world.operations;

import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ChunkOperation {

	protected final JavaPlugin plugin;

	public ChunkOperation(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public abstract void process(final Chunk chunk);

}
