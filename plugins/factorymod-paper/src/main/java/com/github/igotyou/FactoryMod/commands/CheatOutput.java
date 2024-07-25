package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CheatOutput extends BaseCommand {

    @CommandAlias("fmco")
    @CommandPermission("fm.op")
    @Description("Gives you the output of the selected recipe in the factory you are looking at")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Set<Material> transparent = null;
        List<Block> view = ((Player) sender).getLineOfSight(transparent, 10);
        FactoryModManager manager = FactoryMod.getInstance().getManager();
        Factory exis = manager.getFactoryAt(view.get(view.size() - 1));
        if (exis != null && exis instanceof FurnCraftChestFactory) {
            FurnCraftChestFactory fcc = (FurnCraftChestFactory) exis;
            if (fcc.getCurrentRecipe() == null) {
                sender.sendMessage(ChatColor.RED + "This factory has no recipe selected");
                return;
            }
            IRecipe rec = fcc.getCurrentRecipe();
            if (!(rec instanceof ProductionRecipe)) {
                sender.sendMessage(ChatColor.RED + "The selected recipe is not a production recipe");
                return;
            }
            ProductionRecipe prod = (ProductionRecipe) rec;
            for (ItemStack is : prod.getOutput().getItemStackRepresentation()) {
                player.getInventory().addItem(is);
            }
            sender.sendMessage(ChatColor.GREEN + "Gave you all items for recipe " + ChatColor.GREEN + prod.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "You are not looking at a valid factory");
        }
    }
}
