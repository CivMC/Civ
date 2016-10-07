package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.BorderHandler;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLoreProvider;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.config.DocumentConfig;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Factory class for creating new core class instances
 * @author Gordon
 */
public class CorePluginFactory implements PearlFactory {
	
	private final ExilePearlPlugin plugin;
	
	/**
	 * Creates a new ExilePearlFactory instance
	 * @param plugin The plugin instance
	 */
	public CorePluginFactory(final ExilePearlPlugin plugin) {
		Guard.ArgumentNotNull(plugin, "plugin");
		
		this.plugin = plugin;
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, UUID killedBy, int pearlId, Location location) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		Guard.ArgumentNotNull(location, "location");
		
		PearlHolder holder = new BlockHolder(location.getBlock());

		return new CoreExilePearl(plugin, plugin.getStorageProvider().getStorage(), uid, killedBy, pearlId, holder);
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		
		ExilePearl pearl = new CoreExilePearl(plugin, plugin.getStorageProvider().getStorage(), uid, killedBy.getUniqueId(), pearlId, new PlayerHolder(killedBy));
		pearl.enableStorage();
		return pearl;
	}

	public PearlManager createPearlManager() {
		return new CorePearlManager(plugin, this, plugin.getStorageProvider());
	}

	public ExilePearlRunnable createPearlDecayWorker() {
		return new PearlDecayTask(plugin);
	}

	public SuicideHandler createSuicideHandler() {
		return new PlayerSuicideTask(plugin);
	}

	public BorderHandler createPearlBorderHandler() {
		return new PearlBoundaryTask(plugin);
	}

	public PearlPlayer createPearlPlayer(UUID uid) {
		return new CorePearlPlayer(uid, plugin, plugin);
	}

	public PearlLoreProvider createLoreGenerator() {
		return new CoreLoreGenerator(plugin.getPearlConfig());
	}

	public PearlConfig createPearlConfig() {
		return new CorePearlConfig(plugin);
	}
}
