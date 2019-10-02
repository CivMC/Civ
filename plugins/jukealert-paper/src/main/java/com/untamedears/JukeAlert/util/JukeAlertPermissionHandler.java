package com.untamedears.JukeAlert.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private static PermissionType receiveAlerts;
	private static PermissionType lookupSnitch;
	private static PermissionType toggleLever;

	public static void setup() {
		List<PlayerType> memberAndAbove = Arrays.asList(PlayerType.MEMBERS, PlayerType.MODS, PlayerType.ADMINS,
				PlayerType.OWNER);
		List<PlayerType> modAndAbove = Arrays.asList(PlayerType.MODS, PlayerType.ADMINS, PlayerType.OWNER);
		// Also tied to refreshing snitches
		listSnitches = PermissionType.registerPermission("LIST_SNITCHES", new ArrayList<>(modAndAbove));
		receiveAlerts = PermissionType.registerPermission("SNITCH_NOTIFICATIONS", new ArrayList<>(memberAndAbove));
		readLogs = PermissionType.registerPermission("READ_SNITCHLOG", new ArrayList<>(memberAndAbove));
		renameSnitch = PermissionType.registerPermission("RENAME_SNITCH", new ArrayList<>(modAndAbove));
		snitchImmune = PermissionType.registerPermission("SNITCH_IMMUNE", new ArrayList<>(memberAndAbove));
		lookupSnitch = PermissionType.registerPermission("DETECT_SNITCH", new ArrayList<>(memberAndAbove));
		clearLogs = PermissionType.registerPermission("CLEAR_SNITCHLOG", new ArrayList<>(modAndAbove));
		toggleLever = PermissionType.registerPermission("SNITCH_TOGGLE_LEVER", new ArrayList<>(modAndAbove));
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

	public static PermissionType getSnitchAlerts() {
		return receiveAlerts;
	}
	
	public static PermissionType getToggleLevers() {
		return toggleLever;
	}

	public static PermissionType getLookupSnitch() {
		return lookupSnitch;
	}
}
