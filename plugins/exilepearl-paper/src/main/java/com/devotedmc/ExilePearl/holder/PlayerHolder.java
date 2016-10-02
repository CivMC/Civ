package com.devotedmc.ExilePearl.holder;

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
	public HolderVerifyResult validate(ExilePearl pearl, StringBuilder feedback) {
		
		// Is the holder online?
		if (!player.isOnline()) {
			feedback.append(String.format("Jailor %s not online", player.getName()));
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
				return HolderVerifyResult.IN_CHEST;
			}
		}

		// Nope, not found
		feedback.append(String.format("Not in %s's inventory", player.getName()));
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
}
