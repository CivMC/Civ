package vg.civcraft.mc.civchat2.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.event.GlobalChatEvent;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/*
 * @author jjj5311
 *
 */
public class CivChat2Listener implements Listener {

    private CivChat2Manager chatman;
    private CivChatDAO db;
    private CivChat2SettingsManager settings;
    private Set<UUID> localWarn;

    public CivChat2Listener(CivChat2Manager instance) {
        chatman = instance;
        db = CivChat2.getInstance().getDatabaseManager();
        settings = CivChat2.getInstance().getCivChat2SettingsManager();
        localWarn = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        playerDeathEvent.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        playerQuitEvent.quitMessage(null);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (settings.getShowLeaves(p.getUniqueId()) && !db.isIgnoringPlayer(p.getUniqueId(), playerQuitEvent.getPlayer().getUniqueId())) {
                if (playerQuitEvent.getPlayer().hasPermission("civchat2.leavejoinimmune")) {
                    continue;
                }
                p.sendMessage(playerQuitEvent.getPlayer().displayName().append(Component.text(" has left the game", NamedTextColor.YELLOW)));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (!CivChat2.getInstance().getPluginConfig().getLoginAnnounce()) {
            playerJoinEvent.joinMessage(null);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (settings.getShowJoins(p.getUniqueId()) && !db.isIgnoringPlayer(p.getUniqueId(), playerJoinEvent.getPlayer().getUniqueId())) {
                if (playerJoinEvent.getPlayer().hasPermission("civchat2.leavejoinimmune")) {
                    continue;
                }
                p.sendMessage(playerJoinEvent.getPlayer().displayName().append(Component.text(" has joined the game", NamedTextColor.YELLOW)));
            }
        }

        UUID player = chatman.getChannel(playerJoinEvent.getPlayer());
        if (player != null && Bukkit.getPlayer(player) == null) {
            chatman.removeChannel(playerJoinEvent.getPlayer());
        }

        // Set current chat group in scoreboard
        chatman.getScoreboardHUD().updateScoreboardHUD(playerJoinEvent.getPlayer());
        chatman.getScoreboardHUD().updateAFKScoreboardHUD(playerJoinEvent.getPlayer());

        if (CivChat2.getInstance().getPluginConfig().getChatRangeWarn() && !playerJoinEvent.getPlayer().hasPlayedBefore()) {
            localWarn.add(playerJoinEvent.getPlayer().getUniqueId());
        }
        String globalChat = CivChat2.getInstance().getPluginConfig().getGlobalChatGroupName();
        if (globalChat != null && !playerJoinEvent.getPlayer().hasPlayedBefore()) {
            Group group = GroupManager.getGroup(globalChat);
            if (group != null && !group.isCurrentMember(playerJoinEvent.getPlayer().getUniqueId())) {
                group.addMember(playerJoinEvent.getPlayer().getUniqueId(), PlayerType.MEMBERS);
                playerJoinEvent.getPlayer().sendMessage(ChatColor.GREEN + "You autojoined global chat, which is called '!'. Use it like this: '/g ! Hello'");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent playerKickEvent) {
        playerKickEvent.setLeaveMessage("You have been kicked");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlobalChatEvent(GlobalChatEvent localchat) {

        if (localWarn.contains(localchat.getPlayer().getUniqueId())) {
            localchat.getPlayer().sendMessage(ChatColor.GOLD
                + "Only players within "
                + CivChat2.getInstance().getPluginConfig().getChatRange()
                + " blocks of you can see your messages. Join a group to chat with players farther away!");
            localWarn.remove(localchat.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChatEvent(final AsyncChatEvent asyncPlayerChatEvent) {

        asyncPlayerChatEvent.setCancelled(true);
        // This needs to be done sync to avoid a rare deadlock due to minecraft
        // internals
        new BukkitRunnable() {

            @Override
            public void run() {

                Component chatMessage = asyncPlayerChatEvent.message();
                Player sender = asyncPlayerChatEvent.getPlayer();
                UUID chatChannel = chatman.getChannel(sender);
                Group groupChat = chatman.getGroupChatting(sender);


                if (chatChannel != null) {
                    StringBuilder sb = new StringBuilder();
                    Player receiver = Bukkit.getPlayer(chatChannel);
                    if (receiver != null) {
                        chatman.sendPrivateMsg(sender, receiver, chatMessage);
                        return;
                    } else {
                        chatman.removeChannel(sender);
                        String offlineMessage = sb.append(ChatColor.GOLD)
                            .append("The player you were chatting with has gone offline,")
                            .append(" you have been moved to regular chat").toString();
                        sb.delete(0, sb.length());
                        sender.sendMessage(offlineMessage);
                        return;
                    }
                }
                if (groupChat != null) {
                    // Player is group chatting
                    if (NameAPI.getGroupManager().hasAccess(groupChat, sender.getUniqueId(),
                        PermissionType.getPermission("WRITE_CHAT"))) {
                        chatman.sendGroupMsg(sender, groupChat, chatMessage);
                        return;
                        // Player lost perm to write in the chat
                    } else {
                        chatman.removeGroupChat(sender);
                        sender.sendMessage(ChatColor.RED
                            + "You have been removed from groupchat because you were removed from the group or lost the permission required to groupchat");
                    }
                }
                Set<Player> playerViewers = new HashSet<>();
                for (Audience viewer : asyncPlayerChatEvent.viewers()) {
                    if (viewer instanceof Player playerViewer) {
                        playerViewers.add(playerViewer);
                    }
                }
                chatman.broadcastMessage(sender, chatMessage, ChatStrings.localChatFormat, playerViewers);
            }
        }.runTask(CivChat2.getInstance());
    }
}
