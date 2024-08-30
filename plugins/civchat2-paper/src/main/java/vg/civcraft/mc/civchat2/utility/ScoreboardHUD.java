package vg.civcraft.mc.civchat2.utility;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;

public class ScoreboardHUD {

    private final BottomLine chatBottomLine;
    private final CivChat2SettingsManager settingMan;

    public ScoreboardHUD() {
        this.chatBottomLine = BottomLineAPI.createBottomLine("CivChatDisplay", 3);
        this.settingMan = CivChat2.getInstance().getCivChat2SettingsManager();
    }

    /**
     * Updates the scoreboard to display the players currently in use private message channel or chat group
     *
     * @param p player to update scoreboard for
     */
    public void updateScoreboardHUD(Player p) {
        if (!settingMan.getShowChatGroup(p.getUniqueId())) {
            chatBottomLine.removePlayer(p);
        } else {
            DisplayLocationSetting locSetting = settingMan.getChatGroupLocation();
            CivChat2Manager chatman = CivChat2.getInstance().getCivChat2Manager();
            String text;
            if (chatman.getChannel(p) != null && Bukkit.getPlayer(chatman.getChannel(p)) != null) {
                text = ChatColor.GOLD + "Messaging " + ChatColor.LIGHT_PURPLE + Bukkit.getPlayer(chatman.getChannel(p)).getName();
            } else if (chatman.getGroupChatting(p) != null) {
                text = ChatColor.GOLD + "Chat Group " + ChatColor.LIGHT_PURPLE + chatman.getGroupChatting(p).getName();
            } else {
                chatBottomLine.removePlayer(p);
                return;
            }
            if (locSetting.showOnActionbar(p.getUniqueId())) {
                chatBottomLine.updatePlayer(p, text);
            }
        }
    }

}
