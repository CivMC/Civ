package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.finale.Finale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GammaBrightCommand extends BaseCommand {

    @CommandAlias("gamma")
    @Description("Toggles night vision")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Finale.getPlugin().getSettingsManager().getGammaBrightSetting().toggleValue(player.getUniqueId());
    }
}
