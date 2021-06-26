package vg.civcraft.mc.civmodcore.inventory.items;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.base.Strings;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * <p>See <a href="https://github.com/Protonull/BukkitReport/tree/master/reports">BukkitReports</a>.</p>
 *
 * <ul>
 *     <label>See also:</label>
 *     <li>{@link MoreTags MoreTags}</li>
 *     <li>{@link SpawnEggUtils SpawnEggUtils}</li>
 *     <li>{@link TreeTypeUtils TreeTypeUtils}</li>
 * </ul>
 */
@UtilityClass
public final class MaterialUtils {

	private static final List<Material> HASH_MATERIALS = new ArrayList<>() {{
		addAll(Tag.WOOL.getValues());
		addAll(MaterialTags.STAINED_GLASS.getValues());
		addAll(MaterialTags.STAINED_GLASS_PANES.getValues());
		addAll(MaterialTags.CONCRETES.getValues());
	}};

	/**
	 * Attempts to retrieve a material by its slug.
	 *
	 * @param value The value to search for a matching material by.
	 * @return Returns a matched material or null.
	 */
	public static Material getMaterial(final String value) {
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		return Material.getMaterial(value.toUpperCase());
	}

	/**
	 * Checks whether a material is air.
	 * Will also return true if the given material is null.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is air.
	 */
	public static boolean isAir(final Material material) {
		if (material == null) {
			return true;
		}
		return material.isAir();
	}

	/**
	 * Gets a random material based on the given objects hashcode.
	 *
	 * @param object Object to base returned material on
	 * @return Material hash of the given object
	 */
	public static Material getMaterialHash(final Object object) {
		if (object == null) {
			return HASH_MATERIALS.get(0);
		}
		final int index = IntMath.mod(object.hashCode(), HASH_MATERIALS.size());
		return HASH_MATERIALS.get(index);
	}

}
