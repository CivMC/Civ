package com.untamedears.realisticbiomes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;

@CommandAlias("rbgrow")
@CommandPermission("rb.op")
public class GrowCommand extends BaseCommand {

	@Syntax("rbgrow")
	@Description("Testing command to grow stuff")
	public void execute(CommandSender sender, String[] args) {
		/*GaussTree tree = new GaussTree(Double.parseDouble(args[0]), 0.15, Double.parseDouble(args[1]), 0.15,
				Double.parseDouble(args[2]), Double.parseDouble(args[3]),
				new BlockTransformation(Material.DARK_OAK_LOG, new HashMap<>()), Double.parseDouble(args[4]), 0.15,
				Double.parseDouble(args[5]), 0.15, Double.parseDouble(args[6]), Double.parseDouble(args[7]), 0.15,
				Double.parseDouble(args[8]), 0.15, Double.parseDouble(args[9]), 0.15,
				new BlockTransformation(Material.OAK_LEAVES, new HashMap<>())); 
		tree.genAt(((Player) sender).getLocation());*/
		sender.sendMessage("Done");
	}
}
