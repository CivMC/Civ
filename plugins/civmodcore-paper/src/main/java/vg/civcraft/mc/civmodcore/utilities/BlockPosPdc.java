package vg.civcraft.mc.civmodcore.utilities;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPosPdc {

    public static boolean addBlock(Chunk chunk, NamespacedKey key, int x, int y, int z) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
        int[] ints = chunkPdc.get(key, PersistentDataType.INTEGER_ARRAY);
        if (ints != null) {
            for (int i = 0; i < ints.length - 2; i += 3) {
                if (ints[i] == x && ints[i + 1] == y && ints[i + 2] == z) {
                    return false;
                }
            }
        }
        IntList list = ints == null ? new IntArrayList() : new IntArrayList(ints);
        list.add(x);
        list.add(y);
        list.add(z);

        chunkPdc.set(key, PersistentDataType.INTEGER_ARRAY, list.toIntArray());
        return true;
    }

    public static boolean testBlock(Chunk chunk, NamespacedKey key, int x, int y, int z) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
        int[] ints = chunkPdc.get(key, PersistentDataType.INTEGER_ARRAY);
        if (ints == null) {
            return false;
        }

        for (int i = 0; i < ints.length - 2; i += 3) {
            if (ints[i] == x && ints[i + 1] == y && ints[i + 2] == z) {
                return true;
            }
        }
        return false;
    }

    public static boolean removeBlock(Chunk chunk, NamespacedKey key, int x, int y, int z) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
        int[] ints = chunkPdc.get(key, PersistentDataType.INTEGER_ARRAY);
        if (ints == null) {
            return false;
        }

        IntList list = new IntArrayList(ints);
        int index = -1;
        for (int i = 0; i < list.size() - 2; i += 3) {
            if (list.getInt(i) == x && list.getInt(i + 1) == y && list.getInt(i + 2) == z) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return false;
        }

        list.removeElements(index, index + 3);
        chunkPdc.set(key, PersistentDataType.INTEGER_ARRAY, list.toIntArray());
        return true;
    }

    public static List<BlockPos> getBlocks(Chunk chunk, NamespacedKey key) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
        int[] ints = chunkPdc.get(key, PersistentDataType.INTEGER_ARRAY);
        if (ints == null) {
            return Collections.emptyList();
        }

        List<BlockPos> blocks = new ArrayList<>(ints.length / 3);
        for (int i = 0; i < ints.length - 2; i += 3) {
            blocks.add(new BlockPos(ints[i], ints[i + 1], ints[i + 2]));
        }

        return Collections.unmodifiableList(blocks);
    }
}
