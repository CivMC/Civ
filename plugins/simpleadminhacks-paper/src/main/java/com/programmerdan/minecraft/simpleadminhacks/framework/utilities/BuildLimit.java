package com.programmerdan.minecraft.simpleadminhacks.framework.utilities;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class BuildLimit implements ConfigurationSerializable {
	private String world;
	private String type;
	private int min_y;
	private int max_y;

	public BuildLimit (String world, String type, int min_y, int max_y){
		this.world = world;
		this.type = type;
		this.min_y = min_y;
		this.max_y = max_y;
	}

	public String getWorld() {
		return this.world;
	}
	public String getType() {
		return this.type;
	}
	public int getMin_y() {
		return this.min_y;
	}
	public int getMax_y() {
		return this.max_y;
	}

	public void setWorld(String world) {
		this.world = world;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setMin_y(int min_y) {
		this.min_y = min_y;
	}
	public void setMax_y(int max_y) {
		this.max_y = max_y;
	}

	@NotNull
	@Override
	public final Map<String, Object> serialize(){
		Map<String, Object> result = new LinkedHashMap<>();

		result.put("world", this.world);
		result.put("type", this.type);
		result.put("min_y", this.min_y);
		result.put("max_y", this.max_y);

		return result;
	}
	@NotNull
	public static BuildLimit deserialize(@NotNull Map<String, Object> args){
		String world = "world";
		String type = "altitude";
		int min_y = 0;
		int max_y = 0;

		world = (String) args.get("world");
		type = (String) args.get("type");

		if(type.equals("altitude")){
			if(args.containsKey("min_y")){
				min_y = (Integer) args.get("min_y");
			}
			if(args.containsKey("max_y")){
				max_y = (Integer) args.get("max_y");
			}
		}

		return new BuildLimit(world, type, min_y, max_y);
	}
}
