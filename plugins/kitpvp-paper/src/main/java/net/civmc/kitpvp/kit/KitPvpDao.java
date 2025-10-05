package net.civmc.kitpvp.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.UUID;

public interface KitPvpDao {
    Kit getKit(String name, UUID player);
    Kit getKit(int id);
    List<Kit> getKits(UUID player);
    Kit createKit(String name, UUID player);
    Kit setPublicKit(int id, boolean isPublic);
    Kit updateKit(int id, Material icon, ItemStack[] items);
    Kit renameKit(int id, String name);

    void deleteKit(int id);
}
