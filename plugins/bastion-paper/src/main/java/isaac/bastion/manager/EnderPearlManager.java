package isaac.bastion.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.untamedears.humbug.CustomNMSEntityEnderPearl;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.storage.BastionBlockStorage;

@SuppressWarnings("deprecation")
public class EnderPearlManager {
	public static final int MAX_TELEPORT = 800;
	private BastionBlockStorage storage;
	private boolean humbugLoaded = true;
	
	private FlightTask task;
	
	public EnderPearlManager() {
		storage = Bastion.getBastionStorage();
		task = new FlightTask();
		try {
			Class.forName("com.untamedears.humbug.CustomNMSEntityEnderPearl");
		} catch (ClassNotFoundException e) {
			humbugLoaded = false;
		}
	}

	public void handlePearlLaunched(EnderPearl pearl) {
		getBlocking(pearl);
	}

	private void getBlocking(EnderPearl pearl) {
		double gravity = 0.03F;
		if(humbugLoaded && pearl instanceof CustomNMSEntityEnderPearl) {
			gravity = ((CustomNMSEntityEnderPearl)pearl).y_adjust_;
		}

		Vector speed = pearl.getVelocity();
		Vector twoDSpeed = speed.clone();
		twoDSpeed.setY(0);

		double horizontalSpeed = getLengthSigned(twoDSpeed);
		double verticalSpeed = speed.getY();

		Location loc = pearl.getLocation();

		double maxTicks = getMaxTicks(verticalSpeed, loc.getY(), -gravity);
		double maxDistance = getMaxDistance(horizontalSpeed, maxTicks);

		//check if it has any possibility of going through a bastion 
		/*if (!(maxDistance > BastionType.getMaxRadius() / 2 
				|| maxDistance < -1)) {
			return;
		}*/
		
		Player threw = null;
		if (pearl.getShooter() instanceof Player) {
			threw = (Player) pearl.getShooter();
		}

		Set<BastionBlock> possible = storage.getPossibleTeleportBlocking(pearl.getLocation(), maxDistance); //all the bastion blocks within range of the pearl

		// no need to do anything if there aren't any bastions to run into.
		if (possible.isEmpty()) {
			return;
		}

		Location start = pearl.getLocation();
		Location end = start.clone();
		end.add(twoDSpeed.multiply(maxTicks)); 

		Set<BastionBlock> couldCollide = simpleCollide(possible, start.clone(), end.clone(), threw); //all the bastions where the pearl passes over or under their shadow

		if (couldCollide.isEmpty()) {
			return;
		}


		BastionBlock firstCollision = null;
		long firstCollisionTime = -1;
		for (BastionBlock bastion : couldCollide) {
			long currentCollidesBy = (long) collidesBy(bastion, start.clone(), end.clone(), speed, gravity, horizontalSpeed);
			if (currentCollidesBy != -1 && (firstCollision == null || currentCollidesBy < firstCollisionTime)) {
				firstCollisionTime = currentCollidesBy;
				firstCollision = bastion;
			}
		}

		if (firstCollisionTime != -1) { //if we found something add it
			task.manage(new Flight(pearl, firstCollisionTime, firstCollision));
			return;
		}
	}
	
	private Set<BastionBlock> simpleCollide(Set<BastionBlock> possible, Location start, Location end, Player player) {
		Set<BastionBlock> couldCollide = new TreeSet<BastionBlock>();
		for (BastionBlock bastion : possible) {
			Location loc = bastion.getLocation().clone();
			loc.setY(0);
			
			if (bastion.canPearl(player)) { // not blocked, continue
				continue;
			}
			
			if (bastion.getType().isSquare()) {
				if (!getCollisionPointsSquare(start, end, loc, bastion.getType().getEffectRadius()).isEmpty() ) { // TODO: reuse is good, doing the same thing twice is bad
					couldCollide.add(bastion);
				}
			} else {
				if (circleLineCollide(start, end, loc, bastion.getType().getRadiusSquared())) {
					couldCollide.add(bastion);
				}
			}
		}

		return couldCollide;
	}

	double collidesBy(BastionBlock bastion, Location startLoc, Location endLoc, Vector speed, double gravity, double horizontalSpeed) {
		//Get the points were our line crosses the circle
		List<Location> collision_points = null;
		if (bastion.getType().isSquare()) {
			collision_points = getCollisionPointsSquare(startLoc, endLoc, bastion.getLocation(), bastion.getType().getEffectRadius());
		} else {
			collision_points = getCollisionPoints(startLoc, endLoc, bastion.getLocation(), bastion.getType().getRadiusSquared());
		}
		
		if (collision_points.isEmpty()) {
			return -1;
		}
		
		//solve the quadratic equation for the equation governing the pearls y height. See if it ever reaches (bastion.getLocation().getY()+1
		List<Double> solutions = getSolutions(-gravity/2, speed.getY(), startLoc.getY() - (bastion.getLocation().getY() + (bastion.getType().isIncludeY() ? 0 : 1) ));
		
		//If there aren't any results we no there are no intersections
		if (solutions.isEmpty()) {
			return -1;
		}
		
		Location temp = startLoc.clone();
		temp.setY(0);

		//Solutions held the time at which the collision would happen lets change it to a position
		for(int i = 0; i < solutions.size(); ++i) {
			solutions.set(i, solutions.get(i) * horizontalSpeed);
		}

		List<Double> oneDCollisions = new ArrayList<Double>();

		//turn those points into scalers along the line of the pearl
		for(Location collision_point : collision_points){
			Location twoDStart = startLoc.clone();
			twoDStart.setY(0);
			collision_point.setY(0);
			oneDCollisions.add(collision_point.subtract(twoDStart).length());
		}

		for(Double collisionPoint : oneDCollisions){
			//if this is the solution lets convert it to a tick
			//check if the collision point is inside between the solutions if so we know there will be a collision
			if ((solutions.get(0) > collisionPoint && solutions.get(1) < collisionPoint) || (solutions.get(1) > collisionPoint && solutions.get(0) < collisionPoint)) {
				//solution 1 is between the two collision points
				if (!(oneDCollisions.get(0) > solutions.get(1) && oneDCollisions.get(1) < solutions.get(0) ||
						oneDCollisions.get(1) > solutions.get(1) && oneDCollisions.get(0) < solutions.get(0))) {
					return (solutions.get(1)) / horizontalSpeed + startLoc.getWorld().getFullTime();
				} else{
					if (oneDCollisions.get(0) < oneDCollisions.get(1)) {
						return (oneDCollisions.get(0)) / horizontalSpeed + startLoc.getWorld().getFullTime();
					}else{
						return (oneDCollisions.get(1)) / horizontalSpeed + startLoc.getWorld().getFullTime();
					}
				}
			}
		}

		return -1;
	}
	
	//returns the solutions to the quadratic equation
	private List<Double> getSolutions(double a, double b, double c){
		double toTakeSquareRoot=b*b-4*a*c;
		if(toTakeSquareRoot<0||a==0)
			return Collections.emptyList();

		double squareRooted=Math.sqrt(toTakeSquareRoot);

		double s1=(-b-squareRooted)/(2*a);

		double s2=(-b+squareRooted)/(2*a);

		return  Arrays.asList(s1, s2);
	}
	
	/**
	 * Finds points on a line extending infinitely through the points startLoc and endLoc interesting a square centered at squareLoc.
	 * @param startLoc
	 * @param endLoc
	 * @param squareLoc
	 * @param radius
	 * @return
	 */
	private List<Location> getCollisionPointsSquare (Location startLoc, Location endLoc, Location squareLoc, double radius) {
		double zl = (endLoc.getZ() - startLoc.getZ());
		double xl = (endLoc.getX() - startLoc.getX());
		double r2 = radius * 2.0;
		double ba = squareLoc.getX() - startLoc.getX();
		double bb = squareLoc.getZ() - startLoc.getZ();
		
		List<Location> results=new ArrayList<Location>();
		Location so = null;
		double s = 0.0d;
		if (xl != 0){
			s = zl * (ba + radius) / ( xl * r2 ) - bb / r2 + .5d; //bottom
			if (s >= 0.0 && s <= 1.0) {
				so = squareLoc.clone().add(radius, 0, -radius + s*r2);
				results.add(so);
			}
			
			s = zl * (ba - radius) / ( xl * r2 ) - bb / r2 + .5d; //top
			if (s >= 0.0 && s <= 1.0) {
				so = squareLoc.clone().add(-radius, 0, -radius + s*r2);
				results.add(so);
			}
		}
		
		if (zl != 0){
			s = xl * (bb + radius) / ( zl * r2 ) - ba / r2 + .5d; //right
			if (s >= 0.0 && s <= 1.0) {
				so = squareLoc.clone().add(-radius + s*r2, 0, radius);
				results.add(so);
			}
			
			s = xl * (bb - radius) / ( zl * r2 ) - ba / r2 + .5d; //leftt
			if (s >= 0.0 && s <= 1.0) {
				so = squareLoc.clone().add(-radius + s*r2, 0, -radius);
				results.add(so);
			}
		}
		return results;
	}
	
	/**
	 * Finds points on a line extending infinitely through the points startLoc and endLoc intersecting a circle centered at circleLoc
	 * @param startLoc
	 * @param endLoc
	 * @param circleLoc
	 * @param radiusSquared
	 * @return
	 */
	private List<Location> getCollisionPoints (Location startLoc, Location endLoc, Location circleLoc, double radiusSquared) {
		Vector delta = vectorFromLocations(startLoc, endLoc);
		Vector circleLocInTermsOfStart = vectorFromLocations(startLoc, circleLoc);
		
		boolean flipped = false;
		if(delta.getX() == 0){
			flipped = true;
			flipXZ(delta);
			flipXZ(circleLocInTermsOfStart);
		}
		circleLocInTermsOfStart.add(new Vector(0.5,0,0.5)); 
		
		double slope = delta.getZ()/delta.getX();
		
		double circleX = circleLocInTermsOfStart.getX();
		double circleZ = circleLocInTermsOfStart.getZ();
		
		double circleXSquared = circleX*circleX;
		double circleZSquared = circleZ*circleZ;

		
		List<Double> xs = getSolutions(slope*slope+1,-(2*circleZ*slope+2*circleX),circleXSquared+circleZSquared-radiusSquared);
		List<Location> results = new ArrayList<Location>();
		for (double x : xs) {
			Vector offset = new Vector(x, 0, x*slope);
			if(flipped) {
				flipXZ(offset);
			}
			results.add(startLoc.clone().add(offset));
		}
		
		
		return results;
	}

	private double getMaxDistance(double horizontalSpeed, double maxTicks) {
		return horizontalSpeed*maxTicks;
	}

	private double getMaxTicks(double verticalSpeed, double y, double deltaY) {
		return ((-verticalSpeed) - Math.sqrt(verticalSpeed*verticalSpeed - 2*deltaY*y)) / deltaY;
	}

	// TODO evaluate this vs. getCollisonPoints. Which is more efficient? Prefer it, or find a way to re-use the result once captured.
	private boolean circleLineCollide(Location startLoc, Location endLoc, Location circleLoc, double radiusSquared) {
		Location lineStart = startLoc.clone();
		Location lineEnd = endLoc.clone();
		Location circleCenter = circleLoc.clone();

		Vector direction = vectorFromLocations(lineStart,lineEnd);

		if (direction.getZ() == 0) {
			if (direction.getBlockX() == 0) {
				return false;
			}
			flipXZ(lineStart);
			flipXZ(lineEnd);
			flipXZ(circleCenter);
			flipXZ(direction);
		}

		Vector start = lineStart.toVector();
		Vector end = lineEnd.toVector();

		Vector circle = circleCenter.toVector();

		double slope = direction.getZ() / direction.getX();
		double perpSlope = -1/slope;

		//This is the closest x if this line segment was extended for ever
		double closestX = (slope*start.getX() - perpSlope*circle.getX() + circle.getBlockZ() - start.getZ()) /
				(slope - perpSlope);

		//Getting the Z from the x is easy
		double closestZ = slope*(closestX - start.getX()) + start.getZ();

		Vector closest = new Vector(closestX, 0, closestZ);

		double distanceSquared = closest.clone().subtract(circle).lengthSquared();

		if (distanceSquared > radiusSquared) {
			return false;
		}

		if (    ((closest.getX() > lineStart.getX() && closest.getX() > lineEnd.getX()) ||
				 (closest.getZ() > lineStart.getZ() && closest.getZ() > lineEnd.getZ()))||
				((closest.getX() < lineStart.getX() && closest.getX() < lineEnd.getX()) ||
				 (closest.getZ() < lineStart.getZ() && closest.getZ() < lineEnd.getZ()))) {
			if (closest.clone().subtract(end).lengthSquared() < closest.clone().subtract(start).lengthSquared()) {
				closest=end;
			} else {
				closest=start;
			}
		}

		distanceSquared = closest.subtract(circle).lengthSquared();

		if (distanceSquared > radiusSquared) {
			return false;
		}

		return true;
	}
	
	private double getLengthSigned(Vector vec) {
		double length = vec.length();
		
		return length;
	}

	private void flipXZ(Location a) {
		double tempX = a.getX();
		a.setX(a.getZ());
		a.setZ(tempX);
	}

	private void flipXZ(Vector a) {
		double tempX = a.getX();
		a.setX(a.getZ());
		a.setZ(tempX);
	}

	private Vector vectorFromLocations(Location start, Location end) {
		return new Vector(end.getX()-start.getX(),end.getY()-start.getY(),end.getZ()-start.getZ());
	}
	
	private class FlightTask{
		PriorityQueue<Flight> inFlight = new PriorityQueue<Flight>();
		Flight onTask = null;
		int currentTask = -1;
		
		void manage(Flight flight) {
			inFlight.add(flight);
			if (onTask == null){
				next();
				return;
			}
			if (onTask.compareTo(flight) == 1 || onTask == null) {
				this.next();
			}
		}

		private void next() {
			if(currentTask != -1) {
				Bukkit.getScheduler().cancelTask(currentTask);
			}
			
			if (onTask != null) {
				inFlight.add(onTask);
			}
			
			onTask = inFlight.poll();
			if (onTask == null) {
				return;
			}
			
			if (onTask.timeToEnd() <= 0) {
				onTask.cancel();
				currentTask = -1;
				onTask = null;
				next();
				return;
			}
			
			currentTask = new BukkitRunnable(){
				@Override
				public void run() {
					onTask.cancel();
					onTask = null;
					currentTask = -1;
					next();
				}
			}.runTaskLater(Bastion.getPlugin(), onTask.timeToEnd()).getTaskId();
		}
	}
	
	
	private class Flight implements Comparable<Flight> {
		private EnderPearl   pearl;
		private Long         endTime;
		private BastionBlock blocking;
		
		public Flight(EnderPearl pearl, long endTime, BastionBlock blocking) {
			this.pearl    = pearl;
			this.endTime  = endTime;
			this.blocking = blocking;
		}
		
		
		public void cancel() {
			if (pearl.getShooter() instanceof Player) {
				Player player = (Player) pearl.getShooter();
				
				if (blocking.getType().damageFirstBastion() && !Bastion.getBastionManager().onCooldown(player.getUniqueId(), blocking.getType())) {
					blocking.erode(blocking.getErosionFromPearl());
					pearl.getWorld().spigot().playEffect(pearl.getLocation(), Effect.EXPLOSION, 0, 0, 1, 1, 1, 1, 50, 32);
				}
				
				if (blocking.getType().isBlockMidair()) {
					player.sendMessage(ChatColor.RED+"Ender pearl blocked by Bastion Block");
					if (!blocking.getType().isConsumeOnBlock()) {
						player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
						player.updateInventory();
					}
				}
			}
			if (blocking.getType().isBlockMidair()) {
				pearl.remove();
			}
			
		}
		
		public long timeToEnd() {
			return endTime - pearl.getWorld().getFullTime();
		}

		@Override
		public int compareTo(Flight o) {
			return (int) Math.signum(o.endTime - endTime);
		}
	}
}
