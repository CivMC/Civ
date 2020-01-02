package vg.civcraft.mc.civmodcore.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Class of static APIs for Entities.
 * */
public final class EntityAPI {

	private EntityAPI() { }

	/**
	 * Checks whether an entity is a player.
	 *
	 * @param entity The entity to test.
	 * @return Returns true if the entity is a player.
	 * */
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
