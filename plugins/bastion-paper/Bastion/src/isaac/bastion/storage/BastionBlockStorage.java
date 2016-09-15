package isaac.bastion.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import org.bukkit.Location;
import org.bukkit.World;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.manager.ConfigManager;

public class BastionBlockStorage {
	public static Database db;
	static public String bastionBlocksTable;
	private PreparedStatement getAllBastionsForWorld;
	private static boolean tablesCreated = false;
	public BastionBlockStorage(){
		ConfigManager config=Bastion.getConfigManager();
		db = new Database(config.getHost(), config.getPort(), config.getDatabase(),config.getUsername(),config.getPassword(),config.getPrefix(), Bastion.getPlugin().getLogger());
		bastionBlocksTable = "bastion_blocks";
		db.connect();
		if (db.isConnected()) {
			createTables();
			tablesCreated = true;
			prepareStatements();
			getAllBastionsForWorld = db.prepareStatement("SELECT * FROM "+bastionBlocksTable+" WHERE loc_world=?;");
		}
	}
	
	public static PreparedStatement insertBastion;
	public static PreparedStatement updateBastion;
	public static PreparedStatement deleteBastion;
	
	public static void prepareStatements() {
		if (tablesCreated) {
			insertBastion = db.prepareStatement("INSERT INTO "+BastionBlockStorage.bastionBlocksTable+" (loc_x,loc_y,loc_z,loc_world,type,placed,fraction) VALUES(?,?,?,?,?,?,?);");
			updateBastion = db.prepareStatement("UPDATE "+BastionBlockStorage.bastionBlocksTable+" set placed=?,fraction=? where bastion_id=?;");
			deleteBastion = db.prepareStatement("DELETE FROM "+BastionBlockStorage.bastionBlocksTable+" WHERE bastion_id=?;");
		}
	}
	
	public void createTables(){
		//Database only needs to store the loc of each block for now
		String toExicute="CREATE TABLE IF NOT EXISTS "+bastionBlocksTable+" ("
				+ "bastion_id int(10)  unsigned NOT NULL AUTO_INCREMENT,"
				+ "loc_x int(10), "
				+ "loc_y int(10), "
				+ "loc_z int(10), "
				+ "loc_world varchar(40) NOT NULL, "
				+ "type varchar(40) NOT NULL"
				+ "placed bigint(20) Unsigned, "
				+ "fraction float(20) Unsigned, "
				+ "PRIMARY KEY (`bastion_id`)"
				+ ");";
		db.execute(toExicute);
	}
	public Enumeration<BastionBlock> getAllBastions(World world) {
		return new BastionBlockEnumerator(world);
	}
	
	class BastionBlockEnumerator implements Enumeration<BastionBlock> {
		World world;
		ResultSet result;
		BastionBlock next; 
		public BastionBlockEnumerator (World nWorld) {
			world = nWorld;
			if (!db.isConnected()) {
				next = null;
				return;
			}
			try {
				getAllBastionsForWorld.setString(1, world.getName());
				result=getAllBastionsForWorld.executeQuery();
				next=nextBastionBlock();
			} catch(SQLException e) {
				next=null;
				result=null;
			}
		}
		
		@Override
		public boolean hasMoreElements() {
			return (next != null);
		}

		@Override
		public BastionBlock nextElement() {
			BastionBlock result = next;
			next = nextBastionBlock();
			return result;
		}
		
		public BastionBlock nextBastionBlock() {
			int x,y,z,id;
			long placed;
			float balance;
			BastionType type;
			try {
				if (result == null || !result.next()) {
					result = null;
					return null;
				}
				x = result.getInt("loc_x");
				y = result.getInt("loc_y");
				z = result.getInt("loc_z");
				id = result.getInt("bastion_id");
				type = BastionType.getBastionType(result.getString("type"));
				placed = result.getLong("placed");
				balance = result.getFloat("fraction");
				
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			Location loc = new Location(world, x, y, z);
			return new BastionBlock(loc, placed, balance, id, type);
		}

		public Database getDatabase(){
			return db;
		}
	}
}
