package sh.okx.railswitch.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.entity.Player;
import sh.okx.railswitch.settings.SettingsManager;
import vg.civcraft.mc.civmodcore.command.AikarCommand;

/**
 * Continued support for setting and resetting your destination via a command.
 */
public final class DestinationCommand extends AikarCommand {

    @CommandAlias("dest|destination|setdestination|switch|setswitch|setsw")
    @Description("Set your rail destination(s)")
    @Syntax("[destination]")
    public void onSetDestination(Player player, @Optional String destination) {
        SettingsManager.setDestination(player, destination);
    }

}
