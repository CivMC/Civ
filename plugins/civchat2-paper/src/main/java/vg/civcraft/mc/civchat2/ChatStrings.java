package vg.civcraft.mc.civchat2;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;

public class ChatStrings {

    public final static ChatRenderer localChatFormat = (source, sourceDisplayName, message, viewer) -> Component.empty()
        .append(Component.text("<"))
        .append(sourceDisplayName)
        .append(Component.text("> "))
        .append(message);

    public final static String chatPlayerIsOffline = ChatColor.YELLOW + "That player is offline.";

    public final static String chatNoOneToReplyTo = ChatColor.YELLOW + "You have no one to reply to.";

    public final static String chatCantMessageSelf = ChatColor.YELLOW + "You can't message yourself.";

    public final static String chatCantIgnoreSelf = ChatColor.YELLOW + "You can't ignore yourself.";

    public final static String chatNowChattingWith = ChatColor.GREEN + "You are now chatting with %s.";

    public final static String chatMovedToGlobal = ChatColor.YELLOW + "You are now in local chat."; //previously global chat, changed to prevent confusing with global ! chat group

    public final static String chatGroupNotFound = ChatColor.RED + "There is no group with that name.";

    public final static String chatGroupAlreadyChatting = ChatColor.YELLOW + "You are already chatting in that group.";

    public final static String chatGroupNowChattingIn = ChatColor.GREEN + "You are now chatting in group %s.";

    public final static String chatGroupNoPerms = ChatColor.RED + "You don't have permission to chat in this group.";

    public final static String chatNeedToUnignore = ChatColor.YELLOW + "You need to unignore %s.";

    public final static String chatPlayerNotFound = ChatColor.RED + "No player exists with that name.";

    public final static String chatNowIgnoring = ChatColor.GREEN + "You are now ignoring %s";

    public final static String chatStoppedIgnoring = ChatColor.GREEN + "You stopped ignoring %s.";

    public final static String chatNotIgnoringAnyPlayers = ChatColor.GOLD + "You are not ignoring any players.";

    public final static String chatNotIgnoringAnyGroups = ChatColor.GOLD + "You are not ignoring any groups.";

    public final static String chatPlayerIgnoringYou = ChatColor.YELLOW + "That player is ignoring you.";

    public final static String chatRemovedFromChat = ChatColor.GREEN + "You left private chat.";

    public final static String chatNotInPrivateChat = ChatColor.YELLOW + "You aren't in private chat.";

    public final static String chatAfk = ChatColor.BLUE + "You are now AFK, type /afk to remove AFK status.";

    public final static String chatNotAfk = ChatColor.BLUE + "You are no longer AFK.";

    public final static Component chatPlayerAfk = Component.text("That player is currently AFK.", NamedTextColor.AQUA);

    public final static String globalMuted = ChatColor.RED + "You are muted from global and local chat for %s";
}
