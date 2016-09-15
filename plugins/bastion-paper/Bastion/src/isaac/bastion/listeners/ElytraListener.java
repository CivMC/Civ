package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ElytraManager;
import isaac.bastion.storage.BastionBlockSet;

import java.awt.geom.Point2D;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class ElytraListener implements Listener {

	private ElytraManager manager;
	
	public ElytraListener() {
		manager = new ElytraManager();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onElytraGlide(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if(!p.isGliding()) return;
		
		//Bastion.getPlugin().getLogger().info("Gliding");
		
		PlayerInventory inv = p.getInventory();
		if(inv.getChestplate().getType() != Material.ELYTRA) return;
		
		//Bastion.getPlugin().getLogger().info("Via elytra");
		
		if (manager.handleElytraMovement(p, event.getTo())) {
			Bastion.getPlugin().getLogger().info("Blocked.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true) 
	public void onPlayerDepart(PlayerQuitEvent event) {
		unwatchPlayer(event);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerKick(PlayerKickEvent event) {
		unwatchPlayer(event);
	}
	
	private void unwatchPlayer(PlayerEvent event) {
		Player p = event.getPlayer();
		if (p == null) return;
		manager.clearThrottle(p.getUniqueId());
	}
}
