package isaac.bastion.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;

import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.untamedears.humbug.CustomNMSEntityEnderPearl;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;

public class EnderPearlManager {
	public static final int MAX_TELEPORT=800;
	private BastionBlockSet bastions;
	private Map<EnderPearl,Integer> endTimes;
	private Map<EnderPearl,BastionBlock> blocks;
	public EnderPearlManager(){
		bastions=Bastion.getBastionManager().bastions;

		endTimes=new HashMap<EnderPearl,Integer>();
		blocks=new HashMap<EnderPearl,BastionBlock>();
	}
	public void handlePearlLaunched(EnderPearl pearl){
		getBlocking(pearl);
	}
	private void getBlocking(EnderPearl pearl){
		double gravity=0.03F;

		if(pearl instanceof CustomNMSEntityEnderPearl)
			gravity=((CustomNMSEntityEnderPearl)pearl).y_adjust_;

		Vector speed=pearl.getVelocity();
		Vector twoDSpeed=speed.clone();
		twoDSpeed.setY(0);

		double horizontalSpeed=getLengthSigned(twoDSpeed);
		double verticalSpeed=speed.getY();

		Location loc=pearl.getLocation();

		double maxTicks=getMaxTicks(verticalSpeed,loc.getY(),-gravity);
		double maxDistance=getMaxDistance(horizontalSpeed,maxTicks);


		//check if it has any possibility of going through a bastion 
		if(!(maxDistance>2||maxDistance<-1)){
			return;
		}
		
		//Bastion.getPlugin().getLogger().info("Not moving much "+horizontalSpeed);

		LivingEntity threwE=pearl.getShooter();
		Player threw=null;
		String playerName=null;
		if(threwE instanceof Player){
			threw=(Player) threwE;
			playerName=threw.getName();
		}

		Set<BastionBlock> possible=bastions.getPossibleTeleportBlocking(pearl.getLocation(),playerName);

		//no need to do anything if there aren't any bastions to run into.
		if(possible.isEmpty()){
			//Bastion.getPlugin().getLogger().info("There are no that we even have a chance of blocking");
			return;
		}

		Location start=pearl.getLocation();
		Location end=start.clone();
		end.add(twoDSpeed.multiply(maxTicks));

		Set<BastionBlock> couldCollide=simpleCollide(possible,start.clone(),end.clone());

		if(couldCollide.isEmpty()){
			//Bastion.getPlugin().getLogger().info("Simple collide didn't find any");
			return;
		}


		BastionBlock firstCollision=null;
		double collidesBy=-1;
		for(BastionBlock bastion : couldCollide){
			double currentCollidesBy=collidesBy(bastion, start.clone(), end.clone(), speed, gravity, horizontalSpeed);
			if(currentCollidesBy!=-1&&currentCollidesBy<collidesBy){
				collidesBy=currentCollidesBy;
				firstCollision=bastion;
			}
			if(collidesBy==-1&&currentCollidesBy!=-1){
				collidesBy=currentCollidesBy;
				firstCollision=bastion;
			}
		}
		if(collidesBy!=-1){
			//Bastion.getPlugin().getLogger().info("adding collision");
			endTimes.put(pearl, (int) collidesBy);
			blocks.put(pearl, firstCollision);
			return;
		}
		//Bastion.getPlugin().getLogger().info("complicated test failed");


	}
	private Set<BastionBlock> simpleCollide(Set<BastionBlock> possible,Location start,Location end){
		Set<BastionBlock> couldCollide=new TreeSet<BastionBlock>();
		for(BastionBlock bastion : possible){
			Location loc=bastion.getLocation().clone();
			loc.setY(0);
			if(circleLineCollide(start,end,loc,BastionBlock.getRadiusSquared()))
				couldCollide.add(bastion);
		}

		return couldCollide;
	}

	double collidesBy(BastionBlock bastion, Location startLoc,Location endLoc,Vector speed,double gravity,double horizontalSpeed){


		//Get the points were our line crosses the circle
		List<Location>  collision_points=getCollisionPoints(startLoc,endLoc,bastion.getLocation(),BastionBlock.getRadiusSquared());
		
		//solve the quadratic equation for the equation governing the pearls y height. See if it ever reaches (bastion.getLocation().getY()+1
		List<Double> solutions=getSolutions(-gravity/2,speed.getY(),startLoc.getY()-(bastion.getLocation().getY()+1));
		//If there aren't any results we no there are no intersections
		if(solutions.isEmpty()){
			return -1;
		}
		/*for(Location loc: collision_points)
			loc.getBlock().setType(Material.DIAMOND_BLOCK);*/
		
		Location temp=startLoc.clone();
		temp.setY(0);
		//Solutions held the time at which the collision would happen lets change it to a position
		for(int i=0;i<solutions.size();++i){
			solutions.set(i, solutions.get(i)*horizontalSpeed);
			
			/*Vector direction=vectorFromLocations(startLoc,endLoc);
			direction.normalize();
			Location loc=startLoc.clone();
			loc.add(direction.multiply(solutions.get(i)));
			loc.setY(bastion.getLocation().getY());
			loc.getBlock().setType(Material.WATER);*/
			
		}

		List<Double> oneDCollisions=new ArrayList<Double>();

		//turn those points into scalers along the line of the pearl
		for(Location collision_point : collision_points){
			Location twoDStart=startLoc.clone();
			twoDStart.setY(0);
			oneDCollisions.add(collision_point.subtract(twoDStart).length());
		}
		//Bastion.getPlugin().getLogger().info("solutions="+solutions+"collision_points="+oneDCollisions);


		double result=-1;
		for(Double collisionPoint : oneDCollisions){
			//if this is the solution lets convert it to a tick
			//check if the collision point is inside between the solutions if so we no there will be a collision
			if((solutions.get(0) > collisionPoint && solutions.get(1) < collisionPoint)||(solutions.get(1) > collisionPoint && solutions.get(0) < collisionPoint)){
				//solution 1 is between the two collision points
				if(!(oneDCollisions.get(0)>solutions.get(1)&&oneDCollisions.get(1)<solutions.get(0)||
						oneDCollisions.get(1)>solutions.get(1)&&oneDCollisions.get(0)<solutions.get(0))){
					return (solutions.get(1))/horizontalSpeed+startLoc.getWorld().getFullTime();
				} else{
					if(oneDCollisions.get(0)<oneDCollisions.get(1)){
						return (oneDCollisions.get(0))/horizontalSpeed+startLoc.getWorld().getFullTime();
					}else{
						return (oneDCollisions.get(1))/horizontalSpeed+startLoc.getWorld().getFullTime();
					}
				}
			}

		}

		return result;
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
	//Finds points on a line extending infinitely through the points startLoc and endLoc intersecting a circle centered at circleLoc
	private List<Location> getCollisionPoints(Location startLoc,Location endLoc,Location circleLoc,double radiusSquared){
		Vector delta=vectorFromLocations(startLoc,endLoc);
		Vector circleLocInTermsOfStart=vectorFromLocations(startLoc,circleLoc);
		
		
		boolean flipped=false;
		if(delta.getX()==0){
			flipped=true;
			flipXZ(delta);
			flipXZ(circleLocInTermsOfStart);
		}
		circleLocInTermsOfStart.add(new Vector(0.5,0,0.5));
		
		double slope=delta.getZ()/delta.getX();
		
		double circleX=circleLocInTermsOfStart.getX();
		double circleZ=circleLocInTermsOfStart.getZ();
		
		double circleXSquared=circleX*circleX;
		double circleZSquared=circleZ*circleZ;

		
		List<Double> xs=getSolutions(slope*slope+1,-(2*circleZ*slope+2*circleX),circleXSquared+circleZSquared-radiusSquared);
		List<Location> results=new ArrayList<Location>();
		for(double x : xs){
			Vector offset=new Vector(x,0,x*slope);
			if(flipped)
				flipXZ(offset);
			results.add(startLoc.clone().add(offset));
		}
		
		
		return results;
	}
	private double getMaxDistance(double horizontalSpeed,double maxTicks){
		return horizontalSpeed*maxTicks;
	}

	private double getMaxTicks(double verticalSpeed,double y,double deltaY){
		return ((-verticalSpeed)-Math.sqrt(verticalSpeed*verticalSpeed-2*deltaY*y))/deltaY;
	}

	private boolean circleLineCollide(Location startLoc, Location endLoc, Location circleLoc, double radiusSquared){
		Location lineStart=startLoc.clone();
		Location lineEnd=endLoc.clone();
		Location circleCenter=circleLoc.clone();

		Vector direction=vectorFromLocations(lineStart,lineEnd);

		if(direction.getZ()==0){
			if(direction.getBlockX()==0){
				return false;
			}
			flipXZ(lineStart);
			flipXZ(lineEnd);
			flipXZ(circleCenter);
			flipXZ(direction);
		}

		Vector start=lineStart.toVector();
		Vector end=lineEnd.toVector();

		Vector circle=circleCenter.toVector();

		double slope=direction.getZ()/direction.getX();
		double perpSlope=-1/slope;

		//This is the closest x if this line segment was extended for ever
		double closestX=(slope*start.getX()-perpSlope*circle.getX()+circle.getBlockZ()-start.getZ())/
				(slope-perpSlope);

		//Getting the Z from the x is easy
		double closestZ=slope*(closestX-start.getX())+start.getZ();

		Vector closest=new Vector(closestX,0,closestZ);

		double distanceSquared=closest.clone().subtract(circle).lengthSquared();

		if(distanceSquared>radiusSquared){
			return false;
		}

		if(((closest.getX()>lineStart.getX()&&closest.getX()>lineEnd.getX())||
				(closest.getZ()>lineStart.getZ()&&closest.getZ()>lineEnd.getZ()))||

				((closest.getX()<lineStart.getX()&&closest.getX()<lineEnd.getX())||
						(closest.getZ()<lineStart.getZ()&&closest.getZ()<lineEnd.getZ()))
				){
			if(closest.clone().subtract(end).lengthSquared()<closest.clone().subtract(start).lengthSquared()){
				closest=end;
			} else{
				closest=start;
			}
		}

		distanceSquared=closest.subtract(circle).lengthSquared();

		if(distanceSquared>radiusSquared){
			return false;
		}

		return true;
	}
	
	private double getLengthSigned(Vector vec){
		double length=vec.length();
		
		//if(Math.signum(vec.getZ())==-1||Math.signum(vec.getX())==-1||Math.signum(vec.getY())==-1)
			//length*=-1;
		
		return length;
	}
	private void flipXZ(Location a){
		double tempX=a.getX();
		a.setX(a.getZ());
		a.setZ(tempX);
	}

	private void flipXZ(Vector a){
		double tempX=a.getX();
		a.setX(a.getZ());
		a.setZ(tempX);
	}

	private Vector vectorFromLocations(Location start, Location end){
		return new Vector(end.getX()-start.getX(),end.getY()-start.getY(),end.getZ()-start.getZ());
	}


	public void tick(){
		for(World world : Bastion.getPlugin().getServer().getWorlds()){
			worldTick(world);
		}
	}
	private void worldTick(World world){
		Collection<EnderPearl> flying=world.getEntitiesByClass(EnderPearl.class);
		for(EnderPearl pearl : flying){
			pearlTick(pearl);
		}
	}
	private void pearlTick(EnderPearl pearl){
		Integer endTime=endTimes.get(pearl);
		if(endTime==null)
			return;


		if(pearl.getWorld().getFullTime()>endTime){
			String playerName=null;
			Player player=null;
			if(pearl.getShooter() instanceof Player){
				player=(Player)pearl.getShooter();
				playerName=player.getName();
			}

			BastionBlock bastion=blocks.get(pearl);
			if(bastion!=null)
				if(bastion.enderPearlBlocked(pearl.getLocation(), playerName)){
					bastion.handleTeleport(pearl.getLocation(), player);
					pearl.remove();
					blocks.remove(pearl);
					endTimes.remove(pearl);
				}
		}
	}


}
