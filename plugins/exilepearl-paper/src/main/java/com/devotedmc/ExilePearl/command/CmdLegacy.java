package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

/**
 * Command that tells players to use the new command if they try
 * to use the old prison pearl commands
 * @author Gordon
 */
public class CmdLegacy extends PearlCommand {
	
	public CmdLegacy(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("pp");
		
		this.visibility = CommandVisibility.INVISIBLE;
	}

	@Override
	public void perform() {
		msg("<i>Use <c>/ep <i>instead.");
	}
}
