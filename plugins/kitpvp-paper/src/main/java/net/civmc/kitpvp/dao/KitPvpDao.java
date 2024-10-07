package net.civmc.kitpvp.dao;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.UUID;

public interface KitPvpDao {
    List<Kit> getKits(UUID player);
    void newKit(String name, UUID player);
    Kit updateKit(int id, Material icon, ItemStack[] items);
    Kit renameKit(int id, String name);

    void deleteKit(int id);
}
