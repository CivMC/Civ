package com.devotedmc.ExilePearl.command;

import java.util.UUID;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminFreeAny extends PearlCommand {

	public CmdAdminFreeAny(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("freeany");

		this.setHelpShort("Frees any exiled player.");

		this.commandArgs.add(requiredPearlPlayer());

		this.permission = Permission.FREE_ANY.node;
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
			msg("<i>The player <c>%s <i>is not exiled.", playerName);
			return;
		}

		if (plugin.freePearl(pearl, PearlFreeReason.FREED_BY_ADMIN)) {
			msg("<g>You freed <c>%s", playerName);
			return;
		}

		msg("<b>Tried to free <c>%s <b>but the event was cancelled.", playerName);
	}
}
