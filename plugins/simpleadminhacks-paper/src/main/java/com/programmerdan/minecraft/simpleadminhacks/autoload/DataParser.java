package com.programmerdan.minecraft.simpleadminhacks.autoload;

import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

public enum DataParser {

	DEFAULT,

	MATERIAL {
		@Override
		public Object parse(Object value) {
			return MaterialAPI.getMaterial("" + value);
		}
	},

	POTION_EFFECT_TYPE {
		@Override
		public Object parse(Object value) {
			return PotionEffectType.getByName("" + value);
		}
	},

	;

	public Object parse(Object value) {
		return value;
	}

}
