package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.broadcast.PlayerBroadcastListener;

public class CmdPearlBroadcastConfirm extends PearlCommand {

	public CmdPearlBroadcastConfirm(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("confirm");

		this.senderMustBePlayer = true;
		this.setHelpShort("Confirms a pearl broadcast request.");
	}

	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearlManager().getBroadcastRequest(player());
		
		if (pearl == null) {
			msg(Lang.pearlNoBcastRequest);
			return;
		}
		
		pearl.addBroadcastListener(new PlayerBroadcastListener(player()));
		plugin.getPearlManager().removeBroadcastRequest(player());
		msg(Lang.pearlGettingBcasts, pearl.getPlayerName());
	}
}
