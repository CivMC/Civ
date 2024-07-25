package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.untamedears.itemexchange.utility.RuleHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias(DebugCommand.ALIAS)
public final class DebugCommand extends BaseCommand {

    public static final String ALIAS = "ied|iedebug";

    @Default
    @Description("Outputs a string of debug information.")
    public void setMaterial(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        try (RuleHandler handler = new RuleHandler(player)) {
            handler.saveChanges(false); // This is read-only
            handler.relay(handler.getRule().toString());
        }
    }

}
