package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.anvil.AnvilGuiListener;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class CountSelectionGui extends ItemSelectionGui {

    private final AnvilGui anvilGui;

    public CountSelectionGui(KitPvpDao dao, AnvilGui anvilGui, Player player, int slot, Kit kit, EditKitGui gui) {
        super(dao, "Change item count", player, slot, kit, gui::open, gui);
        this.anvilGui = anvilGui;
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        ItemStack kitItem = kit.items()[this.slot].clone();
        ItemStack custom = new ItemStack(Material.PAPER);
        ItemMeta customMeta = custom.getItemMeta();
        customMeta.itemName(Component.text("Custom amount", NamedTextColor.GOLD));
        custom.setItemMeta(customMeta);
        inventory.setSlot(new Clickable(custom) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                inventory.setOnClose(null);
                clicker.closeInventory();
                anvilGui.open(clicker, Component.text("Amount"), new AnvilGuiListener() {
                    @Override
                    public void onClose() {
                        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                        Bukkit.getScheduler().runTask(plugin, CountSelectionGui.this::open);
                    }

                    @Override
                    public boolean onRename(String name) {
                        int num;
                        try {
                            num = Integer.parseInt(name);
                            if (num < 1 || num > kitItem.getMaxStackSize()) {
                                clicker.sendMessage(Component.text("Must be between 1 and " + kitItem.getMaxStackSize(), NamedTextColor.RED));
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            clicker.sendMessage(Component.text("Must be a number", NamedTextColor.RED));
                            return false;
                        }

                        ItemStack[] items = kit.items().clone();
                        ItemStack item = kit.items()[slot];
                        item.setAmount(num);
                        gui.setLastItem(item);
                        items[slot] = item;

                        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                gui.updateKit(updatedKit);
                                inventory.setOnClose(null);
                                gui.open();
                            });
                        });
                        return true;
                    }
                });
            }
        }, 18);
        if (kitItem.getMaxStackSize() == 64) {
            kitItem.setAmount(1);
            inventory.setSlot(toClickable(kitItem.clone()), 19);
            kitItem.setAmount(8);
            inventory.setSlot(toClickable(kitItem.clone()), 20);
            kitItem.setAmount(16);
            inventory.setSlot(toClickable(kitItem.clone()), 21);
            kitItem.setAmount(32);
            inventory.setSlot(toClickable(kitItem.clone()), 22);
            kitItem.setAmount(64);
            inventory.setSlot(toClickable(kitItem.clone()), 23);
        } else if (kitItem.getMaxStackSize() == 16) {
            kitItem.setAmount(1);
            inventory.setSlot(toClickable(kitItem.clone()), 19);
            kitItem.setAmount(2);
            inventory.setSlot(toClickable(kitItem.clone()), 20);
            kitItem.setAmount(4);
            inventory.setSlot(toClickable(kitItem.clone()), 21);
            kitItem.setAmount(8);
            inventory.setSlot(toClickable(kitItem.clone()), 22);
            kitItem.setAmount(16);
            inventory.setSlot(toClickable(kitItem.clone()), 23);
        }
    }
}
