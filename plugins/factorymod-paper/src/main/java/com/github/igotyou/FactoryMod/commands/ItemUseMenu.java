package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.github.igotyou.FactoryMod.utility.ItemUseGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUseMenu extends BaseCommand {

	@CommandAlias("item")
	@Syntax("[material]")
	@Description("Opens a GUI allowing you to browse all recipes which use or output the item in your main hand")
	@CommandCompletion("@materials")
	public void execute(Player p, @Optional String material) {
		if (material == null) {
			ItemUseGUI gui = new ItemUseGUI(p);
			gui.showItemOverview(p.getInventory().getItemInMainHand());
		} else {
			Material mat = Material.getMaterial(material);
			if (mat == null) {
				p.sendMessage(ChatColor.RED + "The item " + material + " does not exist");
				return;
			}
			ItemUseGUI gui = new ItemUseGUI(p);
			gui.showItemOverview(new ItemStack(mat));
		}
	}
}
