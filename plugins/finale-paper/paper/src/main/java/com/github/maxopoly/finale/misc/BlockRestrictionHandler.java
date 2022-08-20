package com.github.maxopoly.finale.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockRestrictionHandler {

	public enum RestrictionMode {

		BLACKLIST, WHITELIST, NOLIST;

	}

	private boolean enabled;
	private RestrictionMode mode;
	private Map<Material, Integer> zoneRadii;
	private List<Material> blacklist;
	private List<Material> whitelist;
	private Map<Material, CooldownHandler> materialCooldownHandlers;

	public BlockRestrictionHandler(boolean enabled, RestrictionMode mode, Map<Material, Integer> zoneRadii, List<Material> blacklist, List<Material> whitelist, Map<Material, CooldownHandler> materialCooldownHandlers) {
		this.enabled = enabled;
		this.mode = mode;
		this.zoneRadii = zoneRadii;
		this.blacklist = blacklist;
		this.whitelist = whitelist;
		this.materialCooldownHandlers = materialCooldownHandlers;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public RestrictionMode getMode() {
		return mode;
	}

	public Map<Material, Integer> getZoneRadii() {
		return zoneRadii;
	}

	public List<Material> getBlacklist() {
		return blacklist;
	}

	public List<Material> getWhitelist() {
		return whitelist;
	}

	public Map<Material, Long> getMaterialCooldowns() {
		Map<Material, Long> result = new HashMap<>();
		for (Map.Entry<Material, CooldownHandler> materialCooldownHandlerEntry : materialCooldownHandlers.entrySet()) {
			result.put(materialCooldownHandlerEntry.getKey(), materialCooldownHandlerEntry.getValue().getCooldown());
		}
		return result;
	}

	public void putOnCooldown(Player player, Material material) {
		if (!materialCooldownHandlers.containsKey(material)) {
			return;
		}

		CooldownHandler cooldownHandler = materialCooldownHandlers.get(material);
		cooldownHandler.putOnCooldown(player);
	}

	public boolean isOnCooldown(Player player, Material material) {
		CooldownHandler cooldownHandler = materialCooldownHandlers.get(material);
		if (cooldownHandler == null) {
			return false;
		}
		return cooldownHandler.onCooldown(player);
	}

}
