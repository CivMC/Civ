package vg.civcraft.mc.citadel.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.reinforcement.MultiBlockReinforcement;
import vg.civcraft.mc.citadel.reinforcement.NaturalReinforcement;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class CitadelReinforcementData {

	private ManagedDatasource db;
	private Citadel plugin = Citadel.getInstance();
	private Logger logger = plugin.getLogger();

	private Map<Integer, ReinforcementNature> natures = new HashMap<Integer, ReinforcementNature>();
	private Map<ReinforcementNature, Integer> invNatures = new HashMap<ReinforcementNature, Integer>();

	private static enum ReinforcementNature {
		PLAYER_REINFORCEMENT ("PlayerReinforcement"),
		NATURAL_REINFORCEMENT ("NaturalReinforcement"),
		MULTIBLOCK_REINFORCEMENT ("MultiblockReinforcement");

		private String label;

		ReinforcementNature(String label) {
			this.label = label;
		}

		public String getLabel() {
			return this.label;
		}

		public static ReinforcementNature decode(String label) {
			if (label != null) {
				for (ReinforcementNature nature : ReinforcementNature.values()) {
					if (nature.label.equalsIgnoreCase(label)) {
						return nature;
					}
				}
			}
			throw new IllegalArgumentException("No ReinforcementNature matching " + label);
		}
	}

	private static final String getRein =
			"SELECT material_id, durability, insecure, maturation_time, acid_time, rein_type_id, lore, group_id, rein_id "
				+ "FROM reinforcement WHERE x = ? and y = ? and z = ? and world = ?";
	private static final String getReins =
			"SELECT x, y, z, material_id, durability, insecure, maturation_time, acid_time, rein_type_id, lore, group_id, rein_id "
				+ "FROM reinforcement WHERE chunk_x = ? and chunk_z = ? and world = ?;";
	private static final String addRein =
			"INSERT INTO reinforcement (x, y, z, world, material_id, durability, insecure, maturation_time, acid_time, rein_type_id, lore, group_id, chunk_x, chunk_z) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); ";
	private static final String removeRein = "DELETE from reinforcement WHERE x = ? and y = ? and z = ? and world = ?";
	private static final String updateRein =
			"UPDATE reinforcement SET durability = ?, insecure = ?, group_id = ?, maturation_time = ?, acid_time = ? "
				+ "WHERE x = ? and y = ? and z = ? and world = ?";
	private static final String getNatures = "SELECT rein_type_id, rein_type FROM reinforcement_type";

	private static final String selectReinCountForGroup =
			"SELECT count(*) AS count FROM reinforcement WHERE FIND_IN_SET(CAST(group_id AS char), ?) > 0";
	private static final String selectReinCount = "SELECT count(*) AS count FROM reinforcement";

	public CitadelReinforcementData(ManagedDatasource db){
		this.db = db;

		if (!db.isManaged()) {
			// First "migration" is conversion from old system to new
			boolean isNew = true;
			try (Connection connection = db.getConnection();
					PreparedStatement checkNewInstall = connection.prepareStatement("SELECT * FROM db_version LIMIT 1;");
					// See if this was a new install. If it was, db_version statement will fail. If it isn't, it'll succeed.
					//   If the version statement fails, return true; this is new install, carryon.
					ResultSet rs = checkNewInstall.executeQuery();) {
				isNew = !rs.next();
			} catch (SQLException se) {
				logger.log(Level.INFO, "New installation: Welcome to Citadel!");
			}

			if (!isNew) {
				try (Connection connection = db.getConnection();
						PreparedStatement migrateInstall = connection.prepareStatement(
								"INSERT INTO managed_plugin_data (plugin_name, current_migration_number, last_migration)"
									+ " SELECT plugin_name, max(db_version), max(str_to_date(update_time, '%Y-%m-%d %H:%i:%s' )) FROM db_version WHERE plugin_name = ? LIMIT 1;");) {
					migrateInstall.setString(1, Citadel.getInstance().getPluginName());
					int rows = migrateInstall.executeUpdate();
					if (rows == 1) {
						logger.log(Level.INFO, "Migration successful!");
					} else {
						Bukkit.shutdown();
						logger.log(Level.SEVERE, "Migration failed; db_version exists but uncaptured. Could be version problem.");
						return;
					}
				} catch (SQLException se) {
					Bukkit.shutdown();
					// Migration failed...
					logger.log(Level.SEVERE, "Migration failure!", se);
					return;
				}
			}
		} else {
			logger.log(Level.INFO, "Still at it, eh?");
		}

		doPrepareNatures();
	}

	private void doPrepareNatures() {
		try (Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement(CitadelReinforcementData.getNatures);
				ResultSet set = statement.executeQuery();) {
			while (set.next()) {
				int id = set.getInt(1);
				String type = set.getString(2);
				try {
					ReinforcementNature nature = ReinforcementNature.decode(type);
					natures.put(id, nature);
					invNatures.put(nature, id);
				} catch (IllegalArgumentException iae) {
					logger.log(Level.WARNING, "Note that although configured, {0} is not a recognized Reinforcement Nature", type);
				}
			}

			if (natures.size() == 0) {
				logger.log(Level.SEVERE, "Failed to find any reinforcement types, shutting down.");
				Bukkit.shutdown();
			}
		} catch (SQLException se) {
			logger.log(Level.SEVERE, "Failed to load the reinforcement types, shutting down.");
			Bukkit.shutdown();
		}
	}

	public void registerMigrations() {
		db.registerMigration(6, false,
				new Callable<Boolean> () {
					@Override
					public Boolean call() throws Exception {
						try (Connection connection = db.getConnection();
								PreparedStatement dotypes = connection.prepareStatement("insert into reinforcement_type(rein_type) values (?);")) {
							for (ReinforcementNature x: ReinforcementNature.values()) {
								dotypes.setString(1, x.getLabel());
								dotypes.addBatch();
							}
							int[] rez = dotypes.executeBatch();
							if (rez.length == ReinforcementNature.values().length) {
								return true;
							} else {
								logger.log(Level.SEVERE, "Failed to insert reinforcement types.");
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Failed to insert reinforcement types.", e);
						}
						return null;
					}
				},
				"create table if not exists reinforcement(" +
					"x int not null," +
					"y int not null," +
					"z int not null," +
					"world varchar(10) not null," +
					"material_id int not null," +
					"durability varchar(10) not null," +
					"chunk_id varchar(255) not null," +
					"insecure tinyint(1) not null," +
					"group_id int not null," +
					"maturation_time int not null," +
					"rein_type_id int not null," +
					"lore varchar(255),"
					+ "primary key (x,y,z,world));",
				"create table if not exists reinforcement_type("
					+ "rein_type_id int not null auto_increment,"
					+ "rein_type varchar(30) not null,"
					+ "primary key rein_type_key (rein_type_id));");
		db.registerMigration(7, false, "create table if not exists reinforcement_id("
					+ "rein_id int not null auto_increment,"
					+ "x int not null,"
					+ "y int not null,"
					+ "z int not null,"
					+ "chunk_id varchar(255),"
					+ "world varchar (255) not null,"
					+ "primary key rein_id_key (rein_id),"
					+ "unique key x_y_z_world(x,y,z,world));-- Your mother is a whore and sleeps with banjos",
					// I like turtles mother fucker. Never program because then you get turtles.
				"insert into reinforcement_id (x, y, z, chunk_id, world) select x, y, z, chunk_id, world from reinforcement;", // populate that bitch.
				"alter table reinforcement add rein_id int not null, drop chunk_id;",
				"update reinforcement r inner join reinforcement_id ri on "
					+ "ri.x = r.x and ri.y = r.y and ri.z = r.z and ri.world = r.world "
					+ "set r.rein_id = ri.rein_id",
				"alter table reinforcement DROP PRIMARY KEY, "
					+ "add primary key rein_id_key(rein_id), "
					+ "drop x,"
					+ "drop y,"
					+ "drop z,"
					+ "drop world;",
				"alter table reinforcement_id add index `chunk_id_index` (chunk_id);");
		db.registerMigration(8, false, "alter table reinforcement_id drop primary key,"
					+ " add primary key (rein_id, x, y, z, world);");
		db.registerMigration(9, false, "alter table reinforcement add acid_time int not null;",
				"update reinforcement set acid_time = maturation_time;"); // Might take a minute.
		db.registerMigration(10, false, "drop procedure if exists insertReinID;",
				"create definer=current_user procedure insertReinID("
					+ "in x int,"
					+ "in y int,"
					+ "in z int,"
					+ "in chunk_id varchar(255),"
					+ "in world varchar(255)"
					+ ") sql security invoker begin "
					+ "insert into reinforcement_id(x, y, z, chunk_id, world) values (x, y, z, chunk_id, world);"
					+ "select LAST_INSERT_ID() as id;"
					+ "end;",
				"drop procedure if exists insertCustomReinID;",
				"create definer=current_user procedure insertCustomReinID("
					+ "in rein_id int,"
					+ "in x int,"
					+ "in y int,"
					+ "in z int,"
					+ "in chunk_id varchar(255),"
					+ "in world varchar(255)"
					+ ") sql security invoker begin "
					+ "insert into reinforcement_id(rein_id, x, y, z, chunk_id, world) values (rein_id, x, y, z, chunk_id, world);"
					+ "select LAST_INSERT_ID() as id;"
					+ "end;");
		db.registerMigration(11, false, "drop procedure if exists insertRein;",
				"create definer=current_user procedure insertRein("
					+ "in x int,"
					+ "in y int,"
					+ "in z int,"
					+ "in chunk_id varchar(255),"
					+ "in world varchar(255),"
					+ "in material_id int,"
					+ "in durability varchar(10),"
					+ "in insecure tinyint(1),"
					+ "in group_id int,"
					+ "in maturation_time int,"
					+ "in lore varchar(255),"
					+ "in acid_time int,"
					+ "in rein_type varchar(30)"
					+ ") sql security invoker begin "
					+ "insert into reinforcement_id(x, y, z, chunk_id, world) values (x, y, z, chunk_id, world);"
					+ "insert into reinforcement ("
					+ "material_id, durability, insecure, group_id, maturation_time, rein_type_id, lore, rein_id, acid_time) VALUES ("
					+ "material_id, durability, insecure, group_id, maturation_time, "
					+ "(SELECT rt.rein_type_id FROM reinforcement_type rt where rt.rein_type = rein_type LIMIT 1), "
					+ "lore, (select LAST_INSERT_ID()), acid_time);"
					+ "end;");
		db.registerMigration(12, false,
				"CREATE TABLE reinforcement_temp ("
					+ "rein_id int not null auto_increment,"
					+ "x int not null,"
					+ "y int not null,"
					+ "z int not null,"
					+ "chunk_x int not null,"
					+ "chunk_z int not null,"
					+ "world varchar(255) not null,"
					+ "material_id int not null,"
					+ "durability varchar(10) not null,"
					+ "insecure tinyint(1) not null,"
					+ "group_id int not null,"
					+ "maturation_time int not null,"
					+ "rein_type_id int not null,"
					+ "lore varchar(255),"
					+ "acid_time int not null,"
					+ "primary key rid (rein_id),"
					+ "unique index realcoord (x,y,z,world),"
					+ "index chunkcoord(chunk_x, chunk_z, world)"
					+ ");",
				"INSERT IGNORE INTO reinforcement_temp SELECT a.rein_id, x, y, z, floor(x/16), floor(z/16), world, "
					+ "material_id, durability, insecure, group_id, maturation_time, rein_type_id, lore, acid_time "
					+ "FROM reinforcement_id a JOIN reinforcement b ON a.rein_id = b.rein_id;",
				"RENAME TABLE reinforcement_id TO deprecated_reinforcement_id;",
				"RENAME TABLE reinforcement TO deprecated_reinforcement;",
				"RENAME TABLE reinforcement_temp TO reinforcement;",
				"DROP PROCEDURE IF EXISTS insertReinID;",
				"DROP PROCEDURE IF EXISTS insertCustomReinID;",
				"DROP PROCEDURE IF EXISTS insertRein;"
				);
	}


	/**
	 * Is used to grab the reinforcement from the mysql db.
	 * If there isn't a reinforcement at the location it
	 * returns null.
	 * @param The Location of the wanted Reinforcement.
	 * @return Returns the Reinforcement of the location.
	 * @return Returns null if there is no reinforcement.
	 */
	public Reinforcement getReinforcement(Location loc){
		if (loc == null){
			logger.log(Level.WARNING, "CitadelReinforcementData getReinforcement called with null");
			return null;
		}

		try(Connection connection = db.getConnection();
				PreparedStatement getRein = connection.prepareStatement(CitadelReinforcementData.getRein);) {
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			getRein.setInt(1, x);
			getRein.setInt(2, y);
			getRein.setInt(3, z);
			getRein.setString(4, loc.getWorld().getName());
			ResultSet set = getRein.executeQuery();
			if (!set.next()) {
				set.close();
				return null;
			}
			@SuppressWarnings("deprecation")
			Material mat = Material.getMaterial(set.getInt(1));
			int durability = set.getInt(2);
			boolean inSecure = set.getBoolean(3);
			int mature = set.getInt(4);
			int acid = set.getInt(5);
			int rein_type_id = set.getInt(6);
			ReinforcementNature rein_type = natures.get(rein_type_id);
			String lore = set.getString(7);
			int group_id = set.getInt(8);
			//int linked_rein_id = set.getInt(9);
			set.close();
			switch(rein_type) {
			case PLAYER_REINFORCEMENT:
				ItemStack stack = new ItemStack(mat);
				if (lore != null) {
					ItemMeta meta = stack.getItemMeta();
					List<String> array = Arrays.asList(lore.split("\n"));
					meta.setLore(array);
					stack.setItemMeta(meta);
				}
				Group g = GroupManager.getGroup(group_id);
				if (g == null) {
					if (CitadelConfigManager.shouldLogReinforcement()) {
						logger.log(Level.WARNING,
								"Player Reinforcement at {0} lacks a valid group (group {1} failed lookup)",
								new Object[] {loc, group_id});
					}
					return null; // group not found!
				}
				PlayerReinforcement rein = new PlayerReinforcement(loc, durability, mature, acid, g, stack);
				rein.setInsecure(inSecure);
				return rein;
			case NATURAL_REINFORCEMENT:
				NaturalReinforcement nrein = new NaturalReinforcement(loc.getBlock(), durability);
				return nrein;
			case MULTIBLOCK_REINFORCEMENT:
				logger.log(Level.WARNING, "Multiblock reinforcements not currently supported");
				return null;
			default:
				logger.log(Level.SEVERE, "Unknown reinforcement type in database: {0}", rein_type_id);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed while retrieving reinforcement from database", e);
		}
		if (CitadelConfigManager.shouldLogInternal()) {
			logger.log(Level.WARNING, "CitadelReinforcementData getReinforcement failed for {0}", loc);
		}
		return null;
	}

	/**
	 * Returns a list of reinforcements in a given chunk.
	 * @param The chunk you want the reinforcements about.
	 * @return A list of reinforcements in a chunk
	 */
	public List<Reinforcement> getReinforcements(Chunk chunk){
		if (chunk == null){
			logger.log(Level.WARNING, "CitadelReinforcementData getReinforcements called with null");
			return null;
		}

		List<Reinforcement> reins = new ArrayList<Reinforcement>();
		int cx = chunk.getX();
		int cz = chunk.getZ();
		String world = chunk.getWorld().getName();

		try (Connection connection = db.getConnection();
				PreparedStatement getReins = connection.prepareStatement(CitadelReinforcementData.getReins);) {
			if (CitadelConfigManager.shouldLogInternal()) {
				logger.log(Level.WARNING,
						"CitadelReinforcementData getReinforcements chunk called for {0}", chunk);
			}
			getReins.setInt(1, cx);
			getReins.setInt(2, cz);
			getReins.setString(3, world);

			try (ResultSet set = getReins.executeQuery();) {
				while (set.next()) {
					int x = set.getInt(1);
					int y = set.getInt(2);
					int z = set.getInt(3);
					@SuppressWarnings("deprecation")
					Material mat = Material.getMaterial(set.getInt(4));
					int durability = set.getInt(5);
					boolean inSecure = set.getBoolean(6);
					int mature = set.getInt(7);
					int acid = set.getInt(8);
					int rein_type_id = set.getInt(9);
					ReinforcementNature rein_type = natures.get(rein_type_id);
					String lore = set.getString(10);
					int group_id = set.getInt(11);
					// Sketch: when doing multiblock, have an FK that self-references the same table, so multiple locations link to a single rein.
					//int linked_rein_id = set.getInt(12);
					Location loc = new Location(chunk.getWorld(), x, y, z);

					switch(rein_type) {
					case PLAYER_REINFORCEMENT:
						Group g = GroupManager.getGroup(group_id);
						if (g == null) {
							if (CitadelConfigManager.shouldLogReinforcement()) {
								logger.log(Level.WARNING,
										"During Chunk {0} load, Player Reinforcement at {1} lacks a valid group (group {2} failed lookup)",
										new Object[] {chunk, loc, group_id});
							}
							continue; // group not found!
						}
						ItemStack stack = new ItemStack(mat);
						if (lore != null){
							ItemMeta meta = stack.getItemMeta();
							List<String> array = Arrays.asList(lore.split("\n"));
							meta.setLore(array);
							stack.setItemMeta(meta);
						}
						PlayerReinforcement rein = new PlayerReinforcement(loc, durability, mature, acid, g, stack);
						rein.setInsecure(inSecure);
						reins.add(rein);
						break;
					case NATURAL_REINFORCEMENT:
						NaturalReinforcement nrein = new NaturalReinforcement(loc.getBlock(), durability);
						reins.add(nrein);
						break;
					case MULTIBLOCK_REINFORCEMENT:
						logger.log(Level.WARNING, "Multiblock reinforcements not currently supported");
						break;
					default:
						logger.log(Level.SEVERE, "Unknown reinforcement type in database: {0}", rein_type_id);
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed while retrieving chunk " + chunk.toString() + " reinforcements from database", e);
		}
		return reins;
	}

	/**
	 * Inserts a reinforcement into the Database. Should only be called from SaveManager.
	 * @param The Reinforcement to save.
	 */
	public void insertReinforcement(Reinforcement rein){
		insertReinforcement(rein, true);
	}

	/**
	 * Internal, just sets the params correctly depending on nature. Assumes a single compatible query across all natures.
	 * @param rein
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private PreparedStatement prepInsertRein(Reinforcement rein, PreparedStatement insertRein) throws SQLException {
		// universal fields.
		Location loc = rein.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		Material mat = rein.getMaterial();
		int dur = rein.getDurability();
		int maturationTime = rein.getMaturationTime();
		int acidTime = rein.getAcidTime();
		int cx = loc.getChunk().getX();
		int cz = loc.getChunk().getZ();

		// specific fields, "safe" values.
		boolean insecure = false;
		String lore = null;
		int groupId = -1;
		int type = 0;

		if (rein instanceof PlayerReinforcement) {
			type = invNatures.get(ReinforcementNature.PLAYER_REINFORCEMENT);

			PlayerReinforcement prein = (PlayerReinforcement) rein;
			insecure = prein.isInsecure();
			ItemMeta meta = prein.getStackRepresentation().getItemMeta();
			if (meta.hasLore()) {
				StringBuilder newlore = new StringBuilder();
				for (String xx: meta.getLore()) {
					newlore.append( xx ).append( "\n" );
				}
				lore = newlore.toString();
			} else {
				lore = null;
			}

			Group g = prein.getGroup();
			if (g == null) {
				logger.log(Level.WARNING, "Player Reinforcement insert at {0} lacks a valid group (lookup failed)", loc);
			}
			groupId = prein.getGroupId();
		} else if (rein instanceof NaturalReinforcement) {
			type = invNatures.get(ReinforcementNature.NATURAL_REINFORCEMENT);
		} else if (rein instanceof MultiBlockReinforcement) {
			type = invNatures.get(ReinforcementNature.MULTIBLOCK_REINFORCEMENT);
			logger.log(Level.WARNING, "A Multiblock reinforcement was prepped for insert, but is not currently supported.");
		}

		insertRein.setInt(1, x);
		insertRein.setInt(2, y);
		insertRein.setInt(3, z);
		insertRein.setString(4, world);
		insertRein.setInt(5, mat.getId());
		insertRein.setInt(6, dur);
		insertRein.setBoolean(7, insecure);
		insertRein.setInt(8, maturationTime);
		insertRein.setInt(9, acidTime);
		insertRein.setInt(10, type);
		insertRein.setString(11, lore);
		insertRein.setInt(12, groupId);
		insertRein.setInt(13, cx);
		insertRein.setInt(14, cz);
		return insertRein;
	}

	/**
	 * Use this to insert a set of Player reinforcements all at once. Note that this automatically fails over to single-insertions if the batch fails in any way.
	 *
	 * @param reins
	 */
	public void insertManyReinforcements(Collection<Reinforcement> reins) {
		if (reins == null || reins.size() == 0) return;
		boolean failover = false;
		try (Connection connection = db.getConnection();
				PreparedStatement insertRein = connection.prepareStatement(CitadelReinforcementData.addRein);) {
			int count = 0;
			for (Reinforcement rein : reins) {
				this.prepInsertRein(rein, insertRein).addBatch();
				count++;
				if (count % 100 == 0) {
					int[] done = insertRein.executeBatch();
					if (done == null || done.length == 0) {
						logger.log(Level.WARNING, "Batch insert of Player reinforcements -- 100 attempted -- appears to have failed.");
						failover = true;
						break;
					} else if (done.length == 100){
						logger.log(Level.INFO, "Inserted a batch of Player reinforcements -- 100 attempted");
					} else {
						failover = true;
						logger.log(Level.INFO, "Inserted a batch of Player reinforcements -- 100 attempted -- outcome indeterminate");
						break;
					}
				}
			}
			if (count % 100 != 0) {
				int[] done = insertRein.executeBatch();
				if (done == null || done.length == 0) {
					logger.log(Level.WARNING, "Batch insert of Player reinforcements -- {0} attempted -- appears to have failed.", (count % 100));
					failover = true;
				} else if (done.length == (count % 100)){
					logger.log(Level.INFO, "Inserted a batch of Player reinforcements -- {0} attempted", (count % 100));
				} else {
					failover = true;
					logger.log(Level.INFO, "Inserted a batch of Player reinforcements -- {0} attempted -- outcome indeterminate", (count % 100));
				}
			}
		} catch (SQLException e) {
			failover = true;
			logger.log(Level.SEVERE, "Citadel encountered a critical error while inserting a batch of reinforcements", e);
		}

		if (failover) {
			logger.log(Level.WARNING, "Citadel encountered uncertainty while inserting a batch of records. Failing over to individual insertion logic.");
			for (Reinforcement rein : reins) {
				insertReinforcement(rein, true);
			}
		}
	}

	public void insertReinforcement(Reinforcement rein, boolean retry){
		if (rein == null){
			logger.log(Level.WARNING, "CitadelReinforcementData insertReinforcement called with null");
			return;
		}

		try (Connection connection = db.getConnection();
				PreparedStatement insertRein = connection.prepareStatement(CitadelReinforcementData.addRein);) {
			if (this.prepInsertRein(rein, insertRein).executeUpdate() != 1) {
				logger.log(Level.WARNING, "Reinforcement insert at {0} failed!", rein.getLocation());
				throw new SQLException("Reinforcement index collision");
			}
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.SEVERE, "Citadel has detected a reinforcement that should not be there. Deleting it "
					+ (retry ? "and trying again. " : "") + "Including the stack in case it is useful.", e);
			// Let's delete the reinforcement; if a user is able to place one then the db is out of synch / messed up some how.
			deleteReinforcement(rein);
			// Now lets try again.
			if (retry) {
				insertReinforcement(rein, false);
			}
		}
	}

	/**
	 * Delete a bunch of reinforcements in a batch.
	 * @param reins
	 */
	public void deleteManyReinforcements(Collection<Reinforcement> reins) {
		if (reins == null || reins.size() == 0) return;
		boolean failover = false;
		try (Connection connection = db.getConnection();
				PreparedStatement removeRein = connection.prepareStatement(CitadelReinforcementData.removeRein);) {
			int count = 0;
			for (Reinforcement rein : reins) {
				Location loc = rein.getLocation();
				int x = loc.getBlockX();
				int y = loc.getBlockY();
				int z = loc.getBlockZ();
				String world = loc.getWorld().getName();

				removeRein.setInt(1, x);
				removeRein.setInt(2, y);
				removeRein.setInt(3, z);
				removeRein.setString(4, world);

				removeRein.addBatch();
				count++;
				if (count % 100 == 0) {
					int[] done = removeRein.executeBatch();
					if (done == null || done.length == 0) {
						logger.log(Level.WARNING, "Batch removal of Reinforcements -- 100 attempted -- appears to have failed.");
						failover = true;
					} else if (done.length == 100){
						logger.log(Level.INFO, "Removed a batch of Reinforcements -- 100 attempted");
					} else {
						failover = true;
						logger.log(Level.INFO, "Removed a batch of Reinforcements -- 100 attempted -- outcome indeterminate");
					}
				}
			}

			if (count % 100 != 0) {
				int[] done = removeRein.executeBatch();
				if (done == null || done.length == 0) {
					logger.log(Level.WARNING, "Batch removal of Reinforcements -- {0} attempted -- appears to have failed.", (count % 100));
					failover = true;
				} else if (done.length == (count % 100)){
					logger.log(Level.INFO, "Removed a batch of Reinforcements -- {0} attempted", (count % 100));
				} else {
					failover = true;
					logger.log(Level.INFO, "Removed a batch of Reinforcements -- {0} attempted -- outcome indeterminate", (count % 100));
				}
			}
		} catch (SQLException e) {
			failover = true;
			logger.log(Level.SEVERE, "Citadel encountered a critical error while removing a batch of reinforcements", e);
		}

		if (failover) {
			logger.log(Level.WARNING, "Citadel encountered uncertainty while deleting a batch of records. Failing over to individual deleting logic.");
			for (Reinforcement rein : reins) {
				deleteReinforcement(rein);
			}
		}
	}

	/**
	 * Deletes a Reinforcement from the database. Should only be called within SaveManager
	 *
	 * @param The Reinforcement to delete.
	 */
	public void deleteReinforcement(Reinforcement rein){
		if (rein == null){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData deleteReinforcement called with null");
			return;
		}
		Location loc = rein.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		try (Connection connection = db.getConnection();
				PreparedStatement removeRein = connection.prepareStatement(CitadelReinforcementData.removeRein);) {
			removeRein.setInt(1, x);
			removeRein.setInt(2, y);
			removeRein.setInt(3, z);
			removeRein.setString(4, world);
			if (removeRein.executeUpdate() != 1) {
				logger.log(Level.SEVERE, "No reinforcement deleted at {0}", loc);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Citadel has failed to delete a reinforcement at "+ loc, e);
		}
	}

	private PreparedStatement prepSaveReinforcement(Reinforcement rein, PreparedStatement updateRein) throws SQLException {
		// Shared
		int dur = rein.getDurability();
		int mature = rein.getMaturationTime();
		int acid = rein.getAcidTime();
		Location loc = rein.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		String world = loc.getWorld().getName();

		// Uniques Prepped
		boolean insecure = false;
		int groupId = -1;

		if (rein instanceof PlayerReinforcement){
			PlayerReinforcement pRein = (PlayerReinforcement) rein;
			insecure = pRein.isInsecure();
			Group g = pRein.getGroup();
			if (g == null) {
				logger.log(Level.WARNING, "Player prepSaveReinforcement at {0} lacks a valid group (lookup failed)", loc);
			} else {
				groupId = g.getGroupId();
			}
		}

		updateRein.setInt(1, dur);
		updateRein.setBoolean(2, insecure);
		updateRein.setInt(3, groupId);
		updateRein.setInt(4, mature);
		updateRein.setInt(5, acid);
		updateRein.setInt(6, x);
		updateRein.setInt(7, y);
		updateRein.setInt(8, z);
		updateRein.setString(9, world);

		return updateRein;
	}

	/**
	 * Save many reinforcements all at once!
	 *
	 * @param reins
	 */
	public void saveManyReinforcements(Collection<Reinforcement> reins) {
		if (reins == null || reins.size() == 0) return;
		boolean failover = false;
		StringBuilder sb = new StringBuilder();
		long s = System.currentTimeMillis();
		long t = 0;
		try (Connection connection = db.getConnection();
				PreparedStatement updateRein = connection.prepareStatement(CitadelReinforcementData.updateRein);) {
			int count = 0;
			t = System.currentTimeMillis();
			sb.append(t - s).append("ms setup ");
			for (Reinforcement rein : reins) {
				s = System.currentTimeMillis();
				this.prepSaveReinforcement(rein, updateRein).addBatch();
				t = System.currentTimeMillis();
				sb.append(t - s).append(" ");
				count++;
				if (count % 100 == 0) {
					s = System.currentTimeMillis();
					int[] done = updateRein.executeBatch();
					t = System.currentTimeMillis();
					sb.append(t - s).append(" batch ");
					if (done == null || done.length == 0) {
						logger.log(Level.WARNING, "Batch save of Reinforcements -- 100 attempted -- appears to have failed.");
						failover = true;
					} else if (done.length == 100){
						logger.log(Level.INFO, "Saved a batch of Reinforcements -- 100 attempted");
					} else {
						failover = true;
						logger.log(Level.INFO, "Saved a batch of Reinforcements -- 100 attempted -- outcome indeterminate");
					}
				}
			}

			if (count % 100 != 0) {
				s = System.currentTimeMillis();
				int[] done = updateRein.executeBatch();
				t = System.currentTimeMillis();
				sb.append(t - s).append(" batch ");
				if (done == null || done.length == 0) {
					logger.log(Level.WARNING, "Batch save of reinforcements -- {0} attempted -- appears to have failed.", (count % 100));
					failover = true;
				} else if (done.length == (count % 100)){
					logger.log(Level.INFO, "Saved a batch of reinforcements -- {0} attempted", (count % 100));
				} else {
					failover = true;
					logger.log(Level.INFO, "Saved a batch of reinforcements -- {0} attempted -- outcome indeterminate", (count % 100));
				}
			}
		} catch (SQLException e) {
			failover = true;
			logger.log(Level.SEVERE, "Citadel encountered a critical error while saving a batch of reinforcements", e);
		}

		logger.log(Level.INFO, sb.toString());
		if (failover) {
			logger.log(Level.WARNING, "Citadel encountered uncertainty while saving a batch of records. Failing over to individual save logic.");
			for (Reinforcement rein : reins) {
				saveReinforcement(rein);
			}
		}
	}

	/**
	 * Saves the Reinforcement to the Database. Should only be called from SaveManager.
	 * @param The Reinforcement to save.
	 */
	public void saveReinforcement(Reinforcement rein){
		if (rein == null){
			logger.log(Level.WARNING, "CitadelReinforcementData saveReinforcement called with null");
			return;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement updateRein = connection.prepareStatement(CitadelReinforcementData.updateRein);) {

			if (this.prepSaveReinforcement(rein, updateRein).executeUpdate() != 1) {
				logger.log(Level.WARNING, "Update did not alter any records for save at {0}", rein.getLocation());
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Failed to save an update to reinforcement at {0}", rein.getLocation());
			logger.log(Level.SEVERE, "The Exception on saving a reinforcement:", e);
		}
	}

	public int getReinCountForGroup(String group){
		if (group == null){
			logger.log(Level.WARNING, "CitadelReinforcementData getReinCountForGroup called with null");
			return 0;
		}

		Group gg = GroupManager.getGroup(group);
		if (gg == null) {
			logger.log(Level.WARNING, "CitadelReinforcementData getReinCountForGroup called for {0} which does not exist", group);
			return 0;
		}
		StringBuilder allIDs = new StringBuilder();
		for (Integer id : gg.getGroupIds()) {
			allIDs.append(id).append(",");
		}
		allIDs.append(gg.getGroupId());

		String finalIDs = allIDs.toString();

		try (Connection connection = db.getConnection();
				PreparedStatement selectReinCountForGroup = connection.prepareStatement(CitadelReinforcementData.selectReinCountForGroup);) {
			selectReinCountForGroup.setString(1, finalIDs);
			try (ResultSet set = selectReinCountForGroup.executeQuery();) {
				if (!set.next()) {
					throw new SQLException("Failed Count");
				}
				return set.getInt(1);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "getReinCountForGroup has failed for " + group, e);
		}
		return 0;
	}

	public int getReinCountForAllGroups(){
		try (Connection connection = db.getConnection();
				PreparedStatement selectReinCount = connection.prepareStatement(CitadelReinforcementData.selectReinCount);
				ResultSet set = selectReinCount.executeQuery();) {
			if (!set.next()) {
				throw new SQLException("Failed Count");
			}
			return set.getInt(1);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "getReinCountForAllGroups has failed", e);
		}
		return 0;
	}
}
