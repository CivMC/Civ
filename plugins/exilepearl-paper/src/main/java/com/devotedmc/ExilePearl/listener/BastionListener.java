package com.devotedmc.ExilePearl.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;

import isaac.bastion.event.BastionCreateEvent;
import isaac.bastion.event.BastionDamageEvent;

public class BastionListener extends RuleListener {


	public BastionListener(ExilePearlApi pearlApi) {
		super(pearlApi);
	}

	/**
	 * Prevents exiled players from creating bastions
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBastionCreate(BastionCreateEvent e) {
		checkAndCancelRule(ExileRule.CREATE_BASTION, e, e.getPlayer());
	}

	/**
	 * Prevents exiled players from damaging bastions
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBastionDamage(BastionDamageEvent e) {
		checkAndCancelRule(ExileRule.DAMAGE_BASTION, e, e.getPlayer());
	}
}
