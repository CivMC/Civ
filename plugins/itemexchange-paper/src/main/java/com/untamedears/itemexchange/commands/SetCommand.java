package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.itemexchange.items.Token;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.modifiers.TokenModifier;
import com.untamedears.itemexchange.utility.ModifierHandler;
import com.untamedears.itemexchange.utility.RuleHandler;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias(SetCommand.ALIAS)
public final class SetCommand extends BaseCommand {

    public static final String ALIAS = "ies|ieset|set";

    @CatchUnknown
    @Description("Sets a pertinent field to an exchange rule.")
    @Syntax("<field> [...values]")
    public void base(Player player) {
        throw new InvalidCommandArgument();
    }

    @Subcommand("material|mat|m")
    @Description("Sets the material of an exchange rule.")
    @Syntax("<material>")
    @CommandCompletion("@itemMaterials")
    public void setMaterial(Player player, @Single String slug) {
        try (RuleHandler handler = new RuleHandler(player)) {
            Material material = Material.getMaterial(slug.toUpperCase());
            if (!ItemUtils.isValidItemMaterial(material)) {
                throw new InvalidCommandArgument("You must enter a valid item material.");
            }
            handler.getRule().setMaterial(material);
            handler.relay(ChatColor.GREEN + "Material successfully changed.");
        }
    }

    @Subcommand("amount|num|number|a")
    @Description("Sets the amount of an exchange rule.")
    @Syntax("<amount>")
    public void setAmount(Player player, int amount) {
        try (RuleHandler handler = new RuleHandler(player)) {
            if (amount <= 0) {
                throw new InvalidCommandArgument("You must enter a valid amount.");
            }
            handler.getRule().setAmount(amount);
            handler.relay(ChatColor.GREEN + "Amount successfully changed.");
        }
    }

    @Subcommand("switchio|switch|swap|swapio")
    @Description("Sets the amount of an exchange rule.")
    public void switchIO(Player player) {
        try (RuleHandler handler = new RuleHandler(player)) {
            if (handler.getRule().getType() == ExchangeRule.Type.INPUT) {
                handler.getRule().setType(ExchangeRule.Type.OUTPUT);
                handler.relay(ChatColor.GREEN + "Rule switched to an output.");
            } else if (handler.getRule().getType() == ExchangeRule.Type.OUTPUT) {
                handler.getRule().setType(ExchangeRule.Type.INPUT);
                handler.relay(ChatColor.GREEN + "Rule switched to an input.");
            } else {
                handler.relay(ChatColor.RED + "Rule could not be switched.");
            }
        }
    }

    // ============================================================
    // Tokens
    // ============================================================

    @Subcommand("token")
    @Description("Sets [or removes] the token modifier.")
    @Syntax("[token]")
    public void commandSetToken(
        final @NotNull Player sender,
        final @Optional @Single String value
    ) {
        try (final var handler = new ModifierHandler<>(sender, TokenModifier.TEMPLATE)) {
            if (StringUtils.isEmpty(value)) {
                handler.setModifier(null);
                handler.relay(ChatColor.GREEN + "Successfully removed token modifier.");
                return;
            }
            final Token token = Token.create(value);
            if (token == null) {
                throw new InvalidCommandArgument("%s is not a valid token!".formatted(value));
            }
            final TokenModifier modifier = handler.ensureModifier();
            modifier.token = token;
            handler.relay(ChatColor.GREEN + "Successfully set token modifier.");
        }
    }

    @Subcommand("anytoken")
    @Description("Adds (or resets) a token modifier to accept any token.")
    public void commandSetAnyToken(
        final @NotNull Player sender
    ) {
        try (final var handler = new ModifierHandler<>(sender, TokenModifier.TEMPLATE)) {
            final TokenModifier modifier = handler.ensureModifier();
            modifier.token = null;
            handler.relay(ChatColor.GREEN + "Successfully allowed any token.");
        }
    }
}
