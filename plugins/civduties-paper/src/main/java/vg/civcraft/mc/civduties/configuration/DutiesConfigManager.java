package vg.civcraft.mc.civduties.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civduties.CivDuties;
import vg.civcraft.mc.civduties.configuration.Command.Executor;
import vg.civcraft.mc.civduties.configuration.Command.Timing;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class DutiesConfigManager extends ConfigParser {
	private ManagedDatasource db;
	private List<Tier> tiers;
	
	public DutiesConfigManager(CivDuties plugin){
		super(plugin);
	}
	
	private void parseTiers(ConfigurationSection config){
		tiers = new ArrayList<>();
		for (String key : config.getKeys(false)) {
			if (config.getConfigurationSection(key) == null) {
				CivDuties.getInstance().warning("Found invalid section that should not exist at " + config.getCurrentPath() + key);
				continue;
			}
			Tier tier = parseTier(key, config.getConfigurationSection(key));
			if (tier == null) {
				CivDuties.getInstance().warning(String.format("Tier %s unable to be added.", key));
			} else {
				tiers.add(tier);
			}
		}
	}
	
	private Tier parseTier(String name, ConfigurationSection config){
		String permission = config.getString("permission");
		int priority = config.getInt("priority");
		List<Command> commands = parseCommands(config.getConfigurationSection("commands"));
		Map<String, Boolean> temporaryPermissions = new HashMap<>();
		for(String temporaryPermission : config.getStringList("temporary.permissions")){
			String[] array = temporaryPermission.split(":");
			if(array.length > 1){
				temporaryPermissions.put(array[0], Boolean.valueOf(array[1]));
				continue;
			}
			temporaryPermissions.put(array[0], true);
		}
		List<String> temporaryGroups = config.getStringList("temporary.groups");
		boolean deathDrops = config.getBoolean("disable_death_drops");
		boolean combattagBlock = config.getBoolean("enable_combattag_block");
		return new Tier(name, priority, permission, commands, temporaryPermissions, temporaryGroups, deathDrops, combattagBlock);
	}
	
	private List<Command> parseCommands(ConfigurationSection config){
		if(config == null){
			return new ArrayList<>();
		}
		List<Command> commands = new ArrayList<>();
		for (String key : config.getKeys(false)) {
			if (config.getConfigurationSection(key) == null) {
				CivDuties.getInstance().warning("Found invalid section that should not exist at " + config.getCurrentPath() + key);
				continue;
			}
			Command command = parseCommand(config.getConfigurationSection(key));
			if (command == null) {
				CivDuties.getInstance().warning(String.format("Tier %s unable to be added.", key));
			} else {
				commands.add(command);
			}
		}
		return commands;
	}
	
	private Command parseCommand(ConfigurationSection config){
		String syntax = config.getString("syntax");
		Timing timing = Timing.valueOf(config.getString("timing"));
		Executor executor = Executor.valueOf(config.getString("executor"));
		return new Command(syntax, timing, executor);
	}
	
	public Tier getTier(Player player){
		Tier tier = null;
		int maxPriority = Integer.MIN_VALUE;
		for(Tier t : tiers){
			if((t.getPermission() == null || player.hasPermission(t.getPermission())) && (tier == null || t.getPriority() > maxPriority)){
				tier = t;
				maxPriority = t.getPriority();
			}
		}
		return tier;
	}
	
	public Tier getTier(String tierName){
		for(Tier tier : tiers){
			if(tier.getName().equals(tierName)){
				return tier;
			}
		}
		return null;
	}
	
	public List<String> getTiersNames(Player player){
		List<String> names = new ArrayList<>();
		for(Tier tier : tiers){
			if(tier.getPermission() == null || player.hasPermission(tier.getPermission())){
				names.add(tier.getName());
			}
		}
		return names;
	}
	
	public ManagedDatasource getDatabase(){
		return db;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		parseTiers(config.getConfigurationSection("tiers"));
		db = ManagedDatasource.construct((ACivMod) plugin, (DatabaseCredentials) config.get("database"));
		return true;
	}	

}
