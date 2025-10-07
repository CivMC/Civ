package com.untamedears.realisticbiomes.breaker;

import com.untamedears.realisticbiomes.RealisticBiomes;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeDelegate implements Consumer<BlockState> {

    public static final NamespacedKey TREE_BLOCKS = new NamespacedKey(JavaPlugin.getPlugin(RealisticBiomes.class), "tree_blocks");

    @Override
    public void accept(BlockState blockState) {
        if (!isTreeBlock(blockState.getType())) {
            return;
        }

        PersistentDataContainer pdc = blockState.getChunk().getPersistentDataContainer();
        int[] ints = pdc.get(TREE_BLOCKS, PersistentDataType.INTEGER_ARRAY);
        IntArrayList list = ints == null ? new IntArrayList() : new IntArrayList(ints);
        for (int i = 0; i < list.size(); i += 3) {
            if (list.getInt(i) == blockState.getX() && list.getInt(i + 1) == blockState.getY() && list.getInt(i + 2) == blockState.getZ()) {
                return;
            }
        }
        list.ensureCapacity(3);
        list.add(blockState.getX());
        list.add(blockState.getY());
        list.add(blockState.getZ());

        pdc.set(TREE_BLOCKS, PersistentDataType.INTEGER_ARRAY, list.toIntArray());
    }

    public static boolean isTreeBlock(Material type) {
        return Tag.LOGS.isTagged(type) || Tag.LEAVES.isTagged(type) || type == Material.BROWN_MUSHROOM_BLOCK || type == Material.RED_MUSHROOM_BLOCK;
    }

    public static boolean remove(Block block) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        int[] ints = pdc.get(TreeDelegate.TREE_BLOCKS, PersistentDataType.INTEGER_ARRAY);
        if (ints == null) {
            return false;
        }

        IntList list = new IntArrayList(ints);
        int index = -1;
        for (int i = 0; i < list.size(); i += 3) {
            if (list.getInt(i) == block.getX() && list.getInt(i + 1) == block.getY() && list.getInt(i + 2) == block.getZ()) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return false;
        }

        list.removeElements(index, index + 3);
        pdc.set(TreeDelegate.TREE_BLOCKS, PersistentDataType.INTEGER_ARRAY, list.toIntArray());
        return true;
    }
}
