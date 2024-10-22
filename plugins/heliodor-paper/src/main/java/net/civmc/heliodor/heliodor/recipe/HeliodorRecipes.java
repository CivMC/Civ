package net.civmc.heliodor.heliodor.recipe;

import net.civmc.heliodor.heliodor.HeliodorGem;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class HeliodorRecipes {

    private final List<NamespacedKey> heliodorRecipes = new ArrayList<>();

    public HeliodorRecipes(Plugin plugin) {
        register(HeliodorPickaxe.getRecipes(plugin));
        register(MeteoritePickaxe.getRecipes(plugin));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                if (heliodorRecipes.isEmpty()) {
                    return;
                }
                NamespacedKey first = heliodorRecipes.getFirst();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasDiscoveredRecipe(first)) {
                        for (ItemStack item : player.getInventory().getStorageContents()) {
                            if (HeliodorGem.isFinished(item)) {
                                player.discoverRecipes(heliodorRecipes);
                                break;
                            }
                        }
                    }
                }
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Iterating inventories for heliodor", ex);
            }
        }, 10 * 20, 10 * 20);
    }

    private void register(List<ShapedRecipe> recipes) {
        for (ShapedRecipe recipe : recipes) {
            Bukkit.getServer().addRecipe(recipe);
            this.heliodorRecipes.add(recipe.getKey());
        }
    }
}
