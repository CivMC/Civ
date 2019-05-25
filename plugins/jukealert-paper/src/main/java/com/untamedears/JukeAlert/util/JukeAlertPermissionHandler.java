package com.untamedears.JukeAlert.util;

import java.util.LinkedList;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class JukeAlertPermissionHandler {

	private JukeAlertPermissionHandler() {
	}

	private static PermissionType snitchImmune;
	private static PermissionType listSnitches;
	private static PermissionType clearLogs;
	private static PermissionType readLogs;
	private static PermissionType renameSnitch;

	@SuppressWarnings("unchecked")
	public static void setup() {
		LinkedList<PlayerType> memberAndAbove = new LinkedList<>();
		LinkedList<PlayerType> modAndAbove = new LinkedList<>();
		memberAndAbove.add(PlayerType.MEMBERS);
		memberAndAbove.add(PlayerType.MODS);
		memberAndAbove.add(PlayerType.ADMINS);
		memberAndAbove.add(PlayerType.OWNER);
		modAndAbove.add(PlayerType.MODS);
		modAndAbove.add(PlayerType.ADMINS);
		modAndAbove.add(PlayerType.OWNER);
		PermissionType.registerPermission("LIST_SNITCHES",
			(LinkedList<PlayerType>) modAndAbove.clone()); // Also tied to refreshing snitches
		PermissionType.registerPermission("SNITCH_NOTIFICATIONS", (LinkedList<PlayerType>) memberAndAbove.clone());
		PermissionType.registerPermission("READ_SNITCHLOG", (LinkedList<PlayerType>) memberAndAbove.clone());
		PermissionType.registerPermission("RENAME_SNITCH", (LinkedList<PlayerType>) modAndAbove.clone());
		PermissionType.registerPermission("SNITCH_IMMUNE", (LinkedList<PlayerType>) memberAndAbove.clone());
		PermissionType.registerPermission("LOOKUP_SNITCH", (LinkedList<PlayerType>) modAndAbove.clone());
		PermissionType.registerPermission("CLEAR_SNITCHLOG", (LinkedList<PlayerType>) modAndAbove.clone());
		PermissionType.registerPermission("SNITCH_TOGGLE_LEVER", (LinkedList<PlayerType>) modAndAbove.clone());
		snitchImmune = PermissionType.getPermission("SNITCH_IMMUNE");
		listSnitches = PermissionType.getPermission("LIST_SNITCHES");
		clearLogs = PermissionType.getPermission("CLEAR_SNITCHLOG");
		readLogs = PermissionType.getPermission("READ_SNITCHLOG");
		renameSnitch = PermissionType.getPermission("RENAME_SNITCH");
	}
	
	public static PermissionType getRenameSnitch() {
		return renameSnitch;
	}

	public static PermissionType getSnitchImmune() {
		return snitchImmune;
	}

	public static PermissionType getListSnitches() {
		return listSnitches;
	}
	
	public static PermissionType getClearLogs() {
		return clearLogs;
	}
	
	public static PermissionType getReadLogs() {
		return readLogs;
	}

}
