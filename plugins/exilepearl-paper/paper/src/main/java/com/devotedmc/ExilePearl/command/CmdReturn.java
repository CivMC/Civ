package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;

public class CmdReturn extends PearlCommand {

	public CmdReturn(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("return");

		this.senderMustBePlayer = true;
		this.helpShort = "Returns a summoned player";
	}

	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearlFromItemStack(player().getInventory().getItemInMainHand());
		if(pearl == null) {
			msg(Lang.pearlMustBeHoldingPearl);
			return;
		}

		if(plugin.returnPearl(pearl)) {
			msg(Lang.pearlReturned, pearl.getPlayerName());
		} else {
			msg(Lang.pearlCantReturn);
		}
	}
}
