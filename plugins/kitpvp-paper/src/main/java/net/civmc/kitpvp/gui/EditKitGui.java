package net.civmc.kitpvp.gui;

import com.destroystokyo.paper.MaterialTags;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.dao.Kit;
import net.civmc.kitpvp.dao.KitPvpDao;
import net.civmc.kitpvp.gui.selection.ArmourSlotSelectionGui;
import net.civmc.kitpvp.gui.selection.ItemCategorySelectionGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import java.util.ArrayList;
import java.util.List;

public class EditKitGui {

    private static ItemStack CREATE_KIT_ITEM;

    static {
        CREATE_KIT_ITEM = new ItemStack(Material.PAPER);
        ItemMeta meta = CREATE_KIT_ITEM.getItemMeta();
        meta.itemName(Component.text("Create new kit", NamedTextColor.GREEN));
    }

    private final KitPvpDao dao;
    private final Player player;
    private Kit kit;
    private final KitListGui gui;

    public EditKitGui(KitPvpDao dao, Player player, Kit kit, KitListGui gui) {
        this.dao = dao;
        this.player = player;
        this.kit = kit;
        this.gui = gui;
        open();
    }

    public void updateKit(Kit kit) {
        this.kit = kit;
        this.gui.invalidate();
    }

    public void open() {
        ClickableInventory inventory = new ClickableInventory(54, "Editing kit");

        inventory.setOnClose(gui::open);

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
                    .withFirstPrompt(new StringPrompt() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public @NotNull String getPromptText(@NotNull ConversationContext context) {
                            return ChatColor.GOLD + "Enter name to rename kit to or 'cancel' to cancel:";
                        }

                        @Override
                        public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                            if (input != null && !input.isBlank()) {
                                Kit renamedKit = dao.renameKit(kit.id(), input);
                                if (renamedKit == null) {
                                    player.sendMessage(Component.text("Kit was deleted", NamedTextColor.RED));
                                    return null;
                                }
                                player.sendMessage(Component.text("Renamed kit to: %s".formatted(renamedKit.name()), NamedTextColor.GOLD));
                                updateKit(renamedKit);
                                open();
                            }
                            return null;
                        }
                    })
                    .addConversationAbandonedListener(abandonedEvent -> {
                        if (!abandonedEvent.gracefulExit()) {
                            player.sendMessage(Component.text("Cancelled renaming kit", NamedTextColor.GOLD));
                            inventory.showInventory(player, false);
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
        guideMeta.itemName(Component.text("Info", NamedTextColor.GOLD, TextDecoration.BOLD));
        guideMeta.lore(List.of(
            Component.text("Left click to change item", NamedTextColor.WHITE),
            Component.text("Shift left click to remove item", NamedTextColor.WHITE),
            Component.text("Right click to enchant or change count", NamedTextColor.WHITE)
        ));
        guide.setItemMeta(guideMeta);
        inventory.setItem(guide, 8);


        inventory.setSlot(getSlot(inventory, 36, Material.ORANGE_STAINED_GLASS_PANE, "Head",
                () -> new ArmourSlotSelectionGui(dao, "Armour", player, 36, kit, this, new ArrayList<>(MaterialTags.HELMETS.getValues())).open()),
            10);

        inventory.setSlot(getSlot(inventory, 37, Material.ORANGE_STAINED_GLASS_PANE, "Chestplate",
                () -> new ArmourSlotSelectionGui(dao, "Armour", player, 37, kit, this, new ArrayList<>(MaterialTags.CHEST_EQUIPPABLE.getValues())).open()),
            11);

        inventory.setSlot(getSlot(inventory, 38, Material.ORANGE_STAINED_GLASS_PANE, "Leggings",
                () -> new ArmourSlotSelectionGui(dao, "Armour", player, 38, kit, this, new ArrayList<>(MaterialTags.LEGGINGS.getValues())).open()),
            12);

        inventory.setSlot(getSlot(inventory, 39, Material.ORANGE_STAINED_GLASS_PANE, "Boots",
                () -> new ArmourSlotSelectionGui(dao, "Armour", player, 39, kit, this, new ArrayList<>(MaterialTags.BOOTS.getValues())).open()),
            13);

        inventory.setSlot(getSlot(inventory, 40, Material.ORANGE_STAINED_GLASS_PANE, "Offhand",
                () -> new ItemCategorySelectionGui(dao, player, 40, kit, this)),
            15);

        for (int i = 0; i < 27; i++) {
            int fi = i;
            inventory.setSlot(getSlot(inventory, i, Material.GRAY_STAINED_GLASS_PANE, "None",
                    () -> new ItemCategorySelectionGui(dao, player, fi, kit, this)),
                18 + i);
        }

        for (int i = 27; i < 36; i++) {
            int fi = i;
            inventory.setSlot(getSlot(inventory, i, Material.YELLOW_STAINED_GLASS_PANE, "None",
                    () -> new ItemCategorySelectionGui(dao, player, fi, kit, this)),
                45 + i);
        }

        inventory.showInventory(player);
    }

    private Clickable getSlot(ClickableInventory inventory, int itemIndex, Material background, String name, Runnable gui) {
        ItemStack item = kit.items()[itemIndex];
        if (item == null) {
            item = new ItemStack(background);
            ItemMeta meta = item.getItemMeta();
            meta.itemName(Component.text(name, NamedTextColor.GRAY));
            meta.lore(List.of(Component.text("Click to add", NamedTextColor.YELLOW)));
            item.setItemMeta(meta);
        }
        return new Clickable(item) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                inventory.setOnClose(null);
                gui.run();
            }
        };
    }
}
