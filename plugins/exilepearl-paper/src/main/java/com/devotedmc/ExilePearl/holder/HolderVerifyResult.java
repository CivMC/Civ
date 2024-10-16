package com.devotedmc.ExilePearl.holder;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Validation result for checking an exile pearl
 * @author Gordon
 *
 */
public enum HolderVerifyResult
{	
	DEFAULT("default", false),
	ENTITY_NOT_IN_CHUNK("pearl not found in chunk", false),
	PLAYER_NOT_ONLINE("holding player not online", false),
	BLOCK_STATE_NULL("block state null", false),
	NOT_BLOCK_INVENTORY("block is not an inventory", false),
	NO_ITEM_PLAYER_OR_LOCATION("no player or location", false),
	ON_GROUND("on ground", true),
	IN_HAND("in player hand", true),
	IN_OFFHAND("in player offhand", true),
	IN_CHEST("in chest", true),
	IN_PLAYER_INVENTORY("in player inventory", true),
	IN_PLAYER_INVENTORY_VIEW("in player inventory view", true),
	IN_VIEWER_HAND("in viewer hand", true),
	CREATIVE_MODE("creative mode", true),
	IN_ITEM_FRAME("in item frame", true);

	private final String text;
	private final boolean isValid;

	/**
	 * Creates a new HolderVerifyResult instance
	 * @param text The name of the result
	 * @param isValid whether the result is valid
	 */
	private HolderVerifyResult(final String text, final boolean isValid) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(text), "text");

		this.text = text;
		this.isValid = isValid;
	}

	@Override
	public String toString() {
		return text;
	}

	public boolean isValid() {
		return isValid;
	}
}
