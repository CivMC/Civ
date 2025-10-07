package net.civmc.kitpvp.gui;

import com.google.common.collect.HashMultimap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import net.civmc.kitpvp.KitApplier;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.anvil.AnvilGuiListener;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.ranked.RankedDao;
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
        meta.itemName(Component.text("Create new kit", NamedTextColor.GOLD));
        CREATE_KIT_ITEM.setItemMeta(meta);
    }

    private final KitPvpDao dao;
    private final RankedDao rankedDao;
    private int rankedKit;
    private final AnvilGui anvilGui;
    private final Player player;
    private final List<Kit> kits = new ArrayList<>();
    private final FastMultiPageView view;

    private boolean ready = false;
    private boolean openWhenReady = true;

    public KitListGui(KitPvpDao dao, RankedDao rankedDao, AnvilGui anvilGui, Player player) {
        this.dao = dao;
        this.rankedDao = rankedDao;
        this.anvilGui = anvilGui;
        this.player = player;
        this.view = new FastMultiPageView(player, this::kitSupplier, "Kits", 6);
        this.view.setMenuSlot(new Clickable(CREATE_KIT_ITEM) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                if (!clicker.hasPermission("kitpvp.admin")) {
                    int ownedKits = 0;
                    for (Kit kit : kits) {
                        if (!kit.isPublic()) {
                            ownedKits++;
                        }
                    }
                    if (ownedKits >= 50) {
                        clicker.sendMessage(Component.text("You cannot create any more kits!", NamedTextColor.RED));
                        clicker.closeInventory();
                        return;
                    }
                }

                clicker.closeInventory();
                clicker.sendMessage(Component.text("Enter kit name to create", NamedTextColor.GOLD));
                anvilGui.open(player, Component.text("Kit name"), new AnvilGuiListener() {
                    @Override
                    public void onClose() {
                        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                        Bukkit.getScheduler().runTask(plugin, KitListGui.this::open);
                    }

                    @Override
                    public boolean onRename(String name) {
                        if (!Kit.checkValidName(player, name)) {
                            return false;
                        }
                        JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

                            Kit createdKit;
                            try {
                                createdKit = dao.createKit(name, player.getUniqueId());
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING, "Error creating kit", e);
                                return;
                            }
                            if (createdKit == null) {
                                player.sendMessage(Component.text("A kit with that name already exists", NamedTextColor.RED));
                                return;
                            }

                            int rankedKit = rankedDao.getKit(player.getUniqueId());

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                invalidate();
                                new EditKitGui(KitListGui.this.dao, rankedDao, rankedKit, anvilGui, clicker, createdKit, KitListGui.this);
                            });
                        });
                        return true;
                    }
                });
            }
        }, 0);

        invalidate();
    }

    public void invalidate() {
        this.ready = false;
        KitPvpPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Kit> playerKits = this.dao.getKits(player.getUniqueId());
            int rankedKit = this.rankedDao.getKit(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                this.kits.clear();
                this.kits.addAll(playerKits.stream().sorted(
                        Comparator
                            .comparing(Kit::isPublic).reversed()
                            .thenComparing(k -> k.name().toLowerCase()))
                    .toList());
                this.rankedKit = rankedKit;
                this.ready = true;
                if (this.openWhenReady) {
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
            iconMeta.setAttributeModifiers(HashMultimap.create());
            if (kit.isPublic()) {
                iconMeta.itemName(Component.text("Public Kit", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .appendSpace().append(Component.text(kit.name(), NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false)));
                iconMeta.setEnchantmentGlintOverride(true);
            } else {
                iconMeta.itemName(Component.text(kit.name(), NamedTextColor.GREEN));
            }
            if (kit.isPublic() && !player.hasPermission("kitpvp.admin")) {
                iconMeta.lore(List.of(
                    Component.text("Left click to load", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
                    Component.text("Right click to view", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
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
                        new EditKitGui(KitListGui.this.dao, KitListGui.this.rankedDao, rankedKit, KitListGui.this.anvilGui, clicker, kit, KitListGui.this);
                    }
                });
            } else {
                iconMeta.lore(List.of(
                    Component.text("Left click to load", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
                    Component.text("Right click to edit", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
                    Component.text("Shift right click to delete", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
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
                        new EditKitGui(KitListGui.this.dao, KitListGui.this.rankedDao, rankedKit, KitListGui.this.anvilGui, clicker, kit, KitListGui.this);
                    }

                    @Override
                    protected void onShiftRightClick(@NotNull Player clicker) {
                        new ConfirmDeletionGui(KitListGui.this.dao, clicker, kit, KitListGui.this.view);
                    }
                });
            }
        }
        return clickables;
    }
}
