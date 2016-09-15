package isaac.bastion;

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

import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.storage.BastionBlockStorage;
import isaac.bastion.storage.Database;
import isaac.bastion.util.QTBox;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionBlock implements QTBox, Comparable<BastionBlock> {	

	private Location location; 
	private int id = -1;
	private double balance = 0; //the amount remaining still to be eroded after the whole part has been removed
	private int strength; //current durability
	private long placed; //time when the bastion block was created
	private boolean inDB = false;
	private int taskId; //the id of the task associated with erosion
	private static Random random = new Random(); //used only to offset the erosion tasks
	public static BastionBlockSet set; 
	private BastionType type;
	
	/**
	 * constructor for new blocks. Reinforcement must be passed because it does not exist at the time of the reinforcement event.
	 * @param location
	 * @param reinforcement
	 */
	public BastionBlock(Location location, PlayerReinforcement reinforcement, BastionType type) {
		this.location = location;
		this.placed = System.currentTimeMillis();
		this.id = set.size();
		this.type = type;
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
	public BastionBlock(Location location, long placed, float balance, int ID, BastionType type) {
		this.id = ID;
		this.location = location;

		this.placed = placed;
		this.balance = balance;

		this.inDB = true;

		this.type = type;
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
		if (type.getErosionPerDay() != 0) {
			taskId = registerTask();
		}
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
		random.nextInt(type.getErosionPerDay()),type.getErosionPerDay()).getTaskId();
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
		double scaleStart = type.getStartScaleFactor();
		double scaleEnd = type.getFinalScaleFactor();
		
		// This needs to be a long or it will roll over after 21 days!
		long time = System.currentTimeMillis() - placed;

		if (type.getWarmupTime() == 0) {
			return scaleStart;
		} else if (time < type.getWarmupTime()) {
			return (((scaleEnd - scaleStart) / (float)type.getWarmupTime()) * time + scaleStart);
		} else{
			return scaleEnd;
		}
	}

	/* *
	 * currently very simple but gives easy options to change
	 */
	public double erosionFromPearl() {
		return erosionFromBlock() * type.getPearlScaleFactor();
	}
	
	public double erosionFromElytra() {
		return erosionFromBlock() * type.getElytraScale();
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
				(type.isSquare() &&
					(Math.abs(loc.getBlockX() - location.getBlockX()) > type.getEffectRadius() || 
					Math.abs(loc.getBlockZ() - location.getBlockZ()) > type.getEffectRadius()) ) || 
				// Round Check
				(!type.isSquare() && 
					((loc.getBlockX() - location.getX()) * (float)(loc.getBlockX() - location.getX()) + 
					(loc.getBlockZ() - location.getZ()) * (float)(loc.getBlockZ() - location.getZ()) >= type.getRadiusSquared()))
			);
	}
	
	public boolean yLevelCheck(Location loc) {
		if (type.isIncludeY()) {
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
			return reinforcement.canBypass(player); //should return true if founder or moderator, but I feel this is more consistant
		}
		return true;
	}
	
	public boolean canPearl(Player p) {
		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){
			return NameAPI.getGroupManager().hasAccess(reinforcement.getGroup(), p.getUniqueId(), PermissionType.getPermission("BASTION_PEARL"));
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

		return NameAPI.getGroupManager().hasAccess(reinforcement.getGroup(), player.getUniqueId(), PermissionType.getPermission("BASTION_PLACE"));
	}
	
	public boolean permAccess(Player p, PermissionType perm) {
		PlayerReinforcement rein = getReinforcement();
		if (rein == null) {
			return true;
		}
		return NameAPI.getGroupManager().hasAccess(rein.getGroup(), p.getUniqueId(), perm);
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
				if (NameAPI.getGroupManager().hasAccess(reinforcement.getGroup(), player, PermissionType.getPermission("BASTION_PLACE")))
					return true;
		}

		return false;
	}


	/**
	 * returns if the Bastion's strength is at zero and it should be removed
	 */
	public boolean shouldCull() {
		if (strength-balance > 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * removes a set amount of durability from the reinforcement
	 * @param amount
	 */
	public void erode(double amount) {
		double toBeRemoved = balance + amount;

		int wholeToRemove = (int) toBeRemoved;
		double fractionToRemove = (double) toBeRemoved - wholeToRemove;

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

	public void mature() {
		placed -= type.getWarmupTime();
	}
	
	public boolean isMature() {
		return System.currentTimeMillis() - placed >= type.getWarmupTime();
	}

	private PlayerReinforcement getReinforcement() {
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
				getReinforcement(location.getBlock());
		if (reinforcement instanceof PlayerReinforcement) {
			return reinforcement;
		} else {
			close();
			Bastion.getPlugin().getLogger().log(Level.SEVERE, "Reinforcement removed without removing Bastion. Fixed");
		}
		return null;
	}

	public UUID getOwner() {
		return getReinforcement().getGroup().getOwner();
	}

	public Location getLocation() {
		return location;
	}

	public long getId() {
		return id;
	}

	// needed to use SparseQuadTree
	@Override
	public int qtXMin() {
		return location.getBlockX()-type.getEffectRadius();
	}

	@Override
	public int qtXMid() {
		return location.getBlockX();
	}

	@Override
	public int qtXMax() {
		return location.getBlockX()+type.getEffectRadius();
	}

	@Override
	public int qtZMin() {
		return location.getBlockZ()-type.getEffectRadius();
	}

	@Override
	public int qtZMid() {
		return location.getBlockZ();
	}

	@Override
	public int qtZMax() {
		return location.getBlockZ()+type.getEffectRadius();
	}

	public String toString(){
		SimpleDateFormat dateFormator = new SimpleDateFormat("M/d/yy H:m:s");
		StringBuilder result = new StringBuilder("Dev text: ");

		PlayerReinforcement reinforcement = getReinforcement();

		double scaleTime_as_hours=0;
		if(type.getWarmupTime()==0){
			result.append("Maturity timers are disabled \n");
		} else{
			scaleTime_as_hours = ((double) type.getWarmupTime())/(1000*60*60);
		}
		if (reinforcement instanceof PlayerReinforcement) {
			strength = reinforcement.getDurability();

			result.append("Current Bastion reinforcement: ")
					.append((double) strength-balance).append('\n');

			result.append("Maturity time is ")
					.append(scaleTime_as_hours).append('\n');

			result.append("Which means ").append(erosionFromBlock())
					.append(" will removed after every blocked placement\n");

			result.append("Placed on ").append(dateFormator.format(new Date(placed))).append('\n');
			result.append("by group ").append(reinforcement.getGroup().getName()).append('\n');
			result.append("At: ").append(location.toString());
		}
		return result.toString();
	}
	
	public String infoMessage(boolean dev, Player asking) {
		StringBuffer result = new StringBuffer(ChatColor.GREEN.toString());
		if (dev) {
			return result.append( this.toString() ).toString();
		}

		double fractionOfMaturityTime = 0;
		if (type.getWarmupTime() == 0) {
			fractionOfMaturityTime = 1;
		} else {
			fractionOfMaturityTime = ((double) (System.currentTimeMillis() - placed)) / type.getWarmupTime();
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
	
	// TODO: Test world-aware comparison
	@Override
	public int compareTo(BastionBlock other) {
		UUID thisWorld = location.getWorld().getUID();
		int thisX = location.getBlockX();
		int thisY = location.getBlockY();
		int thisZ = location.getBlockZ();

		UUID otherWorld = location.getWorld().getUID();
		int otherX = other.location.getBlockX();
		int otherY = other.location.getBlockY();
		int otherZ = other.location.getBlockZ();

		int worldCompare = thisWorld.compareTo(otherWorld);
		if (worldCompare != 0) {
			return worldCompare;
		}

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
		if (type.isDestroyOnRemove()) {
			location.getBlock().setType(Material.AIR);
		}
		close();
	}

	/**
	 * removes Bastion from world
	 */
	public void close() {
		if (!set.contains(this)) {
			Bastion.getPlugin().getLogger().warning("Tried to close already closed Bastion at " + this.location);
		}

		if (type.getErosionPerDay() != 0) {
			Bukkit.getServer().getScheduler().cancelTask(taskId);
		}
		
		set.remove(this);

		Bastion.getPlugin().getLogger().log(Level.INFO, "Removed bastion {0}. Had been placed on {1} at {2}", new Object[]{id, placed, location});
	}

	public BastionType getType() {
		return type;
	}
}
