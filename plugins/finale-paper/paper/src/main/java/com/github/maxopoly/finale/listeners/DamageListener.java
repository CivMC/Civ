package com.github.maxopoly.finale.listeners;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.event.CritHitEvent;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import com.github.maxopoly.finale.misc.arrow.ArrowHandler;
import com.github.maxopoly.finale.misc.DamageModificationConfig;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageListener implements Listener {

	private static final List<Material> ladderBlocks = Arrays.asList(new Material[] { Material.LADDER, Material.VINE });
	private static Set<Material> swords = new TreeSet<Material>(Arrays.asList(new Material[] { Material.WOODEN_SWORD,
			Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD }));

	public static final String powerMetaDataKey = "shooterPowerLevel";
	public static final String impaleMetaDataKey = "shooterImpaleLevel";

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
			if (e.getDamager().getType() == EntityType.TRIDENT) {
				handleTrident(e);
			}
			if (e.getDamager().getType() == EntityType.FIREWORK) {
				DamageModificationConfig fireworkModifier = modifiers.get(DamageModificationConfig.Type.FIREWORK);
				if (fireworkModifier != null) {
					double damage = fireworkModifier.modify(e.getDamage());
					e.setDamage(damage);
				}
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

	private void handleTrident(EntityDamageByEntityEvent e) {
		DamageModificationConfig tridentModifier = modifiers.get(DamageModificationConfig.Type.TRIDENT);
		if (tridentModifier != null) {
			e.setDamage(tridentModifier.modify(e.getDamage()));
		}
		DamageModificationConfig impaleModifier = modifiers.get(DamageModificationConfig.Type.IMPALE_ENCHANT);
		if (impaleModifier == null) {
			return;
		}
		Trident trident = (Trident) e.getDamager();
		List<MetadataValue> values = trident.getMetadata(impaleMetaDataKey);
		if (values == null || values.size() == 0) {
			return;
		}

		int impaleLevel = values.get(0).asInt();
		double damage = impaleModifier.modify(e.getDamage(), impaleLevel);

		e.setDamage(damage);
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
		Arrow arrow = (Arrow) e.getDamager();
		List<MetadataValue> values = arrow.getMetadata(powerMetaDataKey);
		if (values == null || values.size() == 0) {
			return;
		}

		int powerLevel = values.get(0).asInt();
		double damage = powerModifier.modify(e.getDamage(), powerLevel);

		if (arrow.getShooter() instanceof Player && e.getEntity() instanceof Player) {
			Player shooter = (Player) arrow.getShooter();
			Player target = (Player) e.getEntity();
			AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
			ArrowHandler arrowHandler = Finale.getPlugin().getManager().getArrowHandler();

			if (allyHandler.isAllyOf(shooter, target)) {
				damage *= (1 - arrowHandler.getAllyDamageReduction());
			}
		}

		e.setDamage(damage);
	}

	@EventHandler
	public void handleProjectileShot(ProjectileLaunchEvent e) {
		if (!(e.getEntity().getShooter() instanceof Player)) {
			return;
		}
		Player shooter = (Player) e.getEntity().getShooter();
		if (e.getEntityType() == EntityType.ARROW) {
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
		} else if (e.getEntityType() == EntityType.TRIDENT) {
			Trident trident = (Trident) e.getEntity();
			ItemStack tridentItem = trident.getItem();
			int impalingLevel = tridentItem.getEnchantmentLevel(Enchantment.IMPALING);
			trident.setMetadata(impaleMetaDataKey,
					new FixedMetadataValue(Finale.getPlugin(), impalingLevel));
		}
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

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Arrow)) {
			return;
		}

		ArrowHandler arrowHandler = Finale.getPlugin().getManager().getArrowHandler();
		arrowHandler.arrowImpact(event);
	}

	@EventHandler
	public void onProjectileCollide(ProjectileCollideEvent event) {
		if (!(event.getEntity() instanceof Arrow)) {
			return;
		}
		ArrowHandler arrowHandler = Finale.getPlugin().getManager().getArrowHandler();
		if (arrowHandler.isAllyCollide()) {
			return;
		}

		Arrow arrow = (Arrow) event.getEntity();
		if (!(arrow.getShooter() instanceof Player)) {
			return;
		}

		Entity collidedWith = event.getCollidedWith();
		if (!(collidedWith instanceof Player)) {
			return;
		}

		Player shooter = (Player) arrow.getShooter();
		Player target = (Player) collidedWith;

		AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();

		event.setCancelled(allyHandler.isAllyOf(shooter, target));
	}

}
