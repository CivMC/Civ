package com.github.igotyou.FactoryMod.recipes.brewery;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.ClonedInventory;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

/**
 * Used to make BreweryX brews stackable. Brews are potions, which vanilla caps at a stack size of one, so a single
 * bottle occupies a whole inventory slot. This recipe takes one brew at a time and hands back the very same brew with
 * the vanilla max stack size component set on it, so bottles of it merge into a single slot from then on. Only brews
 * whose BreweryX recipe id is on the configured allow list are accepted, each with its own stack size.
 * <p>
 * A brew has to be sealed before it can stack at all, because BreweryX writes a fresh random scramble id into an
 * unsealed brew every time it saves it and no two of those are ever equal.
 */
public class StackableBrewRecipe extends InputRecipe {

    /**
     * Substituted per item rather than per recipe, because the allow list can give every brew its own size
     */
    private static final String SIZE_PLACEHOLDER = "{size}";

    private final Map<String, Integer> allowedBrews;
    private final int defaultStackSize;
    private final boolean sealUnsealed;
    private final String packedLore;
    private boolean validated;

    public StackableBrewRecipe(String identifier, String name, int productionTime, ItemMap input,
                               Map<String, Integer> allowedBrews, int defaultStackSize, boolean sealUnsealed,
                               String packedLore) {
        super(identifier, name, productionTime, input);
        this.allowedBrews = allowedBrews;
        this.defaultStackSize = defaultStackSize;
        this.sealUnsealed = sealUnsealed;
        this.packedLore = packedLore;
    }

    @Override
    public boolean enoughMaterialAvailable(Inventory inputInv) {
        return input.isContainedIn(inputInv) && findCandidate(inputInv) != null;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        Candidate candidate = findCandidate(inputInv);
        if (candidate == null) {
            return new EffectFeasibility(true, null);
        }
        // An unsealed preview never merges into a stack already in the chest, so this can claim the chest is full when
        // one more would have squeezed in. That beats sealing somebody's brew just to answer a question
        if (!InventoryUtils.safelyAddItemsToInventory(
            ClonedInventory.cloneInventory(outputInv), new ItemStack[]{preview(candidate)})) {
            return new EffectFeasibility(false, "it ran out of storage space");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);
        if (input.isContainedIn(inputInv)) {
            Candidate candidate = findCandidate(inputInv);
            if (candidate != null) {
                ItemStack packed = pack(candidate);
                if (packed != null) {
                    if (!InventoryUtils.safelyAddItemsToInventory(
                        ClonedInventory.cloneInventory(outputInv), new ItemStack[]{packed})) {
                        return false; // does not fit in chest
                    }
                    ItemStack toRemove = candidate.source().clone();
                    toRemove.setAmount(1);
                    if (input.removeSafelyFrom(inputInv)) {
                        if (new ItemMap(toRemove).removeSafelyFrom(inputInv)) {
                            outputInv.addItem(packed);
                        }
                    }
                }
            }
        }
        logAfterRecipeRun(combo, fccf);
        return true;
    }

    /**
     * Finds the first brew this recipe is willing to make stackable, or null if there is none. Iterates in inventory
     * order rather than over an ItemMap so the factory always picks the brew a player would expect from the chest.
     */
    private Candidate findCandidate(Inventory inputInv) {
        if (!Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            return null;
        }
        validateAllowedBrews();
        for (ItemStack is : inputInv.getContents()) {
            if (is == null || is.getType() != Material.POTION) {
                continue;
            }
            if (input.getAmount(is) != 0) {
                continue; // never eat our own running cost
            }
            if (is.getItemMeta().hasMaxStackSize()) {
                continue; // already stackable, don't charge for it twice
            }
            Brew brew = Brew.get(is);
            if (brew == null || brew.getCurrentRecipe() == null) {
                continue;
            }
            if (!brew.isSealed() && !sealUnsealed) {
                continue;
            }
            Integer stackSize = resolveStackSize(brew.getCurrentRecipe());
            if (stackSize != null) {
                return new Candidate(is, stackSize);
            }
        }
        return null;
    }

    private @Nullable Integer resolveStackSize(BRecipe recipe) {
        String id = recipe.getId();
        return id == null ? null : allowedBrews.get(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Produces the stackable brew to hand back, sealing it first if needed, or null if it couldn't be sealed. Sealing
     * has to come first because it rewrites the whole potion meta. It also fires an event and cannot be undone, so this
     * is only called while actually running the recipe; use {@link #preview(Candidate)} to merely inspect the result.
     */
    private @Nullable ItemStack pack(Candidate candidate) {
        ItemStack result = candidate.source().clone();
        result.setAmount(1);
        Brew brew = Brew.get(result);
        if (brew == null) {
            return null;
        }
        if (!brew.isSealed()) {
            brew.seal(result, null);
            // Sealing silently restores the original meta if its event gets cancelled, so reading the brew back off the
            // item is the only reliable way to tell whether it took
            Brew sealed = Brew.get(result);
            if (sealed == null || !sealed.isSealed()) {
                return null;
            }
        }
        makeStackable(result, candidate.maxStackSize());
        return result;
    }

    /**
     * Same as {@link #pack(Candidate)} but without sealing, for fit checks and gui display. Both happen outside a
     * recipe run, where sealing the player's brew would be an unwelcome surprise.
     */
    private ItemStack preview(Candidate candidate) {
        ItemStack result = candidate.source().clone();
        result.setAmount(1);
        makeStackable(result, candidate.maxStackSize());
        return result;
    }

    private void makeStackable(ItemStack is, int maxStackSize) {
        is.editMeta(meta -> {
            meta.setMaxStackSize(maxStackSize);
            if (packedLore != null && !packedLore.isBlank()) {
                List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                lore.add(Component.text(packedLore.replace(SIZE_PLACEHOLDER, String.valueOf(maxStackSize))));
                meta.lore(lore);
            }
        });
    }

    /**
     * Warns once about allow list entries BreweryX doesn't know, so a typo doesn't just leave the factory quietly doing
     * nothing. BreweryX loads after us, so this can't happen while parsing the config.
     */
    private void validateAllowedBrews() {
        if (validated) {
            return;
        }
        validated = true;
        for (String id : allowedBrews.keySet()) {
            if (findBrewRecipe(id) == null) {
                FactoryMod.getInstance().warning("Recipe " + name + " allows the brew " + id
                    + ", but BreweryX has no brew with that id. Allow list entries are brew ids, not brew names.");
            }
        }
    }

    /**
     * Looks a brew up by id. BRecipe#getById matches case sensitively, but we hold the allow list lower cased, so it
     * cannot be used here
     */
    private static @Nullable BRecipe findBrewRecipe(String id) {
        for (BRecipe recipe : BRecipe.getAllRecipes()) {
            if (id.equalsIgnoreCase(recipe.getId())) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<ItemStack> result = new LinkedList<>();
        if (i == null) {
            result.add(new ItemStack(Material.POTION));
            result.addAll(input.getItemStackRepresentation());
            return result;
        }
        result = createLoredStacksForInfo(i);
        Candidate candidate = findCandidate(i);
        if (candidate != null) {
            result.add(candidate.source().clone());
        }
        return result;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<ItemStack> result = new LinkedList<>();
        if (i == null) {
            result.add(new ItemStack(Material.POTION));
            return result;
        }
        Candidate candidate = findCandidate(i);
        if (candidate != null) {
            result.add(preview(candidate));
        }
        return result;
    }

    @Override
    public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<String> result = new LinkedList<>(formatLore(input));
        if (sealUnsealed) {
            result.add("A brew from the list below, sealed or not");
            result.add("Unsealed brews are sealed first, which rounds an odd quality down");
        } else {
            result.add("A sealed brew from the list below");
        }
        return result;
    }

    @Override
    public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<String> result = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : allowedBrews.entrySet()) {
            result.add(displayName(entry.getKey()) + ", stackable to " + entry.getValue());
        }
        return result;
    }

    /**
     * The brew's name as players know it, falling back to the raw id while BreweryX can't resolve it
     */
    private String displayName(String id) {
        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            BRecipe recipe = findBrewRecipe(id);
            if (recipe != null) {
                return recipe.getRecipeName();
            }
        }
        return id;
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.POTION;
    }

    @Override
    public String getTypeIdentifier() {
        return "BREW_STACK";
    }

    public int getDefaultStackSize() {
        return defaultStackSize;
    }

    public boolean isSealUnsealed() {
        return sealUnsealed;
    }

    public String getPackedLore() {
        return packedLore;
    }

    /**
     * Copy of the allow list, so an inheriting recipe doesn't share the parent's instance
     */
    public Map<String, Integer> copyAllowedBrews() {
        return new LinkedHashMap<>(allowedBrews);
    }

    private record Candidate(ItemStack source, int maxStackSize) {

    }
}
