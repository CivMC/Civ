package net.civmc.heliodor.meteoriciron;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class BindRecipe extends CustomRecipe {
    private final static RecipeSerializer<BindRecipe> SERIALIZER = RecipeSerializer.register("crafting_special_allow_bind", new SimpleCraftingRecipeSerializer<>(BindRecipe::new));

    public BindRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Nullable
    private ItemStack getTool(CraftingInput input) {
        ItemStack tool = null;
        ItemStack nugget = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack test = input.getItem(i);
            if (!test.isEmpty()) {
                if (CustomItem.isCustomItem(test.getBukkitStack(), "meteoric_iron_nugget") && test.getCount() == 1) {
                    if (nugget == null) {
                        nugget = test;
                    } else {
                        return null;
                    }
                } else if (test.getBukkitStack().getPersistentDataContainer().has(new NamespacedKey("simpleadminhacks", "no_bind")) &&
                    test.getBukkitStack().getPersistentDataContainer().has(new NamespacedKey("heliodor", "meteoric_unbindable"))) {
                    if (tool == null) {
                        tool = test;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return tool != null && nugget != null ? tool : null;
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        return this.getTool(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider lookup) {
        ItemStack tool = this.getTool(input);
        if (tool == null) {
            return ItemStack.EMPTY;
        } else {
            org.bukkit.inventory.ItemStack copy = tool.asBukkitCopy();
            ItemMeta meta = copy.getItemMeta();
            meta.getPersistentDataContainer().remove(new NamespacedKey("heliodor", "meteoric_unbindable"));
            meta.getPersistentDataContainer().remove(new NamespacedKey("simpleadminhacks", "no_bind"));
            copy.setItemMeta(meta);
            return ((CraftItemStack) copy).handle;
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
