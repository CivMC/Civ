package com.github.maxopoly.finale;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;
import com.github.maxopoly.finale.potion.PotionModification;

public class FinaleManager {

	private boolean debug;
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private SaturationHealthRegenHandler regenHandler;
	private WeaponModifier weaponModifier;
	private boolean protocolLibEnabled;
	private Collection<Enchantment> disabledEnchantments;
	private Map<PotionType, List<PotionModification>> potionModifications;

	public FinaleManager(boolean debug, boolean attackSpeedEnabled, double attackSpeed, boolean regenHandlerEnabled,
			SaturationHealthRegenHandler regenHandler, WeaponModifier weaponModifier,
			Collection<Enchantment> disabledEnchants, Map<PotionType, List<PotionModification>> potionModifications) {
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.weaponModifier = weaponModifier;
		this.disabledEnchantments = disabledEnchants;
		this.potionModifications = potionModifications;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isAttackSpeedEnabled() {
		return attackSpeedEnabled;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public boolean isRegenHandlerEnabled() {
		return regenHandlerEnabled;
	}

	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}

	public WeaponModifier getWeaponModifer() {
		return weaponModifier;
	}

	public boolean isDisabledEnchantment(Enchantment enchant) {
		return disabledEnchantments.contains(enchant);
	}
	
	public PotionModification getApplyingModifications(PotionType pot, ItemStack is) {
		List<PotionModification> list = potionModifications.get(pot);
		if (list == null) {
			return null;
		}
		for(PotionModification mod : list) {
			if (mod.appliesTo(is)) {
				return mod;
			}
		}
		return null;
	}
}
