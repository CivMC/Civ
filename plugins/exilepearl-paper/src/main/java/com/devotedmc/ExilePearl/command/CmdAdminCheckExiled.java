package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminCheckExiled extends PearlCommand {

	public CmdAdminCheckExiled(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("check");

		this.setHelpShort("Checks if a player is exiled");
		
		this.requiredArgs.add("player");
		
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
		msg("<g>Found exile pearl for player %s", name);

		for (String s : pearlApi.getLoreGenerator().generateLore(pearl)) {
			if (s.contains("Commands")) {
				return;
			}
			msg(s);
		}
	}
}
