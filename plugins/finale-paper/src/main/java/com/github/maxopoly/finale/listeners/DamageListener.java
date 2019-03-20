package com.github.maxopoly.finale.listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.DamageModificationConfig;

import net.minecraft.server.v1_13_R2.EntityPlayer;

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

	@EventHandler
	public void damageEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		DamageModificationConfig generalModifier = modifiers.get(DamageModificationConfig.Type.ALL);
		if (generalModifier != null) {
			e.setDamage(generalModifier.modify(e.getDamage()));
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
				e.setDamage(strengthModifier.modify(e.getDamage(), strengthEffect.getAmplifier() + 1));
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
				e.setDamage(swordModifier.modify(e.getDamage()));
			}
			int sharpnessLevel = is.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			DamageModificationConfig sharpnessModifier = modifiers.get(DamageModificationConfig.Type.SHARPNESS_ENCHANT);
			if (sharpnessLevel != 0 && sharpnessModifier != null) {
				e.setDamage(sharpnessModifier.modify(e.getDamage(), sharpnessLevel));
			}
		}
		DamageModificationConfig critModifier = modifiers.get(DamageModificationConfig.Type.CRIT);
		if (critModifier != null && isCrit(e)) {
			e.setDamage(critModifier.modify(e.getDamage()));
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

	/**
	 * Checks if a player crit another player entity
	 *
	 * @param e Event to check for
	 * @return True if the attacker crit, false if not
	 */
	public boolean isCrit(EntityDamageByEntityEvent e) {
		if (e.getDamager().getType() != EntityType.PLAYER) {
			// only players can crit
			return false;
		}
		Player player = (Player) e.getDamager();
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		if (nmsPlayer.fallDistance <= 0.0f) {
			// player must be falling
			return false;
		}
		if (nmsPlayer.onGround) {
			// falling and being on the ground are actually two different things. Falling is
			// a state determined by the client
			// and barely/never checked by the server.
			// onGround checks if a solid block is below the player
			return false;
		}
		int x = minecraftFloor(nmsPlayer.locX);
		int boundingY = minecraftFloor(nmsPlayer.getBoundingBox().minY);
		int z = minecraftFloor(nmsPlayer.locZ);
		Block ladderBlock = player.getWorld().getBlockAt(x, boundingY, z);
		if (ladderBlocks.contains(ladderBlock.getType())) {
			// going down a ladder isnt actually falling and doesn't count for a crit
			return false;
		}
		if (nmsPlayer.isInWater()) {
			// cant crit in water
			return false;
		}
		if (player.getPotionEffect(PotionEffectType.BLINDNESS) != null) {
			return false;
		}
		if (player.getVehicle() != null) {
			// no driveby crits
			return false;
		}

		if (nmsPlayer.isSprinting()) {
			// not sure why, but this has to be here
			return false;
		}
		return true;
	}

	/**
	 * Minecrafts own floor implementation
	 */
	public static int minecraftFloor(double value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

}
