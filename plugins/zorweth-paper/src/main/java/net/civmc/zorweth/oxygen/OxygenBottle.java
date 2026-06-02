package net.civmc.zorweth.oxygen;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class OxygenBottle {

    public static ItemStack createCrudeOxygen() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        item.setData(DataComponentTypes.ITEM_MODEL, new NamespacedKey("minecraft", "glass_bottle"));
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().consumeSeconds(0.4f).animation(ItemUseAnimation.DRINK).hasConsumeParticles(false).sound(NamespacedKey.minecraft("entity.generic.drink")).build());
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 16);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Crude Oxygen Bottle", TextColor.color(140, 163, 177)));
        meta.lore(List.of(Component.text("It can keep a traveller going for a little", NamedTextColor.WHITE),
            Component.text("longer on this dastardly planet.", NamedTextColor.WHITE) ,
            Component.text("Provides +90 oxygen", NamedTextColor.WHITE)));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        CustomItem.registerCustomItem("crude_oxygen_bottle", item);

        return item;
    }

    public static boolean isCrudeOxygen(ItemStack item) {
        return CustomItem.isCustomItem(item, "crude_oxygen_bottle");
    }

    public static CraftingRecipe getRecipe(Plugin plugin) {
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "crude_oxygen_dry"), OxygenBottle.createCrudeOxygen())
            .addIngredient(Material.GLASS_BOTTLE)
            .addIngredient(Material.COAL)
            .addIngredient(Material.PITCHER_PLANT);
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}
