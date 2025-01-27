package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.chat.dialog.Dialog;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.collection.SetSetting;

public final class SafeOreBreak extends BasicHack {

    @AutoLoad(id = "ores")
    private List<List<String>> defaultOres;

    private BooleanSetOreSetting setSetting;

    public SafeOreBreak(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        MenuSection mainMenu = plugin.getSettingManager().getMainMenu();
        Set<String> settings = new HashSet<>();
        OUTER:
        for (List<String> parts : defaultOres) {
            List<Material> materialParts = new ArrayList<>(parts.size());

            for (String part : parts) {
                Material material = Material.matchMaterial(part);
                if (material == null) {
                    logger.warning("Invalid material '" + part + "'. Skipping.");
                    continue OUTER;
                }
                materialParts.add(material);
            }
            settings.add(getNiceMatName(materialParts.getFirst()));
        }
        PlayerSettingAPI.registerSetting(setSetting = new BooleanSetOreSetting(
            plugin,
            settings,
            "Safe break",
            "safeBreak",
            new ItemStack(Material.BARREL),
            "Prevents you from breaking blocks without a silk touch tool"
        ), mainMenu);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler(ignoreCancelled = true)
    public void onOreBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
            return;
        }

        if (setSetting.getValue(player).contains(getNiceMatName(event.getBlock().getType()))) {
            event.getPlayer().sendMessage(
                Component.text("A SimpleAdminHacks /config option is preventing you from breaking " +
                    event.getBlock().getType().getKey().getKey()
                    + " without a silk touch tool.", NamedTextColor.RED)
            );

            event.setCancelled(true);
        }
    }

    private static class BooleanSetOreSetting extends SetSetting<String> {

        public BooleanSetOreSetting(JavaPlugin owningPlugin, Set<String> defaultValue, String name, String identifier,
                                    ItemStack gui, String description) {
            super(owningPlugin, defaultValue, name, identifier, gui, description, String.class);
        }

        @Override
        public boolean isValidValue(String input) {
            Material material = Material.matchMaterial(input);
            return material != null && material.isBlock();
        }

        @Override
        public void handleMenuClick(Player player, MenuSection menu) {
            Set<String> value = getValue(player);
            List<IClickable> clickables = new ArrayList<>(value.size());
            for (String element : value) {
                elementSetting.setValue(player, element);
                Material material = Material.matchMaterial(element);
                ItemStack is;
                if (material == null || material.asItemType() == null) {
                    is = new ItemStack(Material.BARRIER);
                } else {
                    is = new ItemStack(material);
                }
                ItemUtils.setComponentDisplayName(is, Component.text(elementSetting.toText(element), NamedTextColor.GOLD));
                clickables.add(new Clickable(is) {

                    @Override
                    public void clicked(Player p) {
                        removeElement(player.getUniqueId(), element);
                        player.sendMessage(Component.text(String.format("Removed %s from %s", elementSetting.toText(element), getNiceName()), NamedTextColor.GREEN));
                        handleMenuClick(player, menu);
                    }
                });
            }
            MultiPageView pageView = new MultiPageView(player, clickables, getNiceName(), true);
            ItemStack parentItem = new ItemStack(Material.ARROW);
            ItemUtils.setComponentDisplayName(parentItem, Component.text("Go back to " + menu.getName(), NamedTextColor.AQUA));
            pageView.setMenuSlot(new Clickable(parentItem) {

                @Override
                public void clicked(@NotNull Player p) {
                    menu.showScreen(p);
                }
            }, 0);
            ItemStack addItemStack = new ItemStack(Material.GREEN_CONCRETE);
            ItemUtils.setComponentDisplayName(addItemStack, Component.text("Add new entry", NamedTextColor.GOLD));
            pageView.setMenuSlot(new Clickable(addItemStack) {

                @Override
                public void clicked(@NotNull Player p) {
                    new Dialog(p, getOwningPlugin(), ChatColor.GOLD + "Enter the name of the entry to add") {

                        @Override
                        public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
                            return null;
                        }

                        @Override
                        public void onReply(String[] message) {
                            String full = String.join(" ", message);
                            Material material = Material.matchMaterial(full);
                            if (material == null || !material.isBlock() || material.asItemType() == null) {
                                p.sendMessage(Component.text("You entered an invalid value", NamedTextColor.RED));
                            } else {
                                p.sendMessage(Component.text("Added " + material.getKey().getKey().replace('_', ' '), NamedTextColor.GREEN));
                                addElement(p.getUniqueId(), elementSetting.deserialize(getNiceMatName(material)));
                            }
                            handleMenuClick(player, menu);
                        }
                    };
                }
            }, 3);
            pageView.showScreen();
        }
    }

    private static String getNiceMatName(Material material) {
        return WordUtils.capitalizeFully(material.getKey().getKey().replace('_', ' '));
    }
}
