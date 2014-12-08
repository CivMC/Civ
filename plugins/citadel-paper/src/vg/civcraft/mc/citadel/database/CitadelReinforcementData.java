package vg.civcraft.mc.citadel.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.MultiBlockReinforcement;
import vg.civcraft.mc.citadel.reinforcement.NaturalReinforcement;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;

public class CitadelReinforcementData {

	private Database db;
	private Citadel plugin = Citadel.getInstance();
	public CitadelReinforcementData(Database db){
		this.db = db;
		if (db.connect()){
			createTables();
			initalizePreparedStatements();
			intitializeProcedures();
		}
	}

	private void intitializeProcedures(){
		db.execute("drop procedure if exists deleteGroup;");
		db.execute("create definer=current_user procedure deleteGroup("
				+ "in deleteGroup varchar(36)"
				+ ") sql security invoker "
				+ " "
				+ "begin "
				+ "declare group_idd int; "
				+ "declare rein_idd int;"
				+ "set group_idd = (select g.group_id from faction_id g where g.group_name = deleteGroup);"
				+ "set rein_idd = (select r.rein_id from reinforcement r where r.group_id = group_idd);"
				+ "delete from reinforcment_id "
				+ "where rein_id = rein_idd limit 50;"
				+ "delete from reinforcement "
				+ "where group_id = group_idd limit 50;"
				+ "select (select count(*) from reinforcement "
				+ "where group_name = deleteGroup) + "
				+ "(select count(*) from reinforcement_id "
				+ "where rein_id = rein_idd) "
				+ "as count;"
				+ "end;");
	}
	/**
	 * Creates the required mysql tables and updates the db if needed.
	 */
	private void createTables(){
		int ver = NameLayerPlugin.getVersionNum(plugin.getName());
		if (ver == 0){
			db.execute(String.format("update db_version set plugin_name = '%s' " +
					"where plugin_name is null", plugin.getName()));
			ver = NameLayerPlugin.getVersionNum(plugin.getName());
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
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				db.execute("alter table reinforcement drop security_level;");
				db.execute("alter table reinforcement drop version");
				Citadel.Log("Remapping from material id to material name " +
						"in process.");
				db.execute("create table if not exists material_mapping(" +
						"material varchar(40) not null," +
						"material_id int not null);");
				for (Material mat: Material.values()){
					PreparedStatement insert = db.prepareStatement
							("insert into material_mapping(" +
							"material, material_id) values(?,?);");
					try {
						insert.setString(1, mat.name());
						insert.setInt(2, mat.getId());
						insert.execute();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				db.execute("alter table reinforcement add " +
						"material varchar(10)");
				db.execute("update reinforcement r " +
						"inner join material_mapping m on m.material_id = r.material_id " +
						"set r.material = m.material;");
				db.execute("alter table reinforcement drop material_id;");
				db.execute("drop table material_mapping;");
				db.execute("insert into faction_id (group_name) values (null);"); // For natural reinforcements
				db.execute("alter table reinforcement add group_id int not null;");
				db.execute("update reinforcement r set r.group_id = (select f.group_id from "
						+ "faction_id f where f.group_name = r.`name`);");
				db.execute("alter table reinforcement drop `name`;");
				db.execute("alter table reinforcement add " +
						"rein_type varchar(30) not null default 'PlayerReinforcement';");
				db.execute("alter table reinforcement add " +
						"lore varchar(255);");
			}
		}
		if (ver == 5 || ver == 0){
			Citadel.Log("Update to Citadel Version 6.");
			db.execute("create table if not exists reinforcement(" +
					"x int not null," +
					"y int not null," +
					"z int not null," +
					"world varchar(10) not null," +
					"material varchar(10) not null," +
					"durability varchar(10) not null," +
					"chunk_id varchar(255) not null," +
					"insecure tinyint(1) not null," +
					"group_id int not null," +
					"maturation_time int not null," +
					"rein_type varchar(30) not null," +
					"lore varchar(255),"
					+ "unique key (x, y, z, world))");
			db.execute("create table if not exists toDeleteReinforcments("
					+ "group_id int not null,"
					+ "primary key (group_id));");
			NameLayerPlugin.insertVersionNum(5, plugin.getName());
			ver = NameLayerPlugin.getVersionNum(plugin.getName());
		}
		if (ver == 6){
			Citadel.Log("Updating to version 6, I don't care if you don't like it, it has to be this way. I'm sorry we couldn't get "
					+ "along.  Is it okay if we can stay friends?  It would really mean a lot to me if we could, I've really enjoyed our "
					+ "time together and this makes me happy.");
			db.execute("create table if not exists reinforcement_id("
					+ "rein_id int not null auto increment,"
					+ "x int not null,"
					+ "y int not null,"
					+ "z int not null,"
					+ "world varchar (255) not null,"
					+ "unique key x_y_z_world(x,y,z,world));-- Your mother is a whore and sleeps with banjos"); 
			// I like turtles mother fucker. Never program because then you get turtles.
			db.execute("insert into reinforcement_id (x,y,z) select x, y, z from reinforcement;"); // populate that bitch.
			db.execute("alter table reinforcement add rein_id int not null;");
			db.execute("update reinforcement r set rein_id = (select ri.rein_id from reinforcement_id ri where ri.x = r.x and "
					+ "ri.y = r.y and ri.z = r.z and ri.world = r.world);");
			db.execute("alter table reinforcement add primary key rein_id_key(rein_id);");
			db.execute("alter table reinforcement drop x;");
			db.execute("alter table reinforcement drop y;");
			db.execute("alter table reinforcement drop z;");
			db.execute("alter table reinforcement drop world;");
			NameLayerPlugin.insertVersionNum(ver, plugin.getName());
		}
	}
	/**
	 * Reconnects and reinitializes the mysql connection and preparedstatements.
	 */
	private void reconnectAndReinitialize(){
		if (db.isConnected())
			return;
		db.connect();
		initalizePreparedStatements();
	}
	
	private PreparedStatement getRein, getReins, addRein, removeRein, updateRein;
	private PreparedStatement deleteGroup, insertDeleteGroup, removeDeleteGroup, getDeleteGroup;
	private PreparedStatement insertReinID, getLastReinID, getCordsbyReinID;
	/**
	 * Initializes the PreparedStatements. Gets called on db connect or
	 * reconnect.
	 */
	private void initalizePreparedStatements(){
		getRein = db.prepareStatement("select r.material, r.durability, " +
				"r.insecure, f.group_name, r.maturation_time, r.rein_id from reinforcement r "
				+ "inner join faction_id f on f.group_id = r.group_id " +
				"inner join reinforcement_id ri on r.rein_id = ri.rein_id "
				+ "where ri.x = ? and ri.y = ? and ri.z = ? and ri.world = ?");
		getReins = db.prepareStatement("select r.material, r.durability, " + // this wont work
				"r.insecure, f.group_name, r.maturation_time from reinforcement r "
				+ "inner join faction_id f on f.group_id = r.group_id " +
				"where chunk_id = ?");
		addRein = db.prepareStatement("insert into reinforcement ("
				+ "x, y, z, world, material, durability, chunk_id,"
				+ "insecure, group_id, maturation_time, rein_type,"
				+ "lore, rein_id) select ?, ?, ?, ?, ?, ?, ?, ?, f.group_id, ?, ?, ?, ? from faction_id f "
				+ "where f.group_name = ?");
		removeRein = db.prepareStatement("delete from reinforcement "
				+ "where x = ? and y = ? and z = ? and world = ?");
		updateRein = db.prepareStatement("update reinforcements set durability = ?,"
				+ "insecure = ?, group_id = (select f.group_id from faction_id f where f.group_name = ?), maturation_time = ? "
				+ "where x = ? and y = ? and z = ? and world = ?");
		deleteGroup = db.prepareStatement("call deleteGroup(?)");
		insertDeleteGroup = db.prepareStatement("insert into toDeleteReinforcments(group_id) select g.group_id from faction_id g "
				+ "where g.group_name = ?");
		removeDeleteGroup = db.prepareStatement("delete from toDeleteReinforcments where group_id = (select f.group_id from "
				+ "faction_id f where f.group_name = ?)");
		getDeleteGroup = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join toDeleteReinforcments d on f.group_id = d.group_id");
		
		insertReinID = db.prepareStatement("insert into reinforcement_id(rein_id, x, y, z, world) values (?, ?, ?, ?, ?)");
		getLastReinID = db.prepareStatement("select LAST_INSERT_ID() as id from reinforcement_id");
		getCordsbyReinID = db.prepareStatement("select x, y, z, world from reinforcement_id where rein_id = ?");
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
		reconnectAndReinitialize();
		
		try {
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			getRein.setInt(1, x);
			getRein.setInt(2, y);
			getRein.setInt(3, z);
			getRein.setString(4, loc.getWorld().getName());
			ResultSet set = getRein.executeQuery();
			if (!set.next())
				return null;
			Material mat = Material.valueOf(set.getString(1));
			int durability = set.getInt(2);
			boolean inSecure = set.getBoolean(3);
			String group = set.getString(4);
			int mature = set.getInt(5);
			String rein_type = set.getString(6);
			String lore = set.getString(7);
			// Check for what type of reinforcement and return the one
			// needed.
			if (rein_type.equals("PlayerReinforcement")){
				ItemStack stack = new ItemStack(mat);
				if (lore != null){
					ItemMeta meta = stack.getItemMeta();
					List<String> array = Arrays.asList(lore.split("\n"));
					meta.setLore(array);
				}
				PlayerReinforcement rein =
						new PlayerReinforcement(loc, durability,
								mature, GroupManager.getGroup(group),
								stack);
				rein.setInsecure(inSecure);
				return rein;
			}
			else if(rein_type.equals("NaturalReinforcement")){
				NaturalReinforcement rein = 
						new NaturalReinforcement(loc.getBlock(), durability);
				return rein;
			}
			else if (rein_type.equals("MultiBlockReinforcement")){
				int id = set.getInt(8);
				MultiBlockReinforcement rein = MultiBlockReinforcement.getMultiRein(id);
				if (rein != null)
					return rein;
				getCordsbyReinID.setInt(1, id);
				set = getCordsbyReinID.executeQuery();
				List<Location> locs = new ArrayList<Location>();
				while (set.next()){
					int xx = set.getInt(1), yy = set.getInt(2), zz = set.getInt(3);
					String world = set.getString(4);
					locs.add(new Location(Bukkit.getWorld(world), xx, yy, zz));
				}
				
				rein = new MultiBlockReinforcement(locs, GroupManager.getGroup(group), durability, mature, id);
				return rein;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	public List<Reinforcement> getReinforcements(Location loc){
		String chunkId = loc.getChunk().toString();
		
	}
	*/
	/**
	 * Inserts a reinforcement into the Database.  Should only be called from
	 * SaveManager.
	 * @param The Reinforcement to save.
	 */
	protected void insertReinforcement(Reinforcement rein){
		reconnectAndReinitialize();
		
		int id = getLastReinId();
		if (rein instanceof PlayerReinforcement){
			Location loc = rein.getLocation();
			int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
			String world = loc.getWorld().getName();
			Material mat = rein.getMaterial();
			int dur = rein.getDurability();
			String chunk_id = loc.getChunk().toString();
			int maturationTime = rein.getMaturationTime();
			boolean insecure = false;
			String group = null;
			String reinType = "";
			String lore = "";
			PlayerReinforcement pRein = (PlayerReinforcement) rein;
			insecure = pRein.isInsecure();
			ItemMeta meta = pRein.getStackRepresentation().getItemMeta();
			for (String xx: meta.getLore())
				lore += xx + "\n";
			if (lore.equals(""))
				lore = null;
			group = pRein.getGroup().getName();
			reinType = "PlayerReinforcement";
			try {
				insertReinID.setInt(1, id);
				insertReinID.setInt(2, x);
				insertReinID.setInt(3, y);
				insertReinID.setInt(4, z);
				insertReinID.setString(5, world);
				insertReinID.execute();
				
				addRein.setInt(1, x);
				addRein.setInt(2, y);
				addRein.setInt(3, z);
				addRein.setString(4, world);
				addRein.setString(5, mat.name());
				addRein.setInt(6, dur);
				addRein.setString(7, chunk_id);
				addRein.setBoolean(8, insecure);
				addRein.setInt(9, maturationTime);
				addRein.setString(10, reinType);
				addRein.setString(11, lore);
				addRein.setInt(12, id);
				addRein.setString(13, group);
				addRein.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (rein instanceof NaturalReinforcement){
			Location loc = rein.getLocation();
			int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
			String world = loc.getWorld().getName();
			Material mat = rein.getMaterial();
			int dur = rein.getDurability();
			String chunk_id = loc.getChunk().toString();
			int maturationTime = rein.getMaturationTime();
			boolean insecure = false;
			String group = null;
			String reinType = "NaturalReinforcement";
			String lore = "";
			lore = null;
			try {
				insertReinID.setInt(1, id);
				insertReinID.setInt(2, x);
				insertReinID.setInt(3, y);
				insertReinID.setInt(4, z);
				insertReinID.setString(5, world);
				insertReinID.execute();
				
				addRein.setInt(1, x);
				addRein.setInt(2, y);
				addRein.setInt(3, z);
				addRein.setString(4, world);
				addRein.setString(5, mat.name());
				addRein.setInt(6, dur);
				addRein.setString(7, chunk_id);
				addRein.setBoolean(8, insecure);
				addRein.setInt(9, maturationTime);
				addRein.setString(10, reinType);
				addRein.setString(11, lore);
				addRein.setInt(12, id);
				addRein.setString(13, group);
				addRein.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (rein instanceof MultiBlockReinforcement){
			MultiBlockReinforcement mbRein = (MultiBlockReinforcement) rein;
			Location loc = mbRein.getLocation();
			try {
				// add all the locations into the db.
				for (Location lo: mbRein.getLocations()){
					insertReinID.setInt(1, id);
					insertReinID.setInt(2, lo.getBlockX());
					insertReinID.setInt(3, lo.getBlockY());
					insertReinID.setInt(4, lo.getBlockZ());
					insertReinID.setString(5, lo.getWorld().getName());
					insertReinID.addBatch();
				}
				insertReinID.executeBatch();
				
				addRein.setInt(1, loc.getBlockX());
				addRein.setInt(2, loc.getBlockY());
				addRein.setInt(3, loc.getBlockZ());
				addRein.setString(4, loc.getWorld().getName());
				addRein.setString(5, null);
				addRein.setInt(6, mbRein.getDurability());
				addRein.setString(7, null);
				addRein.setBoolean(8, false);
				addRein.setInt(9, mbRein.getMaturationTime());
				addRein.setString(10, "MultiBlockReinforcement");
				addRein.setString(11, null);
				addRein.setInt(12, id);
				addRein.setString(13, mbRein.getGroup().getName());
				addRein.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * Deletes a Reinforcement from the database. Should only be called
	 * within SaveManager
	 * @param The Reinforcement to delete.
	 */
	protected void deleteReinforcement(Reinforcement rein){
		reconnectAndReinitialize();
		
		Location loc = rein.getLocation();
		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		try {
			removeRein.setInt(1, x);
			removeRein.setInt(2, y);
			removeRein.setInt(3, z);
			removeRein.setString(4, world);
			removeRein.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Saves the Reinforcement to the Database. Should only be called
	 * from SaveManager.
	 * @param The Reinforcement to save.
	 */
	protected void saveReinforcement(Reinforcement rein){
		reconnectAndReinitialize();
		
		int dur = rein.getDurability();
		boolean insecure = false;
		String groupName = null;
		int mature = rein.getMaturationTime();
		Location loc = rein.getLocation();
		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		if (rein instanceof PlayerReinforcement){
			PlayerReinforcement pRein = (PlayerReinforcement) rein;
			insecure = pRein.isInsecure();
			groupName = pRein.getGroup().getName();
		}
		try {
			updateRein.setInt(1, dur);
			updateRein.setBoolean(2, insecure);
			updateRein.setString(3, groupName);
			updateRein.setInt(4, mature);
			updateRein.setInt(5, x);
			updateRein.setInt(6, y);
			updateRein.setInt(7, z);
			updateRein.setString(8, world);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Begins deleting the reinforcements at a group. Once this is executed
	 * the specified group loses all reinforcements. They are removed from map,
	 * no longer able to be bypassed or broken.
	 * @param The Group name that is being removed.
	 * @return Returns true if there are more records to remove, false otherwise.
	 */
	public boolean deleteGroup(String group){
		reconnectAndReinitialize();
		
		try {
			deleteGroup.setString(1, group);
			ResultSet set = deleteGroup.executeQuery();
			set.next();
			return set.getInt(1) > 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	protected void insertDeleteGroup(String group){
		reconnectAndReinitialize();
		
		try {
			insertDeleteGroup.setString(1, group);
			insertDeleteGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected List<Group> getDeleteGroups(){
		reconnectAndReinitialize();
		
		List<Group> groups = new ArrayList<Group>();
		try {
			ResultSet set = getDeleteGroup.executeQuery();
			while (set.next())
				groups.add(GroupManager.getGroup(set.getString(1)));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return groups;
	}
	
	protected void removeDeleteGroup(String group){
		reconnectAndReinitialize();
		
		try {
			removeDeleteGroup.setString(1, group);
			removeDeleteGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int getLastReinId(){
		reconnectAndReinitialize();
		
		try {
			ResultSet set = getLastReinID.executeQuery();
			set.next();
			return set.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}