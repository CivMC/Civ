package com.devotedmc.ExilePearl.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.event.ExilePearlEvent;
import com.devotedmc.ExilePearl.event.ExilePearlEvent.Type;
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

	private final ExilePearlApi pearlApi;
	private final PearlConfig config;
	
	/**
	 * Creates a new ExileListener instance
	 * @param logger The logger instance
	 * @param pearls The pearl manger
	 * @param config The plugin configuration
	 */
	public ExileListener(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		
		this.pearlApi = pearlApi;
		this.config = pearlApi.getPearlConfig();
	}
	
	
	/**
	 * Clear the bed of a newly exiled player
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void exileRuleClearBed(ExilePearlEvent e) {
		if (config.getRuleCanUseBed()) {
			return;
		}
		
		if (e.getType() == Type.NEW) {
			e.getExilePearl().getPlayer().setBedSpawnLocation(null, true);
		}
	}
	

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onRuleBreakBlocks(BlockBreakEvent e) {
		if (config.getRuleCanMine()) {
			return;
		}
		
		Player p = e.getPlayer();
		
		if (pearlApi.isPlayerExiled(p)) {
			e.setCancelled(true);
			pearlApi.getPearlPlayer(p).msg(Lang.ruleCantDoThat, ExileRule.MINE.getActionString());
		}
	}
}
