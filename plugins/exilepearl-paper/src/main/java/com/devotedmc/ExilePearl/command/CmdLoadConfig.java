package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdLoadConfig extends PearlCommand
{
	public CmdLoadConfig(ExilePearlPlugin plugin) {
		super(plugin);
		
		this.senderMustBePlayer = false;
		
		this.aliases.add("loadconfig");
		this.helpShort = "Reloads the plugin configuration";
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}
	
	@Override
	public void perform() {
		long startTime = System.currentTimeMillis();
		plugin.GetConfig().load();
		msg("<g>Configuration reloaded in %dms.", System.currentTimeMillis() - startTime);
	}
}
