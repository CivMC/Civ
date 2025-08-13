package vg.civcraft.mc.civchat2;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.broadcaster.ServerBroadcaster;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.event.GlobalChatEvent;
import vg.civcraft.mc.civchat2.event.GroupChatEvent;
import vg.civcraft.mc.civchat2.event.PrivateMessageEvent;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2FileLogger;
import vg.civcraft.mc.civchat2.utility.ScoreboardHUD;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class CivChat2Manager {

    private CivChat2Config config;
    private CivChat2FileLogger chatLog;
    private CivChat2 instance;
    private CivChatDAO DBM;
    // chatChannels in hashmap with (Player 1 name, player 2 name)
    private HashMap<UUID, UUID> chatChannels;
    // groupChatChannels have (Player, Group)
    private final HashMap<UUID, Group> groupChatChannels;
    // replyList has (playerName, whotoreplyto)
    private final HashMap<UUID, UUID> replyList;
    private final HashMap<UUID, Component> afkPlayers;
    private ScoreboardHUD scoreboardHUD;
    private String defaultColor;
    private final ServerBroadcaster broadcaster;

    public CivChat2Manager(CivChat2 pluginInstance, ServerBroadcaster broadcaster) {
        instance = pluginInstance;
        this.broadcaster = broadcaster;
        config = instance.getPluginConfig();
        chatLog = instance.getCivChat2FileLogger();
        DBM = instance.getDatabaseManager();
        defaultColor = config.getDefaultColor();
        chatChannels = new HashMap<>();
        groupChatChannels = new HashMap<>();
        replyList = new HashMap<>();
        afkPlayers = new HashMap<>();
        scoreboardHUD = new ScoreboardHUD();
    }

    /**
     * Gets the channel for player to player chat
     *
     * @param player Player name of the channel
     * @return Returns a String of channel name, null if doesn't exist
     */
    public UUID getChannel(Player player) {
        Preconditions.checkNotNull(player, "player");
        return chatChannels.get(player.getUniqueId());
    }

    /**
     * Removes the channel from the channel storage
     *
     * @param player Player Name of the channel
     */
    public void removeChannel(Player player) {
        Preconditions.checkNotNull(player, "player");
        chatChannels.remove(player.getUniqueId());
        scoreboardHUD.updateScoreboardHUD(player);
    }

    /**
     * Adds a channel for player to player chat, if player1 is currently in a
     * chatChannel this will overwrite it
     *
     * @param player1 Sender's name
     * @param player2 Receiver's name
     */
    public void addChatChannel(Player player1, Player player2) {
        Preconditions.checkNotNull(player1, "player1");
        Preconditions.checkNotNull(player2, "player2");
        if (getChannel(player1) != null) {
            chatChannels.put(player1.getUniqueId(), player2.getUniqueId());
        } else {
            chatChannels.put(player1.getUniqueId(), player2.getUniqueId());
        }
        scoreboardHUD.updateScoreboardHUD(player1);
    }

    /**
     * Method to Send private message between to players
     *
     * @param sender      Player sending the message
     * @param receiver    Player Receiving the message
     * @param chatMessage Message to send from sender to receive
     */
    public void sendPrivateMsg(Player sender, Player receiver, String chatMessage) {
        PrivateMessageEvent event = new PrivateMessageEvent(sender, receiver, Component.text(chatMessage));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        Component senderMessage = Component.text("To ", NamedTextColor.LIGHT_PURPLE).append(receiver.displayName())
            .append(Component.text(": " + chatMessage, NamedTextColor.LIGHT_PURPLE));
        Component receiverMessage = Component.text("From ", NamedTextColor.LIGHT_PURPLE).append(sender.displayName())
            .append(Component.text(": " + chatMessage, NamedTextColor.LIGHT_PURPLE));

        if (isPlayerAfk(receiver)) {
            receiver.sendMessage(receiverMessage);
            sender.sendMessage(getPlayerAfkMessage(receiver));
            return;
            // Player is ignoring the sender
        } else if (DBM.isIgnoringPlayer(receiver.getUniqueId(), sender.getUniqueId())) {
            sender.sendRichMessage(parse(ChatStrings.chatPlayerIgnoringYou));
            return;
        } else if (DBM.isIgnoringPlayer(sender.getUniqueId(), receiver.getUniqueId())) {
            sender.sendRichMessage(parse(ChatStrings.chatNeedToUnignore, receiver.getName()));
            return;
        }
        chatLog.logPrivateMessage(sender, chatMessage, receiver.getName());
        replyList.put(receiver.getUniqueId(), sender.getUniqueId());
        replyList.put(sender.getUniqueId(), receiver.getUniqueId());
        sender.sendMessage(senderMessage);
        receiver.sendMessage(receiverMessage);
    }

    /**
     * Method to broadcast a message in global chat
     *
     * @param sender      Player who sent the message
     * @param chatMessage Message to send
     * @param recipients  Players in range to receive the message
     */
    public void broadcastMessage(Player sender, String chatMessage, String messageFormat, Set<Player> recipients) {
        Preconditions.checkNotNull(sender, "sender");
        Preconditions.checkNotNull(chatMessage, "chatMessage");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(messageFormat), "messageFormat");
        Preconditions.checkNotNull(recipients, "recipients");

        GlobalChatEvent event = new GlobalChatEvent(sender, Component.text(chatMessage), messageFormat);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        long mutedUntil = instance.getCivChat2SettingsManager().getGlobalChatMuteSetting().getValue(sender);
        Group targetChatGroup = groupChatChannels.get(sender.getUniqueId());
        if (mutedUntil > System.currentTimeMillis()) {
            if (targetChatGroup == null || targetChatGroup.getName().equals(instance.getPluginConfig().getGlobalChatGroupName())) {
                sender.sendMessage(String.format(ChatStrings.globalMuted, TextUtil.formatDuration(mutedUntil - System.currentTimeMillis())));
                return;
            }
        }

        int range = config.getChatRange();
        int height = config.getYInc();
        Location location = sender.getLocation();
        int y = location.getBlockY();
        double scale = (config.getYScale()) / 1000;

        // Do height check
        // Player is above chat increase range
        if (range > 0 && y > height) {
            int above = y - height;
            int newRange = (int) (range + (range * (scale * above)));
            range = newRange;
        }

        Set<String> receivers = new HashSet<>();
        // Loop through players and send to those that are close enough
        for (Player receiver : recipients) {
            if (!DBM.isIgnoringPlayer(receiver.getUniqueId(), sender.getUniqueId())) {
                if (range <= 0 || receiver.getWorld().equals(sender.getWorld())) {
                    double receiverDistance = range <= 0 ? 0 : location.distance(receiver.getLocation());
                    if (receiverDistance <= range) {
                        TextColor newColor;
                        if (config.useDynamicRangeColoring()) {
                            int comp = (int) (255 - (128.0 * receiverDistance) / range);
                            newColor = TextColor.color(comp, comp, comp);
                        } else {
                            newColor = NamedTextColor.NAMES.value((config.getColorAtDistance(receiverDistance)));
                        }
                        newColor = newColor != null ? newColor : NamedTextColor.NAMES.value(defaultColor);
                        Component compMessage = Component.text("<", NamedTextColor.WHITE)
                            .append(sender.displayName())
                            .append(Component.text("> ", NamedTextColor.WHITE))
                            .append(Component.text(chatMessage, newColor));
                        receiver.sendMessage(compMessage);
                        receivers.add(receiver.getName());
                    }
                }
            }
        }
        receivers.remove(sender.getName());
        chatLog.logGlobalMessage(sender, chatMessage, receivers);
    }

    /**
     * Gets whether a player is AFK
     *
     * @param player The player to check
     * @return true if the player is AFK
     */
    public boolean isPlayerAfk(Player player) {
        Preconditions.checkNotNull(player, "player");
        return afkPlayers.get(player.getUniqueId()) != null;
    }

    /**
     * Sets the AFK status of a player
     *
     * @param player The player to change
     * @return The player AFK status
     */
    public boolean setPlayerAfk(Player player, boolean afkStatus, Component afkMsg) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(afkMsg, "afk message");

        if (afkStatus) {
            afkPlayers.put(player.getUniqueId(), afkMsg);
        } else {
            afkPlayers.remove(player.getUniqueId());
        }

        scoreboardHUD.updateAFKScoreboardHUD(player);
        return afkStatus;
    }

    /**
     * Toggles the AFK status of a player
     *
     * @param player Player to toggle state for
     * @return Whether afk is turned on afterwards
     */
    public boolean togglePlayerAfk(Player player, Component afkMsg) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(afkMsg, "afk message");
        if (isPlayerAfk(player)) {
            afkPlayers.remove(player.getUniqueId());

            scoreboardHUD.updateAFKScoreboardHUD(player);
            return false;
        }
        afkPlayers.put(player.getUniqueId(), afkMsg);

        scoreboardHUD.updateAFKScoreboardHUD(player);
        return true;
    }

    public Component getPlayerAfkMessage(Player player) {
        Preconditions.checkNotNull(player, "player");
        return afkPlayers.get(player.getUniqueId());
    }

    /**
     * Gets the player to send reply to
     *
     * @param sender the person sending reply command
     * @return the UUID of the person to reply to, null if none
     */
    public UUID getPlayerReply(Player sender) {
        Preconditions.checkNotNull(sender, "sender");
        return replyList.get(sender.getUniqueId());
    }

    /**
     * Add a player to the replyList
     *
     * @param player      The player using the reply command.
     * @param replyPlayer The the player that will receive the reply
     */
    public void addPlayerReply(Player player, Player replyPlayer) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(replyPlayer, "replyPlayer");
        replyList.put(player.getUniqueId(), replyPlayer.getUniqueId());
    }

    /**
     * Method to add a group chat channel
     *
     * @param player Player sending the message
     * @param group  Group sending the message to
     */
    public void addGroupChat(Player player, Group group) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(group, "group");
        groupChatChannels.put(player.getUniqueId(), group);
        scoreboardHUD.updateScoreboardHUD(player);
    }

    /**
     * Method to send a message to a group
     *
     * @param sender  sender sending the message
     * @param group   Group to send the message too
     * @param message Message to send to the group
     */

    public void sendGroupMsg(Player sender, Group group, String message) {
        Preconditions.checkNotNull(sender, "sender");
        Preconditions.checkNotNull(group, "group");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "message");
        if (group.getName().equals(config.getGlobalChatGroupName())) {
            long mutedUntil = instance.getCivChat2SettingsManager().getGlobalChatMuteSetting().getValue(sender);
            if (mutedUntil > System.currentTimeMillis()) {
                sender.sendMessage(String.format(ChatStrings.globalMuted, TextUtil.formatDuration(mutedUntil - System.currentTimeMillis())));
                return;
            }
        }
        GroupChatEvent event = new GroupChatEvent(sender, group.getName(), message);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        String senderName = sender.getName();
        Set<String> players = doSendGroupMsg(sender.getUniqueId(), senderName, group, message);
        chatLog.logGroupMessage(sender, message, group.getName(), players);
        broadcaster.broadcastGroup(sender.getUniqueId(), sender.getName(), senderName, group.getName(), message);
    }

    public void sendRemoteGroupMsg(UUID senderId, String senderName, String senderDisplayName, String groupName, String message) {
        Group group = GroupManager.getGroup(groupName);
        if (group == null) {
            return;
        }
        Set<String> players = doSendGroupMsg(senderId, senderDisplayName, group, message);
        chatLog.logRemoteGroupMessage(senderName, message, group.getName(), players);
    }

    private Set<String> doSendGroupMsg(UUID senderId, String senderName, Group group, String message) {
        List<Player> members = new ArrayList<>();
        List<UUID> membersUUID = group.getAllMembers();
        for (UUID uuid : membersUUID) {
            // Only add online players to members
            Player toAdd = Bukkit.getPlayer(uuid);
            if (toAdd != null && toAdd.isOnline() && NameAPI.getGroupManager().hasAccess(group, toAdd.getUniqueId(),
                PermissionType.getPermission("READ_CHAT"))) {
                members.add(toAdd);
            }
        }

        Component compMessage = Component.text("[" + group.getName() + "] ", NamedTextColor.GRAY)
            .append(Bukkit.getPlayer(senderName).displayName())
            .append(Component.text(": " + message, NamedTextColor.GRAY));

        for (Player receiver : members) {
            if (DBM.isIgnoringGroup(receiver.getUniqueId(), group.getName())) {
                continue;
            }
            if (DBM.isIgnoringPlayer(receiver.getUniqueId(), senderId)) {
                continue;
            }
            receiver.sendMessage(compMessage);
        }

        Set<String> players = new HashSet<>();
        for (Player player : members) {
            players.add(NameAPI.getCurrentName(player.getUniqueId()));
        }
        players.remove(senderName);
        return players;
    }

    /**
     * Method to remove player from a group chat
     *
     * @param player The player to remove from chat
     */
    public void removeGroupChat(Player player) {
        Preconditions.checkNotNull(player, "player");
        groupChatChannels.remove(player.getUniqueId());
        scoreboardHUD.updateScoreboardHUD(player);
    }

    /**
     * Method to get the group player is currently chatting in
     *
     * @param player Players name
     * @return Group they are currently chatting in
     */
    public Group getGroupChatting(Player player) {
        Preconditions.checkNotNull(player, "player");
        return groupChatChannels.get(player.getUniqueId());
    }

    public String parse(String text) {
        return ChatUtils.parseColor(text);
    }

    public String parse(String text, Object... args) {
        return String.format(ChatUtils.parseColor(text), args);
    }

    public ScoreboardHUD getScoreboardHUD() {
        return scoreboardHUD;
    }
}
