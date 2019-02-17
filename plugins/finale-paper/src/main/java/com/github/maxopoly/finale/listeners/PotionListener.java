package com.github.maxopoly.finale.listeners;

import java.io.File;
import java.util.Collection;

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
import org.bukkit.scheduler.BukkitRunnable;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.potion.PotionModification;

public class PotionListener implements Listener {

	@EventHandler
	public void potionSplash(PotionSplashEvent e) {
		PotionModification potMod = getModification(e.getPotion().getItem());
		if (potMod == null) {
			return;
		}
		double multiplier = potMod.getMultiplier();
		for (LivingEntity ent : e.getAffectedEntities()) {
			e.setIntensity(ent, e.getIntensity(ent) * multiplier);
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
		new BukkitRunnable() {

			@Override
			public void run() {
				e.getPlayer().addPotionEffect(toReplace);
			}
		}.runTaskLater(Finale.getPlugin(), 1L);
	}

	private static PotionModification getModification(ItemStack is) {
		if (is == null) {
			return null;
		}
		ItemMeta im = is.getItemMeta();
		if (!(im instanceof PotionMeta)) {
			return null;
		}
		PotionMeta potMeta = (PotionMeta) im;
		PotionModification potMod = Finale.getManager().getApplyingModifications(potMeta.getBasePotionData().getType(),
				is);
		return potMod;
	}

	public static PotionEffect fromPotionData(PotionData data) {
		PotionEffectType type = data.getType().getEffectType();
		if (type == PotionEffectType.HEAL || type == PotionEffectType.HARM) {
			return new PotionEffect(type, 1, data.isUpgraded() ? 2 : 1);
		} else if (type == PotionEffectType.REGENERATION || type == PotionEffectType.POISON) {
			if (data.isExtended()) {
				return new PotionEffect(type, 1800, 1);
			} else if (data.isUpgraded()) {
				return new PotionEffect(type, 440, 2);
			} else {
				return new PotionEffect(type, 900, 1);
			}
		} else if (type == PotionEffectType.NIGHT_VISION || type == PotionEffectType.INVISIBILITY
				|| type == PotionEffectType.FIRE_RESISTANCE || type == PotionEffectType.WATER_BREATHING) {
			return new PotionEffect(type, data.isExtended() ? 9600 : 3600, 1);
		} else if (type == PotionEffectType.WEAKNESS || type == PotionEffectType.SLOW) {
			return new PotionEffect(type, data.isExtended() ? 4800 : 1800, 1);
		} else if (data.isExtended()) {
			return new PotionEffect(type, 9600, 1);
		} else if (data.isUpgraded()) {
			return new PotionEffect(type, 1800, 2);
		} else {
			return new PotionEffect(type, 3600, 1);
		}
	}

}
