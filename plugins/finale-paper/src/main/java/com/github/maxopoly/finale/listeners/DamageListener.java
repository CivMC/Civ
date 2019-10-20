package com.github.maxopoly.finale.listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;
import com.github.maxopoly.finale.misc.DamageModificationConfig;

public class DamageListener implements Listener {

	private static final List<Material> ladderBlocks = Arrays.asList(new Material[] { Material.LADDER, Material.VINE });
	private static Set<Material> swords = new TreeSet<Material>(Arrays.asList(new Material[] { Material.WOODEN_SWORD,
			Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD }));

	private static final String powerMetaDataKey = "shooterPowerLevel";

	private Map<DamageModificationConfig.Type, DamageModificationConfig> modifiers;

	public DamageListener(Collection<DamageModificationConfig> configs) {
		modifiers = new TreeMap<>();
		for (DamageModificationConfig config : configs) {
			modifiers.put(config.getType(), config);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		DamageModificationConfig generalModifier = modifiers.get(DamageModificationConfig.Type.ALL);
		if (generalModifier != null) {
			double damage = generalModifier.modify(e.getDamage());
			e.setDamage(damage);
		}
		if (!(e.getDamager() instanceof LivingEntity)) {
			if (e.getDamager().getType() == EntityType.ARROW) {
				handleArrow(e);
			}
			return;
		}
		LivingEntity damager = (LivingEntity) e.getDamager();
		DamageModificationConfig strengthModifier = modifiers.get(DamageModificationConfig.Type.STRENGTH_EFFECT);
		if (strengthModifier != null) {
			PotionEffect strengthEffect = damager.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);
			if (strengthEffect != null) {
				double damage = strengthModifier.modify(e.getDamage(), strengthEffect.getAmplifier() + 1);
				e.setDamage(damage);
			}
		}

		if (!(damager instanceof Player)) {
			return;
		}
		Player attacker = (Player) damager;
		DamageModificationConfig swordModifier = modifiers.get(DamageModificationConfig.Type.SWORD);
		if (swordModifier != null) {
			ItemStack is = attacker.getInventory().getItemInMainHand();
			if (is != null && swords.contains(is.getType())) {
				double damage = swordModifier.modify(e.getDamage());
				e.setDamage(damage);
			}
			int sharpnessLevel = is.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			DamageModificationConfig sharpnessModifier = modifiers.get(DamageModificationConfig.Type.SHARPNESS_ENCHANT);
			if (sharpnessLevel != 0 && sharpnessModifier != null) {
				double damage = sharpnessModifier.modify(e.getDamage(), sharpnessLevel);
				e.setDamage(damage);
			}
		}
	}

	private void handleArrow(EntityDamageByEntityEvent e) {
		DamageModificationConfig arrowModifier = modifiers.get(DamageModificationConfig.Type.ARROW);
		if (arrowModifier != null) {
			e.setDamage(arrowModifier.modify(e.getDamage()));
		}
		DamageModificationConfig powerModifier = modifiers.get(DamageModificationConfig.Type.POWER_ENCHANT);
		if (powerModifier == null) {
			return;
		}
		Arrow arrow = (Arrow) e.getEntity();
		List<MetadataValue> values = arrow.getMetadata(powerMetaDataKey);
		if (values == null || values.size() == 0) {
			return;
		} else {
			int powerLevel = values.get(0).asInt();
			e.setDamage(powerModifier.modify(e.getDamage(), powerLevel));
		}
	}

	@EventHandler
	public void handleProjectileShot(ProjectileLaunchEvent e) {
		if (e.getEntityType() != EntityType.ARROW) {
			return;
		}
		if (!(e.getEntity().getShooter() instanceof Player)) {
			return;
		}
		Player shooter = (Player) e.getEntity().getShooter();
		ItemStack bow = shooter.getInventory().getItemInMainHand();
		if (bow.getType() != Material.BOW) {
			bow = shooter.getInventory().getItemInOffHand();
			if (bow.getType() != Material.BOW) {
				return;
			}
		}
		Arrow arrow = (Arrow) e.getEntity();
		arrow.setMetadata(powerMetaDataKey,
				new FixedMetadataValue(Finale.getPlugin(), bow.getEnchantmentLevel(Enchantment.ARROW_DAMAGE)));
	}

	@EventHandler
	public void onCrit(CritHitEvent e) {
		DamageModificationConfig critModifier = modifiers.get(DamageModificationConfig.Type.CRIT);
		double critMult = critModifier.modify(e.getCritMultiplier());
		e.setCritMultiplier(critMult);
	}
	
	@EventHandler()
	public void enderPearlThrown(PlayerTeleportEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

		Player player = event.getPlayer();
		if (player.getNoDamageTicks() > 0) {
			return;
		}
		// see
		// https://bukkit.org/threads/whats-up-with-setnodamageticks.141901/#post-1638021
		Bukkit.getScheduler().scheduleSyncDelayedTask(Finale.getPlugin(), new Runnable() {

			@Override
			public void run() {
				player.setNoDamageTicks(0);
			}
			
		}, 1L);
	}

}
