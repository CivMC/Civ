package net.civmc.kitpvp.gui;

import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.selection.ArmourSlotSelectionGui;
import net.civmc.kitpvp.gui.selection.CountSelectionGui;
import net.civmc.kitpvp.gui.selection.EnchantmentGui;
import net.civmc.kitpvp.gui.selection.IconSelectionGui;
import net.civmc.kitpvp.gui.selection.ItemCategorySelectionGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;

public class EditKitGui {

    private final KitPvpDao dao;
    private final Player player;
    private Kit kit;
    private ItemStack lastItem;
    private final KitListGui gui;
    private final boolean canEdit;

    public EditKitGui(KitPvpDao dao, Player player, Kit kit, KitListGui gui) {
        this.dao = dao;
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

        if (player.hasPermission("kitpvp.admin")) {
            ItemStack isPublic = new ItemStack(kit.isPublic() ? Material.OAK_FENCE : Material.OAK_FENCE_GATE);
            ItemMeta isPublicMeta = isPublic.getItemMeta();
            if (kit.isPublic()) {
                isPublicMeta.itemName(Component.text("Make kit private", NamedTextColor.GOLD));
            } else {
                isPublicMeta.itemName(Component.text("Make kit public", NamedTextColor.GOLD));
            }
            isPublic.setItemMeta(isPublicMeta);
            inventory.setSlot(new Clickable(isPublic) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    Kit updatedKit = dao.setPublicKit(kit.id(), !kit.isPublic());
                    updateKit(updatedKit);
                    inventory.setOnClose(null);
                    open();
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
                    clicker.beginConversation(new ConversationFactory(JavaPlugin.getPlugin(KitPvpPlugin.class))
                        .withFirstPrompt(new ValidatingPrompt() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                                return ChatColor.GOLD + "Enter name to rename kit to or 'cancel' to cancel:";
                            }

                            @Override
                            public @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
                                try {
                                    Kit renamedKit = dao.renameKit(kit.id(), input);
                                    if (renamedKit == null) {
                                        player.sendMessage(Component.text("A kit with that name already exists", NamedTextColor.RED));
                                        return null;
                                    }
                                    player.sendMessage(Component.text("Renamed kit to: %s".formatted(renamedKit.name()), NamedTextColor.GOLD));
                                    updateKit(renamedKit);
                                    open();
                                } catch (Exception e) {
                                    JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error renaming kit", e);
                                }
                                return null;
                            }

                            @Override
                            protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
                                return Kit.checkValidName(player, input);
                            }
                        })
                        .addConversationAbandonedListener(abandonedEvent -> {
                            if (!abandonedEvent.gracefulExit()) {
                                player.sendMessage(Component.text("Cancelled renaming kit", NamedTextColor.GOLD));
                                open();
                            }
                        })
                        .withTimeout(30)
                        .withModality(false)
                        .withLocalEcho(false)
                        .withEscapeSequence("cancel")
                        .buildConversation(clicker));
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

        // icon
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
                () -> new ArmourSlotSelectionGui(dao, player, 39, kit, this, new ArrayList<>(MaterialTags.HELMETS.getValues())).open()),
            10);

        inventory.setSlot(getSlot(inventory, 38, Material.ORANGE_STAINED_GLASS_PANE, "Chestplate",
                () -> new ArmourSlotSelectionGui(dao, player, 38, kit, this, new ArrayList<>(MaterialTags.CHEST_EQUIPPABLE.getValues())).open()),
            11);

        inventory.setSlot(getSlot(inventory, 37, Material.ORANGE_STAINED_GLASS_PANE, "Leggings",
                () -> new ArmourSlotSelectionGui(dao, player, 37, kit, this, new ArrayList<>(MaterialTags.LEGGINGS.getValues())).open()),
            12);

        inventory.setSlot(getSlot(inventory, 36, Material.ORANGE_STAINED_GLASS_PANE, "Boots",
                () -> new ArmourSlotSelectionGui(dao, player, 36, kit, this, new ArrayList<>(MaterialTags.BOOTS.getValues())).open()),
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

                Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                updateKit(updatedKit);
                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                    inventory.setOnClose(null);
                    open();
                });
            }

            @Override
            protected void onRightClick(@NotNull Player clicker) {
                ItemStack kitItem = kit.items()[itemIndex];
                setLastItem(kitItem);
                if (kitItem.getMaxStackSize() > 1) {
                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                        inventory.setOnClose(null);
                        new CountSelectionGui(dao, player, itemIndex, kit, EditKitGui.this).open();
                    });
                } else if (!kitItem.isEmpty()) {
                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                        inventory.setOnClose(null);
                        new EnchantmentGui(dao, player, itemIndex, kit, EditKitGui.this).open();
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

                Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                updateKit(updatedKit);
                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                    inventory.setOnClose(null);
                    open();
                });
            }
        };
    }
}
