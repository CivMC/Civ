package isaac.bastion.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.events.BastionCreateEvent;
import isaac.bastion.manager.EnderPearlManager;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.civmodcore.locations.SparseQuadTree;

public class BastionBlockStorage {
	
	private ManagedDatasource db;
	private Logger log;
	
	private Map<World, SparseQuadTree> blocks;
	private Set<BastionBlock> changed;
	private Set<BastionBlock> bastions;
	private int taskId;
	private int ID = -1;
	
	private static final String addBastion = "insert into bastion_blocks (bastion_type, loc_x, loc_y, loc_z, loc_world, placed, balance) values (?,?,?,?,?,?,?);";
	private static final String updateBastion = "update bastion_blocks set placed=?,balance=? where bastion_id=?;";
	private static final String deleteBastion = "delete from bastion_blocks where bastion_id=?;";
	
	public BastionBlockStorage(ManagedDatasource db, Logger log) {
		this.db = db;
		this.log = log;
		long saveDelay = 86400000 / Bastion.getPlugin().getConfig().getLong("sql.savesPerDay", 64);
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
				+ "bastion_type varchar(40) NOT NULL DEFAULT " + BastionType.getDefaultType()
				+ "loc_x int(10),"
				+ "loc_y int(10),"
				+ "loc_z int(10),"
				+ "loc_world varchar(40) NOT NULL,"
				+ "placed bigint(20) Unsigned,"
				+ "fraction float(20) Unsigned,"
				+ "PRIMARY_KEY (`bastion_id`));");
		db.registerMigration(1, true, 
				"ALTER TABLE bastion_blocks ADD COLUMN IF NOT EXISTS bastion_type VARCHAR(40) DEAULT '"
				+ BastionType.getDefaultType() + "';");		
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
	public boolean createBastion(Location loc, BastionType type) {
		long placed = System.currentTimeMillis();
		BastionBlock bastion = new BastionBlock(loc, placed, 0, ++ID, type);
		BastionCreateEvent event = new BastionCreateEvent(bastion, Bukkit.getPlayer(bastion.getOwner()));
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return false;
		try(Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(addBastion)) {
			ps.setString(1, type.getName());
			ps.setInt(1, loc.getBlockX());
			ps.setInt(2, loc.getBlockY());
			ps.setInt(3, loc.getBlockZ());
			ps.setString(2, loc.getWorld().getName());
			ps.setLong(1, placed);
			ps.setDouble(1, 0);
			ps.setInt(1, ID);
			ps.executeUpdate();
			bastions.add(bastion);
			blocks.get(loc.getWorld()).add(bastion);
		} catch (SQLException e) {
			log.log(Level.WARNING, "Problem saving bastion at " + bastion.getLocation().toString(), e);
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
			bastions.remove(bastion);
			blocks.get(bastion.getLocation().getWorld()).remove(bastion);
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to delete a bastion at " + bastion.getLocation().toString(), e);
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
			ps.setDouble(1, bastion.getBalance());
			ps.setInt(1, bastion.getId());
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
	 * Loads all bastions from the database
	 */
	@SuppressWarnings("deprecation")
	public void loadBastions() {
		int enderSearchRadius = EnderPearlManager.MAX_TELEPORT + 100;
		for(World world : Bukkit.getWorlds()) {
			SparseQuadTree bastionsForWorld = new SparseQuadTree(enderSearchRadius);
			try (Connection conn = db.getConnection();
					PreparedStatement ps = conn.prepareStatement("select * from bastion_blocks where world_loc=?;")) {
				ps.setString(1, world.getName());
				ResultSet result = ps.executeQuery();
				while(result.next()) {
					int x = result.getInt("loc_x");
					int y = result.getInt("loc_y");
					int z = result.getInt("loc_z");
					int id = result.getInt("bastion_id");
					if(id > ID) ID = id;
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
}
