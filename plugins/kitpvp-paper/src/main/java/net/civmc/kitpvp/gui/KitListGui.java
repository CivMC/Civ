package net.civmc.kitpvp.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.civmc.kitpvp.KitApplier;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.FastMultiPageView;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class KitListGui {

    private static final ItemStack CREATE_KIT_ITEM;

    static {
        CREATE_KIT_ITEM = new ItemStack(Material.PAPER);
        ItemMeta meta = CREATE_KIT_ITEM.getItemMeta();
        meta.itemName(Component.text("Create new kit", NamedTextColor.GREEN));
        CREATE_KIT_ITEM.setItemMeta(meta);
    }

    private final KitPvpDao dao;
    private final Player player;
    private final List<Kit> kits = new ArrayList<>();
    private final FastMultiPageView view;

    private boolean ready = false;
    private boolean openWhenReady = true;

    public KitListGui(KitPvpDao dao, Player player) {
        this.dao = dao;
        this.player = player;
        this.view = new FastMultiPageView(player, this::kitSupplier, "Kits", 6);
        this.view.setMenuSlot(new Clickable(CREATE_KIT_ITEM) {
            @Override
            protected void clicked(@NotNull Player clicker) {

            }
        }, 0);

        invalidate();
    }

    public void invalidate() {
        this.ready = false;
        KitPvpPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Kit> playerKits = this.dao.getKits(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                this.kits.addAll(playerKits.stream().sorted(
                        Comparator
                            .comparing(Kit::isPublic)
                            .thenComparing(Kit::name))
                    .toList());
                if (this.openWhenReady) {
                    this.ready = true;
                    this.openWhenReady = false;
                    this.view.showScreen(false);
                }
            });
        });
    }

    public void open() {
        if (this.ready) {
            this.view.showScreen();
        } else {
            this.openWhenReady = true;
        }
    }

    private List<IClickable> kitSupplier(int start, int max) {
        List<IClickable> clickables = new ArrayList<>();
        for (int i = start; i <= Math.min(this.kits.size() - 1, start + max); i++) {
            Kit kit = kits.get(i);
            ItemStack icon = new ItemStack(kit.icon());
            ItemMeta iconMeta = icon.getItemMeta();
            if (kit.isPublic()) {
                iconMeta.itemName(Component.text("Public Kit", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .appendSpace().append(Component.text(kit.name(), NamedTextColor.GREEN)));
            } else {
                iconMeta.itemName(Component.text(kit.name(), NamedTextColor.GREEN));
            }
            if (kit.isPublic() && !player.hasPermission("kitpvp.admin")) {
                iconMeta.lore(List.of(
                    Component.text("Left click to load", NamedTextColor.AQUA)
                    ));
                icon.setItemMeta(iconMeta);
                clickables.add(new Clickable(icon) {
                    @Override
                    protected void clicked(@NotNull Player clicker) {
                        clicker.closeInventory();
                        KitApplier.applyKit(kit, clicker);
                    }
                });
            } else {
                iconMeta.lore(List.of(
                    Component.text("Left click to load", NamedTextColor.AQUA),
                    Component.text("Right click to edit", NamedTextColor.AQUA),
                    Component.text("Middle click to delete", NamedTextColor.AQUA)
                    ));
                icon.setItemMeta(iconMeta);
                clickables.add(new Clickable(icon) {
                    @Override
                    protected void clicked(@NotNull Player clicker) {
                        clicker.closeInventory();
                        KitApplier.applyKit(kit, clicker);
                    }

                    @Override
                    protected void onRightClick(@NotNull Player clicker) {
                        new EditKitGui(KitListGui.this.dao, clicker, kit, KitListGui.this);
                    }

                    @Override
                    protected void onMiddleClick(@NotNull Player clicker) {
                        new ConfirmDeletionGui(KitListGui.this.dao, clicker, kit, KitListGui.this.view);
                    }
                });
            }
        }
        return clickables;
    }
}
