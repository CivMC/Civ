package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

/**
 * Class of utility functions for Recipes.
 */
public final class RecipeAPI {

	/**
	 * Determines whether a shaped recipe matches another recipe.
	 *
	 * @param base The base recipe to base the matching upon.
	 * @param other The other recipe, the unknown.
	 * @return Returns true if the other recipe matches the base.
	 */
	public static boolean matchRecipe(ShapedRecipe base, Recipe other) {
		if (base == null) {
			return false;
		}
		ShapedRecipe shaped = NullCoalescing.chain(() -> (ShapedRecipe) other);
		if (shaped == null) {
			return false;
		}
		if (!base.getKey().equals(shaped.getKey())) {
			return false;
		}
		if (shaped.getIngredientMap().hashCode() != base.getIngredientMap().hashCode()) {
			return false;
		}
		if (!Objects.equals(shaped.getResult(), base.getResult())) {
			return false;
		}
		return true;
	}

	/**
	 * Determines whether a shapeless recipe matches another recipe.
	 *
	 * @param base The base recipe to base the matching upon.
	 * @param other The other recipe, the unknown.
	 * @return Returns true if the other recipe matches the base.
	 */
	public static boolean matchRecipe(ShapelessRecipe base, Recipe other) {
		if (base == null) {
			return false;
		}
		ShapelessRecipe shapeless = NullCoalescing.chain(() -> (ShapelessRecipe) other);
		if (shapeless == null) {
			return false;
		}
		if (!base.getKey().equals(shapeless.getKey())) {
			return false;
		}
		if (shapeless.getIngredientList().hashCode() != base.getIngredientList().hashCode()) {
			return false;
		}
		if (!Objects.equals(shapeless.getResult(), base.getResult())) {
			return false;
		}
		return true;
	}

	/**
	 * Remove a shaped recipe from Bukkit's registered recipes list.
	 *
	 * @param recipe The shaped recipe to deregister.
	 * @return Returns true if the recipe was de-registered, or wasn't ever registered.
	 */
	public static boolean removeRecipe(ShapedRecipe recipe) {
		Preconditions.checkArgument(recipe != null);
		Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
		while (iterator.hasNext()) {
			if (!matchRecipe(recipe, iterator.next())) {
				continue;
			}
			iterator.remove();
			return true;
		}
		return true;
	}

	/**
	 * Remove a shapeless recipe from Bukkit's registered recipes list.
	 *
	 * @param recipe The shapeless recipe to deregister.
	 * @return Returns true if the recipe was de-registered, or wasn't ever registered.
	 */
	public static boolean removeRecipe(ShapelessRecipe recipe) {
		Preconditions.checkArgument(recipe != null);
		Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();
		while (iterator.hasNext()) {
			if (!matchRecipe(recipe, iterator.next())) {
				continue;
			}
			iterator.remove();
			return true;
		}
		return false;
	}

}
