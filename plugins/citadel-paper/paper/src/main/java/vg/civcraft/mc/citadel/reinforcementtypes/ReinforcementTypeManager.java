package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.inventory.ItemStack;

public class ReinforcementTypeManager {

	private Map<ItemStack, ReinforcementType> typesByItem;
	private Map<Short, ReinforcementType> typesById;

	public ReinforcementTypeManager() {
		typesByItem = new HashMap<>();
		typesById = new TreeMap<>();
	}

	public Collection<ReinforcementType> getAllTypes() {
		return typesById.values();
	}

	public ReinforcementType getById(short id) {
		return typesById.get(id);
	}

	public ReinforcementType getByItemStack(ItemStack is) {
		ItemStack copy = is.clone();
		copy.setAmount(1);
		return typesByItem.get(copy);
	}

	public void register(ReinforcementType type) {
		typesByItem.put(type.getItem(), type);
		typesById.put(type.getID(), type);
	}

}
