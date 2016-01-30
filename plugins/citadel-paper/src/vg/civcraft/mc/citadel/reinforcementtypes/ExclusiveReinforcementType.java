package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import vg.civcraft.mc.citadel.CitadelConfigManager;

//An ExclusiveReinforcementType is a reinforcement type that can only reinforce certain blocks
//For example, maybe you want players to be able to reinforce chests with emeralds,
//but unable to reinforce obsidian with emeralds
public class ExclusiveReinforcementType {

	public static Map<Material, List<Material>> mats = new HashMap<Material, List<Material>>();
	
	public static void initializeExclusiveReinforcementTypes() {
		for(String reinforcementType : CitadelConfigManager.getReinforcementTypes()) {
			Material mat = CitadelConfigManager.getMaterial(reinforcementType);
			List<String> canReinforceNames = CitadelConfigManager.getReinforceableMaterials(reinforcementType);
			if(canReinforceNames == null) {
				continue;
			}
			List<Material> canReinforce = new ArrayList<Material>();
			for(String s : canReinforceNames) {
				canReinforce.add(Material.getMaterial(s));
			}
			//Check if the reinforcement type is meant to be exclusive
			if(!canReinforce.isEmpty()) {
				mats.put(mat, canReinforce);
			}
		}
	}
	
	public static boolean isExclusive(Material mat) {
		return mats.containsKey(mat);
	}
	
	public static List<Material> getReinforceableMaterials(Material mat) {
		return mats.get(mat);
	}
	
	/**
	 * Returns whether or not a given material can be used to reinforce another material
	 * @param The material to reinforce with
	 * @param The material to be reinforced
	 */
	public static boolean canReinforce(Material reinforcer, Material reinforcee) {
		if(!isExclusive(reinforcer)) {
			return true;
		}
		return mats.get(reinforcer).contains(reinforcee);
	}
}
