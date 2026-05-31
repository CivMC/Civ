package com.github.igotyou.FactoryMod.recipes.space;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import java.util.List;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.research.ResearchManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public final class ResearchRecipe extends InputRecipe {

    private final int phase;

    public ResearchRecipe(final String identifier, final String name, final int productionTime, final ItemMap input,
                          final int phase) {
        super(identifier, name, productionTime, input);
        this.phase = phase;
    }

    @Override
    public List<ItemStack> getInputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        if (i == null) {
            return input.getItemStackRepresentation();
        }
        return createLoredStacksForInfo(i);
    }

    @Override
    public List<ItemStack> getOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of();
    }

    @Override
    public List<String> getTextualOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of("Advances phase " + this.phase + " research progress");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.KNOWLEDGE_BOOK;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(final Inventory inputInv, final Inventory outputInv,
                                                       final FurnCraftChestFactory fccf) {
        final ResearchManager researchManager = getResearchManager();
        if (researchManager == null) {
            return new EffectFeasibility(false, "Zorweth is not enabled");
        }
        if (!researchManager.isEnabled()) {
            return new EffectFeasibility(false, "research is disabled");
        }
        final World world = fccf.getFurnace().getWorld();
        if (!researchManager.isResearchWorld(world)) {
            return new EffectFeasibility(false, "wrong world");
        }
        if (!researchManager.canRunResearch(world, this.phase)) {
            return new EffectFeasibility(false, "this research phase is not available");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(final Inventory inputInv, final Inventory outputInv, final FurnCraftChestFactory fccf) {
        final ResearchManager researchManager = getResearchManager();
        final World world = fccf.getFurnace().getWorld();
        if (researchManager == null || !researchManager.canRunResearch(world, this.phase)
            || !input.isContainedIn(inputInv)) {
            return false;
        }
        if (!input.removeSafelyFrom(inputInv)) {
            return false;
        }
        researchManager.recordResearchRun(world, this.phase);
        return true;
    }

    @Override
    public String getTypeIdentifier() {
        return "RESEARCH";
    }

    private ResearchManager getResearchManager() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Zorweth")) {
            return null;
        }
        return JavaPlugin.getPlugin(ZorwethPlugin.class).getResearchManager();
    }
}
