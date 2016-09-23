package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdExilePearl extends PearlCommand {
	
	private static CmdExilePearl instance;
	
	public static CmdExilePearl instance() {
		return instance;
	}
	
	public final CmdPearlLocate cmdLocate;
	public final CmdPearlFree cmdFree;
	
	public CmdExilePearl(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("ep");
		
		this.setHelpShort("The ExilePearl base command");
		this.getLongHelp().add("This is the root command for Exile Pearl.");
		
		cmdLocate = new CmdPearlLocate(p);
		cmdFree = new CmdPearlFree(p);
		
		this.addSubCommand(cmdLocate);
		this.addSubCommand(cmdFree);
		
		// Admin commands
		this.addSubCommand(new CmdAdmin(p));
		this.addSubCommand(new CmdConfig(p));
		
		instance = this;
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
