package vg.civcraft.mc.civchat2;

import org.bukkit.ChatColor;

public class ChatStrings {

    public final static String localChatFormat = "<%1$s> %2$s";

    public final static String chatPlayerIsOffline = "<yellow>That player is offline.</yellow>";

    public final static String chatNoOneToReplyTo = "<yellow>You have no one to reply to.</yellow>";

    public final static String chatCantMessageSelf = "<yellow>You can't message yourself.</yellow>";

    public final static String chatCantIgnoreSelf = "<yellow>You can't ignore yourself.</yellow>";

    public final static String chatNowChattingWith = "<green>You are now chatting with %s.</green>";

    public final static String chatMovedToGlobal = "<yellow>You are now in local chat.</yellow>"; //previously global chat, changed to prevent confusing with global ! chat group

    public final static String chatGroupNotFound = "<red>There is no group with that name.</red>";

    public final static String chatGroupAlreadyChatting = "<yellow>You are already chatting in that group.</yellow>";

    public final static String chatGroupNowChattingIn = "<green>You are now chatting in group %s.</green>";

    public final static String chatGroupNoPerms = "<red>You don't have permission to chat in this group.</red>";

    public final static String chatNeedToUnignore = "<yellow>You need to unignore %s.</yellow>";

    public final static String chatPlayerNotFound = "<red>No player exists with that name.</red>";

    public final static String chatNowIgnoring = "<green>You are now ignoring %s</green>";

    public final static String chatStoppedIgnoring = "<green>You stopped ignoring %s.</green>";

    public final static String chatNotIgnoringAnyPlayers = "<gold>You are not ignoring any players.</gold>";

    public final static String chatNotIgnoringAnyGroups = "<gold>You are not ignoring any groups.</gold>";

    public final static String chatPlayerIgnoringYou = "<yellow>That player is ignoring you.</yellow>";

    public final static String chatRemovedFromChat = "<green>You left private chat.</green>";

    public final static String chatNotInPrivateChat = "<yellow>You aren't in private chat.</yellow>";

    public final static String chatAfk = "<blue>You are now AFK, type /afk to remove AFK status.</blue>";

    public final static String chatNotAfk = "<blue>You are no longer AFK.</blue>";

    public final static String chatPlayerAfk = "<aqua>That player is currently AFK.</aqua>";

    public final static String chatGroupMessage = "<gray>[%s] %s: <white>\"%s\"";

    public final static String globalMuted = "<red>You are muted from global and local chat for %s</red>";
}
