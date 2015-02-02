package vg.civcraft.mc.namelayer.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import vg.civcraft.mc.namelayer.group.Group;

public class PlayerListener implements Listener{

	private static Map<UUID, List<Group>> notifications = new HashMap<UUID, List<Group>>();
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void playerJoinEvent(PlayerJoinEvent event){
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		if (!notifications.containsKey(uuid) || notifications.get(uuid).isEmpty())
			return;
		if (notifications.get(uuid).isEmpty())
			return;
		String x = "You have been invited to the following groups while you were away: ";
		
		for (Group g:notifications .get(uuid)){
			x += g.getName() + ", ";
		}
		x = x.substring(0, x.length()- 2);
		x += ".";
		p.sendMessage(ChatColor.YELLOW + x);
	}
	
	public static void addNotification(UUID u, Group g){
		if (!notifications.containsKey(u))
			notifications.put(u, new ArrayList<Group>());
		notifications.get(u).add(g);
	}

	public static List<Group> getNotifications(UUID player) {
		return notifications.get(player);
	}
	
	public static void removeNotification(UUID u, Group g){
		if (!notifications.containsKey(u))
			notifications.put(u, new ArrayList<Group>());
		notifications.get(u).remove(g);
	}
	
	public static String getNotificationsInStringForm(UUID u){
		if (!notifications.containsKey(u))
			notifications.put(u, new ArrayList<Group>());
		String groups = "";
		for (Group g: notifications.get(u))
			groups += g.getName() + ", ";
		if (groups.length() == 0)
			return ChatColor.GREEN + "You have no notifications.";
		groups = groups.substring(0, groups.length()- 2);
		groups = ChatColor.GREEN + "Your current groups are: " + groups + ".";
		return groups;
	}
}
