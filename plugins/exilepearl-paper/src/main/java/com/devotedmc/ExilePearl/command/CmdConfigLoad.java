package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdConfigLoad extends PearlCommand
{
	public CmdConfigLoad(ExilePearlPlugin plugin) {
		super(plugin);
		
		this.senderMustBePlayer = false;
		this.errorOnToManyArgs = false;
		
		this.aliases.add("load");
		this.helpShort = "Reloads the plugin configuration";
	}
	
	@Override
	public void perform() {
		long startTime = System.currentTimeMillis();
		plugin.GetConfig().load();
		msg("<g>Configuration reloaded in %dms.", System.currentTimeMillis() - startTime);
	}
}
