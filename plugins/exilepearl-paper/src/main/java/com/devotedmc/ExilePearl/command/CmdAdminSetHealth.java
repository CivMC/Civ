package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminSetHealth extends PearlCommand {

	public CmdAdminSetHealth(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("sethealth");

		this.setHelpShort("Sets the health % value of a pearl.");
		
		this.requiredArgs.add("player");
		this.requiredArgs.add("health %");
		
		this.permission = Permission.ADMIN.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		String name = argAsString(0);
		int percent = Math.min(100, Math.max(1, argAsInt(1)));
		
		ExilePearl pearl = pearlApi.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		
		// calculate the actual value
		int healthValue = (int)(pearlApi.getPearlConfig().getPearlHealthMaxValue() * ((double)percent / 100));
		pearl.setHealth(healthValue);
		msg("<g>You updated the pearl health of player % to %d%%.", pearl.getPlayerName(), percent);
	}
}
