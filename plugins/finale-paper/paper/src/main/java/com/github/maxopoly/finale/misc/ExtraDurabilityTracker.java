package com.github.maxopoly.finale.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExtraDurabilityTracker {

	private ArmourModifier armourModifier;
	private Map<UUID, Map<Material, Integer>> playerExtraHits = new HashMap<>();

	public ExtraDurabilityTracker(ArmourModifier armourModifier) {
		this.armourModifier = armourModifier;
	}

	// returns true if durability should reduce, false if durability shouldn't reduce.
	public boolean reduceDurability(Player player, Material material) {
		Map<Material, Integer> materialExtraHits = playerExtraHits.getOrDefault(player.getUniqueId(), new HashMap<>());
		int extraHits = materialExtraHits.containsKey(material) ? materialExtraHits.get(material) : armourModifier.getExtraDurabilityHits(material);
		boolean reduce = extraHits <= 0;
		if (reduce) {
			materialExtraHits.put(material, armourModifier.getExtraDurabilityHits(material));
		} else {
			materialExtraHits.put(material, extraHits - 1);
		}
		playerExtraHits.put(player.getUniqueId(), materialExtraHits);
		return reduce;
	}

	public void clear(Player player) {
		playerExtraHits.remove(player.getUniqueId());
	}

}
