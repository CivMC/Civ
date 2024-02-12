package org.ipvp.canvas.type;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface OpenHandler {

	/**
	 * Is called by {@link AbstractCivMenu} when one of its menus are opened by a viewer.
	 *
	 * @param viewer The viewer of the opened menu.
	 * @param menu The menu being opened.
	 */
	void handle(Player viewer, AbstractCivMenu menu);

}
