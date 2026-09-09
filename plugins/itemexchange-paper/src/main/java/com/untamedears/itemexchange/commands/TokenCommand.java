package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.itemexchange.items.Token;
import com.untamedears.itemexchange.utility.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("ietoken|iet")
public final class TokenCommand extends BaseCommand {
    @Subcommand("create|c")
    @Syntax("<token>")
    public void createToken(
        final @NotNull Player sender,
        final @Single String value
    ) {
        final Token token = Token.create(value);
        if (token == null) {
            throw new InvalidCommandArgument("%s is not a valid token!".formatted(value));
        }
        Utilities.giveItemsOrDrop(
            sender.getInventory(),
            token.asItem()
        );
        sender.sendMessage(Component.text(
            "Token created!",
            NamedTextColor.GREEN
        ));
    }
}
