package vg.civcraft.mc.civduties.configuration;

import java.util.List;
import java.util.Map;

public class Tier {
	private String name;
	private String permission;
	private int priority;
	private List<Command> commands;
	private Map<String, Boolean> temporaryPermissions;
	private List<String> temporaryGroups;
	private boolean deathDrops;
	private boolean combattagBlock;

	public Tier(String name, int priority, String permission, List<Command> commands,
			Map<String, Boolean> temporaryPermissions, List<String> temporaryGroups, boolean deathDrops,
			boolean combattagBlock) {
		this.name = name;
		this.priority = priority;
		this.permission = permission;
		this.commands = commands;
		this.temporaryPermissions = temporaryPermissions;
		this.temporaryGroups = temporaryGroups;
		this.deathDrops = deathDrops;
		this.combattagBlock = combattagBlock;
	}

	public String getName() {
		return name;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public String getPermission() {
		return permission;
	}

	public List<Command> getCommands() {
		return commands;
	}

	public Map<String, Boolean> getTemporaryPermissions() {
		return temporaryPermissions;
	}

	public List<String> getTemporaryGroups() {
		return temporaryGroups;
	}

	public boolean isDeathDrops() {
		return deathDrops;
	}

	public boolean isCombattagBlock() {
		return combattagBlock;
	}
}
