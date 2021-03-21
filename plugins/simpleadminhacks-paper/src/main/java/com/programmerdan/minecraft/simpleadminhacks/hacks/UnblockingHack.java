package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.UnblockingHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import vg.civcraft.mc.civmodcore.inventory.RecipeManager;

public class UnblockingHack extends SimpleHack<UnblockingHackConfig> {

	public UnblockingHack(final SimpleAdminHacks plugin, final UnblockingHackConfig config) {
		super(plugin, config);
	}

	public static UnblockingHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new UnblockingHackConfig(plugin, config);
	}

	private final List<Recipe> recipes = new ArrayList<>();
	private int recipeCounter = 0;

	@Override
	public void onEnable() {
		super.onEnable();
		// Blocks that turn into 9 parts
		config().getNinePartsBlocks().forEach((fromMaterial, toMaterial) -> {
			final var recipe = new ShapelessRecipe(
					new NamespacedKey(this.plugin, "unblocking_9part_" + (++this.recipeCounter)),
					new ItemStack(toMaterial, 9));
			recipe.addIngredient(fromMaterial);
			RecipeManager.registerRecipe(recipe);
		});
		// Blocks that turn into 4 parts
		config().getFourPartsBlocks().forEach((fromMaterial, toMaterial) -> {
			final var recipe = new ShapelessRecipe(
					new NamespacedKey(this.plugin, "unblocking_4part_" + (++this.recipeCounter)),
					new ItemStack(toMaterial, 4));
			recipe.addIngredient(fromMaterial);
			RecipeManager.registerRecipe(recipe);
		});
		// 9 items that turn into blocks
		config().getNineItemsBlocks().forEach((fromMaterial, toMaterial) -> {
			final var recipe = new ShapedRecipe(
					new NamespacedKey(this.plugin, "blocking_9item_" + (++this.recipeCounter)),
					new ItemStack(toMaterial));
			recipe.shape("xxx", "xxx", "xxx");
			recipe.setIngredient('x', fromMaterial);
			RecipeManager.registerRecipe(recipe);
		});
		// 4 items that turn into blocks
		config().getFourItemsBlocks().forEach((fromMaterial, toMaterial) -> {
			final var recipe = new ShapedRecipe(
					new NamespacedKey(this.plugin, "blocking_4item_" + (++this.recipeCounter)),
					new ItemStack(toMaterial));
			recipe.shape("xx", "xx");
			recipe.setIngredient('x', fromMaterial);
			RecipeManager.registerRecipe(recipe);
		});
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this.recipes.forEach(RecipeManager::removeRecipe);
		this.recipes.clear();
		this.recipeCounter = 0;
	}

}
