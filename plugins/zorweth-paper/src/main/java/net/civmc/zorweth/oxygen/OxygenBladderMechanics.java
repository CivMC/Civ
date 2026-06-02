package net.civmc.zorweth.oxygen;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class OxygenBladderMechanics {

    public static final double CONSUME_ITEM_PLAYER_OXYGEN_THRESHOLD = 1;

    public double getOxygenDrain(final Player player, double amount, ActivityManager.Activity activity) {
        ItemStack bladder = OxygenBladder.getOxygenBladder(player);
        if (!OxygenBladder.supportsActivity(bladder, activity)) {
            amount *= 2;
        }
        return amount;
    }

    public double refillPlayerOxygen(final Player player, final double oxygenPerItem,
                                      final double playerOxygen) {
        if (playerOxygen > CONSUME_ITEM_PLAYER_OXYGEN_THRESHOLD) {
            return 0;
        }
        if (OxygenBladder.getOxygenBladder(player) == null) {
            return 0;
        }
        return consumeOneOxygenItem(player, oxygenPerItem);
    }

    private double consumeOneOxygenItem(final Player player, final double oxygenPerItem) {
        for (final ItemStack item : player.getInventory().getContents()) {
            if (OxygenBottle.isCrudeOxygen(item)) {
                consumeItem(player, item);
                return oxygenPerItem;
            }
            final double brewOxygen = getOxygenBrewAmount(item);
            if (brewOxygen > 0) {
                consumeItem(player, item);
                return brewOxygen;
            }
            if (OxygenTank.isFilledBasicOxygenTank(item)) {
                consumeItem(player, item);
                for (final ItemStack leftover : player.getInventory().addItem(OxygenTank.createEmptyBasicOxygenTank()).values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
                return OxygenTank.BASIC_OXYGEN_TANK_AMOUNT;
            }
        }
        return 0;
    }

    private double getOxygenBrewAmount(final ItemStack item) {
        if (item == null || item.isEmpty()) {
            return 0;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            return 0;
        }
        final Brew brew = Brew.get(item);
        if (brew == null) {
            return 0;
        }
        final BRecipe recipe = brew.getCurrentRecipe();
        if (recipe == null || !OxygenManager.OXYGEN_BREW_RECIPE_NAME.equals(recipe.getRecipeName())) {
            return 0;
        }
        return OxygenManager.OXYGEN_BREW_AMOUNT * (brew.getQuality() / 10D);
    }

    private void consumeItem(final Player player, final ItemStack item) {
        final Component itemName = item.effectiveName();
        item.subtract(1);
        player.sendMessage(Component.text("Oxygen bladder consumed ", NamedTextColor.GRAY)
            .append(itemName)
            .decorate(TextDecoration.ITALIC));
    }
}
