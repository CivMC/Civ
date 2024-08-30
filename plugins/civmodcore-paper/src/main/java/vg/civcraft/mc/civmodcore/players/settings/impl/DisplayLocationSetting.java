package vg.civcraft.mc.civmodcore.players.settings.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class DisplayLocationSetting extends LimitedStringSetting {
    private final String displayName;

    public DisplayLocationSetting(
        final @NotNull JavaPlugin plugin,
        final @NotNull DisplayLocation defaultValue,
        final @NotNull String name,
        final @NotNull String identifier,
        final @NotNull ItemStack gui,
        final @NotNull String displayName
    ) {
        super(
            plugin,
            defaultValue.toString(),
            name,
            identifier,
            gui,
            "Set where to display " + displayName,
            Arrays.stream(DisplayLocation.values()).map(DisplayLocation::toString).toList(),
            false
        );
        this.displayName = displayName;
    }

    public boolean showOnActionbar(
        final UUID uuid
    ) {
        return getDisplayLocation(uuid) == DisplayLocation.ACTIONBAR;
    }

    public @NotNull DisplayLocation getDisplayLocation(
        final UUID uuid
    ) {
        return Objects.requireNonNullElse(
            DisplayLocation.fromString(getValue(uuid)),
            DisplayLocation.ACTIONBAR
        );
    }

    @Override
    public void handleMenuClick(
        final @NotNull Player player,
        final @NotNull MenuSection menu
    ) {
        final DisplayLocation currentValue = getDisplayLocation(player.getUniqueId());
        final var selector = new MultiPageView(
            player,
            List.of(
                genLocationClick(
                    Material.STONE_PRESSURE_PLATE,
                    "%sShow %s on action bar",
                    DisplayLocation.ACTIONBAR,
                    menu,
                    currentValue
                ),
                genLocationClick(
                    Material.BARRIER,
                    "%sShow %s neither on side bar, nor action bar",
                    DisplayLocation.NONE,
                    menu,
                    currentValue
                )
            ),
            "Select where to show " + this.displayName,
            true
        );
        selector.showScreen();
    }

    private IClickable genLocationClick(
        final @NotNull Material icon,
        final @NotNull String label,
        final @NotNull DisplayLocation location,
        final @NotNull MenuSection menu,
        final @NotNull DisplayLocation currentLocation
    ) {
        final var item = new ItemStack(icon);
        item.editMeta((meta) -> {
            meta.setDisplayName(label.formatted(ChatColor.GOLD, this.displayName));
            if (location == currentLocation) {
                meta.setEnchantmentGlintOverride(true);
            }
        });
        return new LClickable(item, (clicker) -> {
            setValue(clicker, location.toString());
            menu.showScreen(clicker);
        });
    }

    public enum DisplayLocation {
        ACTIONBAR, NONE;

        public static @Nullable DisplayLocation fromString(
            final @NotNull String string
        ) {
            return switch (string.toUpperCase()) {
                case "ACTIONBAR" -> ACTIONBAR;
                case "NONE" -> NONE;
                default -> null;
            };
        }
    }
}
