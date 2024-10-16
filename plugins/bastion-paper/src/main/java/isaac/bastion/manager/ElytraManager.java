package isaac.bastion.manager;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.Permissions;
import isaac.bastion.event.BastionDamageEvent;
import isaac.bastion.event.BastionDamageEvent.Cause;
import isaac.bastion.storage.BastionBlockStorage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ElytraManager {

	private BastionBlockStorage storage;
	private Map<UUID, Long> throttle;
	private Map<UUID, Long> lastMoves;
	
	public ElytraManager() {
		storage = Bastion.getBastionStorage();
		throttle = new ConcurrentHashMap<>();
		lastMoves = new ConcurrentHashMap<>();
	}
	
	public void clearThrottle(UUID player) {
		throttle.remove(player);
	}
	
	public void cleanLastMove(UUID player) {
		lastMoves.remove(player);
	}
	
	public boolean handleElytraMovement(Player player, Location to) {
		// We use movement data plus vector data to form correct movement projection vectors.
		Long lastMove = lastMoves.get(player.getUniqueId());
		Long nowMove = System.currentTimeMillis();
		lastMoves.put(player.getUniqueId(), nowMove);
		if (lastMove == null) lastMove = nowMove - 50l; // assume 20/sec

		// Throttle checks to no more then 10 a second.
		Long lastCheck = throttle.get(player.getUniqueId());
		if (lastCheck != null && nowMove - lastCheck.longValue() < 100l){
			/*Bastion.getPlugin().getLogger().log(Level.INFO, "Throttled: {0} vs. {1}",
					new Object[] {lastCheck, System.currentTimeMillis()});*/
			return false;
		}
		throttle.put(player.getUniqueId(), System.currentTimeMillis());
		//Bastion.getPlugin().getLogger().info("Not throttled");
		
		// collect to/from
		Location from = player.getLocation();
		if (to == null) {
			to = player.getLocation();
		}
		
		// fix vector
		Vector pVec = player.getVelocity();
		if (pVec == null || pVec.lengthSquared() == 0.0d) {
			pVec = new Vector(to.getX() - from.getX(),
					to.getY() - from.getY(),
					to.getZ() - from.getZ());
		}
		
		// We target 100ms between updates. We want to "project out" past the next movement.
		// So for whatever time _actually_ passed between move packets, map to 200ms
		pVec.multiply( (double) (200l / (nowMove-lastMove) ) );

		// We doubledown; project the vector out another movement cycle.
		Location newTo = from.clone().add(pVec);
		
		/*Bastion.getPlugin().getLogger().log(Level.INFO, "  from: {0}, to: {1}, speed {2}",
				new Object[] {from, newTo, pVec});*/
		
		// find maxset of bastions
		Set<BastionBlock> possible = storage.getPossibleFlightBlocking(
				BastionType.getMaxRadius() * 2,
				from, newTo );
		if (possible == null || possible.isEmpty()) {
			//Bastion.getPlugin().getLogger().info("No interations");
			return false;
		}
		
		// find actual collisions
		Set<BastionBlock> definiteCollide = simpleCollide(possible, pVec, from, newTo, player); //EnderPearlManager.staticSimpleCollide(possible, from, newTo, p);
		if (definiteCollide == null || definiteCollide.isEmpty()) {
			//Bastion.getPlugin().getLogger().info("No definite collisions");
			return false;
		}
		
		// only allocate if we're going to use it.
		Set<BastionBlock> impact = new TreeSet<>();
		BastionType noImpact = null;
		boolean breakElytra = false;
		boolean damageElytra = false;
		double explosionStrength = -1;
		
		// look through bastions
		for(BastionBlock bastion : definiteCollide) {
			BastionType type = bastion.getType();
			breakElytra = breakElytra || type.isDestroyElytra();
			damageElytra = damageElytra || type.isDamageElytra();
			if(type.isExplodeOnBlock()) {
				explosionStrength = Math.max(explosionStrength, type.getExplosionStrength());
			}
			Location bLoc = bastion.getLocation();
			double testY = bLoc.getY() + ( type.isIncludeY() ? 0 : 1);
			
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
				//Bastion.getPlugin().getLogger().log(Level.INFO, "  hit bastion at {0}", bLoc);
				if (type.getElytraScale() > 0 && type.getBlocksToErode() != 0) { // we do no damage so just impact.
					impact.add(bastion);
				} else if(noImpact == null) {
					noImpact = bastion.getType();
				}
			}
		}
		
		// Found a few hits, figure out who gets damaged.
		if (impact != null && impact.size() > 0) {
			doImpact(player, breakElytra, damageElytra, explosionStrength);
			// handle breaking/damage to elytra
			
			HashMap<BastionType, Set<BastionBlock>> typeMap = new HashMap<BastionType, Set<BastionBlock>>();
			for(BastionBlock block : impact) {
				if(!block.getType().isDamageElytra()) continue;
				if(!typeMap.containsKey(block.getType())) {
					typeMap.put(block.getType(), new HashSet<BastionBlock>());
				}
				typeMap.get(block.getType()).add(block);
			}
			
			for(BastionType type : typeMap.keySet()) {
				if(Bastion.getBastionManager().onCooldown(player.getUniqueId(), type)) continue;
				int breakCount = type.getBlocksToErode();
				if(breakCount < 0 || impact.size() >= breakCount) {
					for(BastionBlock bastion : typeMap.get(type)) {
						double damage = bastion.getErosionFromElytra();
						BastionDamageEvent event = new BastionDamageEvent(bastion, player, Cause.ELYTRA, damage);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) continue;
						bastion.erode(damage);
					}
				} else if (breakCount > 0) {
					Random rng = new Random();
					List<BastionBlock> ordered = new LinkedList<BastionBlock>(typeMap.get(type));
					for(int i = 0; i < ordered.size() && (i < breakCount); i++) {
						int erode = rng.nextInt(ordered.size());
						BastionBlock toErode = ordered.get(erode);
						double damage = toErode.getErosionFromElytra();
						BastionDamageEvent event = new BastionDamageEvent(toErode, player, Cause.ELYTRA, damage);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) continue;
						toErode.erode(damage);
						ordered.remove(erode);
					}
				}
			}
			return true;
		} else if (noImpact != null) {
			return true;
		}
		
		return false;
	}
	
	private Set<BastionBlock> simpleCollide(Set<BastionBlock> possible, Vector velocity, Location start, Location end, Player player) {
		Set<BastionBlock> couldCollide = new TreeSet<>();
		for (BastionBlock bastion : possible) {
			Location loc = bastion.getLocation().clone();
			loc.setY(0);

			//check player flight permission.
			if (NameAPI.getGroupManager().hasAccess(bastion.getGroup(), player.getUniqueId(), PermissionType.getPermission(Permissions.BASTION_ELYTRA))) {
				continue;
			}
			if (bastion.getType().isSquare()) {
				if (hasCollisionPointsSquare(start, end, bastion.getLocation(), bastion.getType().getEffectRadius())) {
					couldCollide.add(bastion);
				}
			} else {
				if (hasCollisionPointsCircle(velocity, start, bastion.getLocation(), bastion.getType().getEffectRadius())) {
					couldCollide.add(bastion);
				}
			}
		}
		return couldCollide;
	}
	
	private boolean hasCollisionPointsCircle(Vector d, Location start, Location circleLoc, double radius) {
		Vector f = start.toVector().subtract(circleLoc.toVector());
		double a = d.dot(d);
		double b = 2.0d * f.dot(d);
		double c = f.dot(f) - radius * radius;
		
		double descrim = b*b - 4*a*c;
		if (descrim >= 0.0d || a == 0) {
			descrim = Math.sqrt(descrim);
			
			double t1 = (-b - descrim) / (2*a);
			double t2 = (-b + descrim) / (2*a);
			
			if ((t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1) || (t1 < 0 && t2 > 1 )) {
				return true;
			}
		}
		return false;
	}
	
    private boolean hasCollisionPointsSquare(Location startLoc, Location endLoc, Location squareLoc, double radius) {
        double zl = (endLoc.getZ() - startLoc.getZ());
        double xl = (endLoc.getX() - startLoc.getX());
        double r2 = radius * 2.0;
        double ba = squareLoc.getX() - startLoc.getX();
        double bb = squareLoc.getZ() - startLoc.getZ();
        
        double s = 0.0d;
        double t = 0.0d;
        if (xl != 0){
            s = zl * (ba + radius) / ( xl * r2 ) - bb / r2 + .5d; //bottom
            t = (ba + radius) / xl;
            if (s >= 0.0 && s <= 1.0 && t >= 0.0 && t <= 1.0) {
                return true;
            }
            
            s = zl * (ba - radius) / ( xl * r2 ) - bb / r2 + .5d; //top
            t = (ba - radius) / xl;
            if (s >= 0.0 && s <= 1.0 && t >= 0.0 && t <= 1.0) {
                return true;
            }
        }
        
        if (zl != 0){
            s = xl * (bb + radius) / ( zl * r2 ) - ba / r2 + .5d; //right
            t = (bb + radius) / zl;
            if (s >= 0.0 && s <= 1.0 && t >= 0.0 && t <= 1.0) {
                return true;
            }
            
            s = xl * (bb - radius) / ( zl * r2 ) - ba / r2 + .5d; //leftt
            t = (bb - radius) / zl;
            if (s >= 0.0 && s <= 1.0 && t >= 0.0 && t <= 1.0) {
                return true;
            }
        }
        return false;
    }
	
	private void doImpact(Player p, boolean breakElytra, boolean damageElytra, double explosionStrength) {
		p.sendMessage(ChatColor.RED+"Elytra flight blocked by Bastion Block");
		p.setVelocity(new Vector(0, 0, 0));
		PlayerInventory inv = p.getInventory();
		if (breakElytra) {
			inv.setChestplate(new ItemStack(Material.AIR));
		} else if (damageElytra){
			ItemStack elytra = inv.getChestplate();
			Damageable dmgable = ItemUtils.getDamageable(elytra);
			dmgable.setDamage(432);
			elytra.setItemMeta((ItemMeta) dmgable);
			inv.setChestplate(elytra);
		}
		if(explosionStrength >= 0) {
			p.getWorld().createExplosion(p.getLocation(), (float) explosionStrength);
		}
	}
}
