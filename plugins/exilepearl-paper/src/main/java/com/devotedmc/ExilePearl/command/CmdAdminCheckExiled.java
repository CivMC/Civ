package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminCheckExiled extends PearlCommand {

	public CmdAdminCheckExiled(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("check");

		this.setHelpShort("Checks if a player is exiled");
		
		this.commandArgs.add(requiredPearlPlayer());
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		String name = argAsString(0);
		
		ExilePearl pearl = pearlApi.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		if(pearl.getFreedOffline()) {
			msg("<i>%s has been freed but hasn't logged in yet.", name);
			return;
		}
		
		msg("<g>Found exile pearl for player %s", name);

		for (String s : pearlApi.getLoreProvider().generateLore(pearl)) {
			if (s.contains("Commands")) {
				return;
			}
			msg(s);
		}
	}
}
