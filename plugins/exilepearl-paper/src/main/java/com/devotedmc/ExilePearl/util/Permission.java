package com.devotedmc.ExilePearl.util;

public enum Permission
{
	PLAYER("player"),
	CHECK("check"),
	DECAY("decay"),
	EXILE_ANY("exileany"),
	FREE_ANY("freeany"),
	LIST("list"),
	RELOAD("reload"),
	SET_HEALTH("sethealth"),
	SET_TYPE("settype"),
	SET_KILLER("setkiller"),
	SET_DATE("setdate"),
	CONFIG("config"),
	DOWNGRADE("downgrade"),
	SUMMON("summon"),
	SUMMON_CONFIRM("summon_confirm"),
	UPGRADE("upgrade")
	;

	/**
	 * The node string that is referenced for permissions
	 */
	public final String node;

	Permission(final String node) {
		this.node = "exilepearl." + node;
	}
}
