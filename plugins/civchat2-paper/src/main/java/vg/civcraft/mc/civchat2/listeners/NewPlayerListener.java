package vg.civcraft.mc.civchat2.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class NewPlayerListener implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return;
        }
        String globalGroupName = CivChat2.getInstance().getPluginConfig().getGlobalChatGroupName();
        if (globalGroupName == null) {
            return;
        }
        Group globalGroup = GroupManager.getGroup(globalGroupName);
        if (globalGroup == null) {
            return;
        }

        CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
        chatMan.addGroupChat(player, globalGroup);
    }
}
