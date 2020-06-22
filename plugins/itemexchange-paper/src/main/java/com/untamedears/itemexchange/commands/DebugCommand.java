package com.untamedears.itemexchange.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.untamedears.itemexchange.utility.RuleHandler;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.AikarCommand;

@CommandAlias(DebugCommand.ALIAS)
public class DebugCommand extends AikarCommand {

	public static final String ALIAS = "ied|iedebug";

	@Default
	@Description("Sets the material of an exchange rule.")
	public void setMaterial(Player player) {
		try (RuleHandler handler = new RuleHandler(player)) {
			handler.saveChanges(false); // This is read-only
			handler.relay(handler.getRule().toString());
		}
	}

}
