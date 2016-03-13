package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;

public class NaturalReinforcementType {

	public static Map<Material, NaturalReinforcementType> types = 
			new HashMap<Material, NaturalReinforcementType>();
	
	private Material mat;
	private int dur;
	
	public NaturalReinforcementType(Material mat, int breakcount){
		this.mat = mat;
		this.dur = breakcount;
		types.put(mat, this);
	}
	
	public static void initializeNaturalReinforcementsTypes(){
		for (String type: CitadelConfigManager.getNaturalReinforcementTypes()){
			Material mat = CitadelConfigManager.getNaturalReinforcementMaterial(type);
			int dur = CitadelConfigManager.getNaturalReinforcementHitPoints(type);
			new NaturalReinforcementType(mat, dur);
			if (CitadelConfigManager.shouldLogInternal()) {
				Citadel.getInstance().getLogger().log(Level.INFO, "Adding Natural Reinforcement: {0} w/ health {1}", 
						new Object[] {mat.toString(), dur});
			}
		}
	}
	/**
	 * Get a NaturalReinforcementType from the specified material.
	 * @param The Material of the block.
	 * @return Returns the NaturalReinforcementType or null if none was found.
	 */
	public static NaturalReinforcementType getNaturalReinforcementType(Material mat){
		return types.containsKey(mat) ? types.get(mat) : null;
	}
	/**
	 * Get the Material of a NaturalReinforcementType
	 * @return Returns the Material of a NaturalReinforcementType.
	 */
	public Material getMaterial(){
		return mat;
	}
	/**
	 * 
	 * @return Returns the durability of the NaturalReinforcementType.
	 */
	public int getDurability(){
		return dur;
	}
}
