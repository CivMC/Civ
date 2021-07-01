package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.finale.Finale;
import org.bukkit.entity.Player;

public class GammaBrightCommand extends BaseCommand {

	@CommandAlias("gamma")
	@Description("Toggles night vision")
	public void execute(Player sender) {
		Player p = (Player) sender;
		Finale.getPlugin().getSettingsManager().getGammaBrightSetting().toggleValue(p.getUniqueId());
	}
}
