package vg.civcraft.mc.citadel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.acidtypes.AcidType;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public class AcidManager {

    private List<AcidType> acidTypes;

    public AcidManager(Collection<AcidType> acidTypes) {
        this.acidTypes = new ArrayList<>(acidTypes);
    }

    /**
     * Checks if acid blocking is enabled for this reinforcement type
     *
     * @param acidBlock acid Reinforcement type to check for
     * @param victim    victim block Reinforcement type
     * @return True if the reinforcement type can acid block other reinforcements
     * and has an acid timer configured
     */
    public boolean canAcidBlock(ReinforcementType acidBlock, ReinforcementType victim) {
        return acidBlock.getAcidPriority() >= victim.getAcidPriority();
    }

    /**
     * Checks if acid faces are on the same group
     *
     * @param acidBlock acid Reinforcement type to check for
     * @param victim    victim block Reinforcement type
     * @return True if victim and acid block are on the same group
     */
    public boolean isAcidOnSameGroup(Reinforcement acidBlock, Reinforcement victim) {
        return acidBlock.getGroup().equals(victim.getGroup());
    }

    /**
     * Gets remaining time needed to mature acid in milli seconds for each block face. If the acid
     * is ready 0 will be returned
     *
     * @param rein Reinforcement to check for
     * @return Current block face being checked, remaining time in milli seconds or 0 if the acid is ready
     */
    public Map<BlockFace, Long> getRemainingAcidMaturationTime(Reinforcement rein) {
        Block acidBlock = rein.getLocation().getBlock();

        double acidMultiplier = acidTypes.stream()
            .filter(acidType -> acidType.material() == acidBlock.getType())
            .findFirst()
            .map(AcidType::modifier)
            .orElse(1D);

        List<BlockFace> acidFaces = acidTypes.stream()
            .filter(acidType -> acidType.material() == acidBlock.getType())
            .findFirst()
            .map(AcidType::blockFaces)
            .orElse(List.of(BlockFace.UP));

        double decayMultiplier = 1;
        Map<BlockFace, Long> remainingTimes = new HashMap<>();
        for (BlockFace face : acidFaces) {
            Block relativeBlock = acidBlock.getRelative(face);
            Reinforcement targetBlockRein = ReinforcementLogic.getReinforcementAt(relativeBlock.getLocation());
            if (
                targetBlockRein != null
                    && !MaterialUtils.isAir(relativeBlock.getType())
            ) {
                decayMultiplier = ReinforcementLogic.getDecayDamage(targetBlockRein);
            }

            long targetAcidTime = (targetBlockRein != null) ? targetBlockRein.getType().getAcidTime() : rein.getType().getAcidTime();
            long totalTime = Math.round(targetAcidTime / decayMultiplier * acidMultiplier);
            long remainingTime = Math.max(0, totalTime - rein.getAge());
            remainingTimes.put(face, remainingTime);
        }
        return remainingTimes;
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
        return acidTypes.stream()
            .anyMatch(acidType -> acidType.material() == b.getType());
    }

    public AcidType getAcidTypeFromMaterial(Material material) {
        return acidTypes.stream()
            .filter(acidType -> acidType.material() == material)
            .findFirst()
            .orElseThrow();
    }

    public List<AcidType> getAcidTypes() {
        return acidTypes;
    }
}
