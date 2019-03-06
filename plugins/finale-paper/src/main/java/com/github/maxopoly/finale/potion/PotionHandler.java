package com.github.maxopoly.finale.potion;

import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class PotionHandler {
	
	private double healthPotionMultiplier;
	private Map<PotionType, List<PotionModification>> potionMods;
	
	public PotionHandler(Map<PotionType, List<PotionModification>> potionMods, double healthPotionMultiplier) {
		this.potionMods = potionMods;
		this.healthPotionMultiplier = healthPotionMultiplier;
	}
	
	public PotionModification getApplyingModifications(PotionType pot, ItemStack is) {
		List<PotionModification> list = potionMods.get(pot);
		if (list == null) {
			list = potionMods.get(PotionModification.wildCardType);
			if (list == null) {
				return null;
			}
		}
		for(PotionModification mod : list) {
			if (mod.appliesTo(is)) {
				return mod;
			}
		}
		return null;
	}
	
	public double getHealthPotionMultiplier() {
		return healthPotionMultiplier;
	}
	

}
