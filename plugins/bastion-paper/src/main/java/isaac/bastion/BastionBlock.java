package isaac.bastion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionBlock implements QTBox, Comparable<BastionBlock> {	

	private Location location; 
	private int id = -1;
	private double balance = 0; //the amount remaining still to be eroded after the whole part has been removed
	private int health; //current durability
	private long placed; //time when the bastion block was created
	private BastionType type;

	/**
	 * constructor for blocks loaded from database
	 * @param location
	 * @param placed
	 * @param balance
	 * @param ID
	 */
	public BastionBlock(Location location, long placed, double balance, int ID, BastionType type) {
		this.id = ID;
		this.location = location;
		this.type = type;

		this.placed = placed;
		this.balance = balance;

		PlayerReinforcement reinforcement = getReinforcement();
		if (reinforcement != null) {
			this.health = reinforcement.getDurability();
		} else{
			this.health = 0;
			destroy();
			Bastion.getPlugin().severe("Reinforcement removed during BastionBlock instantiation, removing");
		}

	}

	/**
	 * @return The amount to erode from the bastion for a block place
	 */
	public double getErosionFromBlock() {
		double scaleStart = type.getStartScaleFactor();
		double scaleEnd = type.getFinalScaleFactor();
		
		long time = System.currentTimeMillis() - placed;
		
		if(type.getWarmupTime() == 0) {
			return scaleStart;
		} else if(time < type.getWarmupTime()) {
			return (((scaleEnd - scaleStart) / (float) type.getWarmupTime()) * time + scaleStart);
		} else {
			return scaleEnd;
		}
	}

	/**
	 * @return The amount to erode from the bastion for a pearl
	 */
	public double getErosionFromPearl() {
		return getErosionFromBlock() * type.getPearlScaleFactor();
	}

	/**
	 * @return The amount to erode from the bastion for an elytra collision
	 */
	public double getErosionFromElytra() {
		return getErosionFromBlock() * type.getElytraScale();
	}
	
	/**
	 * Checks if a location is inside this bastion field.
	 * 
	 * @param loc The location to check
	 * @return true if in field
	 */
	public boolean inField(Location loc) {
		return !(yLevelCheck(loc) || // don't add parens if you don't know why it wasn't there.
				(type.isSquare() &&
					(Math.abs(loc.getBlockX() - location.getBlockX()) > type.getEffectRadius() ||
					Math.abs(loc.getBlockZ() - location.getBlockZ()) > type.getEffectRadius() ) ) ||
				(!type.isSquare() &&
					((loc.getBlockX() - location.getX()) * (float)(loc.getBlockX() - location.getX()) + 
					(loc.getBlockZ() - location.getZ()) * (float)(loc.getBlockZ() - location.getZ()) >= type.getRadiusSquared() ) )
				);
	}
	
	public boolean yLevelCheck(Location loc) {
		if(type.isIncludeY()) {
			return loc.getBlockY() < location.getBlockY();
		}
		return loc.getBlockY() <= location.getBlockY();
	}

	/**
	 * checks if a player would be allowed to remove the Bastion block
	 * @param player The player to check
	 * @return true if the player can remove the bastion
	 */
	public boolean canRemove(Player player){

		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){
			return reinforcement.canBypass(player); //should return true if founder or moderator, but I feel this is more consistant
		}
		return true;
	}
	
	/**
	 * Check if a player is allowed to pearl in the bastion field
	 * @param player The player to check
	 * @return true if the player can pearl within the bastion
	 */
	public boolean canPearl(Player player) {
		PlayerReinforcement reinforcement = getReinforcement();

		if(reinforcement!=null){
			return NameAPI.getGroupManager().hasAccess(reinforcement.getGroup(), player.getUniqueId(), PermissionType.getPermission("BASTION_PEARL"));
		}
		return true;
	}

	/**
	 * Check if a player is allowed to place blocks in the bastion field
	 * @param player The player to check
	 * @return true if the player can place blocks within the bastion
	 */
	public boolean canPlace(Player player) {
		PlayerReinforcement reinforcement = getReinforcement();

		if (reinforcement == null) return true;
		if (player == null) return false;

		return NameAPI.getGroupManager().hasAccess(reinforcement.getGroup(), player.getUniqueId(), PermissionType.getPermission("BASTION_PLACE"));
	}
	
	/**
	 * Checks if a player has a permission for the bastion's group
	 * @param player The player to check
	 * @param perm The permission to check
	 * @return true if the player has the permission
	 */
	public boolean permAccess(Player player, PermissionType perm) {
		PlayerReinforcement rein = getReinforcement();
		if (rein == null) {
			return true;
		}
		return NameAPI.getGroupManager().hasAccess(rein.getGroup(), player.getUniqueId(), perm);
	}

	/**
	 * Checks if any of the players in the bastion field
	 * This is used mostly for checking if a dispenser can place things
	 * @param players the players to check
	 * @return true if any of the players can place blocks in this bastion
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
	 * @return true if the Bastion's strength is at zero and it should be removed
	 */
	public boolean shouldCull() {
		return (health - balance) <= 0;
	}
	
	/**
	 * Regenerates one hp on the bastion up to the cap based on reinforcement
	 */
	public void regen() {
		Reinforcement reinf = getReinforcement();
		if(reinf != null) {
			if(reinf instanceof PlayerReinforcement) {
				PlayerReinforcement pr = (PlayerReinforcement) reinf;
				int maxHealth = ReinforcementType.getReinforcementType(pr.getStackRepresentation()).getHitPoints();
				reinf.setDurability(Math.min(health + 1, maxHealth));
			} else {
				destroy();
				Bastion.getPlugin().severe("Reinforcement removed without removing bastion, fixed");
			}
		}
	}

	/**
	 * removes a set amount of durability from the reinforcement
	 * @param amount The amount to remove
	 */
	public void erode(double amount) {
		double toBeRemoved = balance + amount;

		int wholeToRemove = (int) toBeRemoved;
		double fractionToRemove = (double) toBeRemoved - wholeToRemove;

		Reinforcement reinforcement = getReinforcement();

		if (reinforcement != null) {
			health = reinforcement.getDurability();
		} else {
			return;
		}

		health -= wholeToRemove;
		balance = fractionToRemove;
		
		if (health <= 0) health = 0;

		reinforcement.setDurability(health);

		if (shouldCull()) {
			destroy();
			Bastion.getPlugin().severe("Reinforcement destroyed, removing bastion");
		} else {
			Bastion.getBastionStorage().updated(this);
		}
	}

	/**
	 * Instantly matures the bastion
	 */
	public void mature() {
		placed -= type.getWarmupTime();
		Bastion.getBastionStorage().updated(this);
	}
	
	/**
	 * Checks if the bastion is mature
	 * @return true if the bastion is mature
	 */
	public boolean isMature() {
		return System.currentTimeMillis() - placed >= type.getWarmupTime();
	}

	/**
	 * Gets the reinforcement for this bastion
	 * @return The reinforcement for this bastion
	 */
	private PlayerReinforcement getReinforcement() {
		Reinforcement reinforcement = Citadel.getReinforcementManager().
				getReinforcement(location.getBlock());
		if(reinforcement != null && reinforcement instanceof PlayerReinforcement) {
			return (PlayerReinforcement) reinforcement;
		} else {
			destroy();
			Bastion.getPlugin().severe("Reinforcement removed without removing bastion, fixed");
		}
		return null;
	}

	/**
	 * Gets the owner of the bastion's group
	 */
	public UUID getOwner() {
		return getReinforcement().getGroup().getOwner();
	}

	/**
	 * Gets the location of the bastion
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * Gets the bastion's ID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the bastion's type
	 */
	public BastionType getType() {
		return type;
	}
	
	/**
	 * Gets how long the bastion has been in the world in ms
	 */
	public long getPlaced() {
		return placed;
	}
	
	/**
	 * Gets the remaining fraction of health that still needs to be removed
	 */
	public double getBalance() {
		return balance;
	}
	
	/**
	 * Sets the id of this bastion
	 */
	public void setId(int id) {
		this.id = id;
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
			health = reinforcement.getDurability();

			result.append("Current Bastion reinforcement: ")
					.append((double) health-balance).append('\n');

			result.append("Maturity time is ")
					.append(scaleTime_as_hours).append('\n');

			result.append("Which means ").append(getErosionFromBlock())
					.append(" will removed after every blocked placement\n");

			result.append("Placed on ").append(dateFormator.format(new Date(placed))).append('\n');
			result.append("by group ").append(reinforcement.getGroup().getName()).append('\n');
			result.append("At: ").append(location.toString());
		}
		return result.toString();
	}
	
	/**
	 * Creates an info message, adds more info if dev is true
	 * @param dev If the player is a dev
	 * @return An info message
	 */
	public String infoMessage(boolean dev) {
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
			Bastion.getBastionStorage().deleteBastion(this);
		} else {
			Bastion.getBastionStorage().setBastionAsDead(this);
		}
	}

}
