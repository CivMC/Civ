package com.programmerdan.minecraft.simpleadminhacks.framework.utilities;

import com.programmerdan.minecraft.simpleadminhacks.configs.buildlimit.LimitType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record BuildLimit(String world, String type, int minY, int maxY) implements ConfigurationSerializable {

	@NotNull
	@Override
	public Map<String, Object> serialize(){
		Map<String, Object> result = new HashMap<>();

		result.put("world", this.world);
		result.put("type", this.type);
		result.put("min_y", this.minY);
		result.put("max_y", this.maxY);

		return result;
	}
	@Nullable
	public static BuildLimit deserialize(@NotNull final Map<String, Object> args){
		String world = (String) args.get("world");

        String type = (String) args.get("type");
        LimitType limitType = LimitType.valueOf(type);

        int minY = 0;
		int maxY = 0;
		if(limitType == LimitType.ALTITUDE){
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
