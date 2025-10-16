package sh.okx.railswitch.settings;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sh.okx.railswitch.RailSwitchPlugin;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import java.util.UUID;

public class VisualizationSetting extends PlayerSetting<VisualizationMode> {

    public VisualizationSetting(RailSwitchPlugin plugin,
                                VisualizationMode defaultValue,
                                String name, String identifier, String description) {
        super(plugin, defaultValue, name, identifier, new ItemStack(Material.ENDER_EYE), description, true);
    }


    @Override
    public VisualizationMode deserialize(String serial) {
        if (serial == null) return null;
        String s = serial.trim().toUpperCase();
        // Friendly aliases
        return switch (s) {
            case "OFF", "0" -> VisualizationMode.DISABLED;
            case "BASIC", "VISUAL" -> VisualizationMode.VISUALS;
            case "PRED", "ROUTING" -> VisualizationMode.PREDICTION;
            default -> VisualizationMode.valueOf(s); // DISABLED / VISUALS / PREDICTION
        };
    }

    @Override
    public String serialize(VisualizationMode value) {
        return (value == null) ? "DISABLED" : value.name();
    }

    @Override
    public ItemStack getGuiRepresentation(UUID player) {
        VisualizationMode mode = getValue(player);
        if (mode == null) {
            mode = VisualizationMode.PREDICTION;
        }

        ItemStack item;
        switch (mode) {
            case DISABLED -> {
                item = new ItemStack(Material.RED_DYE);
            }
            case PREDICTION -> {
                item = new ItemStack(Material.ENDER_EYE);
                item.addUnsafeEnchantment(Enchantment.LURE, 1);
                item.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            default -> {
                item = new ItemStack(Material.ENDER_EYE);
            }
        }

        applyInfoToItemStack(item, player); // keeps display name + description
        return item;
    }

    @Override
    public void handleMenuClick(Player player, MenuSection menu) {
        VisualizationMode next = switch (getValue(player.getUniqueId())) {
            case DISABLED   -> VisualizationMode.VISUALS;
            case VISUALS    -> VisualizationMode.PREDICTION;
            case PREDICTION -> VisualizationMode.DISABLED;
        };
        setValue(player.getUniqueId(), next);
        menu.showScreen(player);
    }

    @Override
    public String toText(VisualizationMode value) {
        return switch (value) {
            case DISABLED   -> "Disabled";
            case VISUALS    -> "Visuals";
            case PREDICTION -> "Prediction";
        };
    }

    @Override
    public boolean isValidValue(String input) {
        try { deserialize(input); return true; } catch (Exception e) { return false; }
    }
}
