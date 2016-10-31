package com.devotedmc.ExilePearl.command;

import java.util.UUID;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminSetKiller extends PearlCommand {

	public CmdAdminSetKiller(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("setkiller");

		this.setHelpShort("Sets the killer of a pearl.");
		
		this.commandArgs.add(requiredPearlPlayer());
		this.commandArgs.add(requiredPlayerOrUUID("killer"));
		
		this.permission = Permission.SET_KILLER.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		UUID playerId = argAsPlayerOrUUID(0);
		if (playerId == null) {
			msg("<i>No player was found matching <c>%s", argAsString(0));
			return;
		}
		
		ExilePearl pearl = plugin.getPearl(playerId);
		if (pearl == null) {
			msg("<i>No pearl was found matching <c>%s", argAsString(0));
			return;
		}

		UUID killerId = argAsPlayerOrUUID(1);
		if (killerId == null) {
			msg("<i>No player found matching <c>%s", argAsString(1));
			return;
		}
		
		pearl.setKillerId(killerId);
		msg("<g>You updated the pearl killer of player %s to %s", pearl.getPlayerName(), pearl.getKillerName());
	}
}
