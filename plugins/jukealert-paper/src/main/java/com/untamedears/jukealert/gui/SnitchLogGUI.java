package com.untamedears.jukealert.gui;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.chat.Componentify;
import vg.civcraft.mc.civmodcore.chat.dialog.Dialog;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class SnitchLogGUI {

    private final Player player;
    private final Snitch snitch;
    private final SnitchLogAppender logAppender;
    private final SnitchManager snitchManager;
    private List<IClickable> buttonCache;

    public SnitchLogGUI(@NotNull final Player player,
                        @NotNull final Snitch snitch) {
        this.player = Objects.requireNonNull(player);
        this.snitch = Objects.requireNonNull(snitch);
        this.logAppender = snitch.getAppender(SnitchLogAppender.class);
        this.snitchManager = JukeAlert.getInstance().getSnitchManager();
    }

    private boolean INTERNAL_hasPermission(@NotNull final PermissionType permission) {
        return this.snitch.hasPermission(this.player.getUniqueId(), permission);
    }

    private List<IClickable> INTERNAL_constructContent() {
        if (this.logAppender == null) {
            final var item = new ItemStack(Material.BARRIER);
            ItemUtils.setComponentDisplayName(item, Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.RED)
                .content("This snitch can not create logs")
                .build());
            return MoreCollectionUtils.asLazyList(Collections.singletonList(() -> new DecorationStack(item)));
        }
        final var actions = this.logAppender.getFullLogs();
        if (actions.isEmpty()) {
            final var item = new ItemStack(Material.BARRIER);
            ItemUtils.setComponentDisplayName(item, Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.RED)
                .content("This snitch has no logs currently")
                .build());
            return MoreCollectionUtils.asLazyList(Collections.singletonList(() -> new DecorationStack(item)));
        }
        final var buttons = new ArrayList<Supplier<IClickable>>(actions.size());
        for (final LoggableAction action : actions) {
            buttons.add(action::getGUIRepresentation);
        }
        return MoreCollectionUtils.asLazyList(buttons);
    }

    private IClickable INTERNAL_constructClearClick() {
        final var item = new ItemStack(Material.TNT);
        ItemUtils.setComponentDisplayName(item, Component.text()
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.GOLD)
            .content("Clear all logs")
            .build());
        if (INTERNAL_hasPermission(JukeAlertPermissionHandler.getClearLogs())) {
            return new Clickable(item) {
                @Override
                public void clicked(@NotNull final Player _player) {
                    if (INTERNAL_hasPermission(JukeAlertPermissionHandler.getClearLogs())) {
                        SnitchLogGUI.this.logAppender.deleteLogs();
                        if (SnitchLogGUI.this.buttonCache != null) {
                            SnitchLogGUI.this.buttonCache.clear();
                            SnitchLogGUI.this.buttonCache = null;
                        }
                        SnitchLogGUI.this.player.sendMessage(Component.text()
                            .color(NamedTextColor.GREEN)
                            .content("That snitch's logs have been cleared!")
                            .build());
                        showScreen();
                    }
                }
            };
        }
        ItemUtils.addComponentLore(item, Component.text()
            .color(NamedTextColor.RED)
            .content("You do not have permission to do this")
            .build());
        return new DecorationStack(item);
    }

    private IClickable INTERNAL_constructNameChangeClick() {
        final var item = new ItemStack(Material.OAK_SIGN);
        ItemUtils.setComponentDisplayName(item, Component.text()
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.GOLD)
            .content("Rename this snitch")
            .build());
        return new Clickable(item) {
            @Override
            public void clicked(@NotNull final Player clicker) {
                clicker.sendMessage(Component.text()
                    .color(NamedTextColor.YELLOW)
                    .content("Please enter a new name for the snitch:")
                    .build());
                ClickableInventory.forceCloseInventory(clicker);
                new Dialog(clicker, JukeAlert.getInstance()) {
                    @Override
                    public void onReply(String[] message) {
                        StringBuilder builder = new StringBuilder();
                        for (String s : message) {
                            builder.append(s).append(" ");
                        }
                        String newName = "";
                        if (builder.toString().length() > 40) {
                            newName = builder.substring(0, 40);
                        } else {
                            newName = builder.toString();
                        }
                        snitchManager.renameSnitch(SnitchLogGUI.this.snitch, newName);
                        clicker.sendMessage(Component.text()
                            .color(NamedTextColor.AQUA)
                            .content("Changed snitch name to " + newName)
                            .build());
                        showScreen();
                    }

                    @Override
                    public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
                        return null;
                    }
                };
            }
        };
    }

    private IClickable INTERNAL_constructInfoStack() {
        final var item = new ItemStack(Material.PAPER);
        ItemUtils.handleItemMeta(item, (final ItemMeta meta) -> {
            meta.displayName(Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GOLD)
                .content("Logs for ")
                .append(Component.text(this.snitch.getName()))
                .build());
            MetaUtils.setComponentLore(meta,
                Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.AQUA)
                    .content("Location: ")
                    .append(Componentify.blockLocation(this.snitch.getLocation()))
                    .build(),
                Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.YELLOW)
                    .content("Group: ")
                    .append(Component.text(this.snitch.getGroup().getName()))
                    .build(),
                Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.YELLOW)
                    .content("Type: ")
                    .append(Component.text(this.snitch.getType().getName()))
                    .build());
            return true;
        });
        return new DecorationStack(item);
    }

    private IClickable INTERNAL_constructLeverToggleClick() {
        final var leverAppender = this.snitch.getAppender(LeverToggleAppender.class);
        final var item = new ItemStack(Material.LEVER);
        ItemUtils.handleItemMeta(item, (final ItemMeta meta) -> {
            meta.displayName(Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GOLD)
                .content("Toggle lever activation by redstone")
                .build());
            MetaUtils.setComponentLore(meta,
                Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.AQUA)
                    .content("Currently turned " + (leverAppender.shouldToggle() ? "on" : "off"))
                    .build());
            return true;
        });
        if (INTERNAL_hasPermission(JukeAlertPermissionHandler.getToggleLevers())) {
            return new Clickable(item) {
                @Override
                public void clicked(@NotNull final Player clicker) {
                    if (INTERNAL_hasPermission(JukeAlertPermissionHandler.getToggleLevers())) {
                        leverAppender.switchState();
                        SnitchLogGUI.this.player.sendMessage(Component.text()
                            .color(NamedTextColor.GREEN)
                            .content("Toggled lever activation " + (leverAppender.shouldToggle() ? "on" : "off"))
                            .build());
                        showScreen();
                    }
                }
            };
        }
        ItemUtils.addComponentLore(item, Component.text()
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.RED)
            .content("You do not have permission to do this")
            .build());
        return new DecorationStack(item);
    }

    public void showScreen() {
        if (this.buttonCache == null) {
            this.buttonCache = INTERNAL_constructContent();
        }
        final var view = new MultiPageView(this.player, this.buttonCache,
            StringUtils.substring(this.snitch.getName(), 0, 32), true);
        if (this.logAppender != null) {
            view.setMenuSlot(INTERNAL_constructClearClick(), 1);
        }
        view.setMenuSlot(INTERNAL_constructNameChangeClick(), 2);
        view.setMenuSlot(INTERNAL_constructInfoStack(), 3);
        view.setMenuSlot(constructExitClick(), 4);
        if (this.snitch.hasAppender(LeverToggleAppender.class)) {
            view.setMenuSlot(INTERNAL_constructLeverToggleClick(), 5);
        }
        view.showScreen();
    }

    public static IClickable constructExitClick() {
        final var item = new ItemStack(Material.OAK_DOOR);
        ItemUtils.handleItemMeta(item, (final ItemMeta meta) -> {
            meta.displayName(Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GOLD)
                .content("Exit")
                .build());
            MetaUtils.setComponentLore(meta,
                Component.text()
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.AQUA)
                    .content("Click to exit GUI")
                    .build());
            return true;
        });
        return new Clickable(item) {
            @Override
            public void clicked(@NotNull final Player clicker) {
                ClickableInventory.forceCloseInventory(clicker);
            }
        };
    }

}
