package sh.okx.railswitch.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.okx.railswitch.settings.SettingsManager;

/**
 * Continued support for setting and resetting your destination via a command.
 */
public final class DestinationCommand extends BaseCommand {

    @CommandAlias("dest|destination|setdestination|switch|setswitch|setsw")
    @Description("Set your rail destination(s)")
    @Syntax("[destination]")
    public void onSetDestination(CommandSender sender, @Optional String destination) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        SettingsManager.setDestination(player, destination);
    }

}
