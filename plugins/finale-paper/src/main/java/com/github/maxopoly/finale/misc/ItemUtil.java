package com.github.maxopoly.finale.misc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.finale.misc.ArmourModifier.ArmourConfig;
import com.google.common.collect.ImmutableMap;

import net.minecraft.server.v1_14_R1.NBTBase;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagDouble;
import net.minecraft.server.v1_14_R1.NBTTagInt;
import net.minecraft.server.v1_14_R1.NBTTagList;
import net.minecraft.server.v1_14_R1.NBTTagString;

public class ItemUtil {
	
	private static final List<Material> HELMET = Arrays.asList(Material.DIAMOND_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.LEATHER_HELMET);
	private static final List<Material> CHEST = Arrays.asList(Material.DIAMOND_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.ELYTRA);
	private static final List<Material> LEGS = Arrays.asList(Material.DIAMOND_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS);
	private static final List<Material> BOOTS = Arrays.asList(Material.DIAMOND_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS);

	private static final Map<Material, ArmourConfig> DEFAULT_ARMOUR = ImmutableMap.<Material, ArmourConfig>builder()
			.put(Material.LEATHER_BOOTS, new ArmourConfig(0, 1))
			.put(Material.LEATHER_LEGGINGS, new ArmourConfig(0, 2))
			.put(Material.LEATHER_CHESTPLATE, new ArmourConfig(0, 3))
			.put(Material.LEATHER_HELMET, new ArmourConfig(0, 1))
			.put(Material.GOLDEN_BOOTS, new ArmourConfig(0, 1))
			.put(Material.GOLDEN_LEGGINGS, new ArmourConfig(0, 3))
			.put(Material.GOLDEN_CHESTPLATE, new ArmourConfig(0, 5))
			.put(Material.GOLDEN_HELMET, new ArmourConfig(0, 2))
			.put(Material.CHAINMAIL_BOOTS, new ArmourConfig(0, 1))
			.put(Material.CHAINMAIL_LEGGINGS, new ArmourConfig(0, 4))
			.put(Material.CHAINMAIL_CHESTPLATE, new ArmourConfig(0, 5))
			.put(Material.CHAINMAIL_HELMET, new ArmourConfig(0, 2))
			.put(Material.IRON_BOOTS, new ArmourConfig(0, 2))
			.put(Material.IRON_LEGGINGS, new ArmourConfig(0, 5))
			.put(Material.IRON_CHESTPLATE, new ArmourConfig(0, 6))
			.put(Material.IRON_HELMET, new ArmourConfig(0, 2))
			.put(Material.DIAMOND_BOOTS, new ArmourConfig(2, 3))
			.put(Material.DIAMOND_LEGGINGS, new ArmourConfig(2, 6))
			.put(Material.DIAMOND_CHESTPLATE, new ArmourConfig(2, 8))
			.put(Material.DIAMOND_HELMET, new ArmourConfig(2, 3))
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
	
	public static net.minecraft.server.v1_14_R1.ItemStack getNMSStack(ItemStack is) {
		net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
		NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
		if (!compound.hasKey("AttributeModifiers")) {
			compound.set("AttributeModifiers", new NBTTagList());
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
	
	public static ItemStack modifyAttribute(ItemStack is, AttributeModifier attribute) {
		net.minecraft.server.v1_14_R1.ItemStack nmsStack = getNMSStack(is);
		NBTTagCompound compound = nmsStack.getTag();
		NBTTagList modifiers = compound.getList("AttributeModifiers", 10); // 10 for compound
		
		Number value = attribute.getValue();
		NBTBase valueTag = (value instanceof Double) ? new NBTTagDouble((Double) value) : new NBTTagInt((Integer) value);
		
		Slot slot = attribute.getSlot();
		
		NBTTagCompound damage = new NBTTagCompound();
		damage.set("AttributeName", new NBTTagString(attribute.getName()));
		damage.set("Name", new NBTTagString(attribute.getName()));
		damage.set("Operation", new NBTTagInt(0));
		damage.set("Amount", valueTag);
		damage.set("Slot", new NBTTagString(slot.getName()));
		damage.set("UUIDLeast", new NBTTagInt(slot.getUuidLeast()));
		damage.set("UUIDMost", new NBTTagInt(slot.getUuidMost()));
		modifiers.add(damage);
		
		compound.set("AttributeModifiers", modifiers);
		nmsStack.setTag(compound);
		ItemStack result = CraftItemStack.asBukkitCopy(nmsStack);
		return result;
	}
	
}
