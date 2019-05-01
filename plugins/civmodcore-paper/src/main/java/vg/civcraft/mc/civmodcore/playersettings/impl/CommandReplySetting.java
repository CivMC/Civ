package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

public class CommandReplySetting extends StringSetting {
	
	private class CommandArgument {
		String identifier;
		String defaultValue;
		String description;
		
		CommandArgument(String identifier, String defaultValue, String description) {
			this.identifier = identifier;
			this.defaultValue = defaultValue;
			this.description = description;
		}
	}
	
	private Map<String, CommandArgument> exampleArguments;

	public CommandReplySetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		super(plugin, defaultValue, name, identifier, gui, description);
		exampleArguments = new TreeMap<>();
	}
	
	public void registerArgument(String identifier, String defaultValue, String description) {
		this.exampleArguments.put(identifier, new CommandArgument(identifier, defaultValue, description));
	}

	public String formatReply(UUID uuid, Map<String, String> arguments) {
		String reply = getValue(uuid);
		if (reply.equals("null")) {
			return "";
		}
		for (Entry<String, String> entry : arguments.entrySet()) {
			String toInsert = entry.getValue().replace("%%", "0");
			reply = reply.replace("%%" + entry.getKey() + "%%", toInsert);
		}
		return reply;
	}
	
	public String getExampleReply(UUID uuid) {
		String reply = getValue(uuid);
		if (reply.equals("null")) {
			return "";
		}
		for (CommandArgument cArg : exampleArguments.values()) {
			reply = reply.replace("%%" + cArg.identifier + "%%", cArg.defaultValue);
		}
		return reply;
	}
	
	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack item = super.getGuiRepresentation(player);
		ISUtils.addLore(item, ChatColor.GOLD + "Example: " + ChatColor.RESET + getExampleReply(player));
		for(CommandArgument cArg : exampleArguments.values()) {
			ISUtils.addLore(item, ChatColor.BLACK + "-------");
			ISUtils.addLore(item, ChatColor.YELLOW + "%%" + cArg.identifier + "%% will be replaced with "  + cArg.description);
		}
		return item;
	}
}
