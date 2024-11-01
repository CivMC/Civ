package net.civmc.kitpvp;

import com.dre.brewery.api.BreweryApi;
import com.github.maxopoly.finale.Finale;
import net.civmc.kitpvp.data.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KitApplier {

    public static void reset(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType() != PotionEffectType.NIGHT_VISION) {
                player.removePotionEffect(effect.getType());
            }
        }

        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setExhaustion(0);

        player.setHealth(20);

        player.setFireTicks(0);

        player.getInventory().clear();

        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            BreweryApi.setPlayerDrunk(player, 0, 10);
        }
    }

    public static void applyKit(Kit kit, Player player) {
        KitApplier.reset(player);
        ItemStack[] items = kit.items();
        for (int i = 0; i < items.length; i++) {
            ItemStack cloned = items[i].clone();
            Finale.getPlugin().update(cloned);
            items[i] = cloned;
        }
        player.getInventory().setContents(items);

        player.sendMessage(Component.text("Applied kit " + kit.name(), NamedTextColor.GREEN));
    }
}
