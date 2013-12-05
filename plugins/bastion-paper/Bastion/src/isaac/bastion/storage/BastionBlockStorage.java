package isaac.bastion.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;

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
				+ "bastion_id int(10),"
				+ "loc_x int(10),"
				+ "loc_y int(10),"
				+ "loc_z int(10),"
				+ "loc_world varchar(40) NOT NULL);";
		Bastion.getPlugin().getLogger().info(toExicute);
		db.execute(toExicute);
	}
	public void saveBastionBlocks(Map<Integer,BastionBlock> bastions){
		Bastion.getPlugin().getLogger().info("saveBastionBlocks for "+BastionBlock.getHighestID()+1+" blocks");
		for(int i=0;i<BastionBlock.getHighestID()+1;++i){
			BastionBlock current=bastions.get(i);
			if(current!=null){
				saveBastionBlock(current);
				Bastion.getPlugin().getLogger().info("Saved");
			}
		}
	}
	public void saveBastionBlock(BastionBlock block){
		if(!block.loaded()){
			db.execute("DELETE FROM "+bationBlocksTable+" WHERE bastion_id="+block.getID()+";");
			if(!block.ghost()){
				db.execute("INSERT INTO "+bationBlocksTable+" VALUES("
						+block.getID()+","
						+block.getLocation().getBlockX()+","
						+block.getLocation().getBlockY()+","
						+block.getLocation().getBlockZ()+","
						+"'"+block.getLocation().getWorld().getName()+"'"+");");
			}
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
			try {
                if (result == null || !result.next()) {
                	result = null;
                    return null;
                }
				x=result.getInt("loc_x");
				y=result.getInt("loc_y");
				z=result.getInt("loc_z");
				id=result.getInt("bastion_id");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			Location loc=new Location(world, x, y, z);
			return new BastionBlock(loc,id);
		}


	}
}
