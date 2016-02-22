package isaac.bastion;

import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.storage.BastionBlockStorage;
import isaac.bastion.storage.Database;
import isaac.bastion.util.QTBox;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class BastionBlock implements QTBox, Comparable<BastionBlock> {	
	public static int MIN_BREAK_TIME; //Minimum time between erosions that count
	private static int EROSION_TIME; //time between auto erosion. If 0 never called.
	private static int SCALING_TIME; //time between creation and max strength/maturity

	private static int RADIUS_SQUARED; //radius blocked squared
	private static int RADIUS; //radius blocked

	private static double BLOCK_TO_PEARL_SCALE; //factor between reinforcement removed by placing blocks and from blocking pearls
	public static boolean ONLY_BLOCK_PEARLS_ON_MATURE; //only block pearls after maturity has been reached

	private static boolean first = true;

	private Location location; 
	private int id = -1;
	private double balance = 0; //the amount remaining still to be eroded after the whole part has been removed
	private int strength; //current durability
	private long placed; //time when the bastion block was created
	private boolean inDB = false;
	private int taskId; //the id of the task associated with erosion
	private static Random random; //used only to offset the erosion tasks
	public static BastionBlockSet set; 

	/**
	 * constructor for new blocks. Reinforcement must be passed because it does not exist at the time of the reinforcement event.
	 * @param location
	 * @param reinforcement
	 */
	public BastionBlock(Location location, PlayerReinforcement reinforcement) {
		this.location = location;
		this.placed = System.currentTimeMillis();
		this.id = set.size();

		this.strength = reinforcement.getDurability();
		setup();
	}
	
	/**
	 * constructor for blocks loaded from database
	 * @param location
	 * @param placed
	 * @param balance
	 * @param ID
	 */
	public BastionBlock(Location location, long placed, float balance, int ID) {
		this.id = ID;
		this.location = location;

		this.placed = placed;
		this.balance = balance;

		this.inDB = true;

		PlayerReinforcement reinforcement = getReinforcement();
		if (reinforcement != null) {
			this.strength = reinforcement.getDurability();
			setup();
		} else{
			this.strength = 0;
			close();
		}

	}

	/**
	 * called by both constructors to do the things they share
	 */
	private void setup() {
		if (first) {
			firstime_setup();
		}

		if (EROSION_TIME != 0) {
			taskId = registerTask();
		}
	}
	
	/**
	 * called if this is the first Bastion block created to set up the static variables
	 */
	private void firstime_setup() {

		if (Bastion.getConfigManager().getBastionBlockMaxBreaks() !=0 ) { // we really should never have 0
			/* convert getBastionBlockMaxBreaks() from breaks per second to
			 * invulnerability time in milliseconds */ 
			MIN_BREAK_TIME = 60000 / Bastion.getConfigManager().getBastionBlockMaxBreaks();
		} else {
			MIN_BREAK_TIME = 0; //if we do default to no invulnerability time
		}

		SCALING_TIME=Bastion.getConfigManager().getBastionBlockScaleTime();

		if (Bastion.getConfigManager().getBastionBlockErosion() !=0 ){ // 0 disables constant erosion
			/* Convert getBastionBlockErosion() from erosion per day to time between erosions
			 * number of ticks per day / erosion per day = number of ticks between erosions
			 * so time between erosions (EROSION_TIME) = 20t * 60s * 60m * 24h / epd */
			EROSION_TIME = 1728000 / Bastion.getConfigManager().getBastionBlockErosion(); 
		} else{
			EROSION_TIME = 0;
		}

		BLOCK_TO_PEARL_SCALE = Bastion.getConfigManager().getEnderPearlErosionScale();
		ONLY_BLOCK_PEARLS_ON_MATURE = Bastion.getConfigManager().getEnderPearlRequireMaturity();

		RADIUS = Bastion.getConfigManager().getBastionBlockEffectRadius();
		RADIUS_SQUARED = RADIUS*RADIUS;

		random = new Random();
		first = false;
	}
	
	/**
	 * called to register the erosion task
	 * @return Task ID of the {@link BukkitRunnable} that results.
	 */
	private int registerTask() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		return scheduler.runTaskTimer(Bastion.getPlugin(), 
			new Runnable() {
				public void run() {
					erode(1);
				}
			},
		random.nextInt(EROSION_TIME),EROSION_TIME).getTaskId();
	}

	/**
	 * saves a new bastion into the database
	 * TODO: will create double entries if bastion already exists
	 * @param db where to save.
	 */
	public void save(Database db) {
		if (!inDB) {
			if (!db.isConnected()) {
				db.connect();
			}
			PreparedStatement addBastion = BastionBlockStorage.insertBastion;
			try {
				addBastion.setInt   (1, location.getBlockX());
				addBastion.setInt   (2, location.getBlockY());
				addBastion.setInt   (3, location.getBlockZ());
				addBastion.setString(4, location.getWorld().getName());
				addBastion.setLong  (5, placed);
				addBastion.setDouble(6, balance);
				addBastion.execute();
				id = db.getInteger("SELECT LAST_INSERT_ID();");
				inDB = true;
			} catch (SQLException e) {
				Bastion.getPlugin().getLogger().log(Level.SEVERE, "Failed Bastion Save to DB at " + location, e);
			}
		} else {
			Bastion.getPlugin().getLogger().warning("tried to save BastionBlock that was in DB\n " + toString());
		}
	}
	
	/** 
	 * updates placed and balance in db
	 */
	public void update(Database db) {
		if (inDB) {
			if (!db.isConnected()) {
				db.connect();
			}
			PreparedStatement updateBastion = BastionBlockStorage.updateBastion;
			try {
				updateBastion.setLong(1, placed);
				updateBastion.setDouble(2, balance);
				updateBastion.setInt(3, id);
				updateBastion.execute();
			} catch (SQLException e) {
				Bastion.getPlugin().getLogger().log(Level.SEVERE, "Failed Bastion Update to DB at " + location, e);
				e.printStackTrace();
			}
		} else {
			Bastion.getPlugin().getLogger().warning("tried to update BastionBlock that was not in DB\n " + toString());
			save(db);
		}
	}

	public void delete(Database db){
		if (!db.isConnected()) {
			db.connect();
		}
		PreparedStatement deleteBastion = BastionBlockStorage.deleteBastion;
		try {
			deleteBastion.setInt(1, id);
			deleteBastion.execute();
		} catch (SQLException e) {
			Bastion.getPlugin().getLogger().log(Level.SEVERE, "Failed Bastion Delete to DB at " + location, e);
		}

		inDB = false;
	}


	/**
	 * @brief Gets the percentage of the reinforcement that should erode from the block
	 * @return The percentage that should erode
	 */
	public double erosionFromBlock() {
		double scaleStart=Bastion.getConfigManager().getBastionBlockScaleFacStart();
		double scaleEnd=Bastion.getConfigManager().getBastionBlockScaleFacEnd();
		
		// This needs to be a long or it will roll over after 21 days!
		long time = System.currentTimeMillis() - placed;

		if (SCALING_TIME == 0) {
			return scaleStart;
		} else if (time < SCALING_TIME) {
			return (((scaleEnd - scaleStart) / (float)SCALING_TIME) * time + scaleStart);
		} else{
			return scaleEnd;
		}
	}

	/* *
	 * currently very simple but gives easy options to change
	 */
	public double erosionFromPearl() {
		return erosionFromBlock() * BLOCK_TO_PEARL_SCALE;
	}

	/**
	 * Checks if a location is inside this bastion field.
	 * 
	 * @param loc
	 * @return true if in field
	 */
	public boolean inField(Location loc) {
		return !(yLevelCheck(loc) || // Everyone checks the Y, do it first.
				// Square Check
				(Bastion.getConfigManager().squareField() &&
					(Math.abs(loc.getBlockX() - location.getBlockX()) > RADIUS || 
					Math.abs(loc.getBlockZ() - location.getBlockZ()) > RADIUS) ) || 
				// Round Check
				(!Bastion.getConfigManager().squareField() && 
					((loc.getBlockX() - location.getX()) * (float)(loc.getBlockX() - location.getX()) + 
					(loc.getBlockZ() - location.getZ()) * (float)(loc.getBlockZ() - location.getZ()) >= RADIUS_SQUARED))
			);
	}
	
	public boolean yLevelCheck(Location loc) {
		if (Bastion.getConfigManager().includeSameYLevel()) {
			return loc.getBlockY() < location.getY();
		}
		return loc.getBlockY() <= location.getY();
	}

	/**
	 * checks if a player would be allowed to remove the Bastion block
	 * @param loc
	 * @return
	 */
	public boolean canRemove(Player player){

		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){
			return reinforcement.isBypassable(player); //should return true if founder or moderator, but I feel this is more consistant
		}
		return true;
	}

	/**
	 * checks if a player would be allowed to place
	 */
	public boolean canPlace(Player player) {
		PlayerReinforcement reinforcement = getReinforcement();

		if (reinforcement == null) return true;
		if (player == null) return false;

		if (reinforcement.isAccessible(player)) return true;

		return false;
	}

	/**
	 * @param players
	 * @return
	 */
	public boolean oneCanPlace(Set<UUID> players){
		PlayerReinforcement reinforcement = getReinforcement();
		//the object will have been closed if null but we still don't want things to crash
		if (reinforcement == null)
			return true; 

		for (UUID player: players){
			if (player != null)
				if (reinforcement.isAccessible(player))
					return true;
		}

		return false;
	}


	/**
	 * returns if the Bastion's strength is at zero and it should be removed
	 */
	public boolean shouldCull(){
		if(strength-balance > 0){
			return false;
		} else{
			return true;
		}
	}

	/**
	 * removes a set amount of durability from the reinforcement
	 * @param amount
	 */
	public void erode(double amount){
		double toBeRemoved = balance + amount;

		int wholeToRemove=(int) toBeRemoved;
		double fractionToRemove=(double) toBeRemoved-wholeToRemove;

		Reinforcement reinforcement = getReinforcement();

		if (reinforcement != null) {
			strength = reinforcement.getDurability();
		} else {
			return;
		}

		strength -= wholeToRemove;
		balance = fractionToRemove;

		reinforcement.setDurability(strength);

		set.updated(this);

		if (shouldCull()) {
			destroy();
		}
	}

	public void mature(){
		placed -= SCALING_TIME;
	}
	
	public boolean isMature(){
		return System.currentTimeMillis() - placed >= SCALING_TIME;
	}

	private PlayerReinforcement getReinforcement(){
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
				getReinforcement(location.getBlock());
		if(reinforcement instanceof PlayerReinforcement){
			return reinforcement;
		} else {
			close();
			Bastion.getPlugin().getLogger().log(Level.SEVERE, "Reinforcement removed without removing Bastion. Fixed");
		}
		return null;
	}

	public UUID getOwner(){
		return getReinforcement().getGroup().getOwner();
	}

	public Location getLocation(){
		return location;
	}

	static public int getRadiusSquared(){
		return RADIUS_SQUARED;
	}
	
	public static int getRadius() {
		return RADIUS;
	}
	
	public long getId(){
		return id;
	}

	// needed to use SparseQuadTree
	@Override
	public int qtXMin() {
		return location.getBlockX()-RADIUS;
	}

	@Override
	public int qtXMid() {
		return location.getBlockX();
	}

	@Override
	public int qtXMax() {
		return location.getBlockX()+RADIUS;
	}

	@Override
	public int qtZMin() {
		return location.getBlockZ()-RADIUS;
	}

	@Override
	public int qtZMid() {
		return location.getBlockZ();
	}

	@Override
	public int qtZMax() {
		return location.getBlockZ()+RADIUS;
	}

	public String toString(){
		SimpleDateFormat dateFormator = new SimpleDateFormat("M/d/yy H:m:s");
		StringBuilder result = new StringBuilder("Dev text: ");

		PlayerReinforcement reinforcement = getReinforcement();

		double scaleTime_as_hours=0;
		if(SCALING_TIME==0){
			result.append("Maturity timers are disabled \n");
		} else{
			scaleTime_as_hours = ((double) SCALING_TIME)/(1000*60*60);
		}
		if (reinforcement instanceof PlayerReinforcement) {
			strength = reinforcement.getDurability();

			result.append("Current Bastion reinforcement: ")
					.append((double) strength-balance).append('\n');

			result.append("Maturity time is ")
					.append(scaleTime_as_hours).append('\n');

			result.append("Which means ").append(erosionFromBlock())
					.append(" will removed after every blocked placeemnt\n");

			result.append("Placed on ").append(dateFormator.format(new Date(placed))).append('\n');
			result.append("by group ").append(reinforcement.getGroup().getName()).append('\n');
			result.append("At: ").append(location.toString());
		}
		return result.toString();
	}
	
	public String infoMessage(boolean dev, Player asking){
		StringBuffer result = new StringBuffer(ChatColor.GREEN.toString());
		if (dev) {
			return result.append( this.toString() ).toString();
		}

		double fractionOfMaturityTime=0;
		if(SCALING_TIME==0){
			fractionOfMaturityTime=1;
		} else{
			fractionOfMaturityTime=((double) (System.currentTimeMillis()-placed))/SCALING_TIME;
		}
		if (fractionOfMaturityTime == 0) {
			result.append("No strength");
		} else if (fractionOfMaturityTime < 0.25) {
			result.append("Some strength");
		} else if (fractionOfMaturityTime < 0.5) {
			result.append("Low strength");
		} else if (fractionOfMaturityTime < 0.75) {
			result.append("Moderate strength");
		} else if (fractionOfMaturityTime < 1) {
			result.append("High strength");
		} else if (fractionOfMaturityTime >= 1) {
			result.append("Full strength");
		}

		return result.toString();
	}
	
	// TODO: World-aware comparison
	@Override
	public int compareTo(BastionBlock other) {
		int thisX = location.getBlockX();
		int thisY = location.getBlockY();
		int thisZ = location.getBlockZ();

		int otherX = other.location.getBlockX();
		int otherY = other.location.getBlockY();
		int otherZ = other.location.getBlockZ();

		if (thisX < otherX) {
			return -1;
		}
		if (thisX > otherX) {
			return 1;
		}

		if (thisY < otherY) {
			return -1;
		}
		if (thisY > otherY) {
			return 1;
		}
		
		if (thisZ < otherZ) {
			return -1;
		}
		if (thisZ > otherZ) {
			return 1;
		}

		return 0;
	}

	
	/**
	 * removes Bastion from database and destroys the block at the location
	 */
	public void destroy() {
		if (Bastion.getConfigManager().getDestroy()) {
			location.getBlock().setType(Material.AIR);
		}
		close();
	}

	/**
	 * removes Bastion from world
	 */
	public void close(){
		if(!set.contains(this)){
			Bastion.getPlugin().getLogger().warning("Tried to close already closed Bastion at " + this.location);
			//return; //already not in don't need to remove
		}

		if (EROSION_TIME != 0) {
			Bukkit.getServer().getScheduler().cancelTask(taskId);
		}
		
		set.remove(this);

		Bastion.getPlugin().getLogger().info("Removed bastion " + id + " Had been placed on "
				+ placed + " At "+ location);
	}


}
