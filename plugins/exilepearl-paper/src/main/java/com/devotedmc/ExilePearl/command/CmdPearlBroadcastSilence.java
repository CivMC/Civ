package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;

public class CmdPearlBroadcastSilence extends PearlCommand {

	public CmdPearlBroadcastSilence(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("silence");
		
		this.commandArgs.add(requiredPlayer("player"));

		this.senderMustBePlayer = true;
		this.setHelpShort("Silences pearl broadcast request from a player");
	}

	@Override
	public void perform() {
		PearlPlayer player = plugin.getPearlPlayer(this.argAsString(0));
		if (player == null) {
			msg(Lang.pearlNoPlayer);
			return;
		}
		
		if (!player.getBcastPlayers().contains(me())) {
			msg(Lang.pearlNotGettingBcasts, player.getName());
			return;
		}
		
		player.removeBcastPlayer(me());
		msg(Lang.pearlSilencedBcast, player.getName());
	}
}
