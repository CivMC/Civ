package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import com.github.igotyou.FactoryMod.utility.FactoryModGUI;
import com.github.igotyou.FactoryMod.utility.FileHandler;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

/**
 * Manager class which handles all factories, their locations and their creation
 */
public class FactoryModManager {

    private FactoryMod plugin;
    private FileHandler fileHandler;
    private HashMap<Class<? extends MultiBlockStructure>, HashMap<ItemMap, IFactoryEgg>> factoryCreationRecipes;
    private HashMap<IFactoryEgg, ItemMap> totalSetupCosts;
    private HashMap<Location, Factory> locations;
    private HashMap<String, IFactoryEgg> eggs;
    private HashSet<Factory> factories;
    private Map<String, IRecipe> recipes;
    private HashSet<Material> possibleCenterBlocks;
    private HashSet<Material> possibleInteractionBlock;
    private Material factoryInteractionMaterial;
    private boolean citadelEnabled;
    private boolean logInventories;
    private int redstonePowerOn;
    private int redstoneRecipeChange;
    private int maxInputChests;
    private int maxOutputChests;
    private int maxFuelChests;
    private int maxTotalIOFChests;
    private Set<String> compactLore;
    private Set<String> forceInclude;
    private FactoryModPlayerSettings playerSettings;
    private boolean canUpgrade;

    public FactoryModManager(FactoryMod plugin, Material factoryInteractionMaterial, boolean citadelEnabled,
                             boolean nameLayerEnabled, int redstonePowerOn, int redstoneRecipeChange, boolean logInventories,
                             int maxInputChests, int maxOutputChests, int maxFuelChests, int maxTotalIOFChests,
                             Map<String, String> factoryRenames, boolean canUpgrade) {
        this.plugin = plugin;
        this.factoryInteractionMaterial = factoryInteractionMaterial;
        this.citadelEnabled = citadelEnabled;
        this.redstonePowerOn = redstonePowerOn;
        this.redstoneRecipeChange = redstoneRecipeChange;
        this.maxInputChests = maxInputChests;
        this.maxOutputChests = maxOutputChests;
        this.maxFuelChests = maxFuelChests;
        this.maxTotalIOFChests = maxTotalIOFChests;
        this.fileHandler = new FileHandler(this, factoryRenames);
        this.canUpgrade = canUpgrade;

        factoryCreationRecipes = new HashMap<>();
        locations = new HashMap<>();
        eggs = new HashMap<>();
        possibleCenterBlocks = new HashSet<>();
        possibleInteractionBlock = new HashSet<>();
        factories = new HashSet<>();
        totalSetupCosts = new HashMap<>();
        recipes = new HashMap<>();
        compactLore = new HashSet<>();
        forceInclude = new HashSet<>();
        playerSettings = new FactoryModPlayerSettings(plugin);

        // Normal furnace, craftingtable, chest factories
        possibleCenterBlocks.add(Material.CRAFTING_TABLE);
        possibleInteractionBlock.add(Material.CRAFTING_TABLE);
        possibleInteractionBlock.add(Material.FURNACE);
        possibleInteractionBlock.add(Material.CHEST);
        possibleInteractionBlock.add(Material.BARREL);
        possibleInteractionBlock.add(Material.TRAPPED_CHEST);

        // sorter
        possibleCenterBlocks.add(Material.DROPPER);
        possibleInteractionBlock.add(Material.DROPPER);

        // pipe
        possibleCenterBlocks.add(Material.DISPENSER);
        possibleInteractionBlock.add(Material.DISPENSER);
    }

    /**
     * Sets the lore used for compacting recipes. This is needed for the compact
     * item listeners
     *
     * @param lore Lore used for compacting items
     */
    public void addCompactLore(String lore) {
        compactLore.add(lore);
    }

    public boolean logInventories() {
        return logInventories;
    }

    /**
     * @return Lore given to compacted items
     */
    public boolean isCompactLore(String lore) {
        return compactLore.contains(lore);
    }

    /**
     * Gets the setupcost for a specific factory
     *
     * @param c    Class of the structure type the factory is using
     * @param name Name of the factory
     * @return Setupcost if the factory if it was found or null if it wasnt
     */
    public ItemMap getSetupCost(Class<? extends MultiBlockStructure> c, String name) {
        for (Entry<ItemMap, IFactoryEgg> entry : factoryCreationRecipes.get(c).entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Adds a factory and the locations of its blocks to the manager
     *
     * @param f Factory to add
     */
    public void addFactory(Factory f) {
        factories.add(f);
        for (Location loc : f.getMultiBlockStructure().getAllBlocks()) {
            locations.put(loc, f);
        }
    }

    /**
     * @return Whether citadel is enabled on the server
     */
    public boolean isCitadelEnabled() {
        return citadelEnabled;
    }

    public int getMaxInputChests() {
        return maxInputChests;
    }

    public int getMaxOutputChests() {
        return maxOutputChests;
    }

    public int getMaxFuelChests() {
        return maxFuelChests;
    }

    /**
     * @return The maximum total number of inputs, outputs, and fuel inputs a factory has. A chest with multiple IOF
     * settings enabled is counted for each.
     */
    public int getMaxTotalIOFChests() {
        return maxTotalIOFChests;
    }

    /**
     * @return Which material is used to interact with factories, stick by default
     */
    public Material getFactoryInteractionMaterial() {
        return factoryInteractionMaterial;
    }

    public boolean canUpgrade() {
        return canUpgrade;
    }

    /**
     * Removes a factory from the manager
     *
     * @param f Factory to remove
     */
    public void removeFactory(Factory f) {
        if (f.isActive()) {
            f.deactivate();
        }
        factories.remove(f);
        FurnCraftChestFactory.removePylon(f);
        for (Location b : f.getMultiBlockStructure().getAllBlocks()) {
            locations.remove(b);
        }
    }

    /**
     * Tries to get the factory which has a part at the given location
     *
     * @param loc Location which is supposed to be part of a factory
     * @return The factory which had a block at the given location or null if there
     * was no factory
     */
    public Factory getFactoryAt(Location loc) {
        return getFactoryAt(loc.getBlock());
    }

    /**
     * Tries to get the factory which has a part at the given block
     *
     * @param b Block which is supposed to be part of a factory
     * @return The factory which had a block at the given location or null if there
     * was no factory
     */
    public Factory getFactoryAt(Block b) {
        return locations.get(b.getLocation());
    }

    /**
     * Checks whether a part of a factory is at the given location
     *
     * @param loc Location to check
     * @return True if there is a factory block, false if not
     */
    public boolean factoryExistsAt(Location loc) {
        return getFactoryAt(loc) != null;
    }

    /**
     * Attempts to create a factory with the given block as new center block. If all
     * blocks for a specific structure are there and other conditions needed for the
     * factory type are fullfilled, the factory is created and added to the manager
     *
     * @param b Center block
     * @param p Player attempting to create the factory
     */
    public void attemptCreation(Block b, Player p) {
        // this method should probably be taken apart and the individual logic should be
        // exported in
        // a class that fits each factory type
        if (!factoryExistsAt(b.getLocation())) {
            // Cycle through possible structures here
            if (b.getType() == Material.CRAFTING_TABLE) {
                FurnCraftChestStructure fccs = new FurnCraftChestStructure(b);
                if (fccs.isComplete()) {
                    if (fccs.blockedByExistingFactory()) {
                        p.sendMessage(ChatColor.RED
                            + "At least one of the blocks of this factory is already part of another factory");
                        return;
                    }
                    HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(FurnCraftChestStructure.class);
                    if (eggs != null) {
                        IFactoryEgg egg = null;
                        for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
                            if (entry.getKey()
                                .containedExactlyIn(((Container) (fccs.getChest().getState())).getInventory())) {
                                egg = entry.getValue();
                                break;
                            }
                        }
                        if (egg != null) {
                            Factory f = egg.hatch(fccs, p);
                            if (f != null) {
                                // Trigger lazy-initialize default crafting table IOSelector
                                ((FurnCraftChestFactory) f).getTableIOSelector();
                                ((Container) (fccs.getChest().getState())).getInventory().clear();
                                addFactory(f);
                                p.sendMessage(ChatColor.GREEN + "Successfully created " + f.getName());
                                LoggingUtils.log(f.getLogData() + " was created by " + p.getName());
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "There is no factory with the given creation materials");
                            FactoryModGUI gui = new FactoryModGUI(p);
                            gui.showFactoryOverview(true);
                        }
                    }
                    return;
                }
            }
            if (b.getType() == Material.DISPENSER) {
                PipeStructure ps = new PipeStructure(b);
                if (ps.isComplete()) {
                    if (ps.blockedByExistingFactory()) {
                        p.sendMessage(ChatColor.RED
                            + "At least one of the blocks of this factory is already part of another factory");
                        return;
                    }
                    HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(PipeStructure.class);
                    if (eggs != null) {
                        IFactoryEgg egg = null;
                        for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
                            if (entry.getKey()
                                .containedExactlyIn((((Dispenser) (ps.getStart().getState())).getInventory()))) {
                                egg = entry.getValue();
                                break;
                            }
                        }
                        if (egg != null) {
                            if (ps.getPipeType() != ((PipeEgg) egg).getPipeType()) {
                                p.sendMessage(ChatColor.RED + "You dont have the right block for this pipe");
                                return;
                            }
                            if (ps.getLength() > ((PipeEgg) egg).getMaximumLength()) {
                                p.sendMessage(ChatColor.RED + "You cant make pipes of this type, which are that long");
                                return;
                            }
                            Factory f = egg.hatch(ps, p);
                            if (f != null) {
                                ((Dispenser) (ps.getStart().getState())).getInventory().clear();
                                addFactory(f);
                                p.sendMessage(ChatColor.GREEN + "Successfully created " + f.getName());
                                LoggingUtils.log(f.getLogData() + " was created by " + p.getName());
                            }

                        } else {
                            p.sendMessage(ChatColor.RED + "There is no pipe with the given creation materials");
                        }
                    }
                    return;
                } else {
                    p.sendMessage(ChatColor.RED + "This pipe is not set up the right way");
                }
            }
            if (b.getType() == Material.DROPPER) {
                BlockFurnaceStructure bfs = new BlockFurnaceStructure(b);
                if (bfs.isComplete()) {
                    if (bfs.blockedByExistingFactory()) {
                        p.sendMessage(ChatColor.RED
                            + "At least one of the blocks of this factory is already part of another factory");
                        return;
                    }
                    HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.get(BlockFurnaceStructure.class);
                    if (eggs != null) {
                        IFactoryEgg egg = null;
                        for (Entry<ItemMap, IFactoryEgg> entry : eggs.entrySet()) {
                            if (entry.getKey().containedExactlyIn(
                                ((Dropper) (bfs.getCenter().getBlock().getState())).getInventory())) {
                                egg = entry.getValue();
                                break;
                            }
                        }
                        if (egg != null) {
                            Factory f = egg.hatch(bfs, p);
                            if (f != null) {
                                ((Dropper) (bfs.getCenter().getBlock().getState())).getInventory().clear();
                                addFactory(f);
                                p.sendMessage(ChatColor.GREEN + "Successfully created " + f.getName());
                                LoggingUtils.log(f.getLogData() + " was created by " + p.getName());
                            }

                        } else {
                            p.sendMessage(ChatColor.RED + "There is no sorter with the given creation materials");
                        }
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "This sorter is not set up the right way");
                }
            }
        }
    }

    public void calculateTotalSetupCosts() {
        for (HashMap<ItemMap, IFactoryEgg> maps : factoryCreationRecipes.values()) {
            for (Entry<ItemMap, IFactoryEgg> entry : maps.entrySet()) {
                totalSetupCosts.put(entry.getValue(), entry.getKey());
            }
        }
        for (IFactoryEgg egg : this.eggs.values()) {
            totalSetupCosts.put(egg, calculateTotalSetupCost(egg));
        }
    }

    private ItemMap calculateTotalSetupCost(IFactoryEgg egg) {
        ItemMap map = null;
        map = totalSetupCosts.get(egg);
        if (map != null) {
            return map;
        }
        for (IFactoryEgg superEgg : this.eggs.values()) {
            if (superEgg instanceof FurnCraftChestEgg) {
                for (IRecipe recipe : ((FurnCraftChestEgg) superEgg).getRecipes()) {
                    if (recipe instanceof Upgraderecipe && ((Upgraderecipe) recipe).getEgg() == egg) {
                        map = calculateTotalSetupCost(superEgg);
                        if (map == null) {
                            plugin.warning("Could not calculate total setupcost for " + egg.getName()
                                + ". It's parent factory  " + superEgg.getName() + " is impossible to set up");
                            break;
                        }
                        map = map.clone(); // so we dont mess with the original
                        // setup costs
                        map.merge(((Upgraderecipe) recipe).getInput());
                        return map;
                    }
                }

            }
        }
        return map;
    }

    /**
     * Gets all the factories within a certain range of a given location
     *
     * @param l     Location on which the search is centered
     * @param range maximum distance from the center allowed
     * @return All of the factories which are less or equal than the given range
     * away from the given location
     */
    public List<Factory> getNearbyFactories(Location l, int range) {
        List<Factory> facs = new LinkedList<>();
        for (Factory f : factories) {
            if (f.getMultiBlockStructure().getCenter().distance(l) <= range) {
                facs.add(f);
            }
        }
        return facs;
    }

    public ItemMap getTotalSetupCost(Factory f) {
        return getTotalSetupCost(getEgg(f.getName()));
    }

    public ItemMap getTotalSetupCost(IFactoryEgg e) {
        return totalSetupCosts.get(e);
    }

    /**
     * Adds a factory egg to the manager and associates it with a specific setup
     * cost in items and a specific MultiBlockStructure which is the physical
     * representation of the factory created by the egg. See the docu for the eggs
     * for more info on those
     *
     * @param blockStructureClass Class inheriting from MultiBlockStructure, which
     *                            physically represents the factories created by the
     *                            egg
     * @param recipe              Item cost to create the factory
     * @param egg                 Encapsulates the factory itself
     */
    public void addFactoryEgg(Class<? extends MultiBlockStructure> blockStructureClass, ItemMap recipe,
                              IFactoryEgg egg) {
        if (recipe != null) {
            HashMap<ItemMap, IFactoryEgg> eggs = factoryCreationRecipes.computeIfAbsent(blockStructureClass,
                a -> new HashMap<ItemMap, IFactoryEgg>());
            eggs.put(recipe, egg);
        }
        this.eggs.put(egg.getName().toLowerCase(), egg);
    }

    public void saveFactories() {
        plugin.info("Attempting to save factory data");
        fileHandler.save(getAllFactories());
    }

    public void loadFactories() {
        plugin.info("Attempting to load factory data");
        fileHandler.load(eggs);
    }

    /**
     * Called when the plugin is deactivated to first save all factories and then
     * deactivate them, so the deactivated block state is saved
     */
    public void shutDown() {
        saveFactories();
        for (Factory f : factories) {
            f.deactivate();
        }
    }

    /**
     * Checks whether a specific material is a possible center block for a factory
     * and whether a factory could potentionally created from a block with this
     * material
     *
     * @param m Material to check
     * @return true if the material could be the one of a possible center block,
     * false if not
     */
    public boolean isPossibleCenterBlock(Material m) {
        return possibleCenterBlocks.contains(m);
    }

    /**
     * Checks whether the given material is an interaction material and whether a
     * reaction should be tried to get when one of those blocks is part of a factory
     * and interacted with
     *
     * @param m Material to check
     * @return True if the material is a possible interaction material, false if not
     */
    public boolean isPossibleInteractionBlock(Material m) {
        return possibleInteractionBlock.contains(m);
    }

    /**
     * Gets a specific factory egg based on it's name
     *
     * @param name Name of the egg
     * @return The egg with the given name or null if no such egg exists
     */
    public IFactoryEgg getEgg(String name) {
        return eggs.get(name.toLowerCase());
    }

    /**
     * Gets the Redstone power level necessary to active a factory. Fall below this
     * level and the factory will deactivate.
     *
     * @return The power level on which factory activation or de-activation hinges
     */
    public int getRedstonePowerOn() {
        return this.redstonePowerOn;
    }

    /**
     * Gets the Redstone power change necessary to alter the recipe setting of a
     * factory. Any change {@code >=} this level, either positive or negative, will attempt
     * to alter the recipe (implementation depending).
     *
     * @return The amount of Redstone power change necessary to alter recipe setting
     * of a factory.
     */
    public int getRedstoneRecipeChange() {
        return this.redstoneRecipeChange;
    }

    /**
     * Gets all factories which currently exist. Do not mess with the hashset
     * returned as it is used in other places
     *
     * @return All existing factory instances
     */
    public HashSet<Factory> getAllFactories() {
        synchronized (factories) {
            return new HashSet<>(factories);
        }
    }

    /**
     * Gets the recipe with the given identifier, if it exists
     *
     * @param identifier Identifier of the recipe
     * @return Recipe with the given identifier or null if either the recipe doesn't
     * exist or the given string was null
     */
    public IRecipe getRecipe(String identifier) {
        if (identifier == null) {
            return null;
        }
        return recipes.get(identifier);
    }

    /**
     * Registers a recipe and add it to the recipe tracking.
     */
    public void registerRecipe(IRecipe recipe) {
        recipes.put(recipe.getIdentifier(), recipe);
    }

    public void setForceInclude(HashSet<String> forceRecipes) {
        this.forceInclude.addAll(forceRecipes);
    }

    public boolean isForceInclude(String identifier) {
        return this.forceInclude.contains(identifier);
    }

    public Collection<IFactoryEgg> getAllFactoryEggs() {
        return eggs.values();
    }

    public FactoryModPlayerSettings getPlayerSettings() {
        return playerSettings;
    }
}
