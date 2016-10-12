package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;

public class CmdPearlBroadcastConfirm extends PearlCommand {

	public CmdPearlBroadcastConfirm(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("confirm");

		this.senderMustBePlayer = true;
		this.setHelpShort("Confirms a pearl broadcast request.");
	}

	@Override
	public void perform() {
		PearlPlayer requested = me().getRequestedBcastPlayer();
		if (requested == null) {
			msg(Lang.pearlNoBcastRequest);
		}
		
		if (requested.isExiled()) {
			msg(Lang.pearlPlayerNotExiled);
			me().setRequestedBcastPlayer(null);
			return;
		}
		
		if (requested.equals(me())) {
			msg(Lang.pearlCantBcastSelf);
			return;
		}
		
		requested.addBcastPlayer(me());
		me().setRequestedBcastPlayer(null);
		msg(Lang.pearlGettingBcasts, requested.getName());
	}
}
