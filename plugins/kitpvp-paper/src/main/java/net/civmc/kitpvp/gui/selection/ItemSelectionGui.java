package net.civmc.kitpvp.gui.selection;

import com.github.maxopoly.finale.Finale;
import net.civmc.kitpvp.KitPvpPlugin;
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
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;

public abstract class ItemSelectionGui {
    protected final KitPvpDao dao;
    private final String title;
    private final Player player;
    private final Runnable parent;
    protected final EditKitGui gui;
    protected final int slot;
    protected final Kit kit;
    private ClickableInventory inventory;


    public ItemSelectionGui(KitPvpDao dao, String title, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        this.dao = dao;
        this.title = title;
        this.player = player;
        this.parent = parent;
        this.gui = gui;
        this.slot = slot;
        this.kit = kit;
    }

    public void open() {
        inventory = new ClickableInventory(54, title);
        addItems(inventory);

        inventory.setOnClose(parent);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.itemName(Component.text("Back", NamedTextColor.GRAY));
        back.setItemMeta(backMeta);
        inventory.setSlot(new LClickable(back, p -> openParent()), 45);

        inventory.showInventory(player);
    }

    private void openParent() {
        Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
            inventory.setOnClose(null);
            this.parent.run();
        });
    }

    public abstract void addItems(ClickableInventory inventory);

    protected Clickable toClickable(ItemStack item) {
        return toClickable(item, item);
    }

    protected Clickable toClickable(ItemStack displayItem, ItemStack actualItem) {
        ItemStack cloned = displayItem == null ? null : displayItem.clone();
        if (cloned != null) {
            Finale.getPlugin().update(cloned);
        }
        ItemStack actualCloned = actualItem == null ? null : actualItem.clone();
        if (actualCloned != null) {
            Finale.getPlugin().update(actualCloned);
        }
        return new Clickable(cloned) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                ItemStack[] items = kit.items().clone();
                items[slot] = actualCloned;
                if (actualCloned != null) {
                    gui.setLastItem(actualCloned);
                }

                JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        gui.updateKit(updatedKit);
                        inventory.setOnClose(null);
                        gui.open();
                    });
                });
            }
        };
    }
}
