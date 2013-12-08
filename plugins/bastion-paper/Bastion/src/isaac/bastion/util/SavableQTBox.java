package isaac.bastion.util;


import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.World;

public abstract class SavableQTBox extends Savable implements QTBox{
	protected int radius;
	protected Location location;
	static Map<World,SparseQuadTree> getTree(){
		throw new IllegalStateException("getTree not implemented");
	}
	protected SavableQTBox(Location newLocation,int newRadius, int id){
		super(id);
		location=newLocation;
		radius=newRadius;
	}
	
	public static Set<QTBox> getPossible(Location position){
		return getTree().get(position.getWorld()).find(position.getBlockX(), position.getBlockZ());
	}
	public static SavableQTBox get(Location position){
		Set<QTBox> possible=getPossible(position);
		for(QTBox box:possible){
			SavableQTBox saveable=(SavableQTBox) box;
			if(saveable.location==position){
				return saveable;
			}
		}
		return null;
	}
	
	@Override
	public int close(){
		super.close();
		getTree().get(location.getWorld()).remove(this);
		return 0;
	}

	@Override
	public int qtXMin() {
		return location.getBlockX()-radius;
	}

	@Override
	public int qtXMid() {
		return location.getBlockX();
	}

	@Override
	public int qtXMax() {
		return location.getBlockX()+radius;
	}

	@Override
	public int qtZMin() {
		return location.getBlockZ()-radius;
	}

	@Override
	public int qtZMid() {
		return location.getBlockZ();
	}

	@Override
	public int qtZMax() {
		return location.getBlockZ()+radius;
	}
	public static Set<String> getFields(){
		Set<String> result=Savable.getFields();
		result.add(getPrefix()+"_x  int(10)");
		result.add(getPrefix()+"_y  int(10)");
		result.add(getPrefix()+"_z  int(10)");
		return result;
	}
	protected abstract int getNewId();
	protected abstract int save();
}
