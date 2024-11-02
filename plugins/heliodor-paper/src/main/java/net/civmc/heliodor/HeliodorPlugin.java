package net.civmc.heliodor;

import net.civmc.heliodor.backpack.BackpackListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class HeliodorPlugin extends ACivMod {

    private ManagedDatasource database;
    private HeliodorRecipeGiver recipes;
    private NamespacedKey oreLocationsKey;

    @Override
    public void onLoad() {
        this.recipes = new HeliodorRecipeGiver(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        saveDefaultConfig();

        Bukkit.getScheduler().runTaskTimer(this, this.recipes, 15 * 20, 15 * 20);

        getServer().getPluginManager().registerEvents(new AnvilRepairListener(), this);

        getServer().getPluginManager().registerEvents(new BackpackListener(), this);
    }

    public HeliodorRecipeGiver getRecipes() {
        return recipes;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
