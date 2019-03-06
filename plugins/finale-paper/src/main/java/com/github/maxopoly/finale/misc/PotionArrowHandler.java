package com.github.maxopoly.finale.misc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class PotionArrowHandler {

	private class ArrowModification {
		final private boolean dealDamage;
		final private boolean noOverwrite;
		final private PotionEffect potionEffect;

		private ArrowModification(PotionEffect potionEffect, boolean dealDamage, boolean noOverwrite) {
			this.dealDamage = dealDamage;
			this.potionEffect = potionEffect;
			this.noOverwrite = noOverwrite;
		}
	}

	private Map <PotionData, ArrowModification> modifications;

	public PotionArrowHandler() {
		this.modifications = new HashMap<PotionData, PotionArrowHandler.ArrowModification>();
	}

	public void dropPotionArrowModification(PotionData data) {
		modifications.remove(data);
	}

	public void handlePotionArrowHit(EntityDamageByEntityEvent e) {
		TippedArrow arrow = (TippedArrow) e.getDamager();
		ArrowModification modi = modifications.get(arrow.getBasePotionData());
		if (modi == null) {
			return;
		}
		Collection <PotionEffect> existingEffects = ((LivingEntity) e.getEntity()).getActivePotionEffects();
		PotionEffect effectToOverwrite;
		for(PotionEffect pot : existingEffects) {
			if (pot.getType() == modi.potionEffect.getType()) {
				effectToOverwrite = pot;
			}
		}

		if (!modi.dealDamage) {
			e.setCancelled(true);
			((LivingEntity) e.getEntity()).addPotionEffect(modi.potionEffect, !modi.noOverwrite);
			return;
		}
		arrow.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE));
		arrow.addCustomEffect(modi.potionEffect, !modi.noOverwrite);		
	}

	public void registerPotionArrowModification(PotionData arrowType, PotionEffect replacementEffect, boolean dealDamage, boolean noOverwrite) {
		modifications.put(arrowType, new ArrowModification(replacementEffect, dealDamage, noOverwrite));
	}

}
