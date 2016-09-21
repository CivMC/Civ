package com.devotedmc.ExilePearl.util;

public enum Permission
{
	ADMIN("admin"),
	MOD("mod"),
	PLAYER("player");
	

	public static final String ADMIN_NODE = Permission.ADMIN.node;
	public static final String MOD_NODE = Permission.MOD.node;
	public static final String PLAYER_NODE = Permission.PLAYER.node;
	
	/**
	 * The node string that is referenced for permissions
	 */
	public final String node;
	
	Permission(final String node)
	{
		this.node = "exilepearl." + node;
	}
}
