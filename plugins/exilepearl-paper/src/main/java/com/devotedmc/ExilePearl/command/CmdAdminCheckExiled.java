package com.devotedmc.ExilePearl.command;

import java.util.UUID;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminCheckExiled extends PearlCommand {

	public CmdAdminCheckExiled(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("check");

		this.setHelpShort("Checks if a player is exiled");
		
		this.commandArgs.add(requiredPearlPlayer());
		
		this.permission = Permission.CHECK.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		UUID playerId = argAsPlayerOrUUID(0);
		
		if (playerId == null) {
			msg(Lang.unknownPlayer);
			return;
		}
		String playerName = plugin.getRealPlayerName(playerId);
		
		ExilePearl pearl = plugin.getPearl(playerId);
		if (pearl == null) {
			msg("<i>The player <c>%s is not exiled.", playerName);
			return;
		}
		if(pearl.getFreedOffline()) {
			msg("<i>%s has been freed but hasn't logged in yet.", playerName);
			return;
		}
		
		msg("<g>Found exile pearl for player %s", playerName);

		for (String s : plugin.getLoreProvider().generatePearlInfo(pearl)) {
			msg(s);
		}
	}
}
