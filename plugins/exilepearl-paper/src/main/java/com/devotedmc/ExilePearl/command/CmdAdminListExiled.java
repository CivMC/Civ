package com.devotedmc.ExilePearl.command;

import java.util.Collection;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.util.TextUtil;

public class CmdAdminListExiled extends PearlCommand {

	public CmdAdminListExiled(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("list");

		this.setHelpShort("Lists all the exiled players.");
	}

	@Override
	public void perform() {
		final StringBuilder sb = new StringBuilder();
		final Collection<ExilePearl> pearls = pearlApi.getPearls();
		int lineLength = 0;
		
		sb.append(TextUtil.instance().titleize("Exiled Players") + "\n");
		sb.append(String.format("<i>There is a total of <c>%d <i>players exiled.\n <n>", pearls.size()));
		
		for(ExilePearl pearl : pearls) {
			sb.append(pearl.getPlayerName() + ", ");
			lineLength += (pearl.getPlayerName().length() + 2);
			if (lineLength > 50) {
				sb.append("\n");
				lineLength = 0;
			}
		}
		sb.setLength(sb.length() - 2);
		
		msg(sb.toString());
	}
}
