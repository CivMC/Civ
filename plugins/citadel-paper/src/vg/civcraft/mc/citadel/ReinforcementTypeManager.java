package vg.civcraft.mc.citadel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;

public class ReinforcementTypeManager {
	
	private Map<ItemStack, ReinforcementType> typesByItem;
	private Map<Integer, ReinforcementType> typesById;
	
	public ReinforcementTypeManager() {
		typesByItem = new HashMap<>();
		typesById = new TreeMap<>();
	}
	
	public void register(ReinforcementType type) {
		typesByItem.put(type.getItem(), type);
		typesById.put(type.getID(), type);
	}
	
	public ReinforcementType getById(int id) {
		return typesById.get(id);
	}
	
	public ReinforcementType getByItemStack(ItemStack is) {
		ItemStack copy = is.clone();
		copy.setAmount(1);
		return typesByItem.get(copy);
	}
	
	public Collection<ReinforcementType> getAllTypes() {
		return typesById.values();
	}

}
