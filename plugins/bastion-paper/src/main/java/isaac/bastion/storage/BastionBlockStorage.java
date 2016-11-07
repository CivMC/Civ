package isaac.bastion.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.event.BastionCreateEvent;
import isaac.bastion.manager.EnderPearlManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.civmodcore.locations.SparseQuadTree;

public class BastionBlockStorage {
	
	private ManagedDatasource db;
	private Logger log;
	
	private Map<World, SparseQuadTree> blocks;
	private Set<BastionBlock> changed;
	private Set<BastionBlock> bastions;
	private Map<Location, String> dead;
	private int taskId;
	
	private HashMap<Location, BastionType> pendingBastions;
	
	private static final String addBastion = "insert into bastion_blocks (bastion_type, loc_x, loc_y, loc_z, loc_world, placed, fraction) values (?,?,?,?,?,?,?);";
	private static final String updateBastion = "update bastion_blocks set placed=?,fraction=? where bastion_id=?;";
	private static final String deleteBastion = "delete from bastion_blocks where bastion_id=?;";
	private static final String setDead = "update bastion_blocks set dead=1 where bastion_id=?;";
	private static final String deleteDead = "delete from bastion_blocks where loc_world=? and loc_x=? and loc_y=? and loc_z=?;";
	private static final String moveDead = "update bastion_blocks set loc_world=?, loc_x=?, loc_y=?, loc_z=? where loc_world=? and loc_x=? and loc_y=? and loc_z=?;";
	
	public BastionBlockStorage(ManagedDatasource db, Logger log) {
		blocks = new HashMap<World, SparseQuadTree>();
		changed = new TreeSet<BastionBlock>();
		bastions = new TreeSet<BastionBlock>();
		dead = new HashMap<Location, String>();
		pendingBastions = new HashMap<Location, BastionType>();
		this.db = db;
		this.log = log;
		long saveDelay = 86400000 / Bastion.getPlugin().getConfig().getLong("mysql.savesPerDay", 64);
		taskId = new BukkitRunnable(){
			public void run(){
				update();
			}
		}.runTaskTimer(Bastion.getPlugin(),saveDelay,saveDelay).getTaskId();
	}
	
	/**
	 * Registers database migrations
	 */
	public void registerMigrations() {
		db.registerMigration(0, false, 
				"create table if not exists `bastion_blocks`("
				+ "bastion_id int(10) unsigned NOT NULL AUTO_INCREMENT,"
				+ "bastion_type varchar(40) DEFAULT '" + BastionType.getDefaultType() + "',"
				+ "loc_x int(10),"
				+ "loc_y int(10),"
				+ "loc_z int(10),"
				+ "loc_world varchar(40) NOT NULL,"
				+ "placed bigint(20) Unsigned,"
				+ "fraction float(20) Unsigned,"
				+ "PRIMARY KEY (`bastion_id`));");
		db.registerMigration(1, false, 
				"ALTER TABLE bastion_blocks ADD COLUMN IF NOT EXISTS bastion_type VARCHAR(40) DEFAULT '"
				+ BastionType.getDefaultType() + "';");	
		db.registerMigration(2, false, 
				"ALTER TABLE bastion_blocks ADD COLUMN IF NOT EXISTS dead TINYINT(1) DEFAULT 0;");
	}
	
	/**
	 * Updates all remaining bastions and cancels the update task
	 */
	public void close() {
		Bukkit.getScheduler().cancelTask(taskId);
		update();
	}
	
	/**
	 * Creates a new bastion and adds it to the database
	 * @param loc The location of the bastion
	 * @param type The type of bastion
	 * @return Whether or not the bastion was created successfully
	 */
	public boolean createBastion(Location loc, BastionType type, Player owner) {
		long placed = System.currentTimeMillis();
		BastionBlock bastion = new BastionBlock(loc, placed, 0, -1, type);
		BastionCreateEvent event = new BastionCreateEvent(bastion, owner);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return false;
		try(Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(addBastion, Statement.RETURN_GENERATED_KEYS);) {
			ps.setString(1, type.getName());
			ps.setInt(2, loc.getBlockX());
			ps.setInt(3, loc.getBlockY());
			ps.setInt(4, loc.getBlockZ());
			ps.setString(5, loc.getWorld().getName());
			ps.setLong(6, placed);
			ps.setDouble(7, 0);
			ps.executeUpdate();
			int id = -1;
			try (ResultSet nid = ps.getGeneratedKeys();) {
				if (nid.next()) {
					id = nid.getInt(1);
				}
			}
			if (id < 0) {
				log.log(Level.WARNING, "Failed to get ID of bastion during insert at {0}, rolling back", loc);
				dead.put(loc, bastion.getType().getName());
				deleteDeadBastion(loc);
				return false;
			}
			bastion.setId(id);
			bastions.add(bastion);
			blocks.get(loc.getWorld()).add(bastion);
		} catch (SQLException e) {
			log.log(Level.WARNING, "Problem saving bastion at " + bastion.getLocation().toString(), e);
			return false;
		}
		return true;
	}
	
	/**
	 * Deletes a bastion from the database
	 * @param bastion The bastion to delete
	 */
	public void deleteBastion(BastionBlock bastion) {
		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(deleteBastion)) {
			ps.setInt(1, bastion.getId());
			ps.executeUpdate();
			try {
 				bastions.remove(bastion);
 				blocks.get(bastion.getLocation().getWorld()).remove(bastion);
 			} catch (NullPointerException npe) {
 				log.log(Level.WARNING, "Bastion wasn't in cache, or failed to remove from cache: " + bastion.getLocation().toString(), npe);
 			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to delete a bastion at " + bastion.getLocation().toString(), e);
		}
	}
	
	/**
	 * Sets a bastion as dead but still in the world
	 * @param bastion The bastion that died
	 */
	public void setBastionAsDead(BastionBlock bastion) {
		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(setDead)) {
			ps.setInt(1, bastion.getId());
			ps.executeUpdate();
			bastions.remove(bastion);
			blocks.get(bastion.getLocation().getWorld()).remove(bastion);
			dead.put(bastion.getLocation(), bastion.getType().getName());
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to set bastion as dead at " + bastion.getLocation().toString(), e);
		}
	}
	
	public void deleteDeadBastion(Location loc) {
		if(!dead.containsKey(loc)) return;
		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(deleteDead)) {
			ps.setString(1, loc.getWorld().getName());
			ps.setInt(2, loc.getBlockX());
			ps.setInt(3, loc.getBlockY());
			ps.setInt(4, loc.getBlockZ());
			ps.executeUpdate();
			dead.remove(loc);
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to delete dead bastion at " + loc.toString(), e);
		}
	}
	
	public void moveDeadBastion(Location from, Location to) {
		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(moveDead)) {
			ps.setString(1, to.getWorld().getName());
			ps.setInt(2, to.getBlockX());
			ps.setInt(3, to.getBlockY());
			ps.setInt(4, to.getBlockZ());
			ps.setString(5, from.getWorld().getName());
			ps.setInt(6, from.getBlockX());
			ps.setInt(7, from.getBlockY());
			ps.setInt(8, from.getBlockZ());
			ps.executeUpdate();
			dead.put(to, dead.remove(from));
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to move bastion at " + from.toString() + " to " + to.toString(), e);
		}
	}

	/**
	 * Updates a bastion in the database
	 * @param bastion The bastion to update
	 */
	private void updateBastion(BastionBlock bastion) {
		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(updateBastion)) {
			ps.setLong(1, bastion.getPlaced());
			ps.setDouble(2, bastion.getBalance());
			ps.setInt(3, bastion.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to update bastion at " + bastion.getLocation().toString(), e);
		}
	}
	
	/**
	 * Adds this bastion to a set of bastions to be updated
	 * @param bastion The bastion to be updated
	 */
	public void updated(BastionBlock bastion) {
		changed.add(bastion);
	}
	
	/**
	 * Find bastions that intersect a location
	 * @param loc The location to check
	 * @return A set of QTBoxes (bastions) that overlap with the location
	 */
	public Set<QTBox> forLocation(Location loc) {
		return blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ());
	}
	
	/**
	 * Retrieve possible blocking bastions for a thrown pearl
	 * @param loc The location of the pearl
	 * @param maxDistance The max distance the pearl could be from a bastion without collision
	 * @return A set of bastions a pearl could collide with
	 */
	public Set<BastionBlock> getPossibleTeleportBlocking(Location loc, double maxDistance) {
		Set<QTBox> boxes = blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockY(), true);
		
		double maxDistanceSquared = maxDistance * maxDistance;
		double maxBoxDistanceSquared = maxDistanceSquared * 2.0;
		
		Set<BastionBlock> result = new TreeSet<BastionBlock>();
		
		for(QTBox box : boxes) {
			if(box instanceof BastionBlock) {
				BastionBlock bastion = (BastionBlock) box;
				BastionType type = bastion.getType();
				// Skip bastions who don't do midair blocking.
				if (!type.isBlockPearls() || !type.isBlockMidair()) continue;
				// Check on other conditions.
				if (((type.isSquare() && bastion.getLocation().distanceSquared(loc) <= maxBoxDistanceSquared) ||   
						(!type.isSquare() && bastion.getLocation().distanceSquared(loc) <= maxDistanceSquared)) &&
						(!type.isRequireMaturity() || bastion.isMature())) {
					result.add(bastion);
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieve possible blocking bastions for elytra flight
	 * @param maxDistance The max distance you could be without collision
	 * @param locs The locations the player is at while flying
	 * @return A set of bastions a flying player could collide with
	 */
	public Set<BastionBlock> getPossibleFlightBlocking(double maxDistance, Location...locs) {
		Set<QTBox> boxes = null;
		Set<BastionBlock> result = new TreeSet<BastionBlock>();		
		double maxDistanceSquared = maxDistance * maxDistance;
		double maxBoxDistanceSquared = maxDistanceSquared * 2.0;
		
		for (Location loc: locs) {
			boxes = blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ(), true);
			
			for (QTBox box : boxes) {
				if (box instanceof BastionBlock) {
					BastionBlock bastion = (BastionBlock)box;
					BastionType type = bastion.getType();
					// Don't add bastions that don't block flight
					if (!type.isBlockElytra()) continue;
					// Fixed for square field nearness, using diagonal distance as max -- (radius * sqrt(2)) ^ 2
					if (((type.isSquare() && bastion.getLocation().distanceSquared(loc) <= maxBoxDistanceSquared) ||   
								(!type.isSquare() && bastion.getLocation().distanceSquared(loc) <= maxDistanceSquared)) &&
								(!type.isElytraRequireMature() || bastion.isMature())) {
						result.add(bastion);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieves a bastion at a given location
	 * @param loc Location of the bastion block
	 * @return The bastion block, can be null
	 */
	public BastionBlock getBastionBlock(Location loc) {
		Set<? extends QTBox> possible = forLocation(loc);
		for(QTBox box : possible) {
			BastionBlock bastion = (BastionBlock) box;
			if(bastion.getLocation().equals(loc)) {
				return bastion;
			}
		}
		return null;
	}
	
	/**
	 * Gets the type of the bastion, dead or alive, at a location
	 * @param loc Location of the bastion
	 * @return The type of bastion, can be null
	 */
	public BastionType getTypeAtLocation(Location loc) {
		BastionBlock bastion = getBastionBlock(loc);
		if(bastion != null) {
			return bastion.getType();
		}
		String type = dead.get(loc);
		if(type != null) {
			return BastionType.getBastionType(type);
		}
		return null;
	}
	
	/**
	 * Loads all bastions from the database
	 */
	@SuppressWarnings("deprecation")
	public void loadBastions() {
		int enderSearchRadius = EnderPearlManager.MAX_TELEPORT + 100;
		for(World world : Bukkit.getWorlds()) {
			SparseQuadTree bastionsForWorld = new SparseQuadTree(enderSearchRadius);
			blocks.put(world, bastionsForWorld);
			try (Connection conn = db.getConnection();
					PreparedStatement ps = conn.prepareStatement("select * from bastion_blocks where loc_world=?;")) {
				ps.setString(1, world.getName());
				ResultSet result = ps.executeQuery();
				while(result.next()) {
					int x = result.getInt("loc_x");
					int y = result.getInt("loc_y");
					int z = result.getInt("loc_z");
					int id = result.getInt("bastion_id");
					long placed = result.getLong("placed");
					double balance = result.getDouble("fraction");
					BastionType type = BastionType.getBastionType(result.getString("bastion_type"));
					Location loc = new Location(world, x, y, z);
					BastionBlock block = new BastionBlock(loc, placed, balance, id, type);
					//Check if it's a ghost bastion, if so remove from the db
					if(loc.getBlock().getType() != type.getMaterial().getItemType() 
							|| loc.getBlock().getData() != type.getMaterial().getData()) {
						deleteBastion(block);
						continue;
					}
					bastions.add(block);
					bastionsForWorld.add(block);
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Error loading bastions from database, shutting down", e);
				Bukkit.getServer().getPluginManager().disablePlugin(Bastion.getPlugin());
			}
		}
	}
	
	private void update() {
		int count = changed.size();
		for(BastionBlock block : changed) {
			updateBastion(block);
		}
		changed.clear();
		log.info("Updated " + count + " bastions");
	}

	/**
	 * Gets a set of all bastion blocks across all worlds
	 * @return All the bastion blocks
	 */
	public Set<BastionBlock> getAllBastions() {
		return bastions;
	}
	
	/**
	 * Gets all bastions of a certain type
	 * @param type The type of bastion you want
	 * @return A set of bastions of that type
	 */
	public Set<BastionBlock> getBastionsForType(BastionType type) {
		Set<BastionBlock> forType = new HashSet<BastionBlock>();
		for(BastionBlock bastion : bastions) {
			if(bastion.getType().equals(type)) {
				forType.add(bastion);
			}
		}
		return forType;
	}
	
	/**
	 * Allows the Break Listener to properly handle unreinforced bastions, at least
	 * until restart.
	 * 
	 * TODO: Add persistence of "pending" bastions.
	 * 
	 * @param loc The location to check for a pending bastion
	 * @return True if a bastion was pending there.
	 */
	public boolean isPendingBastion(Location loc) {
		return pendingBastions.containsKey(loc);
	}
	
	/**
	 * Remove from pending and return the type removed.
	 * Used by external break handlers.
	 * 
	 * @param loc The location to get and return a pending bastion
	 * @return The type of the bastion that was pended.
	 */
	public BastionType getAndRemovePendingBastion(Location loc) {
		return pendingBastions.remove(loc);
	}
	
	/**
	 * Get from pending and return the type.
	 * Used by external break handlers.
	 * 
	 * @param loc The location to get and return a pending bastion
	 * @return The type of the bastion that is pended.
	 */
	public BastionType getPendingBastion(Location loc) {
		return pendingBastions.get(loc);
	}
	
	/**
	 * Allows the Interact listener to register that a bastion block has been 
	 * placed but is of yet unreinforced and not persisted by the database.
	 * 
	 * TODO: Add persistence.
	 * 
	 * @param loc The location to record a pended bastion
	 * @param type The type of the bastion pended
	 */
	public void addPendingBastion(Location loc, BastionType type) {
		pendingBastions.put(loc, type);
	}
}
