package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.misc.WeaponModifier;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.finale.Finale;

import java.util.HashSet;
import java.util.Set;

import static com.github.maxopoly.finale.misc.WeaponModifier.ATTACK_SPEED_NON_ADJUSTED;
import static com.github.maxopoly.finale.misc.WeaponModifier.DAMAGE_NON_ADJUSTED;

public class WeaponModificationListener implements Listener {
	
	private final WeaponModifier manager = Finale.getManager().getWeaponModifer();

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (item == null) {
			return;
		}
		net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = nmsStack.getTag();
		if (compound == null) {
			compound = new NBTTagCompound();
		}
		Set<NBTTagCompound> attributes = new HashSet<>();
		// NBTTagList doesn't have a method of getting the internal list, so we need to extract it
		// this also gives us the opportunity to remove duplicate entries.
		NBTTagList rawAttributes = compound.getList("AttributeModifiers", compound.getTypeId());
		for (int i = 0, l = rawAttributes.size(); i < l; i++) {
			NBTTagCompound nbt = rawAttributes.get(i);
			if (nbt == null || nbt.isEmpty()) {
				continue;
			}
			attributes.add(nbt);
		}
		int damage = manager.getDamage(item.getType());
		if (damage != DAMAGE_NON_ADJUSTED) {
			NBTTagCompound attribute = new NBTTagCompound();
			attribute.set("AttributeName", new NBTTagString("generic.attackDamage"));
			attribute.set("Name", new NBTTagString("generic.attackDamage"));
			attribute.set("Operation", new NBTTagInt(0));
			attribute.set("Amount", new NBTTagInt(damage));
			attribute.set("Slot", new NBTTagString("mainhand"));
			attribute.set("UUIDLeast", new NBTTagInt(894654));
			attribute.set("UUIDMost", new NBTTagInt(2872));
			attributes.add(attribute);
		}
		double speed = manager.getAttackSpeed(item.getType());
		if (speed != ATTACK_SPEED_NON_ADJUSTED) {
			NBTTagCompound attribute = new NBTTagCompound();
			attribute.set("AttributeName", new NBTTagString("generic.attackSpeed"));
			attribute.set("Name", new NBTTagString("generic.attackSpeed"));
			attribute.set("Operation", new NBTTagInt(0));
			attribute.set("Amount", new NBTTagDouble(speed));
			attribute.set("Slot", new NBTTagString("mainhand"));
			attribute.set("UUIDLeast", new NBTTagInt(894654));
			attribute.set("UUIDMost", new NBTTagInt(2872));
			attributes.add(attribute);
		}
		// If nothing was there and nothing will be set, just exit
		if (attributes.isEmpty() && rawAttributes.isEmpty()) {
			return;
		}
		NBTTagList resulting = new NBTTagList();
		attributes.forEach(resulting::add);
		compound.set("AttributeModifiers", resulting);
		nmsStack.setTag(compound);
		ItemStack result = CraftItemStack.asBukkitCopy(nmsStack);
		event.setCurrentItem(result);
	}

}
