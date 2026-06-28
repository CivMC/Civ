package net.civmc.zorweth;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.minelink.ctplus.CombatTagPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;

public final class PioneerCommand implements CommandExecutor {

    private static final long TRANSFER_TIMEOUT_TICKS = 150L;
    private static final int CONFIRM_SLOT = 11;
    private static final int INFO_SLOT = 13;
    private static final int CANCEL_SLOT = 15;

    private final ZorwethPlugin plugin;

    public PioneerCommand(final ZorwethPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        if (args.length > 0) {
            handleAdminCommand(sender, args);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!canPioneer(player)) {
            return true;
        }

        openConfirmation(player);
        return true;
    }

    private void handleAdminCommand(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("zorweth.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return;
        }

        if (!args[0].equalsIgnoreCase("clear")) {
            sender.sendMessage(Component.text("Usage: /pioneer", NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /pioneer clear <player>", NamedTextColor.RED));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /pioneer clear <player>", NamedTextColor.RED));
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player must be online.", NamedTextColor.RED));
            return;
        }

        target.getPersistentDataContainer().remove(RocketTransferKeys.PIONEER);
        sender.sendMessage(Component.text("Cleared pioneer flag for " + target.getName() + ".", NamedTextColor.GREEN));
        target.sendMessage(Component.text("Your Zorweth pioneer flag has been cleared.", NamedTextColor.GREEN));
    }

    private boolean canPioneer(final Player player) {
        if (hasPioneered(player)) {
            player.sendMessage(Component.text("You have already pioneered to Zorweth.", NamedTextColor.RED));
            return false;
        }

        if (isPearled(player)) {
            player.sendMessage(Component.text("You cannot pioneer while pearled.", NamedTextColor.RED));
            return false;
        }

        if (!this.plugin.getServerName().equals("main")) {
            player.sendMessage(Component.text("You can only pioneer from the main server.", NamedTextColor.RED));
            return false;
        }

        if (!player.getWorld().getName().equals(this.plugin.getSourceWorld())) {
            player.sendMessage(Component.text("You can only pioneer from the " + this.plugin.getSourceWorld()
                + " world.", NamedTextColor.RED));
            return false;
        }

        if (isCombatTagged(player)) {
            player.sendMessage(Component.text("You cannot pioneer while combat tagged.", NamedTextColor.RED));
            return false;
        }

        if (!isOnSurface(player)) {
            player.sendMessage(Component.text("You can only pioneer from the surface.", NamedTextColor.RED));
            return false;
        }

        if (hasPioneerEnded()) {
            player.sendMessage(Component.text("Pioneer transfers to Zorweth have ended.", NamedTextColor.RED));
            return false;
        }

        return true;
    }

    private boolean hasPioneered(final Player player) {
        return player.getPersistentDataContainer().has(RocketTransferKeys.PIONEER, PersistentDataType.BOOLEAN);
    }

    private boolean isPearled(final Player player) {
        return Bukkit.getPluginManager().isPluginEnabled("ExilePearl")
            && ExilePearlPlugin.getApi().getPearlManager().getPearl(player.getUniqueId()) != null;
    }

    private boolean isCombatTagged(final Player player) {
        return ((CombatTagPlus) Bukkit.getPluginManager().getPlugin("CombatTagPlus"))
            .getTagManager().isTagged(player.getUniqueId());
    }

    private boolean isOnSurface(final Player player) {
        final int surfaceY = player.getWorld().getHighestBlockYAt(player.getLocation(), HeightMap.WORLD_SURFACE);
        return player.getLocation().getBlockY() >= surfaceY;
    }

    private void openConfirmation(final Player player) {
        final ClickableInventory inventory = new ClickableInventory(27, "Zorweth Pioneer");
        inventory.setSlot(new Clickable(createConfirmItem()) {

            @Override
            public void clicked(final Player clicker) {
                ClickableInventory.forceCloseInventory(clicker);
                if (!canPioneer(clicker)) {
                    return;
                }
                clicker.sendMessage(Component.text("Preparing transfer to Zorweth.", NamedTextColor.GREEN));
                final UUID playerId = clicker.getUniqueId();
                Bukkit.getScheduler().runTaskAsynchronously(PioneerCommand.this.plugin, () -> prepareTransfer(playerId));
            }
        }, CONFIRM_SLOT);
        inventory.setSlot(new DecorationStack(createInfoItem()), INFO_SLOT);
        inventory.setSlot(new Clickable(createCancelItem()) {

            @Override
            public void clicked(final Player clicker) {
                ClickableInventory.forceCloseInventory(clicker);
            }
        }, CANCEL_SLOT);
        inventory.showInventory(player);
    }

    private boolean hasPioneerEnded() {
        final long endTimestamp = this.plugin.getPioneerEndTimestampMillis();
        return endTimestamp > 0 && System.currentTimeMillis() >= endTimestamp;
    }

    private ItemStack createConfirmItem() {
        final ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Confirm transfer", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("WARNING: THIS IS A ONE WAY TRIP", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("YOU WILL BE UNABLE TO RETURN,", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("AND YOUR INVENTORY WILL BE WIPED.", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("THIS IS NOT A NEWFRIEND FRIENDLY ENVIRONMENT.", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private ItemStack createInfoItem() {
        final ItemStack item = new ItemStack(Material.ENDER_EYE);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Become a Zorweth pioneer", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("WARNING: THIS IS A ONE WAY TRIP", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("YOU WILL BE UNABLE TO RETURN,", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("AND YOUR INVENTORY WILL BE WIPED.", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("THIS IS NOT A NEWFRIEND FRIENDLY ENVIRONMENT.", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private ItemStack createCancelItem() {
        final ItemStack item = new ItemStack(Material.RED_CONCRETE);
        item.editMeta(meta -> meta.displayName(Component.text("Cancel", NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    private void prepareTransfer(final UUID playerId) {
        try {
            this.plugin.getRocketTransferDao().setPlayerRoute(playerId, this.plugin.getDestinationServer());
            this.plugin.getLogger().log(Level.INFO, playerId + " has been routed to Zorweth");
        } catch (final SQLException exception) {
            this.plugin.getLogger().log(Level.INFO, "Failed to set pioneer route override", exception);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                final Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage(Component.text("Unable to prepare transfer to Zorweth. Please try again later.",
                        NamedTextColor.RED));
                }
            });
            return;
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> transfer(playerId));
    }

    private void transfer(final UUID id) {
        final Player player = Bukkit.getPlayer(id);
        if (player == null) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(
                online,
                Sound.BLOCK_BEACON_ACTIVATE,
                SoundCategory.MASTER,
                1,
                1
            );
        }

        player.getInventory().clear();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory inventory) {
            inventory.clear();
        }
        player.setLevel(0);
        player.setExp(0.0f);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setExhaustion(0.0f);
        player.getInventory().setHeldItemSlot(0);
        player.getPersistentDataContainer().set(RocketTransferKeys.PIONEER, PersistentDataType.BOOLEAN, true);
        this.plugin.getStasisHandler().putInStasis(player);
        connect(player, this.plugin.getDestinationServer());
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            final Player toKick = Bukkit.getPlayer(id);
            if (toKick != null) {
                toKick.kick(Component.text(this.plugin.getTransferFailureMessage(), NamedTextColor.RED));
            }
        }, TRANSFER_TIMEOUT_TICKS);
    }

    private void connect(final Player player, final String server) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(server);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }
}
