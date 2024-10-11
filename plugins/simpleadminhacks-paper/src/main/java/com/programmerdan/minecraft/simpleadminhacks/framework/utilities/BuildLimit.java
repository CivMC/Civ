package com.programmerdan.minecraft.simpleadminhacks.framework.utilities;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class BuildLimit implements ConfigurationSerializable {
	private String world;
	private String type;
	private int minY;
	private int maxY;

	public BuildLimit (String world, String type, int minY, int maxY){
		this.world = world;
		this.type = type;
		this.minY = minY;
		this.maxY = maxY;
	}

	public String getWorld() {
		return this.world;
	}
	public String getType() {
		return this.type;
	}
	public int getMinY() {
		return this.minY;
	}
	public int getMaxY() {
		return this.maxY;
	}

	public void setWorld(String world) {
		this.world = world;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setMinY(int minY) {
		this.minY = minY;
	}
	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	@NotNull
	@Override
	public final Map<String, Object> serialize(){
		Map<String, Object> result = new LinkedHashMap<>();

		result.put("world", this.world);
		result.put("type", this.type);
		result.put("min_y", this.minY);
		result.put("max_y", this.maxY);

		return result;
	}
	@NotNull
	public static BuildLimit deserialize(@NotNull Map<String, Object> args){
		String world = "world";
		String type = "altitude";
		int minY = 0;
		int maxY = 0;

		world = (String) args.get("world");
		type = (String) args.get("type");

		if(type.equals("altitude")){
			if(args.containsKey("min_y")){
				minY = (Integer) args.get("min_y");
			}
			if(args.containsKey("max_y")){
				maxY = (Integer) args.get("max_y");
			}
		}

		return new BuildLimit(world, type, minY, maxY);
	}
}
