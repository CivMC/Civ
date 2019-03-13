package vg.civcraft.mc.citadel;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.block.Block;

import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;

public class AcidManager {

	private Set<Material> material;

	public AcidManager(Collection<Material> acidMats) {
		this.material = new TreeSet<>(acidMats);
	}

	/**
	 * Checks if the given valid is of a material registered as valid acid material
	 * 
	 * @param b Block to check for
	 * @return True if the block material is suited for being an acid block
	 */
	public boolean isPossibleAcidBlock(Block b) {
		if (b == null) {
			return false;
		}
		Material mat = b.getType();
		if (mat == null) {
			return false;
		}
		return material.contains(mat);
	}

	/**
	 * Checks if acid blocking is enabled for this reinforcement
	 * 
	 * @param rein Reinforcement to check for
	 * @return True if the reinforcement can acid block other reinforcements and has
	 *         an acid timer configured
	 */
	public boolean canBeAcidBlock(Reinforcement rein) {
		return canBeAcidBlock(rein.getType());
	}
	
	/**
	 * Checks if acid blocking is enabled for this reinforcement type
	 * 
	 * @param rein Reinforcement type to check for
	 * @return True if the reinforcement type can acid block other reinforcements and has
	 *         an acid timer configured
	 */
	public boolean canBeAcidBlock(ReinforcementType type) {
		return type.getAcidTime() >= 0;
	}

	/**
	 * Gets remaining time needed to mature acid block in milli seconds. If the acid
	 * is ready 0 will be returned
	 * 
	 * @param rein Reinforcement to check for
	 * @return Remaining time in milli seconds or 0 if the acid is ready
	 */
	public long getRemainingAcidMaturationTime(Reinforcement rein) {
		long totalTime = rein.getType().getAcidTime();
		return Math.max(0, totalTime - rein.getAge());
	}
}
