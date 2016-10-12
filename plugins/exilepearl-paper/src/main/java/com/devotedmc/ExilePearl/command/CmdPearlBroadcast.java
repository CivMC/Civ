package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;

public class CmdPearlBroadcast extends PearlCommand {

	public CmdPearlBroadcast(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("broadcast");
		
		this.commandArgs.add(requiredPlayer("player"));

		this.senderMustBePlayer = true;
		this.setHelpShort("Broadcasts your pearl location to another player.");
	}

	@Override
	public void perform() {
		if (!me().isExiled()) {
			msg(Lang.pearlNotExiled);
			return;
		}
		
		PearlPlayer player = plugin.getPearlPlayer(this.argAsString(0));
		if (player == null) {
			msg(Lang.pearlNoPlayer);
			return;
		}
		
		player.setRequestedBcastPlayer(me());
		player.msg(Lang.pearlBcastRequest, me().getName());
		me().msg(Lang.pearlBcastRequestSent);
	}
}
