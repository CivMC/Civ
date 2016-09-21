package com.devotedmc.ExilePearl.holder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;

/**
 * A player holding an exile pearl
 * @author Gordon
 *
 */
public class PlayerHolder implements PearlHolder {

	private final Player p;
	
	/**
	 * Creates a new PlayerHolder instance
	 * @param p The player holding the pearl
	 */
	public PlayerHolder(final Player p) {
		this.p = p;
	}

	@Override
	public String getName() {
		return p.getName();
	}

	@Override
	public Location getLocation() {
		return p.getPlayer().getLocation().add(0, -.5, 0);
	}
	
	
	public Player getPlayer() {
		return this.p;
	}

	@Override
	public HolderVerifyResult validate(ExilePearl pearl, StringBuilder feedback) {
		
		// Is the holder online?
		if (!p.isOnline()) {
			feedback.append(String.format("Jailor %s not online", p.getName()));
			return HolderVerifyResult.PLAYER_NOT_ONLINE;
		}
		
		// Is the item held?
		ItemStack cursorItem = p.getItemOnCursor();
		if (pearl.validateItemStack(cursorItem)) {
			return HolderVerifyResult.IN_HAND;
		}
		
		// In the player inventory?
		for (ItemStack item : p.getInventory().all(Material.ENDER_PEARL).values()) {
			if (pearl.validateItemStack(item)) {
				return HolderVerifyResult.IN_CHEST;
			}
		}

		// Nope, not found
		feedback.append(String.format("Not in %s's inventory", p.getName()));
		return HolderVerifyResult.DEFAULT;
	}
}
