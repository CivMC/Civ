package sh.okx.railswitch.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.CommandCompletion;
import org.bukkit.entity.Player;
import sh.okx.railswitch.settings.SettingsManager;

/**
 * Continued support for setting and resetting your destination via a command.
 */
public final class DestinationCommand extends BaseCommand {

    @CommandAlias("dest|destination|setdestination|switch|setswitch|setsw")
    @Description("Set your rail destination(s)")
    @Syntax("[destination]")
    @CommandCompletion("! +,-|+,+|-,+|-,- Impendia|Moloka|Lyrean|MtA|Medi|Arctic|AP|Deluvia|Founders|Proxima|Icenia|Karydia")
    public void onSetDestination(Player player, @Optional String destination) {
        SettingsManager.setDestination(player, destination);
    }

}
