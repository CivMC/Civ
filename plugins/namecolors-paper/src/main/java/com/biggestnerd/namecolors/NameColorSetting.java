package com.biggestnerd.namecolors;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.chat.Componentify;
import vg.civcraft.mc.civmodcore.chat.dialog.Dialog;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogManager;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.players.settings.MenuDialog;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class NameColorSetting extends PlayerSetting<Component> {

    private static Map<TextColor, Material> colorToGui = new HashMap<>();
    public static final String RAINBOW_PERMISSION = "namecolor.rainbow";
    public static final String COLOR_PERMISSION = "namecolor.use";
    public static final String RGB_COLOR_PERMISSION = "namecolor.rgb";

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
    }

    public NameColorSetting(JavaPlugin owningPlugin) {
        super(owningPlugin, Component.empty(), "Name color", "namecolors.choser", new ItemStack(Material.RED_WOOL),
            "Lets you chose the color of your name", true);
    }

    @Override
    public Component deserialize(String serial) {
        return GsonComponentSerializer.gson().deserialize(serial);
    }

    @Override
    public ItemStack getGuiRepresentation(UUID player) {
        ItemStack item = new ItemStack(Material.WHITE_WOOL);
        applyInfoToItemStack(item, player);
        Player play = Bukkit.getPlayer(player);
        if (!NameColors.doesPlayerHavePermissions(play)) {
            MetaUtils.addComponentLore(item.getItemMeta(), Component.text("You do not have permission to do this", NamedTextColor.RED));
        }
        return item;
    }

    @Override
    public void handleMenuClick(Player player, MenuSection menu) {
        List<IClickable> clicks = new ArrayList<>();
        if (player.hasPermission(COLOR_PERMISSION)) {
            clicks.addAll(getNamedColorButton(player));
        }
        if (player.hasPermission(RAINBOW_PERMISSION)) {
            clicks.add(getRainbowButton(player));
        }
        if (player.hasPermission(RGB_COLOR_PERMISSION)) {
            clicks.add(getRGBButton(menu));
        }
        if (clicks.isEmpty()) {
            player.sendRichMessage("<red>You do not have permission to change the color of your name</red>");
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

    public Clickable getRGBButton(MenuSection menu) {
        Clickable button;
        ItemStack is = new ItemStack(Material.WRITABLE_BOOK);
        ItemUtils.setComponentDisplayName(is, MiniMessage.miniMessage().deserialize("Change to color of your name to any RGB value"));
        button = new Clickable(is) {

            @Override
            public void clicked(Player target) {
                new MenuDialog(target, NameColors.getInstance().setting, menu, "Please input a valid hex color") {
                    @Override
                    public void onReply(String[] message) {
                        if (message.length > 1) {
                            target.sendRichMessage("<red>RGB values cannot have spaces.</red>");
                            return;
                        }
                        String result = message[0];
                        if (!isValidValue(result)) {
                            target.sendMessage(ChatColor.RED + "You did not enter a valid RGB value");
                            menu.showScreen(target);
                            return;
                        }
                        setValue(target.getUniqueId(), Component.text(target.getName()).color(TextColor.fromHexString(result)));
                        Component successMsg = Component.text("Your name has been set to: ", NamedTextColor.GREEN).append(target.displayName());
                        target.sendMessage(successMsg);
                        end();
                    }

                    @Override
                    public List<String> onTabComplete(String lastWord, String[] fullMessage) {
                        return List.of();
                    }
                };
            }
        };
        return button;
    }

    public List<Clickable> getNamedColorButton(Player player) {
        List<Clickable> buttons = new ArrayList<>();
        for (Entry<TextColor, Material> entry : colorToGui.entrySet()) {
            ItemStack is = new ItemStack(entry.getValue());
            ItemUtils.setDisplayName(is,
                "Change to color of your name to " + entry.getKey());
            buttons.add(new Clickable(is) {

                @Override
                public void clicked(Player p) {
                    player.sendMessage("The color of your name was changed to " + entry.getKey());
                    //TODO: make message use colour selected
                    setValue(player, Component.text(player.getName()).color(entry.getKey()));
                }
            });
        }
        return buttons;
    }

    public Clickable getRainbowButton(Player player) {
        ItemStack is = new ItemStack(Material.YELLOW_STAINED_GLASS);
        ItemUtils.setComponentDisplayName(is, MiniMessage.miniMessage().deserialize("Change to color of your name to <rainbow>rainbow</rainbow>"));
        return new Clickable(is) {

            @Override
            public void clicked(Player p) {
                player.sendRichMessage("The color of your name was changed to <rainbow>" + player.getName());
                setValue(player, MiniMessage.miniMessage().deserialize("<rainbow>" + player.getName()));
            }
        };
    }

    @Override
    public boolean isValidValue(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            TextColor color = TextColor.fromHexString(input);
            return color != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String serialize(Component value) {
        return GsonComponentSerializer.gson().serialize(value);
    }

    @Override
    public void setValue(UUID player, Component value) {
        super.setValue(player, value);
        Player play = Bukkit.getPlayer(player);
        if (play != null) {
            NameColors.getInstance().updatePlayerName(play);
        }
    }

    @Override
    public String toText(Component value) {
        return PlainTextComponentSerializer.plainText().serialize(value);
    }
}
