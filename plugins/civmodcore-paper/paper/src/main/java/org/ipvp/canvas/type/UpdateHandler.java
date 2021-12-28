package org.ipvp.canvas.type;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface UpdateHandler {

	/**
	 * Is called by {@link AbstractCivMenu} when one of its menus are updated. This is called after the menu has been
	 * updated but before the update packets have been sent to the player.
	 *
	 * @param viewer The viewer of the updating menu.
	 * @param menu The menu being updated.
	 */
    void handle(Player viewer, AbstractCivMenu menu);

}
