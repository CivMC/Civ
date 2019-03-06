package com.github.maxopoly.finale.listeners;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.WeaponModifier;

import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;

public class WeaponModificationListener implements Listener {

	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		if (is == null) {
			return;
		}
		WeaponModifier weaponMod = Finale.getPlugin().getManager().getWeaponModifer();
		int adjustedDamage = weaponMod.getDamage(is.getType());
		double adjustedAttackSpeed = weaponMod.getAttackSpeed(is.getType());
		if (adjustedAttackSpeed == -1.0 && adjustedDamage == -1) {
			// neither should be adjusted
			return;
		}
		net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
		NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
		NBTBase modifierBase = compound.get("AttributeModifiers");
		NBTTagList modifiers;
		if (modifierBase != null) {
			modifiers = (NBTTagList) modifierBase;
		} else {
			modifiers = new NBTTagList();
		}
		if (adjustedDamage != -1) {
			NBTTagCompound damage = new NBTTagCompound();
			damage.set("AttributeName", new NBTTagString("generic.attackDamage"));
			damage.set("Name", new NBTTagString("generic.attackDamage"));
			damage.set("Operation", new NBTTagInt(0));
			damage.set("Amount", new NBTTagInt(adjustedDamage));
			damage.set("Slot", new NBTTagString("mainhand"));
			damage.set("UUIDLeast", new NBTTagInt(894654));
			damage.set("UUIDMost", new NBTTagInt(2872));
			modifiers.add(damage);
		}
		if (adjustedAttackSpeed != - 1.0) {
			NBTTagCompound speed = new NBTTagCompound();
			speed.set("AttributeName", new NBTTagString("generic.attackSpeed"));
			speed.set("Name", new NBTTagString("generic.attackSpeed"));
			speed.set("Operation", new NBTTagInt(0));
			speed.set("Amount", new NBTTagDouble(adjustedAttackSpeed));
			speed.set("Slot", new NBTTagString("mainhand"));
			speed.set("UUIDLeast", new NBTTagInt(894654));
			speed.set("UUIDMost", new NBTTagInt(2872));
			modifiers.add(speed);
		}
		compound.set("AttributeModifiers", modifiers);
		nmsStack.setTag(compound);
		ItemStack result = CraftItemStack.asBukkitCopy(nmsStack);
		e.setCurrentItem(result);
	}

}
