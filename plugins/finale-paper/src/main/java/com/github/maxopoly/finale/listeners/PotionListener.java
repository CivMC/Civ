package com.github.maxopoly.finale.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.github.maxopoly.finale.potion.PotionHandler;
import com.github.maxopoly.finale.potion.PotionModification;

public class PotionListener implements Listener {

	private static final double healthPerPotionLevel = 4;
	public static PotionEffect fromPotionData(PotionData data) {
		PotionEffectType type = data.getType().getEffectType();
		if (type == PotionEffectType.HEAL || type == PotionEffectType.HARM) {
			return new PotionEffect(type, 1, data.isUpgraded() ? 1 : 0);
		} else if (type == PotionEffectType.REGENERATION || type == PotionEffectType.POISON) {
			if (data.isExtended()) {
				return new PotionEffect(type, 1800, 0);
			} else if (data.isUpgraded()) {
				return new PotionEffect(type, 440, 1);
			} else {
				return new PotionEffect(type, 900, 0);
			}
		} else if (type == PotionEffectType.NIGHT_VISION || type == PotionEffectType.INVISIBILITY
				|| type == PotionEffectType.FIRE_RESISTANCE || type == PotionEffectType.WATER_BREATHING) {
			return new PotionEffect(type, data.isExtended() ? 9600 : 3600, 0);
		} else if (type == PotionEffectType.WEAKNESS || type == PotionEffectType.SLOW) {
			return new PotionEffect(type, data.isExtended() ? 4800 : 1800, 0);
		} else if (data.isExtended()) {
			return new PotionEffect(type, 9600, 0);
		} else if (data.isUpgraded()) {
			return new PotionEffect(type, 1800, 1);
		} else {
			return new PotionEffect(type, 3600, 0);
		}
	}

	private PotionHandler potHandler;
	
	public PotionListener(PotionHandler potHandler) {
		this.potHandler = potHandler;
	}

	private PotionModification getModification(ItemStack is) {
		if (is == null) {
			return null;
		}
		ItemMeta im = is.getItemMeta();
		if (!(im instanceof PotionMeta)) {
			return null;
		}
		PotionMeta potMeta = (PotionMeta) im;
		PotionModification potMod = potHandler.getApplyingModifications(potMeta.getBasePotionData().getType(), is);
		return potMod;
	}

	@EventHandler
	public void healthPotSplash(PotionSplashEvent e) {
		double multiplier = potHandler.getHealthPotionMultiplier();
		if (multiplier == 1.0) {
			return;
		}
		PotionMeta potMeta = (PotionMeta) e.getPotion().getItem().getItemMeta();
		PotionEffect potEffect = fromPotionData(potMeta.getBasePotionData());
		if (potMeta.getBasePotionData().getType() != PotionType.INSTANT_HEAL) {
			return;
		}
		for(LivingEntity entity : e.getAffectedEntities()) {
			double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			double modifiedHealth = entity.getHealth();
			modifiedHealth += e.getIntensity(entity) * multiplier * (potEffect.getAmplifier() + 1)
					* healthPerPotionLevel;
			entity.setHealth(Math.min(maxHealth, modifiedHealth));
			e.setIntensity(entity, 0.0);
		}
	}

	@EventHandler
	public void itemConsume(PlayerItemConsumeEvent e) {
		PotionModification potMod = getModification(e.getItem());
		if (potMod == null) {
			return;
		}
		PotionMeta potMeta = (PotionMeta) e.getItem().getItemMeta();
		PotionEffect potEffect = fromPotionData(potMeta.getBasePotionData());
		PotionEffect toReplace = new PotionEffect(potEffect.getType(),
				(int) (potEffect.getDuration() * potMod.getMultiplier()), potEffect.getAmplifier());
		e.setItem(null);
		e.getPlayer().addPotionEffect(toReplace, false);
	}

	@EventHandler
	public void potionSplash(PotionSplashEvent e) {
		PotionModification potMod = getModification(e.getPotion().getItem());
		if (potMod == null) {
			return;
		}
		PotionMeta potMeta = (PotionMeta) e.getPotion().getItem().getItemMeta();
		PotionEffect potEffect = fromPotionData(potMeta.getBasePotionData());
		double multiplier = potMod.getMultiplier();
		// for multipler <= 1 we can just change the intensity. That does not work for
		// more than 1 though, because MC internally enforces a max intensity of 1
		potEffect = new PotionEffect(potEffect.getType(), (int) (potEffect.getDuration() * multiplier),
				potEffect.getAmplifier());
		if (multiplier <= 1.0) {
			for (LivingEntity ent : e.getAffectedEntities()) {
				e.setIntensity(ent, e.getIntensity(ent) * multiplier);
			}
		} else {
			for (LivingEntity ent : e.getAffectedEntities()) {
				e.setIntensity(ent, 0.0);
				ent.addPotionEffect(potEffect, false);
			}
		}
	}

}
