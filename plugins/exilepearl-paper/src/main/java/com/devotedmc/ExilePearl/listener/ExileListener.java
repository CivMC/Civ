package com.devotedmc.ExilePearl.listener;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerEvent;

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
	 * Clears the bed of newly exiled players
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

	/**
	 * Prevent exiled players from using a bed
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEnterBed(PlayerBedEnterEvent e) {
		checkAndCancelRule(ExileRule.USE_BED, e);
	}
	
	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerUseBucket(PlayerBucketEvent e) {
		checkAndCancelRule(ExileRule.USE_BUCKET, e);
	}
	
	/**
	 * Prevent exiled players from using local chat
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(PlayerBucketEvent e) {
		
		// TODO check chat channel
		checkAndCancelRule(ExileRule.CHAT, e);
	}
	
	

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		checkAndCancelRule(ExileRule.MINE, e, e.getPlayer());
	}
	
	
	/**
	 * Gets whether a rule is active for the given player
	 * @param rule The exile rule
	 * @param playerId The player to check
	 * @return true if the rule is active for the player
	 */
	private boolean isRuleActive(ExileRule rule, UUID playerId) {
		return config.isRuleSet(rule) && pearlApi.isPlayerExiled(playerId);
	}
	
	
	/**
	 * Checks if a rule is active for a given player and cancels it
	 * @param rule The rule to check
	 * @param event The event
	 * @param player The player to check
	 */
	private void checkAndCancelRule(ExileRule rule, Event event, Player player) {
		UUID playerId = player.getUniqueId();
		if (isRuleActive(rule, playerId)) {
			if (event instanceof Cancellable) {
				((Cancellable)event).setCancelled(true);
				pearlApi.getPearlPlayer(playerId).msg(Lang.ruleCantDoThat, rule.getActionString());
			}
		}
	}
	
	/**
	 * Checks if a rule is active for a given player even and cancels it
	 * @param rule The rule to check
	 * @param event The player event
	 */
	private void checkAndCancelRule(ExileRule rule, PlayerEvent event) {
		checkAndCancelRule(rule, event, event.getPlayer());
	}
}
