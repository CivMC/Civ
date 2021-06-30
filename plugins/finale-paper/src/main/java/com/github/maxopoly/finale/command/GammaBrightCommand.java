package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.maxopoly.finale.Finale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("gamma")
public class GammaBrightCommand extends BaseCommand {

	@Syntax("/gamma")
	@Description("Toggles night vision")
	public void execute(CommandSender sender) {
		Player p = (Player) sender;
		Finale.getPlugin().getSettingsManager().getGammaBrightSetting().toggleValue(p.getUniqueId());
	}
}
