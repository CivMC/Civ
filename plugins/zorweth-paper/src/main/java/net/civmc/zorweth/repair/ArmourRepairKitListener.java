package net.civmc.zorweth.repair;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public final class ArmourRepairKitListener implements Listener {

    private static final double[] REPAIR_PERCENTAGES = {0.20D, 0.15D, 0.10D, 0.05D};

    private final NamespacedKey repairKitUsesKey;

    public ArmourRepairKitListener(final Plugin plugin) {
        Objects.requireNonNull(plugin);
        this.repairKitUsesKey = new NamespacedKey(plugin, "armour_repair_kit_uses");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPrepareItemCraft(final PrepareItemCraftEvent event) {
        final CraftingInventory inventory = event.getInventory();
        ItemStack armour = null;
        boolean hasRepairKit = false;

        for (final ItemStack item : inventory.getMatrix()) {
            if (item == null || item.isEmpty()) {
                continue;
            }
            if (CustomItem.isCustomItem(item, ArmourRepairKit.ARMOUR_REPAIR_KIT)) {
                if (hasRepairKit) {
                    inventory.setResult(null);
                    return;
                }
                hasRepairKit = true;
            } else if (armour == null) {
                armour = item;
            } else {
                inventory.setResult(null);
                return;
            }
        }

        if (!hasRepairKit) {
            return;
        }
        if (armour == null || !isRepairableArmour(armour)) {
            inventory.setResult(null);
            return;
        }

        inventory.setResult(repair(armour));
    }

    private ItemStack repair(final ItemStack armour) {
        final ItemStack result = armour.clone();
        result.editMeta(Damageable.class, meta -> {
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            final int uses = container.getOrDefault(this.repairKitUsesKey, PersistentDataType.INTEGER, 0);
            final int repairAmount = (int) Math.ceil(result.getType().getMaxDurability() * REPAIR_PERCENTAGES[uses]);
            meta.setDamage(Math.max(0, meta.getDamage() - repairAmount));
            container.set(this.repairKitUsesKey, PersistentDataType.INTEGER, uses + 1);
        });
        return result;
    }

    private boolean isRepairableArmour(final ItemStack item) {
        if (item.getAmount() != 1 || item.getType().getMaxDurability() <= 0 || !isArmour(item.getType())) {
            return false;
        }
        final ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof final Damageable damageable) || damageable.getDamage() <= 0) {
            return false;
        }
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(this.repairKitUsesKey, PersistentDataType.INTEGER, 0) < REPAIR_PERCENTAGES.length;
    }

    private boolean isArmour(final Material material) {
        return material.name().endsWith("_HELMET")
            || material.name().endsWith("_CHESTPLATE")
            || material.name().endsWith("_LEGGINGS")
            || material.name().endsWith("_BOOTS");
    }
}
