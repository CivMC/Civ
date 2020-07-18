package vg.civcraft.mc.civmodcore.playersettings.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class JsonSetting extends PlayerSetting <JsonObject> {
	
	private final JsonParser parser;

	public JsonSetting(JavaPlugin owningPlugin, JsonObject defaultValue, String identifier) {
		super(owningPlugin, defaultValue, "", identifier, new ItemStack(Material.STONE), "", false);
		parser = new JsonParser();
	}

	@Override
	public JsonObject deserialize(String serial) {
		return (JsonObject) parser.parse(serial);
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			JsonObject result = (JsonObject) parser.parse(input);
		}
		catch (JsonSyntaxException | ClassCastException e) {
			return false;
		}
		return true;
	}

	@Override
	public String serialize(JsonObject value) {
		return value.toString();
	}

	@Override
	public String toText(JsonObject value) {
		return value.toString();
	}

}
