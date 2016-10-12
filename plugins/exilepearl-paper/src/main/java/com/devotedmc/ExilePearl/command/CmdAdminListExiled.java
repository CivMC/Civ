package com.devotedmc.ExilePearl.command;

import java.util.Collection;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

import vg.civcraft.mc.civmodcore.util.TextUtil;

public class CmdAdminListExiled extends PearlCommand {

	public CmdAdminListExiled(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("list");

		this.setHelpShort("Lists all the exiled players.");
		
		this.permission = Permission.LIST.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		final StringBuilder sb = new StringBuilder("\n");
		final Collection<ExilePearl> pearls = plugin.getPearls();
		int lineLength = 0;
		
		sb.append(TextUtil.titleize("Exiled Players") + "\n");
		sb.append(String.format("<i>There are <c>%d <i>players exiled.\n <n>", pearls.size()));
		
		for(ExilePearl pearl : pearls) {
			sb.append(pearl.getPlayerName() + ", ");
			lineLength += (pearl.getPlayerName().length() + 2);
			if (lineLength > 50) {
				sb.append("\n");
				lineLength = 0;
			}
		}
		if (pearls.size() > 0) {
			sb.setLength(sb.length() - 2);
		}
		
		msg(sb.toString());
	}
}
