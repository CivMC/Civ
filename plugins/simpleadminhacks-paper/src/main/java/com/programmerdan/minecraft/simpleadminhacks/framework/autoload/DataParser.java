package com.programmerdan.minecraft.simpleadminhacks.framework.autoload;

import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

public enum DataParser {

	DEFAULT,

	MATERIAL {
		@Override
		public Object parse(final Object value) {
			return MaterialAPI.getMaterial(stringify(value));
		}
	},

	POTION_EFFECT_TYPE {
		@Override
		public Object parse(final Object value) {
			return PotionEffectType.getByName(stringify(value));
		}
	};

	public Object parse(final Object value) {
		return value;
	}

	private static String stringify(final Object value) {
		return value == null ? "" : value.toString();
	}

}
