package com.untamedears.itemexchange.commands;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.untamedears.itemexchange.glue.NameLayerGlue;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.modifiers.DamageableModifier;
import com.untamedears.itemexchange.rules.modifiers.RepairModifier;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.api.EnchantAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;
import vg.civcraft.mc.civmodcore.util.TextUtil;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

@CommandAlias(SetCommand.ALIAS)
public class SetCommand extends AikarCommand {

    public static final String ALIAS = "ies|ieset|set";

    private static final Pattern SET_ENCHANT_PATTERN = Pattern.compile("^([+?\\-])([\\w_]+)(\\d*)$");

    @Default
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
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        Material material = Material.getMaterial(slug.toUpperCase());
        if (!MaterialAPI.isValidItemMaterial(material)) {
            throw new InvalidCommandArgument("You must enter a valid item material.");
        }
        player.sendMessage(ChatColor.GREEN + "Material successfully changed.");
        rule.setMaterial(material);
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("amount|num|number|a")
    @Description("Sets the amount of an exchange rule.")
    @Syntax("<amount>")
    public void setAmount(Player player, int amount) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (amount <= 0) {
            throw new InvalidCommandArgument("You must enter a valid amount.");
        }
        player.sendMessage(ChatColor.GREEN + "Amount successfully changed.");
        rule.setAmount(amount);
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("allowenchantments|allowenchants")
    @Description("Allows items with unspecified enchantments to be bought and sold.")
    public void allowUnlistedEnchantments(Player player) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        player.sendMessage(ChatColor.GREEN + "Unlisted enchantments are now allowed.");
        rule.setAllowingUnlistedEnchants(true);
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("denyenchantments|denyenchants")
    @Description("Disallows items with unspecified enchantments to be bought and sold.")
    public void disallowUnlistedEnchantments(Player player) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        player.sendMessage(ChatColor.GREEN + "Unlisted enchantments are now denied.");
        rule.setAllowingUnlistedEnchants(false);
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("enchantment|enchant|e")
    @Description("Disallows items with unspecified enchantments to be bought and sold.")
    @Syntax("<+/?/-><enchantment>[level]")
    public void setEnchantment(Player player, @Single String details) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (Strings.isNullOrEmpty(details)) {
            throw new InvalidCommandArgument("You must enter an enchantment.");
        }
        Matcher matcher = SET_ENCHANT_PATTERN.matcher(details);
        if (!matcher.matches()) {
            throw new InvalidCommandArgument("You must enter a valid instruction.");
        }
        Enchantment enchantment = EnchantAPI.getEnchantment(matcher.group(2));
        if (enchantment == null) {
            throw new InvalidCommandArgument("You must enter a valid enchantment.");
        }
        Map<Enchantment, Integer> requiredEnchants = rule.getRequiredEnchants();
        Set<Enchantment> excludedEnchants = rule.getExcludedEnchants();
        switch (matcher.group(1)) {
            case "+": {
                int level = ExchangeRule.ERROR;
                if (matcher.groupCount() < 3) {
                    level = ExchangeRule.ANY;
                }
                else {
                    try {
                        level = Integer.parseInt(matcher.group(3));
                    }
                    catch (Exception ignored) {
                    } // No need to error here because it'll error below
                    if (level < enchantment.getStartLevel() || level > enchantment.getMaxLevel()) {
                        throw new InvalidCommandArgument("You must enter a valid level.");
                    }
                }
                requiredEnchants.put(enchantment, level);
                excludedEnchants.remove(enchantment);
                player.sendMessage(ChatColor.GREEN + "Successfully added required enchantment.");
                break;
            }
            case "-": {
                requiredEnchants.remove(enchantment);
                excludedEnchants.add(enchantment);
                player.sendMessage(ChatColor.GREEN + "Successfully added excluded enchantment.");
                break;
            }
            case "?": {
                requiredEnchants.remove(enchantment);
                excludedEnchants.remove(enchantment);
                player.sendMessage(ChatColor.GREEN + "Successfully removed rules relating to enchantment.");
                break;
            }
            default: {
                throw new InvalidCommandArgument("You entered an invalid instruction.");
            }
        }
        rule.setRequiredEnchants(requiredEnchants);
        rule.setExcludedEnchants(excludedEnchants);
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("displayname|display|name")
    @Description("Sets or resets the item's display name.")
    @Syntax("[name]")
    public void setDisplayName(Player player, @Optional String value) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (Strings.isNullOrEmpty(value)) {
            player.sendMessage(ChatColor.GREEN + "Successfully removed display name.");
            rule.setDisplayName("");
        }
        else {
            player.sendMessage(ChatColor.GREEN + "Successfully changed display name.");
            rule.setDisplayName(value);
        }
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("ignoredisplayname|ignoredisplay|ignorename")
    @Description("Toggles the rule of ignoring display names.")
    public void toggleIgnoreDisplayName(Player player) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (rule.isIgnoringDisplayName()) {
            rule.setIgnoringDisplayName(false);
            player.sendMessage(ChatColor.GREEN + "That rule will no longer ignore display names.");
        }
        else {
            rule.setIgnoringDisplayName(true);
            player.sendMessage(ChatColor.GREEN + "That rule will now ignore display names.");
        }
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("lore")
    @Description("Sets or resets the item's lore.")
    @Syntax("[...lore]")
    public void setLore(Player player, @Optional String value) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (Strings.isNullOrEmpty(value)) {
            player.sendMessage(ChatColor.GREEN + "Successfully removed lore.");
            rule.setLore(null);
        }
        else {
            player.sendMessage(ChatColor.GREEN + "Successfully changed lore.");
            rule.setLore(Arrays.stream(value.split(";")).map(TextUtil::parse).collect(Collectors.toList()));
        }
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("group")
    @Description("Sets or resets the exchange's group exclusivity.")
    @Syntax("[group]")
    public void setGroup(Player player, @Optional @Single String value) {
        if (!NameLayerGlue.isEnabled()) {
            throw new InvalidCommandArgument("NameLayer is not enabled.");
        }
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (Strings.isNullOrEmpty(value)) {
            player.sendMessage(ChatColor.GREEN + "Successfully removed Citadel group.");
            rule.setGroup(null);
        }
        else {
            if (rule.getType() != ExchangeRule.Type.INPUT) {
                throw new InvalidCommandArgument("You can only set that on input rules.");
            }
            Group group = GroupManager.getGroup(value);
            if (!NameLayerGlue.getEstablishPermission().hasAccess(group, player)) {
                throw new InvalidCommandArgument("You must enter a group you have permissions for.");
            }
            player.sendMessage(ChatColor.GREEN + "Successfully changed Citadel group.");
            rule.setGroup(group);
        }
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("switchio|switch|swap|swapio")
    @Description("Sets the amount of an exchange rule.")
    public void switchIO(Player player) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        player.sendMessage(ChatColor.GREEN + "Type successfully switched.");
        rule.switchIO();
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("durability|d")
    @Description("Sets the durability of an exchange rule.")
    @Syntax("<durability>")
    public void setDurability(Player player, @Optional @Single String value) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (Strings.isNullOrEmpty(value)) {
            rule.removeModifiers(DamageableModifier.SLUG);
        }
        else {
            DamageableModifier modifier = rule.findModifier(DamageableModifier.SLUG);
            if (modifier == null) {
                modifier = new DamageableModifier();
            }
            switch (value.toUpperCase()) {
                case "ANY":
                case "%":
                case "*": {
                    modifier.setDamage(ExchangeRule.ANY);
                    player.sendMessage(ChatColor.YELLOW + "Modifier will now accept any damage level.");
                    break;
                }
                case "DAMAGED":
                case "USED": {
                    modifier.setDamage(ExchangeRule.USED);
                    player.sendMessage(ChatColor.YELLOW + "Modifier will only accept damaged items.");
                    break;
                }
                default:
                    short durability = ExchangeRule.ERROR;
                    try {
                        durability = Short.parseShort(value);
                        if (durability < 0) {
                            durability = ExchangeRule.ERROR;
                        }
                    }
                    catch (NumberFormatException ignored) {
                    }
                    if (durability == ExchangeRule.ERROR) {
                        throw new InvalidCommandArgument("Please enter a valid durability.");
                    }
                    modifier.setDamage(durability);
                    player.sendMessage(ChatColor.YELLOW + "Successfully set a new damage level!");
                    break;
            }
        }
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

    @Subcommand("repair|repairlevel")
    @Description("Sets or resets the exchange's repair level.")
    @Syntax("[repair level]")
    public void setRepairLevel(Player player, @Optional @Single String value) {
        ExchangeRule rule = Utilities.ensureHoldingExchangeRule(player);
        if (Strings.isNullOrEmpty(value)) {
            rule.removeModifiers(RepairModifier.SLUG);
            player.sendMessage(ChatColor.GREEN + "Successfully removed repair level condition.");
        }
        else {
            RepairModifier modifier = rule.findModifier(RepairModifier.SLUG);
            if (modifier == null) {
                modifier = new RepairModifier();
            }
            if (value.startsWith("@")) {
                int level = NullCoalescing.chain(() -> Integer.parseInt(value.substring(1)), (int) ExchangeRule.ERROR);
                if (level < 2) {
                    throw new InvalidCommandArgument("You must enter a valid value, e.g: @9");
                }
                modifier.setRepairCost(level - 2);
            }
            else if (TextUtil.stringEqualsIgnoreCase(value, "NEW") || TextUtil.stringEqualsIgnoreCase(value, "MINT")) {
                modifier.setRepairCost(0);
            }
            else {
                int level = NullCoalescing.chain(() -> Integer.parseInt(value), (int) ExchangeRule.ERROR);
                if (level < 2) {
                    throw new InvalidCommandArgument("You must enter a valid value, e.g: 9");
                }
                modifier.setRepairCost((level - 2) * -1);
            }
            rule.addModifiers(modifier);
            player.sendMessage(ChatColor.GREEN + "Successfully changed repair level condition.");
        }
        Utilities.replaceHoldingExchangeRule(player, rule);
    }

}
