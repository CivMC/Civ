package com.github.igotyou.FactoryMod.recipes.space;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.research.ResearchManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.ClonedInventory;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.namelayer.NameLayerAPI;

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
        return List.of(createResearchNote("<player>"));
    }

    @Override
    public List<String> getTextualOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of("1 Research note", "Advances phase " + this.phase + " research progress");
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
        if (fccf.getActivator() == null) {
            return new EffectFeasibility(false, "research must be activated by a player");
        }
        final World world = fccf.getFurnace().getWorld();
        if (!researchManager.isResearchWorld(world)) {
            return new EffectFeasibility(false, "wrong world");
        }
        if (!researchManager.canRunResearch(world, this.phase)) {
            return new EffectFeasibility(false, "this research phase is not available");
        }
        if (!InventoryUtils.safelyAddItemsToInventory(ClonedInventory.cloneInventory(outputInv),
            new ItemStack[] {createResearchNote("<player>")})) {
            return new EffectFeasibility(false, "it ran out of storage space");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(final Inventory inputInv, final Inventory outputInv, final FurnCraftChestFactory fccf) {
        final ResearchManager researchManager = getResearchManager();
        final World world = fccf.getFurnace().getWorld();
        if (researchManager == null || !researchManager.canRunResearch(world, this.phase)
            || fccf.getActivator() == null || !input.isContainedIn(inputInv)) {
            return false;
        }
        final ItemStack researchNote = createResearchNote(getActivatorName(fccf));
        if (!InventoryUtils.safelyAddItemsToInventory(ClonedInventory.cloneInventory(outputInv),
            new ItemStack[] {researchNote})) {
            return false;
        }
        if (!input.removeSafelyFrom(inputInv)) {
            return false;
        }
        researchManager.recordResearchRun(world, this.phase);
        outputInv.addItem(researchNote);
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

    private ItemStack createResearchNote(final String playerName) {
        final ItemStack researchNote = new ItemStack(Material.PAPER);
        final ItemMeta meta = researchNote.getItemMeta();
        meta.displayName(Component.text("Research note"));
        meta.lore(List.of(Component.text("Written by " + playerName)));
        researchNote.setItemMeta(meta);
        return researchNote;
    }

    private String getActivatorName(final FurnCraftChestFactory fccf) {
        final Player player = Bukkit.getPlayer(fccf.getActivator());
        if (player != null) {
            return player.getName();
        }
        final String playerName = NameLayerAPI.getCurrentName(fccf.getActivator());
        return playerName != null ? playerName : fccf.getActivator().toString();
    }
}
