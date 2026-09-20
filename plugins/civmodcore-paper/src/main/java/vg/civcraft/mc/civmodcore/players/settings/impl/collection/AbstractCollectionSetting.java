package vg.civcraft.mc.civmodcore.players.settings.impl.collection;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogHelpers;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogManager;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.SettingTypeManager;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public abstract class AbstractCollectionSetting<C extends Collection<T>, T> extends PlayerSetting<C> {

    private static final char SEPARATOR = ',';
    private static final String SEPARATOR_STRING = String.valueOf(SEPARATOR);
    private static final char ESCAPE = ';';
    private static final String ESCAPE_STRING = String.valueOf(ESCAPE);
    private static final String ESCAPE_REPLACE = ESCAPE_STRING + ESCAPE_STRING;
    private static final String SEPARATOR_REPLACE = ESCAPE_STRING + SEPARATOR_STRING;

    private PlayerSetting<T> elementSetting;
    private Function<C, C> newFunction;

    public AbstractCollectionSetting(JavaPlugin owningPlugin, C defaultValue, String name, String identifier,
                                     ItemStack gui, String description, Class<T> elementClass, Function<C, C> newFunction) {
        super(owningPlugin, defaultValue != null ? defaultValue : newFunction.apply(null), name, identifier, gui,
            description, true);
        this.newFunction = newFunction;
        elementSetting = SettingTypeManager.getSetting(elementClass);
        if (elementSetting == null) {
            throw new IllegalArgumentException("Can not keep " + elementClass.getName()
                + " in collection, because it was not registed in SettingTypeManager");
        }
    }

    public void addElement(UUID uuid, T element) {
        // need to clone list here to avoid problems with reused default values
        C collection = getValue(uuid);
        collection.add(element);
        setValue(uuid, collection);
    }

    public boolean removeElement(UUID uuid, T element) {
        C collection = getValue(uuid);
        boolean worked = collection.remove(element);
        setValue(uuid, collection);
        return worked;
    }

    public boolean contains(UUID uuid, T element) {
        return getValue(uuid).contains(element);
    }

    /**
     * Always returns a copy, never the original collection
     */
    @Override
    public C getValue(UUID uuid) {
        // make sure we dont hand original out
        return newFunction.apply(super.getValue(uuid));
    }

    @Override
    public boolean isValidValue(String input) {
        boolean escape = false;
        int startingIndex = 0;
        for (int i = 0; i < input.length(); i++) {
            if (escape) {
                escape = false;
                continue;
            }
            if (input.charAt(i) == ESCAPE) {
                escape = true;
                continue;
            }
            if (input.charAt(i) == SEPARATOR) {
                String element = input.substring(startingIndex, i);
                if (!elementSetting.isValidValue(element)) {
                    return false;
                }
                startingIndex = i + 1;
            }
        }
        // final element not checked by the loop, because there's no comma after it
        return elementSetting.isValidValue(input.substring(startingIndex));
    }

    @Override
    public C deserialize(String serial) {
        C result = newFunction.apply(null);
        boolean escape = false;
        int startingIndex = 0;
        for (int i = 0; i < serial.length(); i++) {
            if (escape) {
                escape = false;
                continue;
            }
            if (serial.charAt(i) == ESCAPE) {
                escape = true;
                continue;
            }
            if (serial.charAt(i) == SEPARATOR) {
                String element = serial.substring(startingIndex, i);
                element = removeEscapes(element);
                result.add(elementSetting.deserialize(element));
                startingIndex = i + 1;
            }
        }
        // final element not checked by the loop, because there's no comma after it
        result.add(elementSetting.deserialize(removeEscapes(serial.substring(startingIndex))));
        return result;
    }

    private static String removeEscapes(String val) {
        return val.replaceAll(SEPARATOR_REPLACE, SEPARATOR_STRING).replaceAll(ESCAPE_REPLACE, ESCAPE_STRING);
    }

    private static String escape(String val) {
        return val.replaceAll(ESCAPE_STRING, ESCAPE_REPLACE).replaceAll(SEPARATOR_STRING, SEPARATOR_REPLACE);
    }

    @Override
    public String serialize(C value) {
        StringBuilder sb = new StringBuilder();
        Iterator<T> iter = value.iterator();
        while (iter.hasNext()) {
            T element = iter.next();
            String serialized = elementSetting.serialize(element);
            String escaped = escape(serialized);
            sb.append(escaped);
            if (iter.hasNext()) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    @Override
    public String toText(C value) {
        StringBuilder sb = new StringBuilder();
        Iterator<T> iter = value.iterator();
        while (iter.hasNext()) {
            sb.append(elementSetting.toText(iter.next()));
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public void handleMenuClick(Player player, MenuSection menu) {
        C value = getValue(player);
        List<IClickable> clickables = new ArrayList<>(value.size());
        for (T element : value) {
            elementSetting.setValue(player, element);
            ItemStack is = elementSetting.getGuiRepresentation(player.getUniqueId());
            ItemUtils.setDisplayName(is, ChatColor.GOLD + elementSetting.toText(element));
            clickables.add(new Clickable(is) {

                @Override
                public void clicked(Player p) {
                    removeElement(player.getUniqueId(), element);
                    player.sendMessage(String.format("%sRemoved %s from %s", ChatColor.GREEN,
                        elementSetting.toText(element), getNiceName()));
                    handleMenuClick(player, menu);
                }
            });
        }
        MultiPageView pageView = new MultiPageView(player, clickables, getNiceName(), true);
        ItemStack parentItem = new ItemStack(Material.ARROW);
        ItemUtils.setDisplayName(parentItem, ChatColor.AQUA + "Go back to " + menu.getName());
        pageView.setMenuSlot(new Clickable(parentItem) {

            @Override
            public void clicked(Player p) {
                menu.showScreen(p);
            }
        }, 0);
        pageView.setMenuSlot(this.generateAddEntryButton(menu), 3);
        pageView.showScreen();
    }

    private @NotNull Clickable generateAddEntryButton(
        final @NotNull MenuSection menu
    ) {
        final var addItemStack = new ItemStack(Material.GREEN_CONCRETE);
        ItemUtils.setDisplayName(addItemStack, ChatColor.GOLD + "Add new entry");
        return new LClickable(addItemStack, (p) -> {
            final String INPUT_ID = "prompt_input";
            DialogManager.showDialog(
                p,
                Component.text("Add to " + AbstractCollectionSetting.this.getNiceName() + "?"),
                List.of(),
                List.of(
                    DialogInput.text(INPUT_ID, Component.text("Value to add:", NamedTextColor.GOLD))
                        .maxLength(256)
                        .build()
                ),
                (view) -> {
                    final String value = DialogHelpers.getTrimmedText(view, INPUT_ID);
                    if (value == null || !AbstractCollectionSetting.this.isValidValue(value)) {
                        p.sendMessage(ChatColor.RED + "You entered an invalid value");
                        return;
                    }
                    AbstractCollectionSetting.this.addElement(p.getUniqueId(), AbstractCollectionSetting.this.elementSetting.deserialize(value));
                    p.sendMessage(ChatColor.GREEN + "Added " + value);
                    menu.showScreen(p); // Refresh the GUI
                }
            );
        });
    }
}
