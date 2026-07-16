package net.civmc.zorweth.research;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResearchCurrency {

    public static final long MOON_COST = 1_000L;
    public static final long RAINBOW_ARMOUR_COST = 400L;
    final String BALANCE_KEY = "research_tokens";

    private final NamespacedKey balanceKey;

    public ResearchCurrency(final JavaPlugin plugin) {
        this.balanceKey = new NamespacedKey(plugin, BALANCE_KEY);
    }

    public long getBalance(final Player player) {
        return player.getPersistentDataContainer().getOrDefault(this.balanceKey, PersistentDataType.LONG, 0L);
    }

    public void deposit(final Player player, final long amount) {
        final PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(this.balanceKey, PersistentDataType.LONG, Math.addExact(getBalance(player), amount));
    }

    public boolean withdraw(final Player player, final long amount) {
        final long balance = getBalance(player);
        if (balance < amount) {
            return false;
        }
        player.getPersistentDataContainer().set(this.balanceKey, PersistentDataType.LONG, balance - amount);
        return true;
    }

    public static boolean isResearchNote(final ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }
        final ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }
        final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        if (!"Research note".equals(serializer.serialize(meta.displayName()))) {
            return false;
        }
        final List<Component> lore = meta.lore();
        return lore != null && lore.size() == 1
            && serializer.serialize(lore.getFirst()).startsWith("Written by ");
    }
}
