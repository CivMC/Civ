package com.devotedmc.ExilePearl.holder;

import com.devotedmc.ExilePearl.ExilePearl;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A player holding an exile pearl
 * @author Gordon
 *
 */
public class PlayerHolder implements PearlHolder {

	private final Player player;

	/**
	 * Creates a new PlayerHolder instance
	 * @param player The player holding the pearl
	 */
	public PlayerHolder(final Player player) {
		Preconditions.checkNotNull(player, "player");

		this.player = player;
	}

	@Override
	public String getName() {
		return ChatColor.AQUA + player.getName();
	}

	@Override
	public Location getLocation() {
		return player.getLocation().add(0, -.5, 0);
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public HolderVerifyResult validate(ExilePearl pearl) {
		// When the the pearl holder is in creative mode, the inventory options checks do strange things
		if (player.getGameMode() == GameMode.CREATIVE) {
			//return HolderVerifyResult.CREATVE_MODE;
		}

		// Is the holder online?
		if (!player.isOnline()) {
			return HolderVerifyResult.PLAYER_NOT_ONLINE;
		}

		// Is the item held?
		ItemStack cursorItem = player.getItemOnCursor();
		if (pearl.validateItemStack(cursorItem)) {
			return HolderVerifyResult.IN_HAND;
		}

		// In the player inventory?
		for (ItemStack item : player.getInventory().all(Material.ENDER_PEARL).values()) {
			if (pearl.validateItemStack(item)) {
				return HolderVerifyResult.IN_PLAYER_INVENTORY;
			}
		}

		// In a crafting inventory?
		for (ItemStack item : player.getOpenInventory().getTopInventory().all(Material.ENDER_PEARL).values()) {
			if (pearl.validateItemStack(item)) {
				return HolderVerifyResult.IN_PLAYER_INVENTORY_VIEW;
			}
		}

		//Is the item in the players offhand?
		ItemStack offhand = player.getInventory().getItemInOffHand();
		if (pearl.validateItemStack(offhand)) {
			return HolderVerifyResult.IN_OFFHAND;
		}

		// Nope, not found
		return HolderVerifyResult.DEFAULT;
	}

	@Override
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlayerHolder other = (PlayerHolder) o;

		return player.equals(other.player);
	}

	@Override
	public boolean isBlock() {
		return false;
	}
}
