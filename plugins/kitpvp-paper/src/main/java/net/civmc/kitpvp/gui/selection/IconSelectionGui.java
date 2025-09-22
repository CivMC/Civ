package net.civmc.kitpvp.gui.selection;

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

public class IconSelectionGui {

    private final KitPvpDao dao;
    private final Player player;
    private final EditKitGui gui;
    private final Kit kit;

    public IconSelectionGui(KitPvpDao dao, Player player, Kit kit, EditKitGui gui) {
        this.dao = dao;
        this.player = player;
        this.gui = gui;
        this.kit = kit;
    }

    public void open() {
        ClickableInventory inventory = new ClickableInventory(54, "Icon");

        int slot = 0;
        for (Material m : new Material[]{
            Material.IRON_SWORD,
            Material.IRON_AXE,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_AXE,
            Material.NETHERITE_SWORD,
            Material.NETHERITE_AXE,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS,
            Material.ELYTRA,
            Material.CROSSBOW,
            Material.BOW,
            Material.COBWEB,
            Material.TNT_MINECART,
            Material.SUGAR,
            Material.SPLASH_POTION,
            Material.POTION,
            Material.GOLDEN_APPLE,
            Material.ENDER_PEARL,
            Material.SHIELD,
            Material.CHORUS_FRUIT,
            Material.WATER_BUCKET,
            Material.OBSIDIAN,
        }) {
            inventory.setSlot(getSlot(inventory, m), slot++);
        }

        inventory.setOnClose(gui::open);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.itemName(Component.text("Back", NamedTextColor.GRAY));
        back.setItemMeta(backMeta);
        inventory.setSlot(new LClickable(back, p -> Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
            inventory.setOnClose(null);
            gui.open();
        })), 45);

        inventory.showInventory(player);
    }

    private Clickable getSlot(ClickableInventory inventory, Material icon) {
        ItemStack item = new ItemStack(icon);

        return new Clickable(item) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Kit updatedKit = dao.updateKit(kit.id(), icon, kit.items());
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
