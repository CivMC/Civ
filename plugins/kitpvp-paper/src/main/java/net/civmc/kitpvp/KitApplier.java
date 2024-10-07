package net.civmc.kitpvp;

import net.civmc.kitpvp.dao.Kit;
import net.minecraft.world.food.FoodData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitApplier {

    public static void applyKit(Kit kit, Player player) {
        player.clearActivePotionEffects();

        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setExhaustion(0);

        player.setHealth(20);

        player.setFireTicks(0);

        player.getInventory().clear();
        player.getInventory().setContents(kit.items());
    }
}
