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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        int newAmount;
        try {
            newAmount = Integer.parseInt(runCount);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + runCount + " is not a number");
            return;
        }
        FactoryModManager manager = FactoryMod.getInstance().getManager();
        for (Block b : player.getLineOfSight((Set<Material>) null, 15)) {
            Factory f = manager.getFactoryAt(b);
            if (f instanceof FurnCraftChestFactory) {
                FurnCraftChestFactory fccf = (FurnCraftChestFactory) f;
                fccf.setRunCount(fccf.getCurrentRecipe(), newAmount);
                sender.sendMessage(ChatColor.GREEN + "Set runcount for recipe " + fccf.getCurrentRecipe().getName() + " in " + fccf.getName() + " to " + newAmount);
            }
        }
    }
}
