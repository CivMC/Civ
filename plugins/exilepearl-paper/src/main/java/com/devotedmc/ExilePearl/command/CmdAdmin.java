package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdmin extends PearlCommand {
	
	public CmdAdmin(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("admin");
		
		this.setHelpShort("ExilePearl admin commands");
		
		this.addSubCommand(new CmdAdminDecay(p));
		this.addSubCommand(new CmdAdminFreePlayer(p));
		this.addSubCommand(new CmdAdminExilePlayer(p));
		this.addSubCommand(new CmdAdminCheckExiled(p));
		this.addSubCommand(new CmdAdminListExiled(p));
		this.addSubCommand(new CmdAdminReload(p));
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
