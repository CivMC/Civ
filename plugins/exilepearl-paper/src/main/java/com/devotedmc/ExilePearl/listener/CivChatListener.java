package com.devotedmc.ExilePearl.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;

import vg.civcraft.mc.civchat2.event.GlobalChatEvent;

public class CivChatListener extends RuleListener {

	
	public CivChatListener(ExilePearlApi pearlApi) {
		super(pearlApi);
	}

	/**
	 * Prevents exiled players from chatting globally
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onReinforcementDamage(GlobalChatEvent e) {
		checkAndCancelRule(ExileRule.CHAT, e, e.getPlayer());
	}
}
