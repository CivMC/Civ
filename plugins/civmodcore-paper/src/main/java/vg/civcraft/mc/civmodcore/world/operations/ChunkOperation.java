package vg.civcraft.mc.civmodcore.world.operations;

import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ChunkOperation {

	protected final JavaPlugin plugin;
	protected final Logger logger;

	public ChunkOperation(final JavaPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}

	public abstract void process(final Chunk chunk);

}
