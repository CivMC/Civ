package vg.civcraft.mc.civmodcore.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;

/**
 * Class of utility functions for Recipes.
 */
public final class RecipeManager {

	/**
	 * <p>Determines whether a recipe matches another recipe.</p>
	 *
	 * <p>Note: This matcher pretty much assumes that all recipes are keyed. If other kinds of recipes come up that
	 * aren't keyed, then support here can be added. But until then? /shrug</p>
	 *
	 * @param base The base recipe to base the matching upon.
	 * @param other The other recipe, the unknown.
	 * @return Returns true if the other recipe matches the base.
	 */
	public static boolean matchRecipe(final Recipe base, final Recipe other) {
		if (base == other) {
			return true;
		}
		if (base instanceof Keyed baseKeyed && other instanceof Keyed otherKeyed) {
			return NullUtils.equalsNotNull(
					baseKeyed.getKey(),
					otherKeyed.getKey());
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
	 */
	public static boolean registerRecipe(final Recipe recipe) {
		if (recipe == null) {
			return false;
		}
		try {
			return Bukkit.getServer().addRecipe(recipe);
		}
		catch (final Throwable exception) {
			return false;
		}
	}

	/**
	 * Removes a recipe from Bukkit's registered recipes list.
	 *
	 * @param recipe The shaped recipe to deregister.
	 * @return Returns true if the recipe was de-registered, or wasn't ever registered.
	 */
	public static boolean removeRecipe(final Recipe recipe) {
		if (recipe == null) {
			return false;
		}
		for (final var iterator = Bukkit.getServer().recipeIterator(); iterator.hasNext();) {
			if (matchRecipe(recipe, iterator.next())) {
				iterator.remove();
				break;
			}
		}
		return true;
	}

}
