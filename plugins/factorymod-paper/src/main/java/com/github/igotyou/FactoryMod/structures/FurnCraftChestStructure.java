package com.github.igotyou.FactoryMod.structures;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Physical representation of a factory consisting of a chest, a crafting table
 * and a furnace. The crafting table has to be inbetween the furnace and chest.
 * The chest may be a double chest, but the part of the double chest not
 * adjacent to the crafting table is ignored when doing any checks
 */
public class FurnCraftChestStructure extends MultiBlockStructure {

    private Location craftingTable;
    private Location furnace;
    private Location chest;
    private boolean complete;

    public FurnCraftChestStructure(Block center) {
        if (center.getType() == Material.CRAFTING_TABLE) {
            craftingTable = center.getLocation();
            LinkedList<Block> chestBlocks = new LinkedList<>();
            chestBlocks.addAll(searchForBlockOnAllSides(center, Material.CHEST));
            chestBlocks.addAll(searchForBlockOnAllSides(center, Material.TRAPPED_CHEST));
            chestBlocks.addAll(searchForBlockOnAllSides(center, Material.BARREL));
            for (Block b : chestBlocks) {
                BlockFace chestFace = center.getFace(b);
                if (chestFace == null) continue; // fricc off nullcheck
                BlockFace furnaceFace = chestFace.getOppositeFace();
                Block furnaceBlock = center.getRelative(furnaceFace);
                if (furnaceBlock.getType() == Material.FURNACE) {
                    chest = b.getLocation();
                    furnace = furnaceBlock.getLocation();
                    break;
                }
            }
        }
        complete = chest != null && furnace != null;
    }

    public FurnCraftChestStructure(List<Location> blocks) {
        craftingTable = blocks.get(0);
        furnace = blocks.get(1);
        chest = blocks.get(2);
    }

    public void recheckComplete() {
        complete = craftingTable != null
            && craftingTable.getBlock().getType() == Material.CRAFTING_TABLE
            && furnace != null
            && furnace.getBlock().getType() == Material.FURNACE
            && chest != null
            && (chest.getBlock().getType() == Material.CHEST
            || chest.getBlock().getType() == Material.TRAPPED_CHEST
            || chest.getBlock().getType() == Material.BARREL);
    }

    public boolean isComplete() {
        return complete;
    }

    public Block getCraftingTable() {
        return craftingTable.getBlock();
    }

    public Block getFurnace() {
        return furnace.getBlock();
    }

    public Block getChest() {
        // sometimes a double chest will go across chunk borders and the other
        // half of the chest might be unloaded. To load the other half and the
        // full inventory this is needed to load the chunk
        MultiBlockStructure.searchForBlockOnAllSides(chest.getBlock(),
            Material.CHEST);
        MultiBlockStructure.searchForBlockOnAllSides(chest.getBlock(),
            Material.TRAPPED_CHEST);

        // not necessary to check for more blocks if the "chest" is a barrel because it cannot be double

        return chest.getBlock();
    }

    public boolean relevantBlocksDestroyed() {
        return craftingTable.getBlock().getType() != Material.CRAFTING_TABLE
            && furnace.getBlock().getType() != Material.FURNACE
            && chest.getBlock().getType() != Material.CHEST
            && chest.getBlock().getType() != Material.TRAPPED_CHEST
            && chest.getBlock().getType() != Material.BARREL;
    }

    public List<Block> getRelevantBlocks() {
        LinkedList<Block> result = new LinkedList<>();
        result.add(getCraftingTable());
        result.add(getFurnace());
        result.add(getChest());
        return result;
    }

    public List<Location> getAllBlocks() {
        LinkedList<Location> result = new LinkedList<>();
        result.add(craftingTable);
        result.add(furnace);
        result.add(chest);
        return result;
    }

    public Location getCenter() {
        return craftingTable;
    }

}
