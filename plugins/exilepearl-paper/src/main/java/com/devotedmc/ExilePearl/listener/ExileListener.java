package com.devotedmc.ExilePearl.listener;

import org.bukkit.event.Listener;

import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Listener for disallowing certain actions of exiled players
 * @author Gordon
 * 
 * Loss of privileges (for Devoted) when exiled, each of these needs to be toggleable in the config:
 * Cannot break reinforced blocks. (Citadel can stop damage, use that)
 * Cannot break bastions by placing blocks. (Might need bastion change)
 * Cannot throw ender pearls at all. 
 * Cannot enter a bastion field they are not on. Same teleport back feature as world border.
 * Cannot do damage to other players.
 * Cannot light fires.
 * Cannot light TNT.
 * Cannot chat in local chat. Given a message suggesting chatting in a group chat in Citadel.
 * Cannot use water or lava buckets.
 * Cannot use any potions.
 * Cannot set a bed.
 * Cannot enter within 1k of their ExilePearl. Same teleport back feature as world border.
 * Can use a /suicide command after a 180 second timeout. (In case they get stuck in a reinforced box).
 * Cannot place snitch or note-block.
 * Exiled players can still play, mine, enchant, trade, grind, and explore.
 *
 */
public class ExileListener implements Listener {

	private final PearlLogger logger;
	private final PearlManager pearls;
	private final ExilePearlConfig config;
	
	/**
	 * Creates a new ExileListener instance
	 * @param logger The logger instance
	 * @param pearls The pearl manger
	 * @param config The plugin configuration
	 */
	public ExileListener(final PearlLogger logger, final PearlManager pearls, final ExilePearlConfig config) {
		Guard.ArgumentNotNull(logger, "logger");
		Guard.ArgumentNotNull(pearls, "pearls");
		Guard.ArgumentNotNull(config, "config");
		
		this.logger = logger;
		this.pearls = pearls;
		this.config = config;
	}
}
