package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.eggs.SorterEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Create extends BaseCommand {

    @CommandAlias("fmc")
    @CommandPermission("fm.op")
    @Syntax("<factory>")
    @Description("Creates a factory at the blocks you are looking at")
    @CommandCompletion("@FM_Factories")
    public void execute(CommandSender sender, String factoryName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        FactoryModManager manager = FactoryMod.getInstance().getManager();
        IFactoryEgg egg = manager.getEgg(factoryName);
        if (egg == null) {
            sender.sendMessage(ChatColor.RED + "This factory does not exist");
            return;
        }
        Set<Material> transparent = null;
        List<Block> view = player.getLineOfSight(transparent, 10);
        Factory exis = manager.getFactoryAt(view.get(view.size() - 1));
        if (exis != null) {
            manager.removeFactory(exis);
        }
        if (egg instanceof FurnCraftChestEgg) {
            FurnCraftChestEgg fcce = (FurnCraftChestEgg) egg;
            if (view.get(view.size() - 1).getType() == Material.CRAFTING_TABLE) {
                FurnCraftChestStructure fccs = new FurnCraftChestStructure(view.get(view.size() - 1));
                if (!fccs.isComplete()) {
                    sender.sendMessage(
                        ChatColor.RED + "The required block structure for this factory doesn't exist here");
                    return;
                }
                BlockState chestBS = fccs.getChest().getState();
                if (((Chest) chestBS).getCustomName() == null) {
                    ((Chest) chestBS).setCustomName(fcce.getName());
                    chestBS.update(true);
                }
                BlockState furnaceBS = fccs.getFurnace().getState();
                if (((Furnace) furnaceBS).getCustomName() == null) {
                    ((Furnace) furnaceBS).setCustomName(fcce.getName());
                    furnaceBS.update(true);
                }
                Factory factory = fcce.hatch(fccs, player);
                ((FurnCraftChestFactory) factory).getTableIOSelector();
                manager.addFactory(factory);
                sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "You are not looking at the right block for this factory");
            }
            return;
        }
        if (egg instanceof PipeEgg) {
            PipeEgg fcce = (PipeEgg) egg;
            if (view.get(view.size() - 1).getType() == Material.DISPENSER) {
                PipeStructure fccs = new PipeStructure(view.get(view.size() - 1));
                if (!fccs.isComplete()) {
                    sender.sendMessage(
                        ChatColor.RED + "The required block structure for this factory doesn't exist here");
                    return;
                }
                manager.addFactory(fcce.hatch(fccs, player));
                sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "You are not looking at the right block for this factory");
            }
            return;
        }
        if (egg instanceof SorterEgg) {
            SorterEgg fcce = (SorterEgg) egg;
            if (view.get(view.size() - 1).getType() == Material.DROPPER) {
                BlockFurnaceStructure fccs = new BlockFurnaceStructure(view.get(view.size() - 1));
                if (!fccs.isComplete()) {
                    sender.sendMessage(
                        ChatColor.RED + "The required block structure for this factory doesn't exist here");
                    return;
                }
                manager.addFactory(fcce.hatch(fccs, player));
                sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "You are not looking at the right block for this factory");
            }
        }
    }
}
