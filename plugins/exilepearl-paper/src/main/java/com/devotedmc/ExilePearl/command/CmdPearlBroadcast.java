package com.devotedmc.ExilePearl.command;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;

public class CmdPearlBroadcast extends PearlCommand {

	public CmdPearlBroadcast(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("broadcast");
		
		this.commandArgs.add(requiredPlayer("player"));

		this.senderMustBePlayer = true;
		this.setHelpShort("Broadcasts your pearl location to another player.");
	}

	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearl(player().getUniqueId());
		
		if (pearl == null) {
			msg(Lang.pearlNotExiled);
			return;
		}
		
		Player player = plugin.getPlayer(this.argAsString(0));
		if (player == null) {
			msg(Lang.pearlNoPlayer);
			return;
		}
		
		if (player.equals(player())) {
			msg(Lang.pearlCantBcastSelf);
			return;
		}
		
		plugin.getPearlManager().addBroadcastRequest(player, pearl);

		msg(player, Lang.pearlBcastRequest, player().getName());
		msg(Lang.pearlBcastRequestSent);
	}
}
