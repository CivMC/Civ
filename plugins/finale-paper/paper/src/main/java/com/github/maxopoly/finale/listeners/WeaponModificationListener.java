package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.TippedArrowModifier;
import com.github.maxopoly.finale.misc.WeaponModifier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Map;

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

		if (is.getType() == Material.TIPPED_ARROW) {
			ItemMeta itemMeta = is.getItemMeta();
			PotionMeta potionMeta = (PotionMeta) itemMeta;
			potionMeta = potionMeta.clone();
			PotionData basePotionData = potionMeta.getBasePotionData();
			PotionType potionType = basePotionData.getType();

			TippedArrowModifier tippedArrowModifier = Finale.getPlugin().getManager().getTippedArrowModifier();
			TippedArrowModifier.TippedArrowConfig tippedArrowConfig = tippedArrowModifier.getTippedArrowConfig(potionType);
			if (tippedArrowConfig == null) {
				return;
			}

			TippedArrowModifier.PotionCategory potionCategory;
			if (basePotionData.isExtended()) {
				potionCategory = TippedArrowModifier.PotionCategory.EXTENDED;
			} else if (basePotionData.isUpgraded()) {
				potionCategory = TippedArrowModifier.PotionCategory.AMPLIFIED;
			} else {
				potionCategory = TippedArrowModifier.PotionCategory.NORMAL;
			}

			Map<TippedArrowModifier.PotionCategory, Integer> durations = tippedArrowConfig.getDurations();
			System.out.println("durations: " + durations);
			Integer duration = durations.get(potionCategory);
			System.out.println("duration: " + duration);
			if (duration != null) {
				potionMeta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE, false, false));
				potionMeta.clearCustomEffects();
				potionMeta.setColor(tippedArrowConfig.getColor());

				PotionEffect newEffect = new PotionEffect(potionType.getEffectType(), duration, basePotionData.isUpgraded() ? 1 : 0);
				potionMeta.addCustomEffect(newEffect, true);

				if (potionType == PotionType.TURTLE_MASTER) {
					PotionEffect resEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, basePotionData.isUpgraded() ? 1 : 0);
					potionMeta.addCustomEffect(resEffect, true);
				}

				potionMeta.displayName(Component.text(tippedArrowConfig.getName()));

				is.setItemMeta(potionMeta);
			}
		}

		e.setCurrentItem(is);
	}

}
