package vg.civcraft.mc.citadel.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

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
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class CitadelReinforcementData {

	private Database db;
	private Citadel plugin = Citadel.getInstance();
	public CitadelReinforcementData(Database db){
		this.db = db;
		if (db.connect()){
			initalizePreparedStatements();
			createTables();
			intitializeProcedures();
		}
	}

	private void intitializeProcedures(){
		db.execute("drop procedure if exists insertReinID;");
		db.execute("create definer=current_user procedure insertReinID(" +
				"in x int,"
				+ "in y int,"
				+ "in z int,"
				+ "in chunk_id varchar(255),"
				+ "in world varchar(255)" +
				") sql security invoker begin "
				+ "insert into reinforcement_id(x, y, z, chunk_id, world) values (x, y, z, chunk_id, world);"
				+ "select LAST_INSERT_ID() as id;"
				+ "end;");
		db.execute("drop procedure if exists insertCustomReinID;");
		db.execute("create definer=current_user procedure insertCustomReinID("
				+ "in rein_id int," +
				"in x int,"
				+ "in y int,"
				+ "in z int,"
				+ "in chunk_id varchar(255),"
				+ "in world varchar(255)" +
				") sql security invoker begin "
				+ "insert into reinforcement_id(rein_id, x, y, z, chunk_id, world) values (rein_id, x, y, z, chunk_id, world);"
				+ "select LAST_INSERT_ID() as id;"
				+ "end;");
	}
	/**
	 * Creates the required mysql tables and updates the db if needed.
	 */
	private void createTables(){
		int ver = checkVersion(plugin.getName());
		long begin_time = System.currentTimeMillis();
		db.execute("create table if not exists db_version (db_version int not null," +
				"update_time varchar(24),"
				+ "plugin_name varchar(40));");
		if (ver == 0){
			db.execute(String.format("update db_version set plugin_name = '%s' " +
					"where plugin_name is null", plugin.getName()));
			ver = checkVersion(plugin.getName());
			if (ver == 0){
				Citadel.Log("Creating tables from scratch, this is a new " +
						"instance of citadel.");
			}
			else{
				Citadel.Log("Detected previous version of Citadel, " +
						"updating to new db.\n" +
						"You should have backed up your data, " +
						"if you havent already you have ten seconds to" +
						" shut down the server and perform a backup.");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					Citadel.getInstance().getLogger().log(Level.SEVERE, "Update sleep interrupted", e1);
				}
				long first_time = System.currentTimeMillis();
				db.execute("alter table reinforcement drop security_level, drop version, add group_id int not null;");
				db.execute("insert into faction_id (group_name) values (null);"); // For natural reinforcements
				db.execute("delete from reinforcement where `name` is null;");
				db.execute("update ignore reinforcement r inner join faction_id f on f.group_name = r.`name` "
						+ "set r.group_id = f.group_id;");
				db.execute("alter table reinforcement drop `name`, add rein_type_id int not null default 1, "
						+ "add lore varchar(255);");
				db.execute("drop table citadel_account_id_map;");
				Citadel.Log("The update to new format took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
			}
		}
		if (ver == 5 || ver == 0){
			long first_time = System.currentTimeMillis();
			Citadel.Log("Updating to Citadel Version 6.");
			db.execute("create table if not exists reinforcement(" +
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
					+ "primary key (x,y,z,world));");
			db.execute("create table if not exists reinforcement_type("
					+ "rein_type_id int not null auto_increment,"
					+ "rein_type varchar(30) not null,"
					+ "primary key rein_type_key (rein_type_id));");
			String[] types = {"PlayerReinforcement", "NaturalReinforcement", "MultiBlockReinforcement"};
			for (String x: types) {
				db.execute(String.format("insert into reinforcement_type(rein_type)"
						+ "values('%s');", x));
			}
			updateVersion(5, plugin.getName());
			ver = checkVersion(plugin.getName());
			Citadel.Log("The update to Version 6 took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
		}
		if (ver == 6){
			long first_time = System.currentTimeMillis();
			Citadel.Log("Updating to version 7. No fun message for you :(");
			db.execute("create table if not exists reinforcement_id("
					+ "rein_id int not null auto_increment,"
					+ "x int not null,"
					+ "y int not null,"
					+ "z int not null,"
					+ "chunk_id varchar(255),"
					+ "world varchar (255) not null,"
					+ "primary key rein_id_key (rein_id),"
					+ "unique key x_y_z_world(x,y,z,world));-- Your mother is a whore and sleeps with banjos"); 
			// I like turtles mother fucker. Never program because then you get turtles.
			db.execute("insert into reinforcement_id (x, y, z, chunk_id, world) select x, y, z, chunk_id, world from reinforcement;"); // populate that bitch.
			db.execute("alter table reinforcement add rein_id int not null, drop chunk_id;");
			db.execute("update reinforcement r inner join reinforcement_id ri on "
					+ "ri.x = r.x and ri.y = r.y and ri.z = r.z and ri.world = r.world "
					+ "set r.rein_id = ri.rein_id");
			db.execute("alter table reinforcement DROP PRIMARY KEY, "
					+ "add primary key rein_id_key(rein_id), "
					+ "drop x,"
					+ "drop y,"
					+ "drop z,"
					+ "drop world;");
			db.execute("alter table reinforcement_id add index `chunk_id_index` (chunk_id);");
			updateVersion(ver, plugin.getName());
			ver = checkVersion(plugin.getName());
			Citadel.Log("The update to Version 7 took " + (System.currentTimeMillis() / first_time) / 1000 + " seconds.");
		}
		if (ver == 7){
			long first_time = System.currentTimeMillis();
			db.execute("alter table reinforcement_id drop primary key,"
					+ " add primary key (rein_id, x, y, z, world);");
			updateVersion(ver, plugin.getName());
			ver = checkVersion(plugin.getName());
			Citadel.Log("The update to Version 8 took " + (System.currentTimeMillis() / first_time) / 1000 + " seconds.");
		}
		if (ver == 8) {
			long first_time = System.currentTimeMillis();
			Citadel.Log("Updating to version 9: The acid test. Note: This will take a while.");
			db.execute("alter table reinforcement add acid_time int not null;"); 
			db.execute("update reinforcement set acid_time = maturation_time;"); // Might take a minute.
			updateVersion(ver, plugin.getName());
			ver = checkVersion(plugin.getName());
			Citadel.Log("The update to Version 9 took " + (System.currentTimeMillis() / first_time) / 1000 + " seconds.");
		}
		Citadel.Log("The total time it took Citadel to update was " + 
				(System.currentTimeMillis() - begin_time) / 1000 + " seconds.");
	}
	/**
	 * Reconnects and reinitializes the mysql connection and preparedstatements.
	 */
	private void reconnectAndReinitialize(){
		if (db.isConnected()) {
			return;
		}
		if (CitadelConfigManager.shouldLogInternal()) {
			Citadel.Log("Database went away, reconnecting.");
		}
		db.connect();
		initalizePreparedStatements();
	}
	
	private String version, updateVersion;
	private String getRein, getReins, addRein, removeRein, updateRein;
	//private PreparedStatement deleteGroup, insertDeleteGroup, removeDeleteGroup, getDeleteGroup;
	private String insertReinID, insertCustomReinID, getCordsbyReinID, selectReinCountForGroup, selectReinCount;
	/**
	 * Initializes the PreparedStatements. Gets called on db connect or
	 * reconnect.
	 */
	private void initalizePreparedStatements(){
		getRein = "select r.material_id, r.durability, " +
				"r.insecure, r.maturation_time, r.acid_time, rt.rein_type, "
				+ "r.lore, r.group_id, r.rein_id from reinforcement r "
				+ "inner join reinforcement_id ri on r.rein_id = ri.rein_id "
				+ "inner join reinforcement_type rt on rt.rein_type_id = r.rein_type_id "
				+ "where ri.x = ? and ri.y = ? and ri.z = ? and ri.chunk_id = ? and ri.world = ?";
		getReins = "select ri.x, ri.y, ri.z, ri.world, r.material_id, r.durability, " +
				"r.insecure, r.maturation_time, rt.rein_type, "
				+ "r.lore, r.group_id, r.rein_id, r.acid_time from reinforcement r "
				+ "inner join reinforcement_id ri on r.rein_id = ri.rein_id "
				+ "inner join reinforcement_type rt on rt.rein_type_id = r.rein_type_id "
				+ "where ri.chunk_id = ?";
		addRein = "insert into reinforcement ("
				+ "material_id, durability, "
				+ "insecure, group_id, maturation_time, rein_type_id,"
				+ "lore, rein_id, acid_time) select ?, ?, ?, ?, ?, rt.rein_type_id, ?, ?, ? "
				+ "from reinforcement_type rt where rt.rein_type = ?";
		removeRein = "delete r.*, ri.* from reinforcement r "
				+ "left join reinforcement_id ri on r.rein_id = ri.rein_id "
				+ "where ri.x = ? and ri.y = ? and ri.z = ? and ri.world = ?";
		updateRein = "update reinforcement r "
				+ "inner join reinforcement_id ri on ri.rein_id = r.rein_id "
				+ "set r.durability = ?, r.insecure = ?, r.group_id = ?, "
				+ "maturation_time = ?, acid_time = ? "
				+ "where ri.x = ? and ri.y = ? and ri.z = ? and ri.world =?";
		insertReinID = "call insertReinID(?,?,?,?,?)";
		insertCustomReinID = "call insertCustomReinID(?,?,?,?,?,?)";
		getCordsbyReinID = "select x, y, z, world from reinforcement_id where rein_id = ?";
		selectReinCountForGroup = "select count(*) as count from reinforcement r "
				+ "inner join faction_id f on f.group_id = r.group_id "
				+ "where f.group_name = ?";
		selectReinCount = "select count(*) as count from reinforcement r";
		
		version = "select max(db_version) as db_version from db_version where plugin_name=?";
		updateVersion = "insert into db_version (db_version, update_time, plugin_name) values (?,?,?)";
	}
	
	/**
	 * Checks the version of a specific plugin's db.
	 * @param name- The name of the plugin.
	 * @return Returns the version of the plugin or 0 if none was found.
	 */
	public int checkVersion(String name){
		reconnectAndReinitialize();
		PreparedStatement version = db.prepareStatement(this.version);
		try {
			version.setString(1, name);
			ResultSet set = version.executeQuery();
			if (!set.next()) 
				return 0;
			return set.getInt("db_version");
		} catch (SQLException e) {
			if (CitadelConfigManager.shouldLogInternal()) {
				Citadel.getInstance().getLogger().log(Level.WARNING, "Version control table missing for Citadel!", e);
			}
			return 0;
		}
	}
	
	/**
	 * Updates the version number for a plugin. You must specify what 
	 * the current version number is.
	 * @param version- The current version of the plugin.
	 * @param pluginname- The plugin name.
	 * @return Returns the new version of the db.
	 */
	public synchronized int updateVersion(int version, String pluginname){
		reconnectAndReinitialize();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		PreparedStatement updateVersion = db.prepareStatement(this.updateVersion);
		try {
			updateVersion.setInt(1, version+ 1);
			updateVersion.setString(2, sdf.format(new Date()));
			updateVersion.setString(3, pluginname);
			updateVersion.execute();
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "Version control table error; unable to update DB version for Citadel!", e);
		}
		return ++version;
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
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData getReinforcement called with null");
			return null;
		}
		reconnectAndReinitialize();
		PreparedStatement getRein = db.prepareStatement(this.getRein);
		try {
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			getRein.setInt(1, x);
			getRein.setInt(2, y);
			getRein.setInt(3, z);
			String formatChunk = formatChunk(loc);
			getRein.setString(4, formatChunk);
			getRein.setString(5, loc.getWorld().getName());
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
			String rein_type = set.getString(6);
			String lore = set.getString(7);
			int group_id = set.getInt(8);
			// Check for what type of reinforcement and return the one needed.
			if ("PlayerReinforcement".equals(rein_type)) {
				set.close();
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
						Citadel.getInstance().getLogger().log(Level.WARNING,
								"Player Reinforcement at {0} lacks a valid group (group {1} failed lookup)", 
								new Object[] {loc, group_id});
					}
					return null; // group not found!
				}
				PlayerReinforcement rein = new PlayerReinforcement(loc, durability, 
							mature, acid, g,stack);
				rein.setInsecure(inSecure);
				return rein;
			} else if ("NaturalReinforcement".equals(rein_type)){
				set.close();
				NaturalReinforcement rein = new NaturalReinforcement(loc.getBlock(), durability);
				return rein;
			} else if ("MultiBlockReinforcement".equals(rein_type)) {
				int id = set.getInt(9);
				set.close();
				MultiBlockReinforcement rein = MultiBlockReinforcement.getMultiRein(id);
				if (rein != null) {
					return rein;
				}
				PreparedStatement getCordsbyReinID = db.prepareStatement(this.getCordsbyReinID);
				getCordsbyReinID.setInt(1, id);
				set = getCordsbyReinID.executeQuery();
				List<Location> locs = new ArrayList<Location>();
				while (set.next()){
					int xx = set.getInt(1), yy = set.getInt(2), zz = set.getInt(3);
					String world = set.getString(4);
					locs.add(new Location(Bukkit.getWorld(world), xx, yy, zz));
				}
				set.close();

				Group g = GroupManager.getGroup(group_id);
				if (g == null) {
					if (CitadelConfigManager.shouldLogReinforcement()) {
						Citadel.getInstance().getLogger().log(Level.WARNING,
								"Multiblock Reinforcement touching {0} lacks a valid group (group {1} failed lookup)", 
								new Object[] {loc, group_id});
					}
					return null; // group not found!
				}
				rein = new MultiBlockReinforcement(locs, g, durability, mature, acid, id);
				return rein;
			}
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.SEVERE, "Failed while retrieving reinforcement from database", e);
		}
		if (CitadelConfigManager.shouldLogInternal()) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData getReinforcement failed for {0}", loc);
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
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData getReinforcements called with null");
			return null;
		}
		reconnectAndReinitialize();
		PreparedStatement getReins = db.prepareStatement(this.getReins);
		String formatChunk = formatChunk(chunk);
		if (CitadelConfigManager.shouldLogInternal()) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData getReinforcements chunk called for {0}", formatChunk);
		}
		List<Reinforcement> reins = new ArrayList<Reinforcement>();
		try {
			getReins.setString(1, formatChunk);
			ResultSet set = getReins.executeQuery();
			while (set.next()) {
				int x = set.getInt(1), y = set.getInt(2), z = set.getInt(3);
				String world = set.getString(4);
				@SuppressWarnings("deprecation")
				Material mat = Material.getMaterial(set.getInt(5));
				int durability = set.getInt(6);
				boolean inSecure = set.getBoolean(7);
				int mature = set.getInt(8);
				String rein_type = set.getString(9);
				String lore = set.getString(10);
				int group_id = set.getInt(11);
				int acid = set.getInt(13);
				
				Location loc = new Location(Bukkit.getWorld(world), x, y, z);
				
				if ("PlayerReinforcement".equals(rein_type)){
					Group g = GroupManager.getGroup(group_id);
					if (g == null) {
						if (CitadelConfigManager.shouldLogReinforcement()) {
							Citadel.getInstance().getLogger().log(Level.WARNING,
									"During Chunk {0} load, Player Reinforcement at {1} lacks a valid group (group {2} failed lookup)", 
									new Object[] {formatChunk, loc, group_id});
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
					PlayerReinforcement rein = new PlayerReinforcement(loc, durability,
									mature, acid, g, stack);
					rein.setInsecure(inSecure);
					reins.add(rein);
				}
				else if("NaturalReinforcement".equals(rein_type)){
					NaturalReinforcement rein = new NaturalReinforcement(loc.getBlock(), durability);
					reins.add(rein);
				}
				else if ("MultiBlockReinforcement".equals(rein_type)){
					int id = set.getInt(12);
					MultiBlockReinforcement rein = MultiBlockReinforcement.getMultiRein(id);
					if (rein != null) {
						reins.add(rein);
						continue;
					}
					
					Group g = GroupManager.getGroup(group_id);
					if (g == null) {
						if (CitadelConfigManager.shouldLogReinforcement()) {
							Citadel.getInstance().getLogger().log(Level.WARNING,
									"During Chunk {0} load, Multiblock Reinforcement at {1} lacks a valid group (group {2} failed lookup)", 
									new Object[] {formatChunk, loc, group_id});
						}
						continue; // group not found!
					}
					PreparedStatement getCordsbyReinID = db.prepareStatement(this.getCordsbyReinID);
					getCordsbyReinID.setInt(1, id);
					ResultSet multi = getCordsbyReinID.executeQuery();
					List<Location> locs = new ArrayList<Location>();
					while (multi.next()){
						int xx = multi.getInt(1), yy = multi.getInt(2), zz = multi.getInt(3);
						String w = multi.getString(4);
						locs.add(new Location(Bukkit.getWorld(w), xx, yy, zz));
					}
					multi.close();
					
					rein = new MultiBlockReinforcement(locs, g, durability, mature, acid, id);
					reins.add(rein);
				}
			}
			set.close();
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.SEVERE, "Failed while retrieving chunk " + 
					formatChunk + " reinforcement from database", e);
		}
		return reins;
	}
	
	/**
	 * Inserts a reinforcement into the Database. Should only be called from SaveManager.
	 * @param The Reinforcement to save.
	 */
	public void insertReinforcement(Reinforcement rein){
		insertReinforcement(rein, false);
	}
	
	@SuppressWarnings("deprecation")
	public void insertReinforcement(Reinforcement rein, boolean retry){
		if (rein == null){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData insertReinforcement called with null");
			return;
		}
		reconnectAndReinitialize();
		
		if (rein instanceof PlayerReinforcement){
			Location loc = rein.getLocation();
			int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
			String world = loc.getWorld().getName();
			Material mat = rein.getMaterial();
			int dur = rein.getDurability();
			int maturationTime = rein.getMaturationTime();
			int acidTime = rein.getAcidTime();
			boolean insecure = false;
			String reinType = "PlayerReinforcement";
			
			PlayerReinforcement pRein = (PlayerReinforcement) rein;
			insecure = pRein.isInsecure();
			ItemMeta meta = pRein.getStackRepresentation().getItemMeta();
			String lore = "";
			if (meta.hasLore()) {
				for (String xx: meta.getLore()) {
					lore += xx + "\n";
				}
			} else {
				lore = null;
			}
			
			Group g = pRein.getGroup(); 
			if (g == null) {
				Citadel.getInstance().getLogger().log(Level.WARNING,
						"Player Reinforcement insert at {0} lacks a valid group (lookup failed)", loc);
			}
			
			try {
				PreparedStatement insertReinID = db.prepareStatement(this.insertReinID);
				insertReinID.setInt(1, x);
				insertReinID.setInt(2, y);
				insertReinID.setInt(3, z);
				String formatChunk = formatChunk(loc);
				insertReinID.setString(4, formatChunk);
				insertReinID.setString(5, world);
				ResultSet set = insertReinID.executeQuery();
				if (!set.next()) {
					throw new SQLException("Failed ID insertion");
				}
				int id = set.getInt("id");
				
				PreparedStatement addRein = db.prepareStatement(this.addRein);
				addRein.setInt(1, mat.getId());
				addRein.setInt(2, dur);
				addRein.setBoolean(3, insecure);
				addRein.setInt(4, pRein.getGroupId());
				addRein.setInt(5, maturationTime);
				addRein.setString(6, lore);
				addRein.setInt(7, id);
				addRein.setInt(8, acidTime);
				addRein.setString(9, reinType);
				addRein.execute();
			} catch (SQLException e) {
				Citadel.getInstance().getLogger().log(Level.SEVERE, "Citadel has detected a reinforcement that should not be there. Deleting it and trying again. "
						+ "Including the stack incase it is useful.", e);
				// Let's delete the reinforcement; if a user is able to place one then the db is
				// out of synch / messed up some how.
				deleteReinforcement(rein);
				// Now lets try again.
				if (!retry) {
					insertReinforcement(rein, true);
				}
			}
		} else if (rein instanceof NaturalReinforcement) {
			/* 
			 * TODO: Why removed? We had thoughts of using this..
			 *
			 * We don't need to worry about saving this right now.
			 * 
			Location loc = rein.getLocation();
			int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
			String world = loc.getWorld().getName();
			Material mat = rein.getMaterial();
			int dur = rein.getDurability();
			String chunk_id = loc.getChunk().toString();
			int maturationTime = rein.getMaturationTime();
			int acidTime = rein.getAcidTime();
			boolean insecure = false;
			String group = NameLayerPlugin.getSpecialAdminGroup();
			String reinType = "NaturalReinforcement";
			String lore = "";
			lore = null;
			try {
				PreparedStatement insertReinID = db.prepareStatement(this.insertReinID);
				insertReinID.setInt(1, x);
				insertReinID.setInt(2, y);
				insertReinID.setInt(3, z);
				String formatChunk = formatChunk(loc);
				insertReinID.setString(4, formatChunk);
				insertReinID.setString(5, world);
				insertReinID.execute();
				
				int id = getLastReinId();
				
				PreparedStatement addRein = db.prepareStatement(this.addRein);
				addRein.setInt(1, mat.getId());
				addRein.setInt(2, dur);
				addRein.setBoolean(3, insecure);
				addRein.setInt(4, maturationTime);
				addRein.setString(5, lore);
				addRein.setInt(6, id);
				addRein.setInt(7, acidTime);
				addRein.setString(8, reinType);
				addRein.setString(9, group);
				addRein.execute();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		} else if (rein instanceof MultiBlockReinforcement){
			MultiBlockReinforcement mbRein = (MultiBlockReinforcement) rein;

			Group g = mbRein.getGroup();  // let's confirm it's good.
			if (g == null) {
				Citadel.getInstance().getLogger().log(Level.WARNING,
						"Multiblock Reinforcement insert request lacks a valid group (lookup failed)");
			}
			
			int id = -1; // We add one because we haven't added it yet.
			try {
				PreparedStatement insertCustomReinID = db.prepareStatement(this.insertCustomReinID);
				// add all the locations into the db.
				int count = 0;
				try {
					for (Location lo: mbRein.getLocations()){
						if (count == 0) {
							PreparedStatement insertReinID = db.prepareStatement(this.insertReinID);
							insertReinID.setInt(1, lo.getBlockX());
							insertReinID.setInt(2, lo.getBlockY());
							insertReinID.setInt(3, lo.getBlockZ());
							String formatChunk = formatChunk(lo);
							insertReinID.setString(4, formatChunk);
							insertReinID.setString(5, lo.getWorld().getName());
							ResultSet set = insertReinID.executeQuery();
							if (!set.next()) {
								throw new SQLException("Failed ID insertion");
							}
							
							id = set.getInt("id");
							mbRein.setReinId(id);
							continue;
						}
						insertCustomReinID.setInt(1, id);
						insertCustomReinID.setInt(2, lo.getBlockX());
						insertCustomReinID.setInt(3, lo.getBlockY());
						insertCustomReinID.setInt(4, lo.getBlockZ());
						String formatChunk = formatChunk(lo);
						insertCustomReinID.setString(5, formatChunk);
						insertCustomReinID.setString(6, lo.getWorld().getName());
						insertCustomReinID.addBatch();
					}
					insertCustomReinID.executeBatch();
				} catch (SQLException se) {
					Citadel.getInstance().getLogger().log(Level.SEVERE, "Citadel has failed to insert locations in a" +
							" multiblock reinforcement insertion. ", se);
					insertCustomReinID.clearBatch();
					// TODO: Consider forcing the reinf ID removal.
					throw se; // propagate the exception.
				}

				PreparedStatement addRein = db.prepareStatement(this.addRein);
				addRein.setInt(1, -1);
				addRein.setInt(2, mbRein.getDurability());
				addRein.setBoolean(3, false);
				addRein.setInt(4, mbRein.getGroupId());
				addRein.setInt(5, mbRein.getMaturationTime());
				addRein.setString(6, null);
				addRein.setInt(7, id);
				addRein.setInt(8, mbRein.getAcidTime());
				addRein.setString(9, "MultiBlockReinforcement");
				addRein.execute();
			} catch (SQLException e) {
				Citadel.getInstance().getLogger().log(Level.SEVERE, "Citadel has failed to insert a" +
						" multiblock reinforcement. ", e);
				deleteReinforcement(rein);
				// Now lets try again.
				if (!retry) {
					insertReinforcement(rein);
				}
			}
		}
		
	}
	/**
	 * Deletes a Reinforcement from the database. Should only be called
	 * within SaveManager
	 * @param The Reinforcement to delete.
	 */
	public void deleteReinforcement(Reinforcement rein){
		if (rein == null){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData deleteReinforcement called with null");
			return;
		}
		reconnectAndReinitialize();
		
		Location loc = rein.getLocation();
		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		try {
			PreparedStatement removeRein = db.prepareStatement(this.removeRein);
			removeRein.setInt(1, x);
			removeRein.setInt(2, y);
			removeRein.setInt(3, z);
			removeRein.setString(4, world);
			removeRein.execute();
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.SEVERE, "Citadel has failed to delete a" +
					" reinforcement at "+ loc, e);
		}
	}

	/**
	 * Saves the Reinforcement to the Database. Should only be called
	 * from SaveManager.
	 * @param The Reinforcement to save.
	 */
	public void saveReinforcement(Reinforcement rein){
		if (rein == null){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData saveReinforcement called with null");
			return;
		}
		reconnectAndReinitialize();
		
		int dur = rein.getDurability();
		boolean insecure = false;
		int groupId = -1;
		int mature = rein.getMaturationTime();
		int acid = rein.getAcidTime();
		Location loc = rein.getLocation();
		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		if (rein instanceof PlayerReinforcement){
			PlayerReinforcement pRein = (PlayerReinforcement) rein;
			insecure = pRein.isInsecure();
			Group g = pRein.getGroup();
			if (g == null) {
				Citadel.getInstance().getLogger().log(Level.WARNING,
						"Player saveReinforcement at {0} lacks a valid group (lookup failed)", loc);
			} else {
				groupId = g.getGroupId();
			}
		}
		if (rein instanceof MultiBlockReinforcement){
			MultiBlockReinforcement mbRein = (MultiBlockReinforcement) rein;
			insecure = false;
			Group g = mbRein.getGroup();
			if (g == null) {
				Citadel.getInstance().getLogger().log(Level.WARNING,
						"Player saveinsert at {0} lacks a valid group (lookup failed)", loc);
			} else {
				groupId = g.getGroupId();
			}			
		}
		try {
			PreparedStatement updateRein = db.prepareStatement(this.updateRein);
			updateRein.setInt(1, dur);
			updateRein.setBoolean(2, insecure);
			updateRein.setInt(3, groupId);
			updateRein.setInt(4, mature);
			updateRein.setInt(5, acid);
			updateRein.setInt(6, x);
			updateRein.setInt(7, y);
			updateRein.setInt(8, z);
			updateRein.setString(9, world);
			updateRein.execute();
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.WARNING, String.format("The Null Group Exception that is being followed has to deal with the group id: %s,"
					+ " at location: %d, %d, %d, at world: %s", groupId, x, y, z, world), e);
		}
	}
	
	private String formatChunk(Location loc){
		Chunk c = loc.getChunk();
		return formatChunk(c);
	}
	
	private String formatChunk(Chunk c){
		StringBuilder chunk = new StringBuilder(c.getWorld().getName());
		chunk.append(":").append(c.getX()).append(":").append(c.getZ());
		return chunk.toString();
	}
	
	public int getReinCountForGroup(String group){
		if (group == null){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"CitadelReinforcementData getReinCountForGroup called with null");
			return 0;
		}
		try {
			PreparedStatement selectReinCountForGroup = db.prepareStatement(this.selectReinCountForGroup);
			selectReinCountForGroup.setString(1, group);
			ResultSet set = selectReinCountForGroup.executeQuery();
			if (!set.next()) {
				throw new SQLException("Failed Count");
			}
			int r = set.getInt(1);
			set.close();
			return r;
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.SEVERE, "getReinCountForGroup has failed for " +
					group, e);
		}
		return 0;
	}
	
	public int getReinCountForAllGroups(){
		try {
			PreparedStatement selectReinCount = db.prepareStatement(this.selectReinCount);
			ResultSet set = selectReinCount.executeQuery();
			if (!set.next()) {
				throw new SQLException("Failed Count");
			}
			int r = set.getInt(1);
			set.close();
			return r;
		} catch (SQLException e) {
			Citadel.getInstance().getLogger().log(Level.SEVERE, "getReinCountForAllGroups has failed", e);
		}
		return 0;
	}
}
