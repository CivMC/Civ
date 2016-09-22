package com.devotedmc.ExilePearl;

import com.devotedmc.ExilePearl.util.Guard;

public enum ExileRule {
	PEARL_RADIUS("PEARL_RADIUS"),
	DAMAGE_REINFORCEMENT("DAMAGE_REINFORCEMENT"),
	DAMAGE_BASTION("DAMAGE_BASTION"),
	ENTER_BASTION("ENTER_BASTION"),
	THROW_PEARL("THROW_PEARL"),
	CHAT("CHAT"),
	PVP("PVP"),
	IGNITE("IGNITE"),
	USE_BUCKET("USE_BUCKET"),
	PLACE_WATER("PLACE_WATER"),
	PLACE_LAVA("PLACE_LAVA"),
	USE_POTIONS("USE_POTIONS"),
	USE_BED("USE_BED"),
	SUICIDE("SUICIDE"),
	SNITCH("SNITCH"),
	MINE("MINE"),
	BREW("BREW"),
	ENCHANT("ENCHANT")
	;
	
	private final String text;
	
	private ExileRule(final String text) {
		Guard.ArgumentNotNullOrEmpty(text, "text");
		
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public static ExileRule fromString(String text) {
		switch (text.toUpperCase()) {
		
		case "PEARL_RADIUS":
			return PEARL_RADIUS;
		case "DAMAGE_REINFORCEMENT":
			return DAMAGE_REINFORCEMENT;
		case "DAMAGE_BASTION":
			return DAMAGE_BASTION;
		case "ENTER_BASTION":
			return ENTER_BASTION;
		case "THROW_PEARL":
			return THROW_PEARL;
		case "CHAT":
			return CHAT;
		case "PVP":
			return PVP;
		case "IGNITE":
			return IGNITE;
		case "USE_BUCKET":
			return USE_BUCKET;
		case "PLACE_WATER":
			return PLACE_WATER;
		case "PLACE_LAVA":
			return PLACE_LAVA;
		case "USE_POTIONS":
			return USE_POTIONS;
		case "USE_BED":
			return USE_BED;
		case "SUICIDE":
			return SUICIDE;
		case "SNITCH":
			return SNITCH;
		case "MINE":
			return MINE;
		case "BREW":
			return BREW;
		case "ENCHANT":
			return ENCHANT;
		
		default:
			return null;
		}
	}
}
