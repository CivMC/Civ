package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;

public class CmdConfigSave extends PearlCommand {
	public CmdConfigSave(ExilePearlApi pearlApi) {
		super(pearlApi);
		
		this.senderMustBePlayer = false;
		this.errorOnToManyArgs = false;
		
		this.aliases.add("save");
		this.helpShort = "Saves the plugin configuration";
	}
	
	@Override
	public void perform() {
		long startTime = System.currentTimeMillis();
		plugin.getPearlConfig().saveToFile();
		msg("<g>Configuration saved to disk in %dms.", System.currentTimeMillis() - startTime);
	}
}
