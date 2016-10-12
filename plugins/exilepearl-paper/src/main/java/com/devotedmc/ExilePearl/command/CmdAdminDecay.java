package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminDecay extends PearlCommand {

	public CmdAdminDecay(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("decay");

		this.setHelpShort("Performs decay operation on all pearls");
		
		this.permission = Permission.DECAY.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		plugin.decayPearls();
		msg("<g>Decay operation complete.");
	}
}
