package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NameLockerFMListener implements Listener {

    private final NamespacedKey lockedKey;

    public NameLockerFMListener(NamespacedKey lockedKey) {
        this.lockedKey = lockedKey;
    }

    @EventHandler
    public void onTryWordbank(FactoryActivateEvent event) {
        if (!(event.getFactory() instanceof FurnCraftChestFactory fac)) return;
        if (!fac.getCurrentRecipe().getTypeIdentifier().equals("WORDBANK")) return;

        ItemStack itemToBeRenamed = fac.getInputInventory().getItem(0);

        if (itemToBeRenamed == null)
            return;

        ItemMeta itemMeta = itemToBeRenamed.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        if (pdc.has(lockedKey) && Boolean.TRUE.equals(pdc.get(lockedKey, PersistentDataType.BOOLEAN))) { // Item is locked
            event.getActivator().sendMessage(Component.text()
                .color(NamedTextColor.RED)
                .content("You cannot wordbank name-locked items"));
            event.setCancelled(true);
        }
    }
}
