package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

@CommandAlias("customitem")
public final class CustomItemCommand extends BaseCommand {

    @Subcommand("give")
    @Description("Give a registered custom item to a player.")
    @CommandPermission("cmc.customitem.give")
    @CommandCompletion("@players @customitems")
    @Syntax("<player> <item>")
    public void give(final CommandSender sender, final String targetPlayer, final String itemKey) {
        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) {
            sender.sendMessage(Component.text("Unknown player", NamedTextColor.RED));
            return;
        }
        final ItemStack item = CustomItem.getCustomItem(itemKey);
        if (item == null) {
            sender.sendMessage(Component.text("Unknown custom item: " + itemKey, NamedTextColor.RED));
            return;
        }
        if (!player.getInventory().addItem(item).isEmpty()) {
            sender.sendMessage(Component.text(player.getName() + " does not have room for " + itemKey + ".", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Gave " + itemKey + " to " + player.getName() + ".", NamedTextColor.GREEN));
    }
}
