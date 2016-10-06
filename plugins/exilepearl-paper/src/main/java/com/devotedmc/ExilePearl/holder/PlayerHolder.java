package com.devotedmc.ExilePearl.holder;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * A player holding an exile pearl
 * @author Gordon
 *
 */
public class PlayerHolder implements PearlHolder {

	private final Player player;
	
	/**
	 * Creates a new PlayerHolder instance
	 * @param p The player holding the pearl
	 */
	public PlayerHolder(final Player player) {
		Guard.ArgumentNotNull(player, "player");
		
		this.player = player;
	}

	@Override
	public String getName() {
		return player.getName();
	}

	@Override
	public Location getLocation() {
		return player.getLocation().add(0, -.5, 0);
	}

	@Override
	public HolderVerifyResult validate(ExilePearl pearl) {
		// When the the pearl holder is in creative mode, the inventory options checks do strange things
		if (player.getGameMode() == GameMode.CREATIVE) {
			return HolderVerifyResult.CREATVE_MODE;
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
