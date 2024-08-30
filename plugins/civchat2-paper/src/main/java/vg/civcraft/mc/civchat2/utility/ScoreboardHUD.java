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
    private final BottomLine afkBottomLine;
    private final CivChat2SettingsManager settingMan;

    public ScoreboardHUD() {
        this.chatBottomLine = BottomLineAPI.createBottomLine("CivChatDisplay", 3);
        this.afkBottomLine = BottomLineAPI.createBottomLine("CivChatAFKDisplay", 4);

        this.settingMan = CivChat2.getInstance().getCivChat2SettingsManager();
    }

    /**
     * Updates the scoreboard to display the players currently in use private message channel or chat group
     *
     * @param p player to update scoreboard for
     */
    public void updateScoreboardHUD(Player p) {
        // if player disabled chat group display, hide the scoreboard
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

    /**
     * Updates the scoreboard to display the players AFK status
     */
    public void updateAFKScoreboardHUD(Player p) {
        CivChat2Manager chatman = CivChat2.getInstance().getCivChat2Manager();
        DisplayLocationSetting afkStatusLocation = settingMan.getAfkStatusLocation();

        String text = ChatColor.LIGHT_PURPLE + "AFK";

        if (chatman.isPlayerAfk(p) && afkStatusLocation.showOnActionbar(p.getUniqueId())) {
            afkBottomLine.updatePlayer(p, text);
        } else {
            afkBottomLine.removePlayer(p);
        }
    }

}
