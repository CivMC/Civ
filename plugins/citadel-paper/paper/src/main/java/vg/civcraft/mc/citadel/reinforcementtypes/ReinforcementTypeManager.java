package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.bukkit.inventory.ItemStack;

public class ReinforcementTypeManager {

	private Map<ItemStack, ReinforcementType> globalTypesByItem;
	private Map<ItemStack, Map<String, ReinforcementType>> localTypesByItem;
	private Map<Short, ReinforcementType> typesById;

	public ReinforcementTypeManager() {
		globalTypesByItem = new HashMap<>();
		localTypesByItem = new HashMap<>();
		typesById = new TreeMap<>();
	}

	public Collection<ReinforcementType> getAllTypes() {
		return typesById.values();
	}

	public ReinforcementType getById(short id) {
		return typesById.get(id);
	}

	public ReinforcementType getByItemStack(ItemStack is, String worldName) {
		ItemStack copy = is.clone();
		copy.setAmount(1);

		ReinforcementType foundType = globalTypesByItem.get(copy);

		if (foundType == null) { // Reinforcement type is not global, aka local
			if (localTypesByItem.get(copy) == null) { // The item type is not registered at all, so it can't have any allowed worlds
				return null;
			}

			foundType = localTypesByItem.get(copy).get(worldName);

			if (foundType == null) {
				foundType = localTypesByItem.get(copy).values().stream().filter(Objects::nonNull).findAny().orElse(null);
			}

			return foundType;
		} else { // Reinforcement type is global, and has been found
			return foundType;
		}
	}

	public boolean register(ReinforcementType type) {
		typesById.put(type.getID(), type);

		if (type.getAllowedWorlds().isEmpty()) { // Registering a global reinforcement
			if (localTypesByItem.get(type.getItem()) != null) {
				// Problem because there is already a local reinforcement for the item type
				return false;
			}

			globalTypesByItem.put(type.getItem(), type); // Types with no allowed worlds are global and able to be used everywhere

			return true;
		} else { // Registering a local reinforcement
			if (globalTypesByItem.get(type.getItem()) != null) {
				// Problem because there is already a global reinforcement for the item type
				return false;
			}

			// At this point we have verified there is not already a global reinforcement, so the local reinforcement can go ahead

			for (String allowedWorld : type.getAllowedWorlds()) { // For all the allowed worlds

				if (localTypesByItem.computeIfAbsent(type.getItem(), k -> new HashMap<>()) // Put the item in the map and create a sub-map if it doesn't already exist, if so there should not be any conflicting local-items
						.put(allowedWorld, type) != null) { // Put the allowed world, if a value is returned, there was previously something associated with the world, therefore there are two local reinforcements attempting to exist locally in a world
					return false;
				}
			}

			return true;
		}
	}

}
