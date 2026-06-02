package net.civmc.zorweth.oxygen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class OxygenBladderMechanics {

    public static final double CONSUME_ITEM_PLAYER_OXYGEN_THRESHOLD = 1;

    public double drainOxygen(final Player player, final double amount, final double oxygenPerItem,
                              final boolean canConsumeItem) {
        return amount - drawOxygen(player, amount, oxygenPerItem, canConsumeItem);
    }

    public double refillPlayerOxygen(final Player player, final double amount, final double oxygenPerItem,
                                     final double playerOxygen) {
        if (playerOxygen > CONSUME_ITEM_PLAYER_OXYGEN_THRESHOLD) {
            return 0;
        }
        return drawOxygen(player, Math.min(amount, CONSUME_ITEM_PLAYER_OXYGEN_THRESHOLD - playerOxygen), oxygenPerItem,
            true);
    }

    private double drawOxygen(final Player player, final double amount, final double oxygenPerItem,
                              final boolean canConsumeItem) {
        double remaining = amount;
        final ItemStack bladder = OxygenBladder.getOxygenBladder(player);
        if (bladder == null) {
            return 0;
        }

        double reserve = OxygenBladder.getReserve(bladder);
        while (remaining > 0) {
            if (reserve <= 0 && canConsumeItem) {
                reserve = consumeOneOxygenItem(player, oxygenPerItem);
                if (reserve <= 0) {
                    break;
                }
            }
            if (reserve <= 0) {
                break;
            }

            final double reserveLoss = Math.min(remaining, reserve);
            reserve -= reserveLoss;
            remaining -= reserveLoss;
            OxygenBladder.setReserve(bladder, reserve);
        }

        return amount - remaining;
    }

    private double consumeOneOxygenItem(final Player player, final double oxygenPerItem) {
        for (final ItemStack item : player.getInventory().getContents()) {
            if (!OxygenBottle.isCrudeOxygen(item)) {
                continue;
            }

            final Component itemName = item.effectiveName();
            item.subtract(1);
            player.sendMessage(Component.text("Oxygen bladder consumed ", NamedTextColor.GRAY)
                .append(itemName)
                .decorate(TextDecoration.ITALIC));
            return oxygenPerItem;
        }
        return 0;
    }
}
