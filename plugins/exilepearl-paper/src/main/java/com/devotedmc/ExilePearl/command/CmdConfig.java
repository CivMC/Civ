package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdConfig extends PearlCommand {
	
	public CmdConfig(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("config");
		
		this.setHelpShort("Manage ExilePearl configuration");
		
		this.addSubCommand(new CmdConfigLoad(p));
		this.addSubCommand(new CmdConfigSave(p));
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
