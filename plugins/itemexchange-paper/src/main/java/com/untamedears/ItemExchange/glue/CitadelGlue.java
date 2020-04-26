package com.untamedears.itemexchange.glue;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.util.DependencyGlue;

public final class CitadelGlue extends DependencyGlue {

	public static final CitadelGlue INSTANCE = new CitadelGlue();

	private CitadelGlue() {
		super("Citadel");
	}

	@Override
	public boolean isSafeToUse() {
		if (!super.isSafeToUse()) {
			return false;
		}
		if (!NameLayerGlue.INSTANCE.isEnabled()) {
			return false;
		}
		return true;
	}

	public boolean hasAccessToChest(Block chest, Player player) {
		if (!isSafeToUse() || !BlockAPI.isValidBlock(chest)) {
			return false;
		}
		Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(chest);
		if (reinforcement == null) {
			return true;
		}
		return NameLayerGlue.INSTANCE.hasAccess(reinforcement.getGroup(), "CHESTS", player);
	}

}
