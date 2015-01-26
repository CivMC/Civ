package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import vg.civcraft.mc.citadel.CitadelConfigManager;

public class NonReinforceableType {

	public static List<Material> mats = new ArrayList<Material>();
	
	public static void initializeNonReinforceableTypes(){
		List<String> materials = CitadelConfigManager.getNonReinforceableTypes();
		for (String x: materials){
			mats.add(Material.getMaterial(x));
		}
	}
	
	public static boolean isNonReinforceable(Material mat){
		return mats.contains(mat);
	}
}
