package com.devotedmc.ExilePearl.command;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;

public class CmdPearlBroadcastSilence extends PearlCommand {

	public CmdPearlBroadcastSilence(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("silence");
		
		this.commandArgs.add(requiredPlayer("player"));

		this.senderMustBePlayer = true;
		this.setHelpShort("Silences pearl broadcast request from a player");
	}

	@Override
	public void perform() {
		Player player = plugin.getPlayer(this.argAsString(0));
		if (player == null) {
			msg(Lang.pearlNoPlayer);
			return;
		}
		
		ExilePearl pearl = plugin.getPearl(argAsString(0));
		if (pearl == null) {
			msg(Lang.pearlPlayerNotExiled);
			return;
		}
		
		if (!pearl.isBroadcastingTo(player().getUniqueId())) {
			msg(Lang.pearlNotGettingBcasts, player.getName());
			return;
		}
		
		pearl.removeBroadcastListener(player().getUniqueId());
		msg(Lang.pearlSilencedBcast, player.getName());
	}
}
