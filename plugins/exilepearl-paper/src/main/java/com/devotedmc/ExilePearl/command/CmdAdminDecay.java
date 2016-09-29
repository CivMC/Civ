package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminDecay extends PearlCommand {

	public CmdAdminDecay(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("decay");

		this.setHelpShort("Performs decay operation on all pearls");
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		plugin.getPearlManager().decayPearls();
		msg("<g>Decay operation complete.");
	}
}
