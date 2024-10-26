package net.civmc.heliodor;

import net.civmc.heliodor.backpack.Backpack;
import net.civmc.heliodor.heliodor.HeliodorGem;
import net.civmc.heliodor.heliodor.HeliodorPickaxe;
import net.civmc.heliodor.vein.MeteoricIron;
import net.civmc.heliodor.vein.MeteoricIronPickaxe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class HeliodorRecipeGiver {

    private final List<NamespacedKey> heliodorRecipes = new ArrayList<>();

    public HeliodorRecipeGiver(Plugin plugin) {
        register(HeliodorPickaxe.getRecipes(plugin));
        register(MeteoricIronPickaxe.getRecipes(plugin));
        register(MeteoricIron.getRecipes(plugin));
        register(Backpack.getRecipes(plugin));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                if (heliodorRecipes.isEmpty()) {
                    return;
                }
                NamespacedKey first = heliodorRecipes.get(ThreadLocalRandom.current().nextInt(heliodorRecipes.size()));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasDiscoveredRecipe(first)) {
                        for (ItemStack item : player.getInventory().getStorageContents()) {
                            if ((item != null && !item.isEmpty()) && (HeliodorGem.isFinished(item)
                                || MeteoricIron.isNugget(item)
                                || MeteoricIron.isIngot(item))) {
                                player.discoverRecipes(heliodorRecipes);
                                break;
                            }
                        }
                    }
                }
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Iterating inventories for heliodor", ex);
            }
        }, 15 * 20, 15 * 20);
    }

    private void register(List<ShapedRecipe> recipes) {
        for (ShapedRecipe recipe : recipes) {
            Bukkit.getServer().addRecipe(recipe);
            this.heliodorRecipes.add(recipe.getKey());
        }
    }
}
