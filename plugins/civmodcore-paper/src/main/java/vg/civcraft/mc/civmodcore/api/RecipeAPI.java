package vg.civcraft.mc.civmodcore.api;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;
import vg.civcraft.mc.civmodcore.inventory.RecipeManager;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

/**
 * Class of utility functions for Recipes.
 *
 * @deprecated Use {@link RecipeManager} instead.
 */
@Deprecated
public final class RecipeAPI {

	/**
	 * <p>Determines whether a recipe matches another recipe.</p>
	 *
	 * <p>Note: This matcher pretty much assumes that all recipes are keyed. If other kinds of recipes come up that
	 * aren't keyed, then support here can be added. But until then? /shrug</p>
	 *
	 * @param base The base recipe to base the matching upon.
	 * @param other The other recipe, the unknown.
	 * @return Returns true if the other recipe matches the base.
	 *
	 * @deprecated Use {@link RecipeManager#matchRecipe(Recipe, Recipe)} instead.
	 */
	@Deprecated
	public static boolean matchRecipe(Recipe base, Recipe other) {
		if (base == null) {
			return false;
		}
		if (base instanceof Keyed && other instanceof Keyed) {
			return NullCoalescing.equalsNotNull(
					((Keyed) base).getKey(),
					((Keyed) other).getKey());
		}
		return false;
	}

	/**
	 * <p>Registers a recipe to the Bukkit server.</p>
	 *
	 * <p>This is to prevent thrown exceptions for re-registered recipes. Since registrations often happen within a
	 * plugin's onEnable() method, an exception here will actually disable the plugin entirely, which is a bit
	 * disproportionate. You should check the returned boolean to see whether the registration was successful and
	 * handle that accordingly.</p>
	 *
	 * @param recipe The recipe to register.
	 * @return Returns true if the recipe was registered.
	 *
	 * @deprecated Use {@link RecipeManager#registerRecipe(Recipe)} instead.
	 */
	@Deprecated
	public static boolean registerRecipe(Recipe recipe) {
		if (recipe == null) {
			return false;
		}
		try {
			return Bukkit.getServer().addRecipe(recipe);
		}
		catch (Exception exception) {
			return false;
		}
	}

	/**
	 * Removes a recipe from Bukkit's registered recipes list.
	 *
	 * @param recipe The shaped recipe to deregister.
	 * @return Returns true if the recipe was de-registered, or wasn't ever registered.
	 *
	 * @deprecated Use {@link RecipeManager#registerRecipe(Recipe)} instead.
	 */
	@Deprecated
	public static boolean removeRecipe(Recipe recipe) {
		if (recipe == null) {
			return false;
		}
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

}
