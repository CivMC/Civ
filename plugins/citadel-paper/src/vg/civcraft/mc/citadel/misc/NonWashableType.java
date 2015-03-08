package vg.civcraft.mc.citadel.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import vg.civcraft.mc.citadel.CitadelConfigManager;

public class NonWashableType {
	public static List<Material> mats = new ArrayList<Material>();
	
	public static void initializeNonWashableTypes(){
		List<String> materials = CitadelConfigManager.getNonWashableTypes();
		for (String x: materials){
			mats.add(Material.getMaterial(x));
		}
	}
	
	public static boolean isNonWashable(Material mat){
		return mats.contains(mat);
	}
}
