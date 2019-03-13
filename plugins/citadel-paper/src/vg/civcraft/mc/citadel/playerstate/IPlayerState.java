package vg.civcraft.mc.citadel.playerstate;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class IPlayerState {
	
	protected UUID uuid;
	
	public IPlayerState(Player p) {
		if (p == null) {
			throw new IllegalArgumentException("Player for player state can not be null");
		}
		this.uuid = p.getUniqueId();
	}
	
	public abstract void handleBlockPlace(BlockPlaceEvent e);
	
	public abstract void handleInteractBlock(PlayerInteractEvent e);
	
	public abstract void handleBreakBlock(BlockBreakEvent e);

}
