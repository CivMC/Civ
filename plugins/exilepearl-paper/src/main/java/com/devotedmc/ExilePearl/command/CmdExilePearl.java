package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdExilePearl extends PearlCommand {
	
	private static CmdExilePearl instance;
	
	public static CmdExilePearl instance() {
		return instance;
	}
	
	public final PearlCommand cmdLocate;
	public final PearlCommand cmdFree;
	public final PearlCommand cmdBcast;
	public final PearlCommand cmdBcastConfirm;
	public final PearlCommand cmdBcastSilence;
	
	public CmdExilePearl(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("ep");
		
		this.setHelpShort("The ExilePearl command");
		this.getLongHelp().add("<n>Use <c>/ep help <n>for command help.");
		
		cmdLocate = new CmdPearlLocate(p);
		cmdFree = new CmdPearlFree(p);
		cmdBcast = new CmdPearlBroadcast(p);
		cmdBcastConfirm = new CmdPearlBroadcastConfirm(p);
		cmdBcastSilence = new CmdPearlBroadcastSilence(p);
		
		addSubCommand(plugin.getAutoHelp());
		
		addSubCommand(cmdLocate);
		addSubCommand(cmdFree);
		addSubCommand(cmdBcast);
		addSubCommand(cmdBcastConfirm);
		addSubCommand(cmdBcastSilence);
		
		// Admin commands
		addSubCommand(new CmdConfig(p));
		addSubCommand(new CmdAdminDecay(p));
		addSubCommand(new CmdAdminExilePlayer(p));
		addSubCommand(new CmdAdminFreePlayer(p));
		addSubCommand(new CmdAdminSetHealth(p));
		addSubCommand(new CmdAdminCheckExiled(p));
		addSubCommand(new CmdAdminListExiled(p));
		addSubCommand(new CmdAdminReload(p));
		
		instance = this;
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
