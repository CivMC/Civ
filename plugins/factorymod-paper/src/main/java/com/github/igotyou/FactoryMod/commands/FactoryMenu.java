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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactoryMenu extends BaseCommand {

    @CommandAlias("fm")
    @Syntax("[factory]")
    @Description("Opens a GUI allowing you to browse through all factories")
    @CommandCompletion("@FM_Factories")
    public void execute(CommandSender sender, @Optional String factoryName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        if (factoryName == null) {
            FactoryModGUI gui = new FactoryModGUI(player);
            gui.showFactoryOverview(true);
        } else {
            IFactoryEgg egg = FactoryMod.getInstance().getManager().getEgg(factoryName);
            if (egg == null) {
                sender.sendMessage(ChatColor.RED + "The factory " + factoryName + " does not exist");
                return;
            }
            FactoryModGUI gui = new FactoryModGUI(player);
            gui.showForFactory((FurnCraftChestEgg) egg);
        }
    }

}
