package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLoreGenerator;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Factory class for creating new core class instances
 * @author Gordon
 */
public class CorePearlFactory implements PearlFactory {
	
	private final ExilePearlPlugin plugin;
	
	/**
	 * Creates a new ExilePearlFactory instance
	 * @param plugin The plugin instance
	 */
	public CorePearlFactory(final ExilePearlPlugin plugin) {
		Guard.ArgumentNotNull(plugin, "plugin");
		
		this.plugin = plugin;
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, String killedByName, int pearlId, Location location) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedByName, "killedByName");
		Guard.ArgumentNotNull(location, "location");
		
		PearlHolder holder = new BlockHolder(location.getBlock());

		return new CoreExilePearl(plugin, plugin.getStorage(), uid, killedByName, pearlId, holder);
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		
		ExilePearl pearl = new CoreExilePearl(plugin, plugin.getStorage(), uid, plugin.getPearlPlayer(killedBy).getName(), pearlId, new PlayerHolder(killedBy));
		pearl.enableStorage();
		return pearl;
	}

	@Override
	public PearlManager createPearlManager() {
		return new CorePearlManager(plugin, this, plugin.getStorage());
	}

	@Override
	public ExilePearlRunnable createPearlDecayWorker() {
		return new PearlDecayTask(plugin);
	}

	@Override
	public SuicideHandler createSuicideHandler() {
		return new PlayerSuicideTask(plugin);
	}

	@Override
	public ExilePearlRunnable createPearlBorderTask() {
		return new PearlBoundaryTask(plugin);
	}

	@Override
	public PearlPlayer createPearlPlayer(UUID uid) {
		return new CorePearlPlayer(uid, plugin, plugin);
	}

	@Override
	public PearlLoreGenerator createLoreGenerator() {
		return new CoreLoreGenerator();
	}

	@Override
	public PearlConfig createPearlConfig() {
		return new CorePearlConfig(plugin);
	}
}
