package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import org.bukkit.entity.Player;

public class CmdPearlBroadcastSilence extends PearlCommand {

	public CmdPearlBroadcastSilence(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("silence");

		this.commandArgs.add(requiredPlayer("player"));

		this.senderMustBePlayer = true;
		this.setHelpShort("Stops pearl broadcasting from a player");
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
