package net.civmc.zorweth;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class PhantomMembraneLoreListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPhantomDeath(final EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.PHANTOM) {
            return;
        }

        for (final ItemStack drop : event.getDrops()) {
            if (drop.getType() == Material.PHANTOM_MEMBRANE) {
                drop.editMeta(meta -> meta.lore(List.of(
                    Component.empty().append(Component.text("Found on Zorweth"))
                )));
            }
        }
    }
}
