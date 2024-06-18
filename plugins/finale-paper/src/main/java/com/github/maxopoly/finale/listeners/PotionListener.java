package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.potion.PotionHandler;
import com.github.maxopoly.finale.potion.PotionModification;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionListener implements Listener {

	private static final double healthPerPotionLevel = 4; // 2 hearts per level

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
		return potHandler.getApplyingModifications(potMeta.getBasePotionType(), is);
	}

	@EventHandler
	public void healthPotSplash(PotionSplashEvent e) {
		double multiplier = potHandler.getHealthPotionMultiplier();
		if (multiplier == 1.0) {
			return;
		}


		PotionEffect healingEffect = e.getPotion().getEffects().stream().filter(eff -> eff.getType().equals(PotionEffectType.INSTANT_HEALTH)).findFirst().orElse(null);
		if (healingEffect == null) {
			return;
		}

		double minIntensityCutOff = potHandler.getMinIntensityCutOff();
		double minIntensityImpact = potHandler.getMinIntensityImpact();
		ThrownPotion thrownPotion = e.getPotion();

		if (thrownPotion.getShooter() instanceof Player) {
			final Player shooter = (Player) thrownPotion.getShooter();
			if (shooter.isSprinting() && e.getIntensity(shooter) >= minIntensityImpact) {
				e.setIntensity(shooter, 1.0);
			}
		}

		for (LivingEntity entity : e.getAffectedEntities()) {
			double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			double modifiedHealth = entity.getHealth();
			double intensity = e.getIntensity(entity);
			e.setIntensity(entity, 0.0);

			if (intensity < minIntensityCutOff)
				continue;

			modifiedHealth += intensity * multiplier * (healingEffect.getAmplifier() + 1)
				* healthPerPotionLevel;
			if (!entity.isDead()) entity.setHealth(Math.min(maxHealth, modifiedHealth));
		}
	}

	@EventHandler
	public void itemConsume(PlayerItemConsumeEvent e) {
		PotionModification potMod = getModification(e.getItem());
		if (potMod == null || e.getPlayer().isDead()) {
			return;
		}
		PotionMeta potMeta = (PotionMeta) e.getItem().getItemMeta();
		List<PotionEffect> potEffects = potMeta.getBasePotionType().getPotionEffects();
		for (PotionEffect potEffect : potEffects) {
			PotionEffect toReplace = new PotionEffect(potEffect.getType(),
				(int) (potEffect.getDuration() * potMod.getMultiplier()), potEffect.getAmplifier());
			e.getPlayer().addPotionEffect(toReplace);
		}
		e.setItem(null);

	}

	@EventHandler
	public void potionSplash(PotionSplashEvent e) {
		PotionModification potMod = getModification(e.getPotion().getItem());
		if (potMod == null) {
			return;
		}
		//PotionMeta potMeta = (PotionMeta) e.getPotion().getItem().getItemMeta();
		e.getPotion().getEffects().forEach(originalEffect -> {
			double multiplier = potMod.getMultiplier();
			// for multipler <= 1 we can just change the intensity. That does not work for
			// more than 1 though, because MC internally enforces a max intensity of 1
			final PotionEffect potEffect = new PotionEffect(originalEffect.getType(), (int) (originalEffect.getDuration() * multiplier),
				originalEffect.getAmplifier());
			Consumer<LivingEntity> impact = (multiplier <= 1.0) ?
				(ent) -> {
					e.setIntensity(ent, e.getIntensity(ent) * multiplier);
				} :
				(ent) -> {
					e.setIntensity(ent, 0.0);
					ent.addPotionEffect(potEffect, false);
				};
			for (LivingEntity ent : e.getAffectedEntities()) {
				if (!ent.isDead()) impact.accept(ent);
			}
		});
	}

}
