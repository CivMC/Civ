package sh.okx.railswitch.glue;

import org.bukkit.block.Block;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.util.DependencyGlue;
import vg.civcraft.mc.civmodcore.util.NullUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Glue for Citadel.
 */
public final class CitadelGlue extends DependencyGlue {

    private ReinforcementManager manager;

    public CitadelGlue() {
        super("Citadel");
    }

    @Override
    public boolean isSafeToUse() {
        if (!super.isSafeToUse()) {
            return false;
        }
        if (this.manager == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onGlueEnabled() {
        this.manager = Citadel.getInstance().getReinforcementManager();
    }

    @Override
    protected void onGlueDisabled() {
        this.manager = null;
    }

    /**
     * Do two blocks, representing the sign and the rail, have the same reinforcement group?
     *
     * @param sign The block representing the sign.
     * @param rail The block representing the rail.
     * @return Returns true both blocks share the same reinforcement group, or if both are un-reinforced.
     */
    public boolean doSignAndRailHaveSameReinforcement(Block sign, Block rail) {
        if (!isSafeToUse() || !WorldUtils.isValidBlock(sign) || !WorldUtils.isValidBlock(rail)) {
            return false;
        }
        Reinforcement signReinforcement = this.manager.getReinforcement(sign);
        Reinforcement railReinforcement = this.manager.getReinforcement(rail);
        if (signReinforcement == null && railReinforcement == null) {
            return true;
        }
        if (signReinforcement == null || railReinforcement == null) {
            return false;
        }
        return NullUtils.equalsNotNull(
                signReinforcement.getGroup(),
                railReinforcement.getGroup());
    }

}
