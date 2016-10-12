package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdConfig extends PearlCommand {
	
	public CmdConfig(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("config");
		
		this.setHelpShort("Manage ExilePearl configuration");
		
		this.addSubCommand(new CmdConfigLoad(plugin));
		this.addSubCommand(new CmdConfigSave(plugin));
		this.addSubCommand(new CmdConfigList(plugin));
		this.addSubCommand(new CmdConfigSet(plugin));
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
