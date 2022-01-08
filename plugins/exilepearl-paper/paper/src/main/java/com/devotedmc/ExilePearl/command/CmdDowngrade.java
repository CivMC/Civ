package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.util.SpawnUtil;

/**
 * Downgrades the Prison Pearl in senders main hand
 */
public class CmdDowngrade extends PearlCommand {

	public CmdDowngrade(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("downgrade");
		this.helpShort = "Downgrades a Prison Pearl";
		this.senderMustBePlayer = true;
	}

	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearlFromItemStack(player().getInventory().getItemInMainHand());
		if (pearl == null) {
			msg(Lang.pearlMustBeHoldingPearl);
			return;
		}
		if (pearl.getPearlType() != PearlType.PRISON) {
			msg("<i>This type of pearl cannot be downgraded");
			return;
		}
		downgradePearl(pearl);
	}

	public void downgradePearl(ExilePearl pearl) {
		pearl.setPearlType(PearlType.EXILE);
		if (pearl.getPlayer() != null && pearl.getPlayer().isOnline()) {
			SpawnUtil.spawnPlayer(pearl.getPlayer(), pearl.getPearlType() == PearlType.PRISON ? plugin.getPearlConfig().getPrisonWorld() : plugin.getPearlConfig().getMainWorld());
			msg(pearl.getPlayer(), "<i>Your pearl has been downgraded by %s.", player().getDisplayName());
		}
		String feedback = "The pearl for player %s was downgraded to an Exile Pearl.";
		plugin.log(feedback, pearl.getPlayerName());
		msg(feedback, pearl.getPlayerName());
	}
}
