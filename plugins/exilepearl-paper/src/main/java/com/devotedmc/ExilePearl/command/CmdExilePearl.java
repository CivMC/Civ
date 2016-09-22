package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdExilePearl extends PearlCommand {
	
	public CmdExilePearl(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("ep");
		
		this.setHelpShort("The ExilePearl base command");
		this.getLongHelp().add("This is the root command for Exile Pearl.");
		
		this.addSubCommand(new CmdPearlLocate(p));
		
		// Admin commands
		this.addSubCommand(new CmdConfig(p));
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
