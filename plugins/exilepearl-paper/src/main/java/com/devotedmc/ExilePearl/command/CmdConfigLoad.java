package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;

public class CmdConfigLoad extends PearlCommand {
	public CmdConfigLoad(ExilePearlApi pearlApi) {
		super(pearlApi);
		
		this.senderMustBePlayer = false;
		this.errorOnToManyArgs = false;
		
		this.aliases.add("load");
		this.helpShort = "Reloads the plugin configuration";
	}
	
	@Override
	public void perform() {
		long startTime = System.currentTimeMillis();
		msg("<i>Reloading configuration...");
		plugin.getPearlConfig().reload();
		msg("<g>Configuration reloaded in %dms.", System.currentTimeMillis() - startTime);
	}
}
