package vg.civcraft.mc.civmodcore.events;

import java.util.Objects;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class EventUtils {

	/**
	 * Updates the item used in the given interact event to the given item. This utility assumes that the interaction
	 * was done with the player's hands.
	 *
	 * @param event The interact event.
	 * @param item The item to set.
	 */
	public static void setPlayerInteractItem(@NotNull final PlayerInteractEvent event,
											 final ItemStack item) {
		event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), item);
	}

}
