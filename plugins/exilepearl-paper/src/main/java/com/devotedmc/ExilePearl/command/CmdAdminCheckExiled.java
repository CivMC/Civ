package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdAdminCheckExiled extends PearlCommand {

	public CmdAdminCheckExiled(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("check");

		this.setHelpShort("Checks if a player is exiled");
		
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
		msg("<g>The player <c>%s <g>is exiled and is", pearl.getLocationDescription());
	}
}
