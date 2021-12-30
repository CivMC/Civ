package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.inventory.ItemStack;

public class ReinforcementTypeManager {

	private Map<Short, ReinforcementType> typesById;
	private Map<String, Map<ItemStack, ReinforcementType>> typesByWorld;

	public ReinforcementTypeManager() {
		typesByWorld = new HashMap<>();
		typesById = new TreeMap<>();
	}

	public Collection<ReinforcementType> getAllTypes() {
		return typesById.values();
	}

	public ReinforcementType getById(short id) {
		return typesById.get(id);
	}

	public ReinforcementType getByItemStackAndWorld(ItemStack is, String worldName)
	{
		ItemStack copy = is.clone();
		copy.setAmount(1);
		return typesByWorld.getOrDefault(worldName, new HashMap<>()).get(copy);
	}

	public void register(ReinforcementType type) {
		type.getAllowedWorlds().forEach(allowedWorld -> typesByWorld.computeIfAbsent(allowedWorld, k -> new HashMap<>()).put(type.getItem(), type));
		typesById.put(type.getID(), type);
	}

}
