package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdConfigSave extends PearlCommand
{
	public CmdConfigSave(ExilePearlPlugin plugin) {
		super(plugin);
		
		this.senderMustBePlayer = false;
		this.errorOnToManyArgs = false;
		
		this.aliases.add("save");
		this.helpShort = "Saves the plugin configuration";
	}
	
	@Override
	public void perform() {
		long startTime = System.currentTimeMillis();
		plugin.GetConfig().save();
		msg("<g>Configuration saved to disk in %dms.", System.currentTimeMillis() - startTime);
	}
}
