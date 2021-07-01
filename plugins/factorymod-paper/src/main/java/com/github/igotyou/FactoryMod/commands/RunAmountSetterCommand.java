package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RunAmountSetterCommand extends BaseCommand {

	@CommandAlias("fmsrc")
	@Syntax("<amount>")
	@Description("Sets the amount of runs for the currently selected recipe in the factory you are looking at")
	@CommandPermission("fm.op")
	public void execute(CommandSender sender, String runCount) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC
					+ "How the hell is this supposed to work");
			return;
		}
		Player p = (Player) sender;
		int newAmount; 
		try {
			newAmount = Integer.parseInt(runCount);
		}
		catch(NumberFormatException e) {
			p.sendMessage(ChatColor.RED + runCount + " is not a number");
			return;
		}
		FactoryModManager manager = FactoryMod.getInstance().getManager();
		for(Block b : p.getLineOfSight((Set <Material>)null, 15)) {
			Factory f = manager.getFactoryAt(b);
			if (f instanceof FurnCraftChestFactory) {
				FurnCraftChestFactory fccf = (FurnCraftChestFactory) f;
				fccf.setRunCount(fccf.getCurrentRecipe(), newAmount);
				p.sendMessage(ChatColor.GREEN + "Set runcount for recipe " + fccf.getCurrentRecipe().getName() + " in " + fccf.getName() + " to "+ newAmount);
			}
		}
	}
}
