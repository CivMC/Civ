package sh.okx.railswitch.glue;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.civmodcore.utilities.DependencyGlue;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Glue for Citadel.
 */
public final class CitadelGlue extends DependencyGlue {

    private ReinforcementManager manager;

    public CitadelGlue(Plugin plugin) {
        super(plugin, "Citadel");
    }

    public boolean isSafeToUse() {
        if (!super.isDependencyEnabled()) {
            return false;
        }
        if (this.manager == null) {
            return false;
        }
        return true;
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

    /**
     * Determines whether a player may modify the provided block according to Citadel permissions.
     *
     * @param player The player attempting to modify the block.
     * @param block The target block.
     * @return Returns true if the player may modify the block, or if Citadel isn't active for this server.
     */
    public boolean canModify(Player player, Block block) {
        if (player == null || !WorldUtils.isValidBlock(block) || !isSafeToUse()) {
            return true;
        }
        Reinforcement reinforcement = this.manager.getReinforcement(block);
        if (reinforcement == null) {
            return true;
        }
        return reinforcement.hasPermission(player, CitadelPermissionHandler.getModifyBlocks());
    }

    @Override
    protected void onDependencyEnabled() {
        this.manager = Citadel.getInstance().getReinforcementManager();
    }

    @Override
    protected void onDependencyDisabled() {
        this.manager = null;
    }
}
