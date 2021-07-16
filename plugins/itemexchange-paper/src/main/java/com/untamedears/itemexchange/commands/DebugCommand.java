package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.untamedears.itemexchange.utility.RuleHandler;
import org.bukkit.entity.Player;

@CommandAlias(DebugCommand.ALIAS)
public final class DebugCommand extends BaseCommand {

	public static final String ALIAS = "ied|iedebug";

	@Default
	@Description("Outputs a string of debug information.")
	public void setMaterial(Player player) {
		try (RuleHandler handler = new RuleHandler(player)) {
			handler.saveChanges(false); // This is read-only
			handler.relay(handler.getRule().toString());
		}
	}

}
