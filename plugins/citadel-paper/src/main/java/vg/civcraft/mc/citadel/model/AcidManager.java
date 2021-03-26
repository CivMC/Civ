package vg.civcraft.mc.citadel.model;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public class AcidManager {

	private Set<Material> material;

	public AcidManager(Collection<Material> acidMats) {
		this.material = new TreeSet<>(acidMats);
	}

	/**
	 * Checks if acid blocking is enabled for this reinforcement type
	 * 
	 * @param acidBlock acid Reinforcement type to check for
	 * @param victim victim block Reinforcement type
	 * @return True if the reinforcement type can acid block other reinforcements
	 *         and has an acid timer configured
	 */
	public boolean canAcidBlock(ReinforcementType acidBlock, ReinforcementType victim) {
		return acidBlock.getAcidPriority() >= victim.getAcidPriority();
	}

	/**
	 * Gets remaining time needed to mature acid block in milli seconds. If the acid
	 * is ready 0 will be returned
	 * 
	 * @param rein Reinforcement to check for
	 * @return Remaining time in milli seconds or 0 if the acid is ready
	 */
	public long getRemainingAcidMaturationTime(Reinforcement rein) {
		Block acidBlock = rein.getLocation().getBlock();
		Block targetBlock = acidBlock.getRelative(BlockFace.UP);
		double decayMultiplier = 1;
		if (!MaterialUtils.isAir(targetBlock.getType())) {
			Reinforcement targetBlockRein = ReinforcementLogic.getReinforcementAt(targetBlock.getLocation());
			if (targetBlockRein != null) {
				decayMultiplier = ReinforcementLogic.getDecayDamage(targetBlockRein);
			}
		}
		long totalTime = Math.round(rein.getType().getAcidTime() / decayMultiplier);
		return Math.max(0, totalTime - rein.getAge());
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
		return material.contains(b.getType());
	}
}
