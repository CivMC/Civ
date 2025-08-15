package net.civmc.heliodor;

import net.civmc.heliodor.backpack.Backpack;
import net.civmc.heliodor.heliodor.HeliodorGem;
import net.civmc.heliodor.heliodor.HeliodorPickaxe;
import net.civmc.heliodor.meteoriciron.FactoryUpgrade;
import net.civmc.heliodor.farmbeacon.FarmBeacon;
import net.civmc.heliodor.meteoriciron.MeteoricIron;
import net.civmc.heliodor.meteoriciron.MeteoricIronArmour;
import net.civmc.heliodor.meteoriciron.MeteoricIronTools;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class HeliodorRecipeGiver implements Runnable {

    private final List<NamespacedKey> heliodorRecipes = new ArrayList<>();

    public HeliodorRecipeGiver(Plugin plugin) {
        register(HeliodorPickaxe.getRecipes(plugin));
        register(MeteoricIronTools.getRecipes(plugin));
        register(MeteoricIron.getRecipes(plugin));
        register(MeteoricIronArmour.getRecipes(plugin));
        register(Backpack.getRecipes(plugin));
        register(FactoryUpgrade.getRecipes(plugin));
        register(FarmBeacon.getRecipes(plugin));
    }

    @Override
    public void run() {
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
            JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger().log(Level.WARNING, "Iterating inventories for heliodor", ex);
        }
    }

    private void register(List<CraftingRecipe> recipes) {
        for (CraftingRecipe recipe : recipes) {
            Bukkit.getServer().addRecipe(recipe);
            this.heliodorRecipes.add(recipe.getKey());
        }
    }
}
