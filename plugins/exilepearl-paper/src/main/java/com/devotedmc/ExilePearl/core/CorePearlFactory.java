package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.LocationHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Factory class for creating new pearl instances
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
		return new CoreExilePearl(plugin, plugin.getStorage(), uid, killedBy.getUniqueId(), new PlayerHolder(killedBy), health);
	}
}
