package isaac.bastion;


import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.util.QTBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;


public class BastionBlock implements QTBox, Comparable<BastionBlock>
{
	public Location location; 
	private int id;
	private double balance=0; //the amount remaining still to be eroded after the whole part has been removed
	private int strength; //current durability
	private long placed; //time when the bastion block was created

	private long lastPlace; //time when the last erode took place
	private boolean loaded=true; //has the object not been modified since it was loaded?
	private boolean ghost=false; //should the object be deleted

	private static int highestId=0; //the current highest id
	private static int min_break_time; //Minimum time between erosions that count
	private static int erosionTime; //time between auto erosion. If 0 never called.
	private static int scaleTime; //time between creation and max strength/maturity

	private static int radiusSquared; //radius blocked squared
	private static int radius; //radius blocked

	private static double pearlScale; //factor between reinforcement removed by placing blocks and from blocking pearls
	private static boolean pearlNeedsMaturity; //only block pearls after maturity has been reached

	private static boolean first=true;

	private int taskId; //the id of the task associated with erosion
	private static Random random; //used only to offset the erosion tasks
	public static BastionBlockSet set; 

	//constructor for new blocks. Reinforcement must be passed because it does not exist at the time of the reinforcement event.
	public BastionBlock(Location nlocation, PlayerReinforcement reinforcement){
		id=++highestId;
		location = nlocation;
		placed=System.currentTimeMillis();

		strength=reinforcement.getDurability();
		loaded=false;

		setup();

	}
	//constructor for blocks loaded from database
	public BastionBlock(Location nLocation,long nPlaced,float nBalance,int nID){
		id=nID;
		location = nLocation;

		placed=nPlaced;
		balance=nBalance;
		loaded=true;

		PlayerReinforcement reinforcement = getReinforcement();
		if(reinforcement!=null){
			strength=reinforcement.getDurability();
			setup();
		} else{
			strength=0;
			silentClose();
		}

	}

	//called by both constructor to do the things they shair
	private void setup(){
		if(id>highestId){
			highestId=id;
		}

		if(first){
			intiStatic();
		}

		if(erosionTime!=0){
			taskId=registerTask();
		}

		balance=0;
		lastPlace=System.currentTimeMillis();

	}
	//called if this is the first Bastion block created to set up the static variables
	private void intiStatic(){

		if(Bastion.getConfigManager().getBastionBlockMaxBreaks()!=0){ //we really should never have 0
			//convert getBastionBlockMaxBreaks() from breaks per second to
			//invulnerability time in milliseconds 
			min_break_time=(1000*60)/Bastion.getConfigManager().getBastionBlockMaxBreaks();
		} else{
			min_break_time=0; //if we do default to no invulnerability time
		}

		scaleTime=Bastion.getConfigManager().getBastionBlockScaleTime();

		if(Bastion.getConfigManager().getBastionBlockErosion()!=0){ //0 disables constant erosion
			//Convert getBastionBlockErosion() from erosion per day to time between erosions
			erosionTime=(1000*60*60*24*20)/Bastion.getConfigManager().getBastionBlockErosion(); 
		} else{
			erosionTime=0;
		}

		pearlScale=Bastion.getConfigManager().getEnderPearlErosionScale();
		pearlNeedsMaturity=Bastion.getConfigManager().getEnderPearlRequireMaturity();

		radius=Bastion.getConfigManager().getBastionBlockEffectRadius();
		radiusSquared=radius*radius;

		random=new Random();
		first=false;

	}
	//called to register the erosion task
	private int registerTask(){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		return scheduler.scheduleSyncRepeatingTask(Bastion.getPlugin(),
				new BukkitRunnable(){
			public void run(){
				erode(1);
			}
		},
		random.nextInt(erosionTime),erosionTime);
	}
	public void close(){
		if(!ghost){
			if(erosionTime!=0){
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.cancelTask(taskId);
			}
			loaded=false;
			ghost=true;
			location.getBlock().setType(Material.AIR);
			set.updated(this);
			
			Bastion.getPlugin().getLogger().info("Removed bastion "+id);
			Bastion.getPlugin().getLogger().info("Had been placed on "+placed);
			Bastion.getPlugin().getLogger().info("At "+location);
			
		}
	}
	public void silentClose(){
		if(!ghost){
			if(erosionTime!=0){
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.cancelTask(taskId);
			}
			loaded=false;
			ghost=true;
			set.updated(this);
		}
	}
	private double erosionFromPlace(){
		double scaleStart=Bastion.getConfigManager().getBastionBlockScaleFacStart();
		double scaleEnd=Bastion.getConfigManager().getBastionBlockScaleFacEnd();
		int time=(int) (System.currentTimeMillis()-placed);

		if(scaleTime==0){
			return scaleStart;
		} else if(time<scaleTime){
			return (((scaleEnd-scaleStart)/(float)scaleTime)*time+scaleStart);
		} else{
			return scaleEnd;
		}
	}
	static public int getHighestID(){
		return highestId;
	}
	static public double getRadiusSquared(){
		return radiusSquared;
	}
	static public double getRadius(){
		return radius;
	}


	//checks if a ender pearl should be blocked at a location for a player.
	public boolean ghost(){
		return ghost;
	}
	public boolean loaded(){
		return loaded;
	}	
	public boolean blocked(BlockEvent event)
	{
		String playerName;
		if(event instanceof BlockPlaceEvent){
			playerName=((BlockPlaceEvent) event).getPlayer().getName();
		} else{
			playerName=null;
		}
		PlayerReinforcement reinforcement = getReinforcement();
		if(reinforcement instanceof PlayerReinforcement){
			Faction owner = reinforcement.getOwner();
			if(playerName!=null){
				if(owner.isMember(playerName)||owner.isFounder(playerName)||owner.isModerator(playerName)){
					return false;
				}
			}

			if (((event.getBlock().getX() - location.getX()) * (float)(event.getBlock().getX() - location.getX()) + 
					(event.getBlock().getZ() - location.getZ()) * (float)(event.getBlock().getZ() - location.getZ()) >= radiusSquared)
					|| (event.getBlock().getY() <= location.getY())) {
				return false;
			}

		} else{
			return false;
		}
		return true;
	}

	public boolean enderPearlBlocked(Location loc,String playerName){
		if((scaleTime>(System.currentTimeMillis()-placed))&&pearlNeedsMaturity)
			return false;

		return blocked(loc, playerName);
	}
	//Checks if location is in the bastion's field
	public boolean blocked(Location loc,String playerName)
	{
		
		Player bastion_owner  = Bukkit.getPlayer(playerName);
		if(bastion_owner instanceof Player)
			if(bastion_owner.hasPermission("Bastion.bypass")) //let admins do whatever 
				return false; 
		
		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){

			Faction owner = reinforcement.getOwner();
			if(playerName!=null){
				if(owner.isMember(playerName)||owner.isFounder(playerName)||owner.isModerator(playerName)){
					return false;
				}
			}
			if (((loc.getBlock().getX() - location.getX()) * (float)(loc.getBlock().getX() - location.getX()) + 
					(loc.getBlock().getZ() - location.getZ()) * (float)(loc.getBlock().getZ() - location.getZ()) >= radiusSquared)
					|| (loc.getBlock().getY() <= location.getY())) {
				return false;
			}

		} else{
			return false;
		}

		return true;
	}

	public boolean inField(Location loc){
		if (((loc.getBlockX() - location.getX()) * (float)(loc.getBlockX() - location.getX()) + 
				(loc.getBlockZ() - location.getZ()) * (float)(loc.getBlockZ() - location.getZ()) >= radiusSquared)
				|| (loc.getBlockY() <= location.getY())) {
			return false;
		}
		return true;	
	}
	//checks if a player would be allowed to remove the Bastion block

	public boolean canRemove(Player player){
		
		if(player.hasPermission("Bastion.bypass")) //let admins do whatever 
			return true;
		
		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){

			Faction owner = reinforcement.getOwner();
			return owner.isModerator(player.getName())||owner.isFounder(player.getName());
		}
		return true;
	}

	//checks if a player would be allowed to place
	public boolean canPlace(String playerName){
		
		Player bastion_owner  = Bukkit.getPlayer(playerName);
		if(bastion_owner instanceof Player)
			if(bastion_owner.hasPermission("Bastion.bypass")) //let admins do whatever 
				return true; 
		
		
		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){

			Faction owner = reinforcement.getOwner();
			if(playerName!=null){
				if(owner.isMember(playerName)||owner.isFounder(playerName)||owner.isModerator(playerName)){
					return true;
				}
			}
		} else{
			return true;
		}

		return false;
	}

	//Take care of a block that should be removed from inside the field
	public void handlePlaced(Block block,boolean handleRemoval) {
		if(handleRemoval)
			block.breakNaturally();
		
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(block);
		if(reinforcement instanceof PlayerReinforcement){
			reinforcement.setDurability(0);
			Citadel.getReinforcementManager().addReinforcement(reinforcement);
		}
		erode(erosionFromPlace());
	}

	//Send a message and apply the necessary erosion of a pearl being blocked.
	public void handleTeleport(Location loc,Player player){
		player.sendMessage(ChatColor.RED+"Ender pearl blocked by Bastion Block");
		player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL,1));
		erode(erosionFromPlace()*pearlScale);
	}
	
	//returns if the Bastion's strength is at zero and it should be removed
	public boolean shouldCull(){
		if(strength+balance> 0){
			return false;
		} else{
			return true;
		}
	}

	//removes a set amount of durability from the reinforcement
	private void erode(double amount){
		long time=System.currentTimeMillis();

		double toBeRemoved=balance+amount;

		int wholeToRemove=(int) toBeRemoved;
		double fractionToRemove=(double) toBeRemoved-wholeToRemove;

		if((time-lastPlace)>=min_break_time){ //not still locked after last erosion
			IReinforcement reinforcement =  getReinforcement();

			if (reinforcement != null) {
				strength=reinforcement.getDurability();
			} else{
				close();
				return;
			}

			strength-=wholeToRemove;
			balance=fractionToRemove;

			reinforcement.setDurability(strength);
			Citadel.getReinforcementManager().addReinforcement(reinforcement);
			lastPlace=time;

		}
		set.updated(this);
		if(shouldCull()){
			close();
		}
	}

	public void mature(){
		placed -= scaleTime;
	}

	public Location getLocation(){
		return location;
	}
	public int getID(){
		return id;
	}

	public long getPlaced(){
		return placed;
	}
	public double getBalance(){
		return balance;
	}

	public PlayerReinforcement getReinforcement(){
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
				getReinforcement(location.getBlock());
		if(reinforcement instanceof PlayerReinforcement){
			return reinforcement;
		}
		return null;
	}
	
	//need to use SparseQuadTree
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


	public String toString(){
		SimpleDateFormat dateFormator = new SimpleDateFormat("M/d/yy H:m:s");
		String result="Dev text: ";
		
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location.getBlock());
	
		double scaleTime_as_hours=0;
		if(scaleTime==0){
			result+="Maturity timers are disabled \n";
		} else{
			scaleTime_as_hours = ((double) scaleTime)/(1000*60*60);
		}
		if (reinforcement instanceof PlayerReinforcement) {
			strength=reinforcement.getDurability();

			result+="Current Bastion reinforcement: "+String.valueOf((double) strength-balance)+'\n';

			result+="Maturity time is ";
			result+=String.valueOf(scaleTime_as_hours)+'\n';

			result+="Which means  " + String.valueOf(erosionFromPlace()) + " will removed after every blocked placeemnt"+'\n';
			
			result+="Placed on "+dateFormator.format(new Date(placed))+'\n';
			result+="by group "+reinforcement.getOwner().getName();
		}



		return result;
	}
	public String infoMessage(boolean dev,Player asking){
		if(dev){
			return ChatColor.GREEN+this.toString();
		}

		String result="";

		//PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location.getBlock());
		//Faction fac;
		double fractionOfMaturityTime=0;
		if(scaleTime==0){
			fractionOfMaturityTime=1;
		} else{
			fractionOfMaturityTime=((double) (System.currentTimeMillis()-placed))/scaleTime;
		}
		if(fractionOfMaturityTime==0){
			result = ChatColor.GREEN+"No strength";
		} else if(fractionOfMaturityTime < 0.25){
			result = ChatColor.GREEN+"Some strength";
		} else if(fractionOfMaturityTime < 0.5){
			result = ChatColor.GREEN+"Low strength";
		} else if(fractionOfMaturityTime < 0.75){
			result = ChatColor.GREEN+"Moderate strength";
		} else if(fractionOfMaturityTime < 1){
			result = ChatColor.GREEN+"High strength";
		} else if(fractionOfMaturityTime>=1){
			result = ChatColor.GREEN+"Full strength";
		}


		return result;
	}
	@Override
	public int compareTo(BastionBlock other) {
		int thisX=location.getBlockX();
		int thisY=location.getBlockY();
		int thisZ=location.getBlockZ();

		int otherX=other.location.getBlockX();
		int otherY=other.location.getBlockY();
		int otherZ=other.location.getBlockZ();

		if(thisX<otherX)
			return -1;
		if(thisX>otherX)
			return 1;
		
		if(thisY<otherY)
			return -1;
		if(thisY>otherY)
			return 1;
		
		if(thisZ<otherZ)
			return -1;
		if(thisZ>otherZ)
			return 1;

		return 0;
	}
}