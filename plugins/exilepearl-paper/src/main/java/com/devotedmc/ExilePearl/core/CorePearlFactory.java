package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PearlWorker;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.LocationHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
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
	public ExilePearl createExilePearl(UUID uid, UUID killedBy, Location location, double health) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		Guard.ArgumentNotNull(location, "location");
		
		PearlHolder holder;
		
		if (location.getBlock().getState() instanceof InventoryHolder) {
			holder = new BlockHolder(location.getBlock());
		} else {
			holder = new LocationHolder(location);
		}

		return new CoreExilePearl(plugin, plugin.getStorage(), uid, killedBy, holder, health);
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, double health) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		
		ExilePearl pearl = new CoreExilePearl(plugin, plugin.getStorage(), uid, killedBy.getUniqueId(), new PlayerHolder(killedBy), health);
		pearl.enableStorage();
		return pearl;
	}

	@Override
	public PearlManager createPearlManager() {
		return new CorePearlManager(plugin, this, plugin.getStorage(), plugin.getPearlConfig());
	}

	@Override
	public PearlWorker createPearlWorker() {
		return new CorePearlWorker(plugin, plugin.getPearlConfig());
	}

	@Override
	public PearlPlayer createPearlPlayer(Player player) {
		return new CorePearlPlayer(player, plugin);
	}
}
