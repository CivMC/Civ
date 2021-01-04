package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.entities.EntityUtils;

/**
 * Class of static APIs for Entities.
 *
 * @deprecated Use {@link EntityUtils} instead.
 */
@Deprecated
public final class EntityAPI {

	/**
	 * Attempts to retrieve an entity type by its slug or id.
	 *
	 * @param value The value to search for a matching entity type by.
	 * @return Returns a matched entity type or null.
	 *
	 * @deprecated Use {@link EntityUtils#getEntityType(String)} instead.
	 */
	@Deprecated
	public static EntityType getEntityType(String value) {
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		try {
			return EntityType.valueOf(value.toUpperCase());
		}
		catch (Exception ignored) { }
		try {
			EntityType type = EntityType.fromId(Short.parseShort(value));
			if (type != null) {
				return type;
			}
		}
		catch (Exception ignored) { }
		return null;
	}

	/**
	 * Checks whether an entity is a player.
	 *
	 * @param entity The entity to test.
	 * @return Returns true if the entity is a player.
	 *
	 * @deprecated Use {@link EntityUtils#isPlayer(Entity)} instead.
	 */
	@Deprecated
	public static boolean isPlayer(Entity entity) {
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
