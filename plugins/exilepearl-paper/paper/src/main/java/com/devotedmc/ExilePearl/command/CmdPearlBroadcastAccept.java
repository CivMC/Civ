package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.broadcast.PlayerBroadcastListener;

public class CmdPearlBroadcastAccept extends PearlCommand {

	public CmdPearlBroadcastAccept(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("accept");

		this.senderMustBePlayer = true;
		this.setHelpShort("Accepts a pearl broadcast request.");
	}

	@Override
	protected void perform() {
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
