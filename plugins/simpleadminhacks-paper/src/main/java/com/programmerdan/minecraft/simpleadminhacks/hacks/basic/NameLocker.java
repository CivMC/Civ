package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NameLocker extends BasicHack {

    final NamespacedKey lockedKey = new NamespacedKey(plugin, "name-locked");

    public NameLocker(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (plugin.getServer().getPluginManager().getPlugin("FactoryMod") != null) {
            plugin.getServer()
                .getPluginManager()
                .registerEvents(
                    new NameLockerFMListener(lockedKey), plugin);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onPrepareLock(PrepareAnvilEvent event) {
        if (event.getInventory().getSecondItem() == null || !event.getInventory().getSecondItem().getType().equals(Material.HONEYCOMB))
            return;

        ItemStack itemToBeLocked = event.getInventory().getFirstItem();

        if (itemToBeLocked == null) return;

        ItemMeta itemMeta = itemToBeLocked.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        if (pdc.has(lockedKey) && Boolean.TRUE.equals(pdc.get(lockedKey, PersistentDataType.BOOLEAN))) { // Item is locked
            event.getView().setRepairCost(AnvilMenu.DEFAULT_DENIED_COST); // Cheap trick to get the X because it's nice.
            event.setResult(ItemStack.empty());
        } else if (!pdc.has(lockedKey)) {
            // We can't put honeycomb on anything so we have to manually do the result setup.
            ItemStack result = event.getInventory().getFirstItem().clone();
            result.editMeta(meta -> {
                meta.setDisplayName(event.getView().getRenameText());
                meta.getPersistentDataContainer().set(lockedKey, PersistentDataType.BOOLEAN, true);
                meta.lore(List.of(Component.text().content("Name-locked").build())); // Just to make it clear. If we can change the message in the anvil this should be removed.
            });
            event.getView().setRepairCost(1); // We need to do this so the client will let the user withdraw the item
            event.setResult(result);
        }
    }

    @EventHandler
    public void onExecuteLock(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL || event.getRawSlot() != 2) return;
        ItemStack honeyComb = event.getInventory().getItem(1);
        if (honeyComb == null || !honeyComb.getType().equals(Material.HONEYCOMB))
            return;

        if (honeyComb.getAmount() > 1) {
            honeyComb.setAmount(honeyComb.getAmount() - 1);
            ((Player) event.getWhoClicked()).give(honeyComb); // Items in the second slot of an anvil are deleted AFTER InventoryClickEvent so we can't decrement the stack size
        }
    }

    @EventHandler
    public void onTryRename(PrepareAnvilEvent event) {
        ItemStack itemToBeRenamed = event.getInventory().getFirstItem();
        ItemStack resultItem = event.getInventory().getResult();
        if (itemToBeRenamed == null
            || resultItem == null)
            return;

        ItemMeta itemMeta = itemToBeRenamed.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        checkItem(event, itemToBeRenamed, resultItem, pdc);

        // Now check the other way. This only applies for tools where you can combine them
        itemToBeRenamed = event.getInventory().getSecondItem();
        if (itemToBeRenamed == null)
            return;

        itemMeta = itemToBeRenamed.getItemMeta();
        pdc = itemMeta.getPersistentDataContainer();
        checkItem(event, itemToBeRenamed, resultItem, pdc);
    }

    private void checkItem(PrepareAnvilEvent event, ItemStack itemToBeRenamed, ItemStack resultItem, PersistentDataContainer pdc) {
        if (pdc.has(lockedKey) && Boolean.TRUE.equals(pdc.get(lockedKey, PersistentDataType.BOOLEAN))) { // Item is locked
            if (Objects.equals(itemToBeRenamed.effectiveName().compact(), resultItem.effectiveName().compact()))
                return; // No name change, no enchant change (text colour white->aqua)
            if (Objects.equals(itemToBeRenamed.effectiveName().compact().color(NamedTextColor.AQUA).compact(), resultItem.effectiveName().compact()))
                return; // No name change, but we are adding an enchantment so the text colour changes
            event.getView().setRepairCost(AnvilMenu.DEFAULT_DENIED_COST);
            event.setResult(ItemStack.empty());
            // Can we change the text? Like "Too Expensive!" - > "Can't Rename!"?
        }
    }
}
