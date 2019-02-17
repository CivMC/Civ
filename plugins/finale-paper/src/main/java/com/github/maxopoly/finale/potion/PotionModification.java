package com.github.maxopoly.finale.potion;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class PotionModification {

	private PotionType type;
	private Boolean extended;
	private Boolean upgraded;
	private double multiplier;
	private Boolean splash;

	public PotionModification(PotionType type, Boolean extended, Boolean upgraded, double multiplier, Boolean splash) {
		this.splash = splash;
		this.multiplier = multiplier;
		this.type = type;
		this.extended = extended;
		this.upgraded = upgraded;
	}

	public boolean appliesTo(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		if (!(im instanceof PotionMeta)) {
			return false;
		}
		PotionMeta pm = (PotionMeta) im;
		if (splash != null) {
			if (splash) {
				if (is.getType() != Material.SPLASH_POTION) {
					return false;
				}
			} else {
				if (is.getType() != Material.POTION) {
					return false;
				}
			}
		}
		PotionData pot = pm.getBasePotionData();
		if (type != null && pot.getType() != type) {
			return false;
		}
		if (extended != null && pot.isExtended() != extended) {
			return false;
		}
		if (upgraded != null && pot.isUpgraded() != upgraded) {
			return false;
		}
		return true;
	}
	
	public double getMultiplier() {
		return multiplier;
	}

}
