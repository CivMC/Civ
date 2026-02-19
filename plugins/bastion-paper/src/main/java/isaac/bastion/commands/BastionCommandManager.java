package isaac.bastion.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import isaac.bastion.Bastion;
import isaac.bastion.BastionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("bastion")
public class BastionCommandManager extends BaseCommand {
    @Subcommand("about")
    @Description("Shows Bastion version information.")
    @Default
    public void about(final CommandSender sender) {
        sender.sendMessage(Component.text("Bastion version " + Bastion.getPlugin().getPluginMeta().getVersion(), NamedTextColor.GREEN));
    }

    @Subcommand("give")
    @Description("Gives a bastion of the specified type.")
    @Syntax("<type>")
    @CommandCompletion("@bastionTypes")
    @CommandPermission("Bastion.admin")
    public void give(final Player player, final String typeName) {
        BastionType type = BastionType.getBastionType(typeName);
        if (type == null) {
            player.sendMessage(Component.text("Unknown bastion type: " + typeName, NamedTextColor.RED));
            return;
        }
        ItemStack item = type.getItemRepresentation();
        player.getInventory().addItem(item);
        player.sendMessage(Component.text("Gave 1 " + type.getName() + " bastion.", NamedTextColor.GREEN));
    }
}
