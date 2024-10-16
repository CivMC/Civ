package com.programmerdan.minecraft.simpleadminhacks.framework.autoload;

import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public enum DataParser {

	DEFAULT,

	MATERIAL {
		@Override
		public Object parse(final Object value) {
			return MaterialUtils.getMaterial(stringify(value));
		}
	},

	POTION_EFFECT_TYPE {
		@Override
		public Object parse(final Object value) {
			return PotionEffectType.getByName(stringify(value));
		}
	},

	ENTITY_TYPE {
		@Override
		public Object parse(final Object value) {
			try {
				return EntityType.valueOf(stringify(value));
			}
			catch (final Exception exception) {
				return null;
			}
		}
	};

	public Object parse(final Object value) {
		return value;
	}

	private static String stringify(final Object value) {
		return value == null ? "" : value.toString();
	}

}
