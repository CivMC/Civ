package isaac.bastion;


import java.util.Random;

import isaac.bastion.util.QTBox;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings("rawtypes")
public class BastionBlock implements QTBox, Comparable
{
	private Location location;
	private int id;
	private int strength;
	private int radius;
	private long placed;
	
	private long lastPlace;
	private int radiusSquared;
	private boolean loaded=true;
	private boolean ghost=false;

	private static int highestId=-1;
	private static int min_break_time;
	private static int erosionTime;
	private static boolean first=true;
	private int taskId;
	private static Random random;

	public BastionBlock(Location nlocation, PlayerReinforcement reinforcement){
		id=++highestId;
		location = nlocation;
		radius=Bastion.getConfigManager().getBastionBlockEffectRadius();
		radiusSquared=radius*radius;
		placed=System.currentTimeMillis();

		strength=reinforcement.getDurability();
		loaded=false;

		setup();

	}
	public BastionBlock(Location nLocation,long nPlaced,int nID)
	{
		id=nID;
		location = nLocation;
		radius=Bastion.getConfigManager().getBastionBlockEffectRadius();
		placed=nPlaced;
		
		radiusSquared=radius*radius;

		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location.getBlock());

		strength=reinforcement.getDurability();
		setup();

	}
	private void setup(){
		if(id>highestId){
			highestId=id;
		}


		if(first){
			intiStatic();
		}

		taskId=registerTask();
		
		lastPlace=System.currentTimeMillis();
		
	}
	private void intiStatic(){
		min_break_time=(1000*60)/Bastion.getConfigManager().getBastionBlockMaxBreaks();
		random=new Random();
		erosionTime=(1000*60*60*24*20)/Bastion.getConfigManager().getBastionBlockErosion();
		first=true;
	}
	private int registerTask(){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		return scheduler.scheduleSyncRepeatingTask(Bastion.getPlugin(),
				new BukkitRunnable(){
			public void run(){
				erode();
			}
		},
		random.nextInt(erosionTime),erosionTime);
	}
	public void close(){
		if(!ghost){
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.cancelTask(taskId);
			loaded=false;
			ghost=true;
			location.getBlock().setType(Material.AIR);
		}
	}
	public void free_id(){
		if(id==highestId){
			--highestId;
		}
	}
	static public int getHighestID(){
		return highestId;
	}
	public Location getLocation(){
		return location;
	}
	public boolean ghost(){
		return ghost;
	}
	public boolean loaded(){
		return loaded;
	}
	public boolean blocked(BlockPlaceEvent event)
	{
		String playerName=event.getPlayer().getName();
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location.getBlock());
		if(reinforcement instanceof PlayerReinforcement){
			Faction owner = reinforcement.getOwner();
			if(owner.isMember(playerName)||owner.isFounder(playerName)||owner.isModerator(playerName)){
				//return false;
			}

			if (((event.getBlock().getX() - location.getX()) * (float)(event.getBlock().getX() - location.getX()) + 
					(event.getBlock().getZ() - location.getZ()) * (float)(event.getBlock().getZ() - location.getZ()) > radiusSquared)
					|| (event.getBlock().getY() <= location.getY())) {

				Bastion.getPlugin().getLogger().info("not blocked");
				return false;
			}

			Bastion.getPlugin().getLogger().info("blocked");
		}
		return true;
	}
	public void handlePlaced(Block block) {
		block.breakNaturally();
		erode();
	}
	public boolean shouldCull(){

		if(strength > 0){
			return false;
		} else{
			return true;
		}
	}
	private void erode(){
		long time=System.currentTimeMillis();
		if((time-lastPlace)>=min_break_time){
			IReinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(location.getBlock());


			if (reinforcement != null) {
				strength=reinforcement.getDurability();
				--strength;
				reinforcement.setDurability(strength);
				Citadel.getReinforcementManager().addReinforcement(reinforcement);
			}
			
		}
		if(shouldCull()){
			close();
		}
	}
	public int getID(){
		return id;
	}

	public long getPlaced(){
		return placed;
	}
	@Override
	public int qtXMin() {
		return location.getBlockX()-radius;
	}

	@Override
	public int qtXMid() {
		return location.getBlockX();
	}

	@Override
	public int qtXMax() {
		return location.getBlockX()+radius;
	}

	@Override
	public int qtZMin() {
		return location.getBlockZ()-radius;
	}

	@Override
	public int qtZMid() {
		return location.getBlockZ();
	}

	@Override
	public int qtZMax() {
		return location.getBlockZ()+radius;
	}

	@Override
	public int compareTo(Object o) {
		BastionBlock other=(BastionBlock)o;
		int thisX=location.getBlockX();
		int thisY=location.getBlockY();
		int thisZ=location.getBlockZ();

		int otherX=other.location.getBlockX();
		int otherY=other.location.getBlockX();
		int otherZ=other.location.getBlockX();

		if(thisX<otherX)
			return -1;
		if(thisY<otherY)
			return -1;
		if(thisZ<otherZ)
			return -1;

		if(thisX>otherX)
			return 1;
		if(thisY>otherY)
			return 1;
		if(thisZ>otherZ)
			return 1;

		return 0;
	}
}