package vg.civcraft.mc.citadel.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.NaturalReinforcement;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;

public class CitadelReinforcementData {

	private Database db;
	private Citadel plugin;
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
				+ "set group_idd = (select g.group_id from faction_id g where g.group_name = deleteGroup;"
				+ "delete from reinforcement r "
				+ "where r.group_id = group_idd limit 50;"
				+ "select count(*) as count from reinforcement "
				+ "where group_name = deleteGroup;"
				+ "end;");
	}
	/**
	 * Creates the required mysql tables and updates the db if needed.
	 */
	private void createTables(){
		int ver = NameLayerPlugin.getVersionNum(plugin.getName());
		if (ver == 0){
			db.execute("update db_version set plugin_name = ? " +
					"where plugin_name = null");
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
						"material_id int not null," +
						"primary key (id));");
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
				db.execute("alter table reinforcement add group_id int;");
				db.execute("update reinforcement ");
				db.execute("insert into faction_id (group_name) values (null);"); // For natural reinforcements
				db.execute("alter table reinforcement add group_id int not null;");
				db.execute("update reinfrocement r set r.group_id = (select f.group_id from "
						+ "faction_id f where f.group_name = r.`name`);");
				db.execute("alter table reinforcement drop `name`;");
				db.execute("delete from reinforcement where name = null;");
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
		}
	}
	/**
	 * Reconnects and reinitializes the mysql connection and preparedstatements.
	 */
	private void reconnectAndReinitialize(){
		db.connect();
		initalizePreparedStatements();
	}
	
	private PreparedStatement getRein, getReins, addRein, removeRein, updateRein;
	private PreparedStatement deleteGroup, insertDeleteGroup, removeDeleteGroup, getDeleteGroup;
	/**
	 * Initializes the PreparedStatements. Gets called on db connect or
	 * reconnect.
	 */
	private void initalizePreparedStatements(){
		// update all these statements for group_ids
		getRein = db.prepareStatement("select r.material, r.durability, " +
				"r.insecure, f.group_name, r.maturation_time from reinforcement r "
				+ "inner join faction_id f on f.group_id = r.group_id" +
				"where r.x = ? and r.y = ? and r.z = ?");
		getReins = db.prepareStatement("select r.material, r.durability, " +
				"r.insecure, f.group_name, r.maturation_time from reinforcement r "
				+ "inner join faction_id f on f.group_id = r.group_id " +
				"where chunk_id = ?");
		addRein = db.prepareStatement("insert into reinforcement ("
				+ "x, y, z, world, material, durability, chunk_id,"
				+ "insecure, group_id, maturation_time, rein_type,"
				+ "lore) select ?, ?, ?, ?, ?, ?, ?, ?, f.group_id, ?, ?, ? from faction_id f "
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
		if (!db.isConnected())
			reconnectAndReinitialize();
		try {
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			getRein.setInt(1, x);
			getRein.setInt(2, y);
			getRein.setInt(3, z);
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
	 * @param A boolean set to true if it should flush or false if not.
	 */
	protected void insertReinforcement(Reinforcement rein, boolean execute){
		if (!db.isConnected())
			reconnectAndReinitialize();
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
		if (rein instanceof PlayerReinforcement){
			PlayerReinforcement pRein = (PlayerReinforcement) rein;
			insecure = pRein.isInsecure();
			ItemMeta meta = pRein.getStackRepresentation().getItemMeta();
			for (String xx: meta.getLore())
				lore += xx + "\n";
			if (lore.equals(""))
				lore = null;
			group = pRein.getGroup().getName();
			reinType = "PlayerReinforcement";
		}
		else if (rein instanceof NaturalReinforcement){
			lore = null;
		}
		try {
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
			addRein.setString(12, group);
			addRein.addBatch();
			if (execute)
				addRein.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Deletes a Reinforcement from the database. Should only be called
	 * within SaveManager
	 * @param The Reinforcement to delete.
	 * @param If it should execute to database or store as a batch.
	 */
	protected void deleteReinforcement(Reinforcement rein, boolean execute){
		if (!db.isConnected())
			reconnectAndReinitialize();
		Location loc = rein.getLocation();
		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		String world = loc.getWorld().getName();
		try {
			removeRein.setInt(1, x);
			removeRein.setInt(2, y);
			removeRein.setInt(3, z);
			removeRein.setString(4, world);
			removeRein.addBatch();
			if (execute)
				removeRein.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Saves the Reinforcement to the Database. Should only be called
	 * from SaveManager.
	 * @param The Reinforcement to save.
	 * @param True if it should execute to db or false if it should be added to a batch.
	 */
	protected void saveReinforcement(Reinforcement rein, boolean execute){
		if (!db.isConnected())
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
			updateRein.addBatch();
			if (execute)
				updateRein.executeBatch();
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
		if (!db.isConnected())
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
		try {
			insertDeleteGroup.setString(1, group);
			insertDeleteGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected List<Group> getDeleteGroups(){
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
		try {
			removeDeleteGroup.setString(1, group);
			removeDeleteGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}