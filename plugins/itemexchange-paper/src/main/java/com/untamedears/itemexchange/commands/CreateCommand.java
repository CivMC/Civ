package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.events.BlockInventoryRequestEvent;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule.Type;
import com.untamedears.itemexchange.utility.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Commands class involved in creating shop exchange rules
 */
@CommandAlias(CreateCommand.ALIAS)
public final class CreateCommand extends BaseCommand {

    public static final String ALIAS = "iec|iecreate";
    public static final String ALIAS_INPUT_TYPES = "input|i|in|inputs";
    public static final String ALIAS_OUTPUT_TYPES = "output|o|out|outputs";
    public static final String CREATION_SUCCESS = ChatColor.GREEN + "Created exchange successfully.";

    // ------------------------------------------------------------
    // Creating from shop block
    // ------------------------------------------------------------

    @Default
    @Description("Creates an exchange rule based on a shop block.")
    public void createFromShop(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        if (!ItemExchangeConfig.canCreateFromShop()) {
            player.sendMessage(ChatColor.RED + "That command is disabled.");
            return;
        }
        BlockIterator ray = new BlockIterator(player, 6);
        while (ray.hasNext()) {
            Block block = ray.next();
            if (!WorldUtils.isValidBlock(block)) {
                continue;
            }
            if (!ItemExchangeConfig.hasCompatibleShopBlock(block.getType())) {
                // If the block is a full block then prevent the ray from going through walls.
                if (block.getType().isOccluding()) {
                    break;
                } else {
                    continue;
                }
            }
            Inventory inventory = BlockInventoryRequestEvent.emit(block, player,
                BlockInventoryRequestEvent.Purpose.ACCESS).getInventory();
            if (inventory == null) {
                player.sendMessage(ChatColor.RED + "You do not have access to that.");
                return;
            }
            ItemStack inputItem = null;
            ItemStack outputItem = null;
            for (ItemStack item : inventory.getContents()) {
                if (!ItemUtils.isValidItem(item)) {
                    continue;
                }
                if (inputItem == null) {
                    inputItem = item.clone();
                } else if (inputItem.isSimilar(item)) {
                    inputItem.setAmount(inputItem.getAmount() + item.getAmount());
                } else if (outputItem == null) {
                    outputItem = item.clone();
                } else if (outputItem.isSimilar(item)) {
                    outputItem.setAmount(outputItem.getAmount() + item.getAmount());
                } else {
                    player.sendMessage(ChatColor.RED + "Inventory should only contain two types of items!");
                    return;
                }
            }
            if (inputItem == null) {
                player.sendMessage(ChatColor.RED + "Inventory should have at least one type of item.");
                return;
            }
            if (Utilities.isExchangeRule(inputItem) || Utilities.isExchangeRule(outputItem)) {
                player.sendMessage(ChatColor.RED + "You cannot exchange rule blocks!");
                return;
            }
            ExchangeRule inputRule = new ExchangeRule(Type.INPUT, inputItem);
            if (outputItem == null) {
                Utilities.giveItemsOrDrop(inventory, inputRule.toItem());
            } else {
                ExchangeRule outputRule = new ExchangeRule(Type.OUTPUT, outputItem);
                Utilities.giveItemsOrDrop(inventory, inputRule.toItem(), outputRule.toItem());
            }
            player.sendMessage(CREATION_SUCCESS);
            return;
        }
        player.sendMessage(ChatColor.RED + "No block in view is a suitable shop block.");
    }

    // ------------------------------------------------------------
    // Creating from a held item
    // ------------------------------------------------------------

    public static final String HELD_DESCRIPTION = "Creates an exchange rule based on a held item.";

    private void createFromHeld(Player player, Type type) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isValidItem(held)) {
            throw new InvalidCommandArgument("You must be holding an item to do that.");
        }
        if (Utilities.isExchangeRule(held)) {
            throw new InvalidCommandArgument("You cannot create a rule from a rule.", false);
        }
        Utilities.givePlayerExchangeRule(player, new ExchangeRule(type, held));
        player.sendMessage(CREATION_SUCCESS);
    }

    @Subcommand(ALIAS_INPUT_TYPES)
    @Description(HELD_DESCRIPTION)
    public void createInputFromHeld(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        createFromHeld(player, Type.INPUT);
    }

    @Subcommand(ALIAS_OUTPUT_TYPES)
    @Description(HELD_DESCRIPTION)
    public void createOutputFromHeld(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        createFromHeld(player, Type.OUTPUT);
    }

    // ------------------------------------------------------------
    // Creating from explicit details
    // ------------------------------------------------------------

    public static final String DETAILS_SYNTAX = "<material> [amount]";

    public static final String DETAILS_DESCRIPTION = "Sets the material of an exchange rule.";

    public static final String DETAILS_COMPLETION = "@itemMaterials";

    private void createFromDetails(Player player, Type type, String slug, int amount) {
        Material material = MaterialUtils.getMaterial(slug);
        if (!ItemUtils.isValidItemMaterial(material)) {
            throw new InvalidCommandArgument("You must enter a valid item material.");
        }
        ExchangeRule rule = new ExchangeRule();
        rule.setType(type);
        rule.setMaterial(material);
        if (amount <= 0) {
            throw new InvalidCommandArgument("You must enter a valid amount.");
        }
        rule.setAmount(amount);
        Utilities.givePlayerExchangeRule(player, rule);
        player.sendMessage(CREATION_SUCCESS);
    }

    @Subcommand(ALIAS_INPUT_TYPES)
    @Syntax(DETAILS_SYNTAX)
    @Description(DETAILS_DESCRIPTION)
    @CommandCompletion(DETAILS_COMPLETION)
    public void createInputFromDetails(CommandSender sender, String slug, @Default("1") int amount) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        createFromDetails(player, Type.INPUT, slug, amount);
    }

    @Subcommand(ALIAS_OUTPUT_TYPES)
    @Syntax(DETAILS_SYNTAX)
    @Description(DETAILS_DESCRIPTION)
    @CommandCompletion(DETAILS_COMPLETION)
    public void createOutputFromDetails(CommandSender sender, String slug, @Default("1") int amount) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        createFromDetails(player, Type.OUTPUT, slug, amount);
    }

}
