package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;

import java.awt.geom.Point2D;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class PlayerMoveListener implements Listener {

	private BastionBlockSet bastions;
	
	public PlayerMoveListener() {
		bastions=Bastion.getBastionManager().set;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if(p.isGliding()) {
			Set<BastionBlock> possible = bastions.getPossibleTeleportBlocking(p.getLocation(), 20);
			//The distance the player can travel in about a tenth of a second
			double maxTravel = (p.getVelocity().getX() + p.getVelocity().getZ()) * 2;
			for(BastionBlock bastion : possible) {
				Location bLoc = bastion.getLocation();
				Location pLoc = p.getLocation();
				Point2D bPoint = new Point2D.Double(bLoc.getX(), bLoc.getZ());
				Point2D pPoint = new Point2D.Double(pLoc.getX(), pLoc.getZ());
				double distance = bPoint.distance(pPoint);
				if(distance - BastionBlock.getRadius() < maxTravel) {
					p.setVelocity(new Vector(0, 0, 0));
					PlayerInventory inv = p.getInventory();
					if(inv.getItemInOffHand().getType() == Material.ELYTRA) {
						inv.getItemInOffHand().setDurability((short)0);
					}
				}
			}
		}
	}
}
