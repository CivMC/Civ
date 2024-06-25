package com.github.maxopoly.finale.misc;

import com.github.maxopoly.finale.misc.ArmourModifier.ArmourConfig;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

	private static final List<Material> HELMET = Arrays.asList(Material.NETHERITE_HELMET, Material.DIAMOND_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.LEATHER_HELMET);
	private static final List<Material> CHEST = Arrays.asList(Material.NETHERITE_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.ELYTRA);
	private static final List<Material> LEGS = Arrays.asList(Material.NETHERITE_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS);
	private static final List<Material> BOOTS = Arrays.asList(Material.NETHERITE_BOOTS, Material.DIAMOND_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS);

	private static final Map<Material, ArmourConfig> DEFAULT_ARMOUR = ImmutableMap.<Material, ArmourConfig>builder()
			.put(Material.LEATHER_BOOTS, new ArmourConfig(0, 1, 0))
			.put(Material.LEATHER_LEGGINGS, new ArmourConfig(0, 2, 0))
			.put(Material.LEATHER_CHESTPLATE, new ArmourConfig(0, 3, 0))
			.put(Material.LEATHER_HELMET, new ArmourConfig(0, 1, 0))
			.put(Material.GOLDEN_BOOTS, new ArmourConfig(0, 1, 0))
			.put(Material.GOLDEN_LEGGINGS, new ArmourConfig(0, 3, 0))
			.put(Material.GOLDEN_CHESTPLATE, new ArmourConfig(0, 5, 0))
			.put(Material.GOLDEN_HELMET, new ArmourConfig(0, 2, 0))
			.put(Material.CHAINMAIL_BOOTS, new ArmourConfig(0, 1, 0))
			.put(Material.CHAINMAIL_LEGGINGS, new ArmourConfig(0, 4, 0))
			.put(Material.CHAINMAIL_CHESTPLATE, new ArmourConfig(0, 5, 0))
			.put(Material.CHAINMAIL_HELMET, new ArmourConfig(0, 2, 0))
			.put(Material.IRON_BOOTS, new ArmourConfig(0, 2, 0))
			.put(Material.IRON_LEGGINGS, new ArmourConfig(0, 5, 0))
			.put(Material.IRON_CHESTPLATE, new ArmourConfig(0, 6, 0))
			.put(Material.IRON_HELMET, new ArmourConfig(0, 2, 0))
			.put(Material.DIAMOND_BOOTS, new ArmourConfig(2, 3, 0))
			.put(Material.DIAMOND_LEGGINGS, new ArmourConfig(2, 6, 0))
			.put(Material.DIAMOND_CHESTPLATE, new ArmourConfig(2, 8, 0))
			.put(Material.DIAMOND_HELMET, new ArmourConfig(2, 3, 0))
			.put(Material.NETHERITE_BOOTS, new ArmourConfig(3,3, 0.1))
			.put(Material.NETHERITE_LEGGINGS, new ArmourConfig(3, 6, 0.1))
			.put(Material.NETHERITE_CHESTPLATE, new ArmourConfig(3, 8, 0.1))
			.put(Material.NETHERITE_HELMET, new ArmourConfig(3, 3, 0.1))
			.build();

	public static double getDefaultArmourToughness(ItemStack is) {
		if (!(DEFAULT_ARMOUR.containsKey(is.getType()))) {
			return 0;
		}
		
		return DEFAULT_ARMOUR.get(is.getType()).getToughness();
	}
	
	public static double getDefaultArmour(ItemStack is) {
		if (!(DEFAULT_ARMOUR.containsKey(is.getType()))) {
			return 0;
		}
		
		return DEFAULT_ARMOUR.get(is.getType()).getArmour();
	}

	public static double getDefaultKnockbackResistance(ItemStack is) {
		if (!(DEFAULT_ARMOUR.containsKey(is.getType()))) {
			return 0;
		}

		return DEFAULT_ARMOUR.get(is.getType()).getKnockbackResistance();
	}
	
	public static boolean isHelmet(ItemStack is) {
		return HELMET.contains(is.getType());
	}
	
	public static boolean isChestplate(ItemStack is) {
		return CHEST.contains(is.getType());
	}
	
	public static boolean isLeggings(ItemStack is) {
		return LEGS.contains(is.getType());
	}
	
	public static boolean isBoots(ItemStack is) {
		return BOOTS.contains(is.getType());
	}

	public static ItemStack setAttackSpeed(ItemStack is, double adjustedAttackSpeed) {
		is.getItemMeta().removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
		is.getItemMeta().addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
			new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
				"generic.attackSpeed",
				adjustedAttackSpeed,
				org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
				EquipmentSlot.HAND)
		);
		return is;
	}

	public static ItemStack setDamage(ItemStack is, double adjustedDamage)  {
		is.getItemMeta().removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
		is.getItemMeta().addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
			new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
				"generic.attackDamage",
				adjustedDamage,
				org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
				EquipmentSlot.HAND)
		);
		return is;
	}

	public static ItemStack setArmour(ItemStack is, double adjustedArmour) {
		is.getItemMeta().removeAttributeModifier(Attribute.GENERIC_ARMOR);
		is.getItemMeta().addAttributeModifier(Attribute.GENERIC_ARMOR,
			new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
				"generic.armor",
				adjustedArmour,
				org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
				is.getType().getEquipmentSlot())
		);
		return is;
	}
	
	public static ItemStack setArmourToughness(ItemStack is, double adjustedArmourToughness) {
		is.getItemMeta().removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
		is.getItemMeta().addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
			new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
				"generic.armorToughness",
				adjustedArmourToughness,
				org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
				is.getType().getEquipmentSlot())
		);
		return is;
	}

	public static ItemStack setArmourKnockbackResistance(ItemStack is, double adjustedArmourKnockbackResistance) {
		is.getItemMeta().removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
		is.getItemMeta().addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
			new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
				"generic.knockbackResistance",
				adjustedArmourKnockbackResistance,
				org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
				is.getType().getEquipmentSlot())
		);
		return is;
	}
	
}
