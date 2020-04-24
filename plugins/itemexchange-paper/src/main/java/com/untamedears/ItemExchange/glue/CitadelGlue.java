package com.untamedears.itemexchange.glue;

import com.untamedears.itemexchange.glue.NameLayerGlue.Permission;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.util.DependencyGlue;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;
import vg.civcraft.mc.namelayer.group.Group;

public final class CitadelGlue {

	public static Permission chestPermission;

	public static final DependencyGlue INSTANCE = new DependencyGlue("Citadel") {

		@Override
		public boolean isEnabled() {
			return super.isEnabled() && NameLayerGlue.isEnabled();
		}

		@Override
		protected void onGlueEnabled() {
			chestPermission = new Permission("CHESTS");
		}

		@Override
		protected void onGlueDisabled() {
			chestPermission = null;
		}

	};

	// ------------------------------------------------------------
	// Glue Implementation
	// ------------------------------------------------------------

	public static boolean isEnabled() {
		return INSTANCE.isEnabled();
	}

	public static Group getReinforcementGroupFromBlock(Block block) {
		if (!isEnabled() || !BlockAPI.isValidBlock(block)) {
			return null;
		}
		return NullCoalescing
				.chain(() -> Citadel.getInstance().getReinforcementManager().getReinforcement(block).getGroup());
	}

	public static void addGroupDetailsToRuleDetails(ExchangeRule rule, List<String> info) {
		if (!isEnabled() || rule.getType() != ExchangeRule.Type.INPUT) {
			return;
		}
		NullCoalescing.exists(rule.getGroup(), (group) -> info.add(ChatColor.RED + "Restricted to " + group.getName()));
	}

	public static boolean hasAccessToChest(Block chest, Player player) {
		if (!isEnabled()) {
			return false;
		}
		return NullCoalescing
				.chain(() -> chestPermission.hasAccess(getReinforcementGroupFromBlock(chest), player), false);
	}

}
