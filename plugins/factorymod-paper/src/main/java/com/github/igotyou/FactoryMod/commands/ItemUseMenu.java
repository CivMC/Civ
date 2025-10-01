package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.github.igotyou.FactoryMod.utility.ItemUseGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

@CommandAlias("item")
public class ItemUseMenu extends BaseCommand {

    @Default
    @Syntax("[material]")
    @Description("Opens a GUI allowing you to browse all recipes which use or output the item in your main hand")
    @CommandCompletion("@materials_and_custom_items")
    public void execute(Player p, @Optional String material) {
        if (material == null) {
            ItemUseGUI gui = new ItemUseGUI(p);
            gui.showItemOverview(p.getInventory().getItemInMainHand());
        } else if (Material.matchMaterial(material) != null) {
            Material mat = Material.matchMaterial(material);
            ItemUseGUI gui = new ItemUseGUI(p);
            gui.showItemOverview(new ItemStack(mat));
        } else if (CustomItem.getCustomItem(material.toLowerCase()) != null) {
            ItemUseGUI gui = new ItemUseGUI(p);
            gui.showItemOverview(CustomItem.getCustomItem(material.toLowerCase()));
        } else {
            p.sendMessage(ChatColor.RED + "The item " + material + " does not exist");
        }
    }
}
