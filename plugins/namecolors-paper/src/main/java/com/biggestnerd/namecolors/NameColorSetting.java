package com.biggestnerd.namecolors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import static net.kyori.adventure.text.format.NamedTextColor.NAMES;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.players.settings.MenuDialog;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class NameColorSetting extends PlayerSetting<String> {

    private static Map<NamedTextColor, Material> colorToGui = new HashMap<>();
    public static final String RAINBOW_PERMISSION = "namecolor.rainbow";
    public static final String COLOR_PERMISSION = "namecolor.use";

    static {
        colorToGui.put(NamedTextColor.DARK_RED, Material.RED_WOOL);
        colorToGui.put(NamedTextColor.DARK_GREEN, Material.GREEN_WOOL);
        colorToGui.put(NamedTextColor.BLUE, Material.BLUE_WOOL);
        colorToGui.put(NamedTextColor.DARK_PURPLE, Material.PURPLE_WOOL);
        colorToGui.put(NamedTextColor.DARK_AQUA, Material.CYAN_WOOL);
        colorToGui.put(NamedTextColor.GRAY, Material.LIGHT_GRAY_WOOL);
        colorToGui.put(NamedTextColor.DARK_GRAY, Material.GRAY_WOOL);
        colorToGui.put(NamedTextColor.GREEN, Material.LIME_WOOL);
        colorToGui.put(NamedTextColor.YELLOW, Material.YELLOW_WOOL);
        colorToGui.put(NamedTextColor.AQUA, Material.LIGHT_BLUE_WOOL);
        colorToGui.put(NamedTextColor.GOLD, Material.YELLOW_GLAZED_TERRACOTTA);
        colorToGui.put(NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_WOOL);
    }

    public NameColorSetting(JavaPlugin owningPlugin) {
        super(owningPlugin, "", "Name color", "namecolors.format", new ItemStack(Material.RED_WOOL),
            "Lets you chose the color of your name", true);
    }

    @Override
    public String deserialize(String serial) {
        return serial;
    }

    @Override
    public ItemStack getGuiRepresentation(UUID player) {
        ItemStack item = new ItemStack(Material.WHITE_WOOL);
        applyInfoToItemStack(item, player);
        Player play = Bukkit.getPlayer(player);
        if (play != null && !play.hasPermission(COLOR_PERMISSION) && !play.hasPermission(RAINBOW_PERMISSION)) {
            ItemUtils.addLore(item, ChatColor.RED + "You do not have permission to do this");
        }
        return item;
    }

    @Override
    public void handleMenuClick(Player player, MenuSection menu) {
        List<IClickable> clicks = new ArrayList<>();
        if (player.hasPermission(COLOR_PERMISSION)) {

            for (Entry<NamedTextColor, Material> entry : colorToGui.entrySet()) {
                ItemStack is = new ItemStack(entry.getValue());
                ItemUtils.setComponentDisplayName(is, Component.empty().append(Component.text("Change to color of your name to ")).append(Component.text(entry.getKey().toString(), entry.getKey())));
                clicks.add(new Clickable(is) {

                    @Override
                    public void clicked(Player p) {
                        player.sendMessage(Component.empty().append(Component.text("The color of your name was changed to ")).append(Component.text(entry.getKey().toString(), entry.getKey())));
                        setValue(player, entry.getKey().toString());
                    }
                });
            }
            ItemStack is = new ItemStack(Material.AMETHYST_BLOCK);
            ItemMeta im = is.getItemMeta();
            im.customName(Component.text("Change to colour of your name to a custom RGB colour (format: #abcdef)"));
            is.setItemMeta(im);
            clicks.add(new Clickable(is) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    new MenuDialog(player, NameColorSetting.this, menu, "Invalid input");
                }
            });

            ItemStack rmis = new ItemStack(Material.GLASS);
            ItemMeta rmim = rmis.getItemMeta();
            rmim.customName(Component.text("Remove your name color"));
            rmis.setItemMeta(rmim);
            clicks.add(new Clickable(rmis) {

                @Override
                public void clicked(Player p) {
                    player.sendMessage(
                        "The color of your name was removed.");
                    setValue(player, "");
                }
            });
        }
        if (player.hasPermission(RAINBOW_PERMISSION)) {
            ItemStack is = new ItemStack(Material.YELLOW_STAINED_GLASS);
            ItemUtils.setComponentDisplayName(is, Component.empty().append(Component.text("Change to color of your name to ")).append(NameColors.rainbowify("rainbow")));
            clicks.add(new Clickable(is) {

                @Override
                public void clicked(Player p) {
                    player.sendMessage(Component.empty().append(Component.text("The color of your name was changed to ")).append(NameColors.rainbowify("rainbow")));
                    setValue(player, "rainbow");
                }
            });
        }
        if (clicks.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to change the color of your name");
            return;
        }
        MultiPageView view = new MultiPageView(player, clicks, "Select a name color", true);
        ItemStack returnStack = new ItemStack(Material.BOOK);
        ItemUtils.setDisplayName(returnStack, "Return to previous menu");
        view.setMenuSlot(new Clickable(returnStack) {

            @Override
            public void clicked(Player p) {
                menu.showScreen(player);
            }
        }, 0);
        view.showScreen();
    }

    private final static Pattern RGB = Pattern.compile("#[A-Fa-f0-9]{6}");

    @Override
    public boolean isValidValue(String input) {
        return NAMES.value(input) != null || "rainbow".equals(input) || RGB.matcher(input).matches();
    }

    @Override
    public String serialize(String value) {
        return value;
    }

    @Override
    public void setValue(UUID player, String value) {
        super.setValue(player, value);
        Player play = Bukkit.getPlayer(player);
        if (play != null) {
            NameColors.getInstance().updatePlayerName(play, value);
        }
    }

    @Override
    public String toText(String value) {
        return value;
    }

}
