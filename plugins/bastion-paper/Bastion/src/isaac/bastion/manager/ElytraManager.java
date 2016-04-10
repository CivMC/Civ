package isaac.bastion.manager;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class ElytraManager {

	private BastionBlockSet bastions;
	private Map<UUID, Long> throttle;
	
	public ElytraManager() {
		bastions = Bastion.getBastionManager().set;
		throttle = new ConcurrentHashMap<UUID, Long>();
	}
	
	public void clearThrottle(UUID player) {
		throttle.remove(player);
	}
	
	public boolean handleElytraMovement(Player p, Location to) {
		// Throttle checks to no more then 10 a second.
		Long lastCheck = throttle.get(p.getUniqueId());
		if (lastCheck != null && lastCheck.longValue() < System.currentTimeMillis()) return false;
		throttle.put(p.getUniqueId(), System.currentTimeMillis()+100l);
		
		// collect to/from
		Location from = p.getLocation();
		if (to == null) {
			to = p.getLocation();
		}
		
		// fix vector
		Vector pVec = p.getVelocity();
		if (pVec == null || pVec.lengthSquared() == 0.0d) {
			pVec = new Vector(to.getX() - from.getX(),
					to.getY() - from.getY(),
					to.getZ() - from.getZ());
		}
		
		// find maxset of bastions
		Set<BastionBlock> possible = bastions.getPossibleFlightBlocking(to, 
				Bastion.getConfigManager().getBastionBlockEffectRadius() * 2);
		if (possible == null || possible.isEmpty()) return false;
		
		// We doubledown; project the vector out another movement cycle.
		Location newTo = to.clone().add(pVec);
		
		Set<BastionBlock> definiteCollide = EnderPearlManager.staticSimpleCollide(possible, from, newTo, p);
		if (definiteCollide == null || definiteCollide.isEmpty()) return false;
		
		// only allocate if we're going to use it.
		Set<BastionBlock> impact = null;
		if (Bastion.getConfigManager().getElytraErosionScale() > 0 && 
				 Bastion.getConfigManager().getBastionBlocksToErode() != 0) {
			impact = new TreeSet<BastionBlock>();
		}
		
		// look through bastions
		for(BastionBlock bastion : definiteCollide) {
			Location bLoc = bastion.getLocation();
			double testY = bLoc.getY() + ( Bastion.getConfigManager().includeSameYLevel() ? 0 : 1);
			
			// Are we fully below the bastion?
			if (testY > Math.max(from.getY(), newTo.getY())) continue;
			
			boolean hit = false;
			// Are we fully above the bastion?
			if (testY <= Math.min(from.getY(), newTo.getY())) {
				hit = true; // definite hit. 
			} else { // Are we inbetween?
				// We'll use simple interpolation. We know they are not equal so no div by zero possible.
				double s = (newTo.getY() - testY) / (newTo.getY() - from.getY());
				Location midpoint = from.clone();
				midpoint.add((newTo.getX() - from.getX()) * s,
						(newTo.getY() - from.getY()) * s,
						(newTo.getZ() - from.getZ()) * s);
				hit = bastion.inField(midpoint);
			}
			
			// We're going to clip this bastion field.
			if (hit) {
				if (impact == null) { // we do no damage so just impact.
					doImpact(p);
					return true;
				}
				impact.add(bastion); // keep track of all intersections for overlapping fields
			}
		}
		
		// Found a few hits, figure out who gets damaged.
		if (impact != null && impact.size() > 0) {
			// handle breaking/damage to elytra
			doImpact(p);
			
			// now handle damage / breaking of bastions.  
			if (!Bastion.getBastionManager().onCooldown(p.getName())) {
				int breakCount = Bastion.getConfigManager().getBastionBlocksToErode();
				if (breakCount < 0 || impact.size() >= breakCount) { // break all
					for (BastionBlock bastion : impact) {
						bastion.erode(bastion.erosionFromElytra());
					}					
				} else if (breakCount > 0) { // break some randomly
					Random rng = new Random();
					List<BastionBlock> ordered = new LinkedList<BastionBlock>(impact);
					for (int i = 0;i < ordered.size() && (i < breakCount); ++i){
						int erode = rng.nextInt(ordered.size());
						BastionBlock toErode = ordered.get(erode);
						toErode.erode(toErode.erosionFromElytra());
						ordered.remove(erode);
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	private void doImpact(Player p) {
		p.sendMessage(ChatColor.RED+"Elytra flight blocked by Bastion Block");
		p.setVelocity(new Vector(0, 0, 0));
		PlayerInventory inv = p.getInventory();
		if (Bastion.getConfigManager().getElytraIsDestroyOnBlock()) {
			inv.setChestplate(new ItemStack(Material.AIR));
		} else if (Bastion.getConfigManager().getElytraIsDamagedOnBlock()){
			ItemStack elytra = inv.getChestplate();
			elytra.setDurability((short)432);
			inv.setChestplate(elytra);
		}
	}
}
