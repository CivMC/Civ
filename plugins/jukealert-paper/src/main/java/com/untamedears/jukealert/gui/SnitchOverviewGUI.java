package com.untamedears.jukealert.gui;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.FastMultiPageView;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class SnitchOverviewGUI {

    private final List<Snitch> snitches;
    private final Player player;
    private final String title;
    private final boolean canShowDetails;

    public SnitchOverviewGUI(Player player, List<Snitch> snitches, String title, boolean canShowDetails) {
        this.snitches = snitches;
        this.player = player;
        this.title = title;
        this.canShowDetails = canShowDetails;
    }

    private List<IClickable> constructSnitchClickables(int start, int offset) {
        final List<IClickable> clickables = new LinkedList<>();
        for (int i = start; i <= Math.min(snitches.size() - 1, start + offset); i++) {
            Snitch snitch = snitches.get(i);
            // Base the snitch icon on the snitch type
            final var icon = snitch.getType().getItem().clone();
            ItemUtils.handleItemMeta(icon, (ItemMeta meta) -> {
                meta.displayName(Component.text(snitch.getName(), NamedTextColor.GOLD));
                final var location = snitch.getLocation();
                final var lore = new ArrayList<Component>();
                lore.add(
                    ChatUtils.nonItalic()
                        .color(NamedTextColor.AQUA)
                        .content("Location: ")
                        .append(
                            Component.text(location.getWorld().getName(), NamedTextColor.WHITE),
                            Component.space(),
                            Component.text(location.getBlockX(), NamedTextColor.RED),
                            Component.text(", ", NamedTextColor.WHITE),
                            Component.text(location.getBlockY(), NamedTextColor.GREEN),
                            Component.text(", ", NamedTextColor.WHITE),
                            Component.text(location.getBlockZ(), NamedTextColor.BLUE)
                        )
                        .build()
                );
                lore.add(
                    ChatUtils.nonItalic()
                        .color(NamedTextColor.YELLOW)
                        .content("Group: " + snitch.getGroup().getName())
                        .build()
                );
                if (snitch.hasAppender(DormantCullingAppender.class)) {
                    final var cull = snitch.getAppender(DormantCullingAppender.class);
                    if (cull.isActive()) {
                        lore.add(
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.AQUA)
                                .content("Will go dormant in " + TextUtil.formatDuration(cull.getTimeUntilDormant()))
                                .build()
                        );
                        meta.setEnchantmentGlintOverride(true);
                    } else if (cull.isDormant()) {
                        lore.add(
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.AQUA)
                                .content("Will cull in " + TextUtil.formatDuration(cull.getTimeUntilCulling()))
                                .build()
                        );
                    }
                }
                if (this.canShowDetails) {
                    lore.add(
                        ChatUtils.nonItalic()
                            .color(NamedTextColor.GREEN)
                            .content("Click to show details")
                            .build()
                    );
                }
                lore.add(
                    ChatUtils.nonItalic()
                        .color(NamedTextColor.GOLD)
                        .content("Right click to send waypoint")
                        .build()
                );
                meta.lore(lore);
                return true;
            });
            clickables.add(new Clickable(icon) {
                @Override
                public void clicked(final Player clicker) {
                    if (canShowDetails) {
                        new SnitchLogGUI(clicker, snitch).showScreen();
                    }
                }

                @Override
                protected void onRightClick(final Player clicker) {
                    final var location = snitch.getLocation();
                    if (!WorldUtils.doLocationsHaveSameWorld(location, clicker.getLocation())) {
                        clicker.sendMessage(ChatColor.RED + "That snitch is in a different world!");
                        return;
                    }
                    clicker.sendMessage("["
                        + "name:" + snitch.getName() + ","
                        + "x:" + location.getBlockX() + ","
                        + "y:" + location.getBlockY() + ","
                        + "z:" + location.getBlockZ()
                        + "]");
                }
            });
        }
        return clickables;
    }

    public void showScreen() {
        final var view = new FastMultiPageView(this.player, this::constructSnitchClickables, this.title, 6);
        view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
        view.showScreen();
    }

}
