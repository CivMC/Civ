package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdSaveConfig extends PearlCommand
{
	public CmdSaveConfig(ExilePearlPlugin plugin) {
		super(plugin);
		
		this.senderMustBePlayer = false;
		
		this.aliases.add("saveconfig");
		this.helpShort = "Saves the plugin configuration";
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}
	
	@Override
	public void perform() {
		long startTime = System.currentTimeMillis();
		plugin.GetConfig().save();
		msg("<g>Configuration saved to disk in %dms.", System.currentTimeMillis() - startTime);
	}
}
