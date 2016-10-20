package com.devotedmc.ExilePearl.listener;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.config.PearlConfig;

import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class RuleListener implements Listener {
	
	protected final ExilePearlApi pearlApi;
	protected final PearlConfig config;
	  
	/**
	 * Creates a new ExileListener instance
	 * @param logger The logger instance
	 * @param pearls The pearl manger
	 * @param config The plugin configuration
	 */
	public RuleListener(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		
		this.pearlApi = pearlApi;
		this.config = pearlApi.getPearlConfig();
	}
	
	/**
	 * Gets whether a rule is active for the given player
	 * @param rule The exile rule
	 * @param playerId The player to check
	 * @return true if the rule is active for the player
	 */
	protected boolean isRuleActive(ExileRule rule, UUID playerId) {
		return pearlApi.isPlayerExiled(playerId) && !config.canPerform(rule);
	}
	
	
	/**
	 * Checks if a rule is active for a given player and cancels it
	 * @param rule The rule to check
	 * @param event The event
	 * @param player The player to check
	 * @param notify whether to notify the player
	 */
	protected void checkAndCancelRule(ExileRule rule, Cancellable event, Player player, boolean notify) {
		if (event == null || player == null) {
			return;
		}
		
		UUID playerId = player.getUniqueId();
		if (isRuleActive(rule, playerId)) {
			((Cancellable)event).setCancelled(true);
			if (notify) {
				player.sendMessage(TextUtil.parse(Lang.ruleCantDoThat, rule.getActionString()));
			}
		}
	}
	
	/**
	 * Checks if a rule is active for a given player and cancels.
	 * It also notifies the player.
	 * @param rule The rule to check
	 * @param event The event
	 * @param player The player to check
	 */
	protected void checkAndCancelRule(ExileRule rule, Cancellable event, Player player) {
		checkAndCancelRule(rule, event, player, true);
	}
}
