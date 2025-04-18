package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MapCopyProtection extends BasicHack implements CommandExecutor {

    private static final String COPY_PROTECTED = "Copy protected by ";

    final NamespacedKey copyKey = new NamespacedKey(plugin, "copy-protected");

    public MapCopyProtection(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin().registerCommand("copyprotect", this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onMapCopyCartography(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof CartographyInventory) || event.getSlotType() != InventoryType.SlotType.RESULT)
            return;

        ItemStack item = event.getInventory().getItem(1); // paper, glass pane, blank map slot

        if (item == null) return;

        if (item.getType() == Material.GLASS_PANE || item.getType() == Material.PAPER)
            return; // Allow locking and scaling of copy protected maps

        handleCopyProtection(event);
    }

    @EventHandler
    public void onMapCopyCrafting(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof CraftInventoryCrafting) || event.getSlotType() != InventoryType.SlotType.RESULT)
            return;

        handleCopyProtection(event);
    }

    private void handleCopyProtection(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (isCopy(item) && !isCreatorOfCopy(event.getWhoClicked().getName(), item)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(
                Component.text()
                    .color(NamedTextColor.RED)
                    .content("You can not clone copy protected maps")
            );
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                Component.text()
                    .color(NamedTextColor.RED)
                    .content("This command can only be run by players")
            );
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.FILLED_MAP) {
            sender.sendMessage(
                Component.text()
                    .color(NamedTextColor.RED)
                    .content("Only filled maps can be copy protected")
            );
            return true; // Command failed, but was syntactically correct, so no need to tell the player how to use it
        }

        if (isCopy(item)) {
            sender.sendMessage(
                Component.text()
                    .color(NamedTextColor.RED)
                    .content("You can not copy protect copy protected maps")
            );
            return true; // Command failed, but was syntactically correct, so no need to tell the player how to use it
        }

        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.getPersistentDataContainer().set(copyKey, PersistentDataType.INTEGER, 1);

        itemMeta.lore(List.of(Component.text("%s%s".formatted(COPY_PROTECTED, sender.getName()))));

        player.getInventory().getItemInMainHand().setItemMeta(itemMeta);

        return true;
    }

    private boolean isCopy(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(copyKey, PersistentDataType.INTEGER);
    }

    private boolean isCreatorOfCopy(final String playerName, final ItemStack item) {
        return Optional.ofNullable(item.getItemMeta().lore())
            .flatMap(lores -> lores.stream()
                .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                .filter(lore -> lore.contains(COPY_PROTECTED))
                .map(lore -> lore.replace(COPY_PROTECTED, ""))
                .map(playerName::equals)
                .findFirst())
            .orElse(false);
    }
}
