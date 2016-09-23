package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdAdminDecay extends PearlCommand {

	public CmdAdminDecay(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("decay");

		this.setHelpShort("Perform decay operation on all pearls");
	}

	@Override
	public void perform() {
		plugin.getPearlManager().decayPearls();
		msg("<g>Decay operation complete.");
	}
}
