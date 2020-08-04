package vg.civcraft.mc.civchat2.utility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting;

import java.util.UUID;

public class CivChat2SettingsManager {

	private BooleanSetting showJoins;
	private BooleanSetting showLeaves;
	private BooleanSetting sendOwnKills;
	private BooleanSetting receiveKills;
	private BooleanSetting receiveKillsFromIgnoredPlayers;
	private BooleanSetting showChatGroup;
	private DisplayLocationSetting chatGroupLocation;

	public CivChat2SettingsManager() {
		initSettings();
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("CivChat",
				"All options related to CivChat.");

		showJoins = new BooleanSetting(CivChat2.getInstance(), true, "Show Player Joins", "showJoins",
				"Should player join messages be shown?");
		PlayerSettingAPI.registerSetting(showJoins, menu);

		showLeaves = new BooleanSetting(CivChat2.getInstance(), true, "Show Players Leaving", "showLeaves",
				"Should player leave messages be shown?");
		PlayerSettingAPI.registerSetting(showLeaves, menu);

		sendOwnKills = new BooleanSetting(CivChat2.getInstance(), true, "Broadcast your kills", "civChatBroadcastKills",
				"Should kills you make be broadcasted to nearby players?");
		PlayerSettingAPI.registerSetting(sendOwnKills, menu);

		receiveKills = new BooleanSetting(CivChat2.getInstance(), true, "Receive kill broadcasts",
				"civChatReceiveKills", "Do you want to receive broadcasts for nearby kills");
		PlayerSettingAPI.registerSetting(receiveKills, menu);

		receiveKillsFromIgnoredPlayers = new BooleanSetting(CivChat2.getInstance(), false,
				"Receive kill broadcasts from ignored players", "civChatReceiveKillsIgnored",
				"Do you want to receive kill broadcasts from killers you have ignored");
		PlayerSettingAPI.registerSetting(receiveKillsFromIgnoredPlayers, menu);

		showChatGroup = new BooleanSetting(CivChat2.getInstance(), true, "Show current chat group", "showChatGroup",
				"Should player chat group be shown?");
		PlayerSettingAPI.registerSetting(showChatGroup, menu);

		chatGroupLocation = new DisplayLocationSetting(CivChat2.getInstance(), DisplayLocationSetting.DisplayLocation.SIDEBAR,
				"Chat Group Location", "chatGroupLocation", new ItemStack(Material.ARROW), "the current chat group");
		PlayerSettingAPI.registerSetting(chatGroupLocation, menu);
	}

	public boolean getShowJoins(UUID uuid) {
		return showJoins.getValue(uuid);
	}

	public boolean getShowLeaves(UUID uuid) {
		return showLeaves.getValue(uuid);
	}
	
	public boolean getSendOwnKills(UUID uuid) {
		return sendOwnKills.getValue(uuid);
	}
	
	public boolean getReceiveKills(UUID uuid) {
		return receiveKills.getValue(uuid);
	}
	
	public boolean getReceiveKillsFromIgnored(UUID uuid) {
		return receiveKillsFromIgnoredPlayers.getValue(uuid);
	}

	public boolean getShowChatGroup(UUID uuid) {
		return showChatGroup.getValue(uuid);
	}

	public DisplayLocationSetting getChatGroupLocation() {
		return chatGroupLocation;
	}
}
