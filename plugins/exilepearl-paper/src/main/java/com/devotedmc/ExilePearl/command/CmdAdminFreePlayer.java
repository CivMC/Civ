package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdAdminFreePlayer extends PearlCommand {

	public CmdAdminFreePlayer(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("free");

		this.setHelpShort("Frees an exiled player.");
		
		this.requiredArgs.add("player");
	}

	@Override
	public void perform() {
		String name = argAsString(0);
		
		ExilePearl pearl = pearlApi.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		
		if (pearlApi.freePearl(pearl)) {
			msg("<g>You freed <c>%s", name);
			return;
		}
		
		msg("<b>Tried to free <c>%s but the operation was cancelled.", name);
	}
}
