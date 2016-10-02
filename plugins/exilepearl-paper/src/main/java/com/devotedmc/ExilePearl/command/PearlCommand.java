package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlPlayer;

public abstract class PearlCommand extends BaseCommand<ExilePearlPlugin> {
	
	protected final ExilePearlApi pearlApi;
	
	public PearlCommand(ExilePearlPlugin plugin) {
		super(plugin);
		
		pearlApi = plugin;
	}
	

	
	/**
	 * Gets the sender instance
	 * @return The sender instance
	 */
	protected PearlPlayer me() {
		return plugin.getPearlPlayer(player().getUniqueId());
	}
}
