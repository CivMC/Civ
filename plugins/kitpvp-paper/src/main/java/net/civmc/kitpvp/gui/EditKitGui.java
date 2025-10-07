package net.civmc.kitpvp.gui;

import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.anvil.AnvilGuiListener;
import net.civmc.kitpvp.gui.selection.ArmourSlotSelectionGui;
import net.civmc.kitpvp.gui.selection.CountSelectionGui;
import net.civmc.kitpvp.gui.selection.EnchantmentGui;
import net.civmc.kitpvp.gui.selection.IconSelectionGui;
import net.civmc.kitpvp.gui.selection.ItemCategorySelectionGui;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.kit.KitCost;
import net.civmc.kitpvp.ranked.RankedDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;

public class EditKitGui {

    private final KitPvpDao dao;
    private final RankedDao rankedDao;
    private int rankedKit;
    private final AnvilGui anvilGui;
    private final Player player;
    private Kit kit;
    private ItemStack lastItem;
    private final KitListGui gui;
    private final boolean canEdit;

    public EditKitGui(KitPvpDao dao, RankedDao rankedDao, int rankedKit, AnvilGui anvilGui, Player player, Kit kit, KitListGui gui) {
        this.dao = dao;
        this.rankedDao = rankedDao;
        this.rankedKit = rankedKit;
        this.anvilGui = anvilGui;
        this.player = player;
        this.kit = kit;
        this.gui = gui;
        this.canEdit = !kit.isPublic() || player.hasPermission("kitpvp.admin");
        open();
    }

    public void updateKit(Kit kit) {
        this.kit = kit;
        this.gui.invalidate();
    }

    public void setLastItem(ItemStack lastItem) {
        this.lastItem = lastItem;
    }

    public void open() {
        ClickableInventory inventory = new ClickableInventory(54, (canEdit ? "Editing " : "Viewing ") + kit.name());

        inventory.setOnClose(gui::open);

        ItemStack copy = new ItemStack(Material.OAK_SIGN);
        ItemMeta copyMeta = copy.getItemMeta();
        copyMeta.itemName(Component.text("Copy kit", NamedTextColor.GOLD));
        copyMeta.lore(List.of(
            Component.text("To copy this kit, anyone can run:", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
            Component.text("/kit copy " + (kit.isPublic() ? "public" : player.getName()) + " " + kit.name(), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
        copy.setItemMeta(copyMeta);
        inventory.setItem(copy, 0);

        ItemStack rankedKit = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta rankedKitMeta = rankedKit.getItemMeta();
        rankedKitMeta.itemName(Component.text("Select as ranked kit", NamedTextColor.GOLD));
        List<TextComponent> lore = new ArrayList<>();
        if (kit.id() == this.rankedKit) {
            rankedKitMeta.setEnchantmentGlintOverride(true);
            lore.add(Component.text("Currently selected", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty().append(Component.text("Maximum " + KitCost.MAX_POINTS + " points", NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
        int cost = KitCost.getCost(kit.items());
        lore.add(Component.empty().append(Component.text("Current cost: ", NamedTextColor.GOLD).append(Component.text(cost + " points", NamedTextColor.YELLOW))).decoration(TextDecoration.ITALIC, false));
        if (cost > KitCost.MAX_POINTS) {
            lore.add(Component.text("Kit is too expensive for ranked!", NamedTextColor.RED));
        }
        rankedKitMeta.lore(lore);
        rankedKit.setItemMeta(rankedKitMeta);
        inventory.setSlot(new Clickable(rankedKit) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                if (!kit.name().equals(EditKitGui.this.rankedKit)) {
                    JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        rankedDao.setKit(player.getUniqueId(), kit.id());
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            EditKitGui.this.rankedKit = kit.id();
                            inventory.setOnClose(null);
                            open();
                        });
                    });
                }
            }
        }, 1);

        if (player.hasPermission("kitpvp.admin")) {
            ItemStack isPublic = new ItemStack(kit.isPublic() ? Material.OAK_FENCE : Material.OAK_FENCE_GATE);
            ItemMeta isPublicMeta = isPublic.getItemMeta();
            if (kit.isPublic()) {
                isPublicMeta.itemName(Component.text("Make kit private", NamedTextColor.GOLD));
            } else {
                isPublicMeta.itemName(Component.text("Make kit public", NamedTextColor.GOLD));
            }
            isPublic.setItemMeta(isPublicMeta);
            boolean kitIsPublic = kit.isPublic();
            inventory.setSlot(new Clickable(isPublic) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        Kit updatedKit = dao.setPublicKit(kit.id(), !kitIsPublic);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            updateKit(updatedKit);
                            inventory.setOnClose(null);
                            open();
                        });
                    });
                }
            }, 6);
        }
        if (canEdit) {
            ItemStack rename = new ItemStack(Material.NAME_TAG);
            ItemMeta renameMeta = rename.getItemMeta();
            renameMeta.itemName(Component.text("Rename kit", NamedTextColor.GOLD));
            rename.setItemMeta(renameMeta);
            inventory.setSlot(new Clickable(rename) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    inventory.setOnClose(null);
                    player.closeInventory();
                    anvilGui.open(player, Component.text("Kit name"), new AnvilGuiListener() {
                        @Override
                        public void onClose() {
                            JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                            Bukkit.getScheduler().runTask(plugin, EditKitGui.this::open);
                        }

                        @Override
                        public boolean onRename(String name) {
                            if (!Kit.checkValidName(player, name)) {
                                return false;
                            }
                            JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                Kit renamedKit;
                                try {
                                    renamedKit = dao.renameKit(kit.id(), name);
                                } catch (Exception e) {
                                    JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error renaming kit", e);
                                    return;
                                }
                                if (renamedKit == null) {
                                    player.sendMessage(Component.text("A kit with that name already exists", NamedTextColor.RED));
                                    return;
                                }
                                player.sendMessage(Component.text("Renamed kit to: %s".formatted(renamedKit.name()), NamedTextColor.GOLD));
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    updateKit(renamedKit);
                                    open();
                                });
                            });
                            return true;
                        }
                    });
                }
            }, 7);

            ItemStack guide = new ItemStack(Material.PAPER);
            ItemMeta guideMeta = guide.getItemMeta();
            guideMeta.itemName(Component.text("Help", NamedTextColor.GOLD, TextDecoration.BOLD));
            guideMeta.lore(List.of(
                Component.text("Left click to change item", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Shift left click to remove item", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Right click to enchant or change count", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Shift right click to copy last item", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
            ));
            guide.setItemMeta(guideMeta);
            inventory.setItem(guide, 8);
        }

        if (canEdit) {
            ItemStack icon = new ItemStack(Material.ITEM_FRAME);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.itemName(Component.text("Set kit icon", NamedTextColor.GOLD));
            icon.setItemMeta(iconMeta);
            inventory.setSlot(new Clickable(icon) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                        inventory.setOnClose(null);
                        new IconSelectionGui(dao, player, kit, EditKitGui.this).open();
                    });
                }
            }, 17);
        }

        inventory.setSlot(getSlot(inventory, 39, Material.ORANGE_STAINED_GLASS_PANE, "Helmet",
                () -> new ArmourSlotSelectionGui(dao, player, 39, kit, this, new ArrayList<>(MaterialTags.HELMETS.getValues()), List.of("meteoric_iron_helmet")).open()),
            10);

        inventory.setSlot(getSlot(inventory, 38, Material.ORANGE_STAINED_GLASS_PANE, "Chestplate",
                () -> new ArmourSlotSelectionGui(dao, player, 38, kit, this, new ArrayList<>(MaterialTags.CHEST_EQUIPPABLE.getValues()), List.of("meteoric_iron_chestplate")).open()),
            11);

        inventory.setSlot(getSlot(inventory, 37, Material.ORANGE_STAINED_GLASS_PANE, "Leggings",
                () -> new ArmourSlotSelectionGui(dao, player, 37, kit, this, new ArrayList<>(MaterialTags.LEGGINGS.getValues()), List.of("meteoric_iron_leggings")).open()),
            12);

        inventory.setSlot(getSlot(inventory, 36, Material.ORANGE_STAINED_GLASS_PANE, "Boots",
                () -> new ArmourSlotSelectionGui(dao, player, 36, kit, this, new ArrayList<>(MaterialTags.BOOTS.getValues()), List.of("meteoric_iron_boots")).open()),
            13);

        inventory.setSlot(getSlot(inventory, 40, Material.ORANGE_STAINED_GLASS_PANE, "Offhand",
                () -> new ItemCategorySelectionGui(dao, player, 40, kit, this).open()),
            15);

        for (int i = 9; i < 36; i++) {
            int fi = i;
            inventory.setSlot(getSlot(inventory, i, Material.GRAY_STAINED_GLASS_PANE, "None",
                    () -> new ItemCategorySelectionGui(dao, player, fi, kit, this).open()),
                9 + i);
        }

        for (int i = 0; i < 9; i++) {
            int fi = i;
            inventory.setSlot(getSlot(inventory, i, Material.YELLOW_STAINED_GLASS_PANE, "None",
                    () -> new ItemCategorySelectionGui(dao, player, fi, kit, this).open()),
                45 + i);
        }

        inventory.showInventory(player);
    }

    private Clickable getSlot(ClickableInventory inventory, int itemIndex, Material background, String name, Runnable gui) {
        ItemStack item = kit.items()[itemIndex];
        if (item == null || item.isEmpty()) {
            item = new ItemStack(background);
            ItemMeta meta = item.getItemMeta();
            meta.itemName(Component.text(name, NamedTextColor.GRAY));
            if (canEdit) {
                meta.lore(List.of(Component.text("Click to add", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)));
            }
            item.setItemMeta(meta);
        }
        if (!canEdit) {
            return new DecorationStack(item);
        }
        return new Clickable(item) {
            @Override
            protected void clicked(@NotNull Player clicker) {

            }

            @Override
            protected void onLeftClick(@NotNull Player clicker) {
                setLastItem(kit.items()[itemIndex]);
                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                    inventory.setOnClose(null);
                    gui.run();
                });
            }

            @Override
            protected void onShiftLeftClick(@NotNull Player clicker) {
                ItemStack[] items = kit.items().clone();
                items[itemIndex] = null;

                JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        updateKit(updatedKit);
                        inventory.setOnClose(null);
                        open();
                    });
                });
            }

            @Override
            protected void onRightClick(@NotNull Player clicker) {
                ItemStack kitItem = kit.items()[itemIndex];
                setLastItem(kitItem);
                if (kitItem.getMaxStackSize() > 1) {
                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                        inventory.setOnClose(null);
                        new CountSelectionGui(dao, anvilGui, player, itemIndex, kit, EditKitGui.this).open();
                    });
                } else if (!kitItem.isEmpty()) {
                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                        inventory.setOnClose(null);
                        new EnchantmentGui(dao, anvilGui, player, itemIndex, kit, EditKitGui.this).open();
                    });
                }
            }

            @Override
            protected void onShiftRightClick(@NotNull Player clicker) {
                if ((itemIndex >= 36 && itemIndex < 40) || lastItem == null) {
                    return;
                }
                ItemStack[] items = kit.items().clone();
                items[itemIndex] = lastItem;

                JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        updateKit(updatedKit);
                        inventory.setOnClose(null);
                        open();
                    });
                });
            }
        };
    }
}
