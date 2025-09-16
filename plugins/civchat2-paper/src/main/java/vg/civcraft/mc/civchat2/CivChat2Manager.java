package vg.civcraft.mc.civchat2;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
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

    protected static final GroupManager GM = NameAPI.getGroupManager();

    private String defaultColor;

    private static Map<UUID, String> customNames = new HashMap<>();

    private final ServerBroadcaster broadcaster;

    private LongSetting banSetting;

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
        bannedWords = loadBannedWords();

        banSetting = instance.getCivChat2SettingsManager().getGlobalChatMuteSetting();
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

        PrivateMessageEvent event = new PrivateMessageEvent(sender, receiver, chatMessage);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        String senderName = customNames.containsKey(sender.getUniqueId()) ? customNames.get(sender.getUniqueId())
            : sender.getDisplayName();
        String receiverName = customNames.containsKey(receiver.getUniqueId()) ? customNames.get(receiver.getUniqueId())
            : receiver.getDisplayName();

        String senderMessage = sb.append(ChatColor.LIGHT_PURPLE).append("To ").append(receiverName)
            .append(ChatColor.LIGHT_PURPLE).append(": ").append(chatMessage).toString();
        sb = new StringBuilder();
        String receiverMessage = sb.append(ChatColor.LIGHT_PURPLE).append("From ").append(senderName)
            .append(ChatColor.LIGHT_PURPLE).append(": ").append(chatMessage).toString();

        if (isPlayerAfk(receiver)) {
            receiver.sendMessage(receiverMessage);
            sender.sendMessage(getPlayerAfkMessage(receiver));
            return;
            // Player is ignoring the sender
        } else if (DBM.isIgnoringPlayer(receiver.getUniqueId(), sender.getUniqueId())) {
            sender.sendMessage(parse(ChatStrings.chatPlayerIgnoringYou));
            return;
        } else if (DBM.isIgnoringPlayer(sender.getUniqueId(), receiver.getUniqueId())) {
            sender.sendMessage(parse(ChatStrings.chatNeedToUnignore, receiverName));
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

        GlobalChatEvent event = new GlobalChatEvent(sender, chatMessage, messageFormat);
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

        // Chat filter check - block message if it contains banned words
        if (containsBannedWord(chatMessage)) {
            //Flag inappropriate message, mute sender for 1 hour
            sender.sendMessage(ChatColor.RED + "Your message has been flagged for inappropriate content.");
            banSetting.setValue(sender, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)); // 1 hour mute

            // Log the filtered message to console and mods
            String senderName = customNames.containsKey(sender.getUniqueId()) ? customNames.get(sender.getUniqueId())
                : sender.getDisplayName();

            //Set<String> players = doSendGroupMsg(sender.getUniqueId(), senderName, group, message);

            Group modsGroup = GroupManager.getGroup("Mods");

            Set<String> modPlayers = doSendGroupMsg(sender.getUniqueId(), senderName, modsGroup, ChatColor.RED + "[Filtered]: " + chatMessage);

            broadcaster.broadcastGroup(sender.getUniqueId(), senderName, sender.getDisplayName(), modsGroup.getName(), chatMessage);

            chatLog.logGroupMessage(sender, chatMessage, modsGroup.getName(), modPlayers);
            return;
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
                        net.md_5.bungee.api.ChatColor newColor;
                        if (config.useDynamicRangeColoring()) {
                            int comp = (int) (255 - (128.0 * receiverDistance) / range);
                            newColor = net.md_5.bungee.api.ChatColor.of(new Color(comp, comp, comp));
                        } else {
                            newColor = net.md_5.bungee.api.ChatColor.of(config.getColorAtDistance(receiverDistance));
                        }
                        newColor = newColor != null ? newColor : net.md_5.bungee.api.ChatColor.of(defaultColor);

                        String senderName = customNames.containsKey(sender.getUniqueId())
                            ? customNames.get(sender.getUniqueId())
                            : sender.getDisplayName();
                        TextComponent text = new TextComponent(String.format(messageFormat, senderName + ChatColor.RESET, ""));
                        TextComponent msgPart = new TextComponent(chatMessage);
                        msgPart.setColor(newColor);
                        receiver.spigot().sendMessage(text, msgPart);
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
            // afkPlayers.add(player.getUniqueId());
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
    // Load banned words once when CivChat2Manager is created
    private final Set<String> bannedWords;

    private Set<String> loadBannedWords() {
        Set<String> words = new HashSet<>();
        try {
            File file = new File(instance.getDataFolder(), "banned-words.txt");
            if (file.exists()) {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    String cleanWord = line.trim().toLowerCase();
                    if (!cleanWord.isEmpty()) {
                        words.add(cleanWord);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    private boolean containsBannedWord(String message) {
        String lowerMessage = message.toLowerCase();
        for (String bannedWord : bannedWords) {
            if (lowerMessage.contains(bannedWord)) {
                return true;
            }
        }
        return false;
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
        //LongSetting banSetting = instance.getCivChat2SettingsManager().getGlobalChatMuteSetting();

        if (group.getName().equals(config.getGlobalChatGroupName())) {
            // Chat filter check - block message if it contains banned words
            if (containsBannedWord(message)) {
                //Flag inappropriate message, mute sender for 1 hour
                sender.sendMessage(ChatColor.RED + "Your message has been flagged for inappropriate content.");
                banSetting.setValue(sender, System.currentTimeMillis() + 3600000L); // 1 hour mute
                
                // Log the filtered message to console and mods
                String senderName = customNames.containsKey(sender.getUniqueId()) ? customNames.get(sender.getUniqueId())
                    : sender.getDisplayName();
                
                
                //Set<String> players = doSendGroupMsg(sender.getUniqueId(), senderName, group, message);

                Group modsGroup = GroupManager.getGroup("Mods");

                Set<String> modPlayers = doSendGroupMsg(sender.getUniqueId(), senderName, modsGroup, ChatColor.RED + "[Filtered]: " + message);

                broadcaster.broadcastGroup(sender.getUniqueId(), senderName, sender.getDisplayName(), modsGroup.getName(), message);

                chatLog.logGroupMessage(sender, message, modsGroup.getName(), modPlayers);
                return;
            }
        }

        GroupChatEvent event = new GroupChatEvent(sender, group.getName(), message);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        String senderName = customNames.containsKey(sender.getUniqueId()) ? customNames.get(sender.getUniqueId())
            : sender.getDisplayName();
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

        String formatted = parse(ChatStrings.chatGroupMessage, group.getName(), senderName, message);

        for (Player receiver : members) {
            if (DBM.isIgnoringGroup(receiver.getUniqueId(), group.getName())) {
                continue;
            }
            if (DBM.isIgnoringPlayer(receiver.getUniqueId(), senderId)) {
                continue;
            }
            receiver.sendMessage(formatted);
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

    public void setCustomName(UUID player, String name) {
        customNames.put(player, name);
    }

    public void removeCustomName(UUID player) {
        customNames.remove(player);
    }

    public ScoreboardHUD getScoreboardHUD() {
        return scoreboardHUD;
    }
}
