package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class CmdAdminReload extends PearlCommand {

	public CmdAdminReload(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("reload");

		this.setHelpShort("Reloads the entire plugin.");
	}

	@Override
	public void perform() {
		try {
			msg("<i>Performing plugin reload.");
			
			msg("<i>Disabling plugin.");
			plugin.onDisable();

			msg("<i>Enabling plugin.");
			plugin.onEnable();
			
			msg("<g>Plugin reload complete.");
		} catch(Exception ex) {
			msg("<b>Plugin reload failed.");
			ex.printStackTrace();
		}
	}
}
