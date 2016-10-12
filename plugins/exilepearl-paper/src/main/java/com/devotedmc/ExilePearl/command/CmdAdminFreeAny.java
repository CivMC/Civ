package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
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
		String name = argAsString(0);
		
		ExilePearl pearl = plugin.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		
		if (plugin.freePearl(pearl, PearlFreeReason.FREED_BY_ADMIN)) {
			msg("<g>You freed <c>%s", name);
			return;
		}
		
		msg("<b>Tried to free <c>%s but the operation was cancelled.", name);
	}
}
