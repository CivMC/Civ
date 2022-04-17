package com.github.maxopoly.finale.misc;

import com.github.maxopoly.finale.misc.ArmourModifier.ArmourConfig;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
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
	
	public static boolean isArmour(ItemStack is) {
		return isHelmet(is) || isChestplate(is) || isLeggings(is) || isBoots(is);
	}

	public static ItemStack newModifiers(ItemStack is) {
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
		CompoundTag compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new CompoundTag();
		compound.put("AttributeModifiers", new ListTag());
		nmsStack.setTag(compound);
		return CraftItemStack.asBukkitCopy(nmsStack);
	}

	public static net.minecraft.world.item.ItemStack getNMSStack(ItemStack is) {
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
		CompoundTag compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new CompoundTag();
		nmsStack.setTag(compound);
		return nmsStack;
	}
	
	public static net.minecraft.world.item.ItemStack getNMSStackAttributeModifiers(ItemStack is) {
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
		CompoundTag compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new CompoundTag();
		if (!compound.contains("AttributeModifiers")) {
			compound.put("AttributeModifiers", new ListTag());
		}
		nmsStack.setTag(compound);
		return nmsStack;
	}

	public static ItemStack setDamage(ItemStack is, double adjustedDamage)  {
		return modifyAttribute(is, AttributeModifier.attackDamage(adjustedDamage));
	}
	
	public static ItemStack setAttackSpeed(ItemStack is, double adjustedAttackSpeed) {
		return modifyAttribute(is, AttributeModifier.attackSpeed(adjustedAttackSpeed));
	}
	
	public static ItemStack setArmour(ItemStack is, double adjustedArmour) {
		return modifyAttribute(is, AttributeModifier.armour(adjustedArmour, Slot.getArmourSlot(is)));
	}
	
	public static ItemStack setArmourToughness(ItemStack is, double adjustedArmourToughness) {
		return modifyAttribute(is, AttributeModifier.toughness(adjustedArmourToughness, Slot.getArmourSlot(is)));
	}

	public static ItemStack setArmourKnockbackResistance(ItemStack is, double adjustedArmourKnockbackResistance) {
		return modifyAttribute(is, AttributeModifier.knockbackResistance(adjustedArmourKnockbackResistance, Slot.getArmourSlot(is)));
	}
	
	public static ItemStack modifyAttribute(ItemStack is, AttributeModifier attribute) {
		net.minecraft.world.item.ItemStack nmsStack = getNMSStackAttributeModifiers(is);
		CompoundTag compound = nmsStack.getTag();
		ListTag modifiers = compound.getList("AttributeModifiers", 10); // 10 for compound
		
		Number value = attribute.getValue();
		Tag valueTag = (value instanceof Double) ? DoubleTag.valueOf((Double) value) : IntTag.valueOf((Integer) value);
		
		Slot slot = attribute.getSlot();

		CompoundTag attributeTag = new CompoundTag();
		attributeTag.putString("AttributeName", attribute.getName());
		attributeTag.putString("Name", attribute.getName());
		attributeTag.putInt("Operation", 0);
		attributeTag.put("Amount", valueTag);
		attributeTag.putString("Slot", slot.getName());
		attributeTag.put("UUIDLeast", IntTag.valueOf(slot.getUuidLeast()));
		attributeTag.put("UUIDMost", IntTag.valueOf(slot.getUuidMost()));
		modifiers.add(attributeTag);
		
		compound.put("AttributeModifiers", modifiers);
		nmsStack.setTag(compound);
		return CraftItemStack.asBukkitCopy(nmsStack);
	}

	public static ItemStack setAAKey(ItemStack is, String key) {
		net.minecraft.world.item.ItemStack nmsItem = ItemUtil.getNMSStack(is);
		CompoundTag compoundTag = nmsItem.getTag();
		compoundTag.putString("aa-key", key);
		nmsItem.setTag(compoundTag);
		return CraftItemStack.asBukkitCopy(nmsItem);
	}

	public static String getAAKey(ItemStack is) {
		net.minecraft.world.item.ItemStack nmsItem = ItemUtil.getNMSStack(is);
		CompoundTag compoundTag = nmsItem.getTag();
		return compoundTag.getString("aa-key");
	}
	
}
