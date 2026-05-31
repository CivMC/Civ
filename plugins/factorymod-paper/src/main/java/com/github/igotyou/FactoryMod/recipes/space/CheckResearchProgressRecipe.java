package com.github.igotyou.FactoryMod.recipes.space;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import java.util.List;
import java.util.UUID;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.research.ResearchManager;
import net.civmc.zorweth.research.ResearchProgress;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public final class CheckResearchProgressRecipe extends InputRecipe {

    public CheckResearchProgressRecipe(final String identifier, final String name, final int productionTime,
                                       final ItemMap input) {
        super(identifier, name, productionTime, input);
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
        return List.of("Reports current research progress");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.WRITABLE_BOOK;
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
        if (!researchManager.isResearchWorld(fccf.getFurnace().getWorld())) {
            return new EffectFeasibility(false, "wrong world");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(final Inventory inputInv, final Inventory outputInv, final FurnCraftChestFactory fccf) {
        final ResearchManager researchManager = getResearchManager();
        final World world = fccf.getFurnace().getWorld();
        if (researchManager == null || !researchManager.isEnabled() || !researchManager.isResearchWorld(world)
            || !input.isContainedIn(inputInv)) {
            return false;
        }
        if (!input.removeSafelyFrom(inputInv)) {
            return false;
        }
        sendProgressMessage(fccf, researchManager.getResearchProgress());
        return true;
    }

    @Override
    public String getTypeIdentifier() {
        return "CHECK_RESEARCH_PROGRESS";
    }

    private void sendProgressMessage(final FurnCraftChestFactory fccf, final ResearchProgress progress) {
        final UUID activator = fccf.getActivator();
        if (activator == null) {
            return;
        }
        final Player player = Bukkit.getPlayer(activator);
        if (player == null) {
            return;
        }
        if (progress.complete()) {
            player.sendMessage(Component.text("Research is complete", NamedTextColor.GREEN));
            return;
        }
        player.sendMessage(Component.text("Research phase " + progress.phase() + ": " + progress.runs() + "/"
            + progress.requiredRuns() + " runs complete", NamedTextColor.GREEN));
    }

    private ResearchManager getResearchManager() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Zorweth")) {
            return null;
        }
        return JavaPlugin.getPlugin(ZorwethPlugin.class).getResearchManager();
    }
}
