package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Sapling;
import java.util.Random;

public class TreeGrower extends AgeableGrower {
    private final Random random = new Random();

    private static boolean canBeBig(Material mat) {
        switch (mat) {
            case OAK_SAPLING:
            case BIRCH_SAPLING:
            case ACACIA_SAPLING:
            case CHORUS_FLOWER:
            case FLOWERING_AZALEA:
            case CHERRY_SAPLING:
            case MANGROVE_PROPAGULE:
                return false;
            case DARK_OAK_SAPLING:
            case JUNGLE_SAPLING:
            case SPRUCE_SAPLING:
            case PALE_OAK_SAPLING:
                return true;
            default:
                return false;
        }
    }

    /**
     * checks whether this sapling is the NW sapling of a valid 2x2 sapling setup
     *
     * @param mat the material the saplings must share
     * @param northwest the sapling to check for
     * @return true iff this is a valid 2x2 setup
     */
    private static boolean adjacentSaplingCheck(Material mat, Block northwest) {
        for (int i = 0; i < 4; i++) {
            Block block = northwest.getRelative(i % 2, 0, i / 2);
            if (block.getType() != mat) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the block is part of a 2x2 grid and returns the north west block.
     * The block is tested as the northwest, northeast, southwest, and southeast sapling,
     * in that order.
     *
     * @param block to check for
     * @param mat   Sapling material
     * @return North west block; null if the block is not part of a 2x2 sapling grid
     */
    private static Block find2x2NWSapling(Block block, Material mat) {
        for (int i = 0; i < 4; i++) {
            Block nwCandidate = block.getRelative(-(i % 2), 0, -(i / 2));
            if (adjacentSaplingCheck(mat, nwCandidate)) {
                return nwCandidate;
            }
        }
        return null;
    }

    private static TreeType remapSaplingToTree(Material mat, boolean big) {
        switch (mat) {
            case OAK_SAPLING:
                return TreeType.TREE;
            case BIRCH_SAPLING:
                return TreeType.BIRCH;
            case ACACIA_SAPLING:
                return TreeType.ACACIA;
            case FLOWERING_AZALEA:
                return TreeType.AZALEA;
            case CHERRY_SAPLING:
                return TreeType.CHERRY;
            case MANGROVE_PROPAGULE:
                return big ? TreeType.TALL_MANGROVE : TreeType.MANGROVE;
            case DARK_OAK_SAPLING:
                return big ? TreeType.DARK_OAK : null;
            case PALE_OAK_SAPLING:
                return big ? TreeType.PALE_OAK : null;
            case JUNGLE_SAPLING:
                return big ? TreeType.JUNGLE : TreeType.SMALL_JUNGLE;
            case SPRUCE_SAPLING:
                return big ? TreeType.MEGA_REDWOOD : TreeType.REDWOOD;
            case CHORUS_FLOWER:
                return TreeType.CHORUS_PLANT;
            default:
                return null;
        }
    }


    public TreeGrower(Material saplingType) {
        super(saplingType, 1, 1);
    }

    @Override
    public int getStage(Plant plant) {
        Block block = plant.getLocation().getBlock();
        if (block.getType() != this.material) {
            return -1;
        }
        return 0;
    }

    /**
     * Set the saplings of the tree to a given material
     *
     * @param northwest northwestern sapling of the base
     * @param isBig whether to set a 2x2 or 1x1 shape
     * @param mat the material to change to
     */
    public void setSaplings(Block northwest, boolean isBig, Material mat) {
        northwest.setType(mat);

        if (!isBig) {
            return;
        }

        Block northeast, southwest, southeast;
        northeast = northwest.getRelative(BlockFace.EAST);
        southwest = northwest.getRelative(BlockFace.SOUTH);
        southeast = northeast.getRelative(BlockFace.SOUTH);

        northeast.setType(mat);
        southwest.setType(mat);
        southeast.setType(mat);
    }

    /**
     * Attempts to grow a tree at the provided position.
     * It will do 10 grow attempts.
     *
     * @param location The position to grow the tree at. NW sapling for 2x2 trees.
     * @param type The type of tree to grow at the position.
     * @return True if the tree grew successfully
     */
    public boolean tryGrowTree(Location location, TreeType type) {
        World world = location.getWorld();
        for (int i = 0; i < 10; i++) {
            if (world.generateTree(location, random, type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean setStage(Plant plant, int stage) {
        if (stage < 1) {
            return true;
        }
        Block block = plant.getLocation().getBlock();
        // Re-Read the block data to make sure it is up to date
        if (!(block.getBlockData() instanceof Sapling) && block.getType() != Material.CHORUS_FLOWER) {
            return true;
        }
        Material mat = block.getType();
        boolean canBeBig = canBeBig(mat);
        if (canBeBig) {
            Block found = find2x2NWSapling(block, mat);
            canBeBig = found != null;

            if (canBeBig) {
                block = found;
            }
        }

        TreeType type = remapSaplingToTree(mat, canBeBig);
        if (type == null) {
            return true;
        }

        setSaplings(block, canBeBig, Material.AIR);
        if (!tryGrowTree(block.getLocation(), type)) {
            setSaplings(block, canBeBig, mat);
        }
        return true;
    }

    @Override
    public boolean deleteOnFullGrowth() {
        return true;
    }

}
