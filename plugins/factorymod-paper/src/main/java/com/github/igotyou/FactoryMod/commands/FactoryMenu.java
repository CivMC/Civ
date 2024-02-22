package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.utility.FactoryModGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FactoryMenu extends BaseCommand {

	@CommandAlias("fm")
	@Syntax("[factory]")
	@Description("Opens a GUI allowing you to browse through all factories")
	@CommandCompletion("@FM_Factories")
	public void execute(Player sender, @Optional String factoryName) {
		if (factoryName == null) {
			FactoryModGUI gui = new FactoryModGUI(sender);
			gui.showFactoryOverview(true);
		} else {
			IFactoryEgg egg = FactoryMod.getInstance().getManager().getEgg(factoryName);
			if (egg == null) {
				sender.sendMessage(ChatColor.RED + "The factory " + factoryName + " does not exist");
				return;
			}
			FactoryModGUI gui = new FactoryModGUI(sender);
			gui.showForFactory((FurnCraftChestEgg) egg);
		}
	}

}
