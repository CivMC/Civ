package isaac.bastion.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;

public class BastionBlockStorage {
	private Database db;
	private String bationBlocksTable;
	private PreparedStatement getAllBastionsForWorld; 
	public BastionBlockStorage(){
		db=new Database("localhost", 3306, "bukkit","","","bastion_", Bastion.getPlugin().getLogger());
		bationBlocksTable="bastion_"+"blocks";
		Bastion.getPlugin().getLogger().info("db=  "+(db.getDb()));
		db.connect();
		if (db.isConnected()) {
			createTables();
			getAllBastionsForWorld=db.prepareStatement("SELECT * FROM "+bationBlocksTable+" WHERE loc_world=?;");
		}
	}
	public void createTables(){
		//Database only needs to store the loc of each block for now
		String toExicute="CREATE TABLE IF NOT EXISTS "+bationBlocksTable+" ("
				+ "bastion_id int(10)  unsigned NOT NULL AUTO_INCREMENT,"
				+ "loc_x int(10), "
				+ "loc_y int(10), "
				+ "loc_z int(10), "
				+ "loc_world varchar(40) NOT NULl, "
				+ "placed bigint(20) Unsigned, "
				+ "fraction float(20) Unsigned, "
				+ "PRIMARY KEY (`bastion_id`)"
				+ ");";
		Bastion.getPlugin().getLogger().info(toExicute);
		db.execute(toExicute);
	}
	public void saveBastionBlocks(Set<BastionBlock> blocks){
		Bastion.getPlugin().getLogger().info("saveBastionBlocks for "+blocks.size()+" blocks");
		for(BastionBlock block: blocks){
			saveBastionBlock(block);
		}
	}
	public void saveBastionBlock(BastionBlock block){
		db.execute("DELETE FROM "+bationBlocksTable+" WHERE bastion_id="+block.getID()+";");
		if(!block.ghost()){
			String toExicute="INSERT INTO "+bationBlocksTable+" VALUES("
					+block.getID()+","
					+block.getLocation().getBlockX()+","
					+block.getLocation().getBlockY()+","
					+block.getLocation().getBlockZ()+","
					+"'"+block.getLocation().getWorld().getName()+"',"
					+block.getPlaced()+","
					+block.getBalance()
					+");";
			Bastion.getPlugin().getLogger().info(toExicute);
			db.execute(toExicute);
		}
	}
	public Enumeration<BastionBlock> getAllSnitches(World world) {
		return new BastionBlockEnumerator(world);
	}
	class BastionBlockEnumerator implements Enumeration<BastionBlock>{
		World world;
		ResultSet result;
		BastionBlock next; 
		public BastionBlockEnumerator(World nWorld){
			world=nWorld;
			try{
				getAllBastionsForWorld.setString(1, world.getName());
				result=getAllBastionsForWorld.executeQuery();
				next=nextBastionBlock();
				Bastion.getPlugin().getLogger().info("next==null "+(next==null));
			} catch(SQLException e){
				Bastion.getPlugin().getLogger().info("couldn't get Bastions \n"+e.getMessage());
				next=null;
				result=null;
			}
		}
		@Override
		public boolean hasMoreElements() {
			return (next!=null);
		}

		@Override
		public BastionBlock nextElement() {
			BastionBlock result=next;
			next=nextBastionBlock();
			return result;
		}
		public BastionBlock nextBastionBlock() {
			int x,y,z,id;
			long placed;
			float balance;
			try {
				if (result == null || !result.next()) {
					result = null;
					return null;
				}
				x=result.getInt("loc_x");
				y=result.getInt("loc_y");
				z=result.getInt("loc_z");
				id=result.getInt("bastion_id");
				placed=result.getLong("placed");
				balance=result.getFloat("fraction");

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			Location loc=new Location(world, x, y, z);
			return new BastionBlock(loc,placed,balance,id);
		}


	}
}
