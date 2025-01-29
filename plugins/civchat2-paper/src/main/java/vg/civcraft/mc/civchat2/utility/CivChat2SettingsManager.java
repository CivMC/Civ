package vg.civcraft.mc.civchat2.utility;

import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;

public class CivChat2SettingsManager {

    private BooleanSetting showJoins;
    private BooleanSetting showLeaves;
    private BooleanSetting sendOwnKills;
    private BooleanSetting receiveKills;
    private BooleanSetting receiveKillsFromIgnoredPlayers;
    private BooleanSetting showChatGroup;
    private DisplayLocationSetting chatGroupLocation;
    private LongSetting chatUnmuteTimer;
    private BooleanSetting killMessageFormat;
    private BooleanSetting showAFKStatus;
    private DisplayLocationSetting afkStatusLocation;

    public CivChat2SettingsManager() {
        initSettings();
    }

    private void initSettings() {
        MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("CivChat",
            "All options related to CivChat.", new ItemStack(Material.OAK_SIGN));

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

        chatUnmuteTimer = new LongSetting(CivChat2.getInstance(), 0L, "Global chat mute", "chatGlobalMuteTimer");
        PlayerSettingAPI.registerSetting(chatUnmuteTimer, null);

        PlayerSettingAPI.registerSetting(killMessageFormat = new BooleanSetting(
            CivChat2.getInstance(),
            true,
            "Show weapon in kill broadcast",
            "include-weapon-in-kill-broadcasts",
            "Should your kill broadcasts include what weapon was used?"
        ), menu);

        afkStatusLocation = new DisplayLocationSetting(CivChat2.getInstance(), DisplayLocationSetting.DisplayLocation.SIDEBAR,
            "AFK Status Location", "afkStatusLocation", new ItemStack(Material.ARROW), "the AFK status");
        PlayerSettingAPI.registerSetting(afkStatusLocation, menu);
    }

    public LongSetting getGlobalChatMuteSetting() {
        return chatUnmuteTimer;
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

    public boolean includeWeaponInKillBroadcasts(
        final @NotNull UUID uuid
    ) {
        return this.killMessageFormat.getValue(uuid);
    }

    public boolean getShowAFKStatus(UUID uuid) {return showAFKStatus.getValue(uuid); }

    public DisplayLocationSetting getAfkStatusLocation() { return afkStatusLocation; }
}
