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
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.modifiers.ReceiptModifier;
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

    // ------------------------------------------------------------
    // Receipts
    // ------------------------------------------------------------

    @Subcommand("receipt")
    @Description("Adds or removes a receipt modifier from an exchange rule.")
    public void toggleReceiptModifier(
        final @NotNull Player sender
    ) {
        try (final var handler = new ModifierHandler<>(sender, ReceiptModifier.TEMPLATE)) {
            if (handler.getModifier() == null) {
                handler.ensureModifier();
                switch (handler.getRule().getType()) {
                    case INPUT -> handler.relay(ChatColor.GREEN + "Added receipt modifier!");
                    case OUTPUT -> handler.relay(ChatColor.GOLD + "Added receipt modifier, but it will only work if you swap this rule to an input!");
                }
            }
            else {
                handler.setModifier(null);
                handler.relay(ChatColor.GREEN + "Removed receipt modifier!");
            }
        }
    }

    @Subcommand("receipt alwaysprint|forceprint")
    @Description("Toggles whether to override customer preferences and always give a receipt. (Will create modifier if it doesn't already exist)")
    public void toggleForcedReceipts(
        final @NotNull Player sender
    ) {
        try (final var handler = new ModifierHandler<>(sender, ReceiptModifier.TEMPLATE)) {
            final ReceiptModifier modifier = handler.ensureModifier();
            if (modifier.forceReceiptGeneration) {
                modifier.forceReceiptGeneration = false;
                handler.relay(ChatColor.GREEN + "Customers will now only be given receipts if preferred!");
            }
            else {
                modifier.forceReceiptGeneration = true;
                handler.relay(ChatColor.GREEN + "Customers will now be given receipts regardless of preference!");
            }
        }
    }

    @Subcommand("receipt footer|slogan clear")
    @Description("Resets any footer/slogan text.")
    public void resetReceiptFooter(
        final @NotNull Player sender
    ) {
        try (final var handler = new ModifierHandler<>(sender, ReceiptModifier.TEMPLATE)) {
            final ReceiptModifier modifier = handler.getModifier();
            if (modifier == null) {
                handler.relay(ChatColor.GREEN + "That rule doesn't have a receipt modifier!");
                return;
            }
            if (modifier.footerText == null) {
                handler.relay(ChatColor.GREEN + "Receipt modifier already has no footer text!");
                return;
            }
            modifier.footerText = null;
            handler.relay(ChatColor.GREEN + "Receipt modifier footer text has been reset!");
        }
    }

    @Subcommand("receipt footer|slogan set")
    @Description("Sets or resets any footer/slogan text. (Will create modifier if it doesn't already exist)")
    public void setReceiptFooter(
        final @NotNull Player sender,
        final @Optional String footer
    ) {
        try (final var handler = new ModifierHandler<>(sender, ReceiptModifier.TEMPLATE)) {
            final ReceiptModifier modifier = handler.ensureModifier();
            if (StringUtils.isBlank(footer)) {
                modifier.footerText = null;
                handler.relay(ChatColor.GREEN + "Reset receipt footer text!");
            }
            else {
                modifier.footerText = footer;
                handler.relay(ChatColor.GREEN + "Receipt footer text set!");
            }
        }
    }
}
