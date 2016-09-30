package com.devotedmc.ExilePearl;

import com.devotedmc.ExilePearl.util.Guard;

public enum ExileRule {
	PEARL_RADIUS("PEARL_RADIUS", "pearl radius"),
	DAMAGE_REINFORCEMENT("DAMAGE_REINFORCEMENT", "break reinforced blocks"),
	DAMAGE_BASTION("DAMAGE_BASTION", "break bastion blocks"),
	ENTER_BASTION("ENTER_BASTION", "enter bastion fields"),
	THROW_PEARL("THROW_PEARL", "throw ender pearls"),
	CHAT("CHAT", "use local chat"),
	PVP("PVP", "fight other players"),
	IGNITE("IGNITE", "start fire"),
	USE_BUCKET("USE_BUCKET", "use buckets"),
	USE_POTIONS("USE_POTIONS", "use potions"),
	USE_BED("USE_BED", "set your bed"),
	SUICIDE("SUICIDE", "commit suicide"),
	SNITCH("SNITCH", "place snitches"),
	MINE("MINE", "break blocks"),
	BREW("BREW", "brew potions"),
	ENCHANT("ENCHANT", "enchant items")
	;
	
	private final String text;
	private final String actionText;
	
	private ExileRule(final String text, final String actionText) {
		Guard.ArgumentNotNullOrEmpty(text, "text");
		Guard.ArgumentNotNullOrEmpty(actionText, "actionText");
		
		this.text = text;
		this.actionText = actionText;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public String getActionString() {
		return actionText;
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
