package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.WeaponModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WeaponModificationListener implements Listener {
	
	private final WeaponModifier manager = Finale.getPlugin().getManager().getWeaponModifer();

	@EventHandler
	public void weaponMod(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		if (is == null) {
			return;
		}

		is = ItemUtil.newModifiers(is); // there was a bug where modifiers weren't changing for items with already changed modifiers.

		ArmourModifier armourMod = Finale.getPlugin().getManager().getArmourModifier();

		double toughness = armourMod.getToughness(is.getType());
		double armour = armourMod.getArmour(is.getType());
		double knockbackResistance = armourMod.getKnockbackResistance(is.getType());

		if (toughness != -1 || armour != -1 || knockbackResistance != -1) {
			if (toughness == -1) {
				toughness = ItemUtil.getDefaultArmourToughness(is);
			}
			if (armour == -1) {
				armour = ItemUtil.getDefaultArmour(is);
			}
			if (knockbackResistance == -1) {
				knockbackResistance = ItemUtil.getDefaultKnockbackResistance(is);
			}
			is = ItemUtil.setArmour(ItemUtil.setArmourToughness(ItemUtil.setArmourKnockbackResistance(is, knockbackResistance), toughness), armour);
		}

		WeaponModifier weaponMod = Finale.getPlugin().getManager().getWeaponModifer();

		double adjustedDamage = weaponMod.getDamage(is.getType());
		double adjustedAttackSpeed = weaponMod.getAttackSpeed(is.getType());

		if (adjustedAttackSpeed != -1.0 || adjustedDamage != -1) {
			is = ItemUtil.setDamage(ItemUtil.setAttackSpeed(is, adjustedAttackSpeed), adjustedDamage);
		}
		e.setCurrentItem(is);
	}

}
