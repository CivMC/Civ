package vg.civcraft.mc.civmodcore.entities;

import com.google.common.base.Strings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Class of static APIs for Entities.
 */
public final class EntityUtils {

	/**
	 * Attempts to retrieve an entity type by its slug or id.
	 *
	 * @param value The value to search for a matching entity type by.
	 * @return Returns a matched entity type or null.
	 */
	@SuppressWarnings("deprecation")
	public static EntityType getEntityType(final String value) {
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		try {
			return EntityType.valueOf(value.toUpperCase());
		}
		catch (Exception ignored) { }
		try {
			final EntityType type = EntityType.fromId(Short.parseShort(value));
			if (type != null) {
				return type;
			}
		}
		catch (final Exception ignored) { }
		return null;
	}

	/**
	 * Checks whether an entity is a player.
	 *
	 * @param entity The entity to test.
	 * @return Returns true if the entity is a player.
	 */
	public static boolean isPlayer(final Entity entity) {
		if (entity == null) {
			return false;
		}
		if (entity.getType() != EntityType.PLAYER) {
			return false;
		}
		if (!(entity instanceof Player)) {
			return false;
		}
		return true;
	}

}
