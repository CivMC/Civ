package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;

public abstract class PearlCommand extends BaseCommand<ExilePearlPlugin> {
	
	protected final PearlManager pearls;
	
	public PearlCommand(ExilePearlPlugin plugin) {
		super(plugin);
		
		pearls = plugin.getPearlManager();
	}
	

	
	/**
	 * Gets the sender instance
	 * @return The sender instance
	 */
	protected PearlPlayer me() {
		return plugin.getPearlPlayer(super.player().getUniqueId());
	}
}
