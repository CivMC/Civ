package sh.okx.railswitch.glue;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;

public final class CitadelGlue {
    /**
     * Do two blocks, representing the sign and the rail, have the same reinforcement group?
     *
     * @param sign The block representing the sign.
     * @param rail The block representing the rail.
     * @return Returns true both blocks share the same reinforcement group, or if both are un-reinforced.
     */
    public static boolean doSignAndRailHaveSameReinforcement(
        final @NotNull Block sign,
        final @NotNull Block rail
    ) {
        final Reinforcement signReinforcement = ReinforcementLogic.getReinforcementAt(sign.getLocation());
        final Reinforcement railReinforcement = ReinforcementLogic.getReinforcementAt(rail.getLocation());
        if (signReinforcement == null && railReinforcement == null) {
            return true;
        }
        if (signReinforcement == null ^ railReinforcement == null) {
            return false;
        }
        return NullUtils.equalsNotNull(
            signReinforcement.getGroup(),
            railReinforcement.getGroup()
        );
    }
}
