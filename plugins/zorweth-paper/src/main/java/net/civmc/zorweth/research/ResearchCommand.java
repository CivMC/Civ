package net.civmc.zorweth.research;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.civmc.zorweth.RainbowArmour;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;

public final class ResearchCommand implements CommandExecutor {

    public static final String MOON_PREFIX_PERMISSION = "civchat.prefix.moon";
    private static final int BALANCE_SLOT = 4;
    private static final int MOON_PURCHASE_SLOT = 22;

    private final JavaPlugin plugin;
    private final ResearchCurrency currency;
    private final boolean enabled;
    private final Set<UUID> pendingPurchases = new HashSet<>();

    public ResearchCommand(final JavaPlugin plugin, final ResearchCurrency currency, final boolean enabled) {
        this.plugin = plugin;
        this.currency = currency;
        this.enabled = enabled;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!this.enabled) {
            player.sendMessage(Component.text("Research purchases are not available on this server.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            open(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("deposit")) {
            deposit(player);
            return true;
        }
        player.sendMessage(Component.text("Usage: /research [deposit]", NamedTextColor.RED));
        return true;
    }

    private void deposit(final Player player) {
        final ItemStack held = player.getInventory().getItemInMainHand();
        if (!ResearchCurrency.isResearchNote(held)) {
            player.sendMessage(Component.text("Hold a stack of Research notes to deposit it.", NamedTextColor.RED));
            return;
        }
        final int amount = held.getAmount();
        this.currency.deposit(player, amount);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("Deposited " + amount + " research token" + (amount == 1 ? "" : "s")
            + ". Balance: " + this.currency.getBalance(player), NamedTextColor.GREEN));
    }

    private void open(final Player player) {
        final ClickableInventory inventory = new ClickableInventory(27, "Research");
        inventory.setSlot(new DecorationStack(createBalanceItem(player)), BALANCE_SLOT);
        inventory.setSlot(new Clickable(createPurchaseItem(player)) {

            @Override
            public void clicked(final Player clicker) {
                purchaseMoonPrefix(clicker);
            }
        }, MOON_PURCHASE_SLOT);
        addArmourPurchase(inventory, player, RainbowArmour.RAINBOW_HELMET, "Rainbow Helmet", 10);
        addArmourPurchase(inventory, player, RainbowArmour.RAINBOW_CHESTPLATE, "Rainbow Chestplate", 12);
        addArmourPurchase(inventory, player, RainbowArmour.RAINBOW_LEGGINGS, "Rainbow Leggings", 14);
        addArmourPurchase(inventory, player, RainbowArmour.RAINBOW_BOOTS, "Rainbow Boots", 16);
        inventory.showInventory(player);
    }

    private void addArmourPurchase(final ClickableInventory inventory, final Player player, final String itemKey,
                                   final String itemName, final int slot) {
        inventory.setSlot(new Clickable(createArmourPurchaseItem(player, itemKey)) {

            @Override
            public void clicked(final Player clicker) {
                purchaseArmour(clicker, itemKey, itemName);
            }
        }, slot);
    }

    private ItemStack createBalanceItem(final Player player) {
        final ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> {
            meta.displayName(noItalics(Component.text("Research Tokens", NamedTextColor.GOLD)));
            meta.lore(List.of(noItalics(Component.text("Balance: " + this.currency.getBalance(player),
                    NamedTextColor.GRAY)),
                noItalics(Component.text("To gain research tokens, hold research notes and type /research deposit", NamedTextColor.GRAY))));
        });
        return item;
    }

    private ItemStack createPurchaseItem(final Player player) {
        final boolean owned = player.hasPermission(MOON_PREFIX_PERMISSION);
        final boolean affordable = this.currency.getBalance(player) >= ResearchCurrency.MOON_COST;
        final ItemStack item = new ItemStack(owned ? Material.GOLD_BLOCK : Material.GOLD_INGOT);
        item.editMeta(meta -> {
            meta.displayName(noItalics(Component.text("Moon Prefix ☾", NamedTextColor.GOLD)));
            meta.lore(List.of(
                noItalics(Component.text("Cost: " + ResearchCurrency.MOON_COST + " research tokens",
                    NamedTextColor.GRAY)),
                noItalics(Component.text(owned ? "Already purchased" : affordable ? "Click to purchase" :
                                                                       "Not enough research tokens", owned || affordable ? NamedTextColor.GREEN : NamedTextColor.RED))));
        });
        return item;
    }

    private ItemStack createArmourPurchaseItem(final Player player, final String itemKey) {
        final ItemStack item = CustomItem.getCustomItem(itemKey);
        if (item == null) {
            return new ItemStack(Material.BARRIER);
        }
        final boolean affordable = this.currency.getBalance(player) >= ResearchCurrency.RAINBOW_ARMOUR_COST;
        item.editMeta(meta -> meta.lore(List.of(
            noItalics(Component.text("Cost: " + ResearchCurrency.RAINBOW_ARMOUR_COST + " research tokens",
                NamedTextColor.GRAY)),
            noItalics(Component.text(affordable ? "Click to purchase" : "Not enough research tokens",
                affordable ? NamedTextColor.GREEN : NamedTextColor.RED)))));
        return item;
    }

    private Component noItalics(final Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    private void purchaseMoonPrefix(final Player player) {
        final User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(Component.text("Unable to load user. Please try again.", NamedTextColor.RED));
            open(player);
            return;
        }
        if (player.hasPermission(MOON_PREFIX_PERMISSION)) {
            player.sendMessage(Component.text("You already own the moon prefix.", NamedTextColor.RED));
            return;
        }
        if (!this.pendingPurchases.add(player.getUniqueId())) {
            player.sendMessage(Component.text("Your moon prefix purchase is already being processed.",
                NamedTextColor.RED));
            return;
        }
        if (!this.currency.withdraw(player, ResearchCurrency.MOON_COST)) {
            this.pendingPurchases.remove(player.getUniqueId());
            player.sendMessage(Component.text("You need " + ResearchCurrency.MOON_COST
                + " research tokens to purchase this prefix.", NamedTextColor.RED));
            return;
        }
        ClickableInventory.forceCloseInventory(player);
        final PermissionNode node = PermissionNode.builder(MOON_PREFIX_PERMISSION).build();
        user.data().add(node);
        LuckPermsProvider.get().getUserManager().saveUser(user).whenComplete((result, error) ->
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                this.pendingPurchases.remove(player.getUniqueId());
                if (error != null) {
                    user.data().remove(node);
                    this.currency.deposit(player, ResearchCurrency.MOON_COST);
                    player.sendMessage(Component.text("Unable to save your moon prefix. Your tokens were refunded.",
                        NamedTextColor.RED));
                } else {
                    player.sendMessage(Component.text("You purchased the moon prefix.", NamedTextColor.GREEN));
                }
                if (player.isOnline()) {
                    open(player);
                }
            }));
    }

    private void purchaseArmour(final Player player, final String itemKey, final String itemName) {
        final ItemStack item = CustomItem.getCustomItem(itemKey);
        if (item == null) {
            player.sendMessage(Component.text("This armour is currently unavailable.", NamedTextColor.RED));
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.text("You need an empty inventory slot.", NamedTextColor.RED));
            return;
        }
        if (!this.currency.withdraw(player, ResearchCurrency.RAINBOW_ARMOUR_COST)) {
            player.sendMessage(Component.text("You need " + ResearchCurrency.RAINBOW_ARMOUR_COST
                + " research tokens to purchase this armour.", NamedTextColor.RED));
            return;
        }
        if (!InventoryUtils.safelyAddItemsToInventory(player.getInventory(), new ItemStack[] {item})) {
            this.currency.deposit(player, ResearchCurrency.RAINBOW_ARMOUR_COST);
            player.sendMessage(Component.text("Unable to add the armour.",
                NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text("You purchased a " + itemName + ".", NamedTextColor.GREEN));
        open(player);
    }
}
