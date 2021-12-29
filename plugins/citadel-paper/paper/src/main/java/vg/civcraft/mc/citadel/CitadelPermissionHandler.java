package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class CitadelPermissionHandler {

	private CitadelPermissionHandler() {
	}

	private static PermissionType chestPerm;
	private static PermissionType bypassPerm;
	private static PermissionType cropsPerm;
	private static PermissionType insecurePerm;
	private static PermissionType reinforcePerm;
	private static PermissionType doorPerm;
	private static PermissionType acidPerm;
	private static PermissionType infoPerm;
	private static PermissionType repairPerm;
	private static PermissionType modifyBlockPerm;
	private static PermissionType beaconPerm;
	private static PermissionType hangingPlaceBreak;
	private static PermissionType itemFramePutTake;
	private static PermissionType itemFrameRotate;

	public static void setup() {
		List<PlayerType> membersAndAbove = Arrays.asList(PlayerType.MEMBERS, PlayerType.MODS, PlayerType.ADMINS,
				PlayerType.OWNER);
		List<PlayerType> modAndAbove = Arrays.asList(PlayerType.MODS, PlayerType.ADMINS, PlayerType.OWNER);
		reinforcePerm = PermissionType.registerPermission("REINFORCE", new ArrayList<>(modAndAbove),
				"Allows reinforcing blocks on this group");
		acidPerm = PermissionType.registerPermission("ACIDBLOCK", new ArrayList<>(modAndAbove),
				"Allows activating acid blocks reinforced on this group");
		infoPerm = PermissionType.registerPermission("REINFORCEMENT_INFO", new ArrayList<>(membersAndAbove),
				"Allows viewing information on reinforcements reinforced on this group");
		bypassPerm = PermissionType.registerPermission("BYPASS_REINFORCEMENT", new ArrayList<>(modAndAbove),
				"Allows bypassing reinforcements reinforced on this group");
		repairPerm = PermissionType.registerPermission("REPAIR_REINFORCEMENT", new ArrayList<>(modAndAbove),
				"Allows repairing reinforcements reinforced on this group");
		doorPerm = PermissionType.registerPermission("DOORS", new ArrayList<>(membersAndAbove),
				"Allows opening doors reinforced on this group");
		chestPerm = PermissionType.registerPermission("CHESTS", new ArrayList<>(membersAndAbove),
				"Allows opening containers like chests reinforced on this group");
		cropsPerm = PermissionType.registerPermission("CROPS", new ArrayList<>(membersAndAbove),
				"Allows harvesting crops growing on soil reinforced on this group");
		insecurePerm = PermissionType.registerPermission("INSECURE_REINFORCEMENT", new ArrayList<>(membersAndAbove),
				"Allows toggling the insecure flag on reinforcements");
		modifyBlockPerm = PermissionType.registerPermission("MODIFY_BLOCK", new ArrayList<>(modAndAbove),
				"Allows modifying reinforced blocks like flipping levers, stripping logs etc.");
		beaconPerm = PermissionType.registerPermission("BEACONS", new ArrayList<>(membersAndAbove),
				"Allow changing beacon effects");
		hangingPlaceBreak = PermissionType.registerPermission("HANGING_PLACE_BREAK", new ArrayList<>(membersAndAbove),
				"Allows placing/breaking hanging entities on reinforced blocks.");
		itemFramePutTake = PermissionType.registerPermission("ITEM_FRAME_PUT_TAKE", new ArrayList<>(membersAndAbove),
				"Allows the placing/removal of items into/from Item Frames.");
		itemFrameRotate = PermissionType.registerPermission("ITEM_FRAME_ROTATE", new ArrayList<>(membersAndAbove),
				"Allows the rotation of items placed within Item Frames.");
	}
	
	public static PermissionType getModifyBlocks() {
		return modifyBlockPerm;
	}

	public static PermissionType getChests() {
		return chestPerm;
	}

	public static PermissionType getDoors() {
		return doorPerm;
	}

	public static PermissionType getBypass() {
		return bypassPerm;
	}

	public static PermissionType getReinforce() {
		return reinforcePerm;
	}

	public static PermissionType getAcidblock() {
		return acidPerm;
	}

	public static PermissionType getCrops() {
		return cropsPerm;
	}

	public static PermissionType getInsecure() {
		return insecurePerm;
	}

	public static PermissionType getInfo() {
		return infoPerm;
	}

	public static PermissionType getRepair() {
		return repairPerm;
	}

	public static PermissionType getBeacon() {
		return beaconPerm;
	}

	public static PermissionType getHangingPlaceBreak() {
		return hangingPlaceBreak;
	}

	public static PermissionType getItemFramePutTake() {
		return itemFramePutTake;
	}

	public static PermissionType getItemFrameRotate() {
		return itemFrameRotate;
	}

}
