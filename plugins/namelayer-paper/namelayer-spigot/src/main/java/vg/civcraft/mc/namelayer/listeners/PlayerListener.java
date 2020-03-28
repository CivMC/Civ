package vg.civcraft.mc.namelayer.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.RunnableOnGroup;
import vg.civcraft.mc.namelayer.group.Group;

public class PlayerListener implements Listener{

	private static Map<UUID, Set<Group>> notifications = new HashMap<>();
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void playerJoinEvent(PlayerJoinEvent event){
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		
		if (!p.hasPlayedBefore()) {
			handleFirstJoin(p);
		}
		
		if (!notifications.containsKey(uuid) || notifications.get(uuid).isEmpty())
			return;
		
		String x = null;
				
		boolean shouldAutoAccept = NameLayerPlugin.getAutoAcceptHandler().getAutoAccept(uuid);
		if(shouldAutoAccept){
			x = "You have auto-accepted invitation from the following groups while you were away: ";
		}
		else{
			x = "You have been invited to the following groups while you were away. You can accept each invitation by using the command: /nlag [groupname].  ";
		}			
		
		for (Group g: getNotifications(uuid)){
			x += g.getName() + ", ";
		}
		x = x.substring(0, x.length()- 2);
		x += ".";
		p.sendMessage(ChatColor.YELLOW + x);
	}
	
	public static void addNotification(UUID u, Group g) {
		getNotifications(u).add(g);
	}

	public static Set<Group> getNotifications(UUID player) {
		return notifications.computeIfAbsent(player, e -> new HashSet<>());
	}
	
	public static void removeNotification(UUID u, Group g){
		getNotifications(u).remove(g);
	}
	
	public static String getNotificationsInStringForm(UUID u){
		String groups = "";
		for (Group g: getNotifications(u))
			groups += g.getName() + ", ";
		if (groups.length() == 0)
			return ChatColor.GREEN + "You have no notifications.";
		groups = groups.substring(0, groups.length()- 2);
		groups = ChatColor.GREEN + "Your current groups are: " + groups + ".";
		return groups;
	}
	
	private void handleFirstJoin(Player p) {
		if (!NameLayerPlugin.createGroupOnFirstJoin()) {
			return;
		}
		if (NameLayerPlugin.getDefaultGroupHandler().getDefaultGroup(p) != null) {
			//assume something went wrong, feel free to chose a random civcraft dev to blame
			return;
		}
		final UUID uuid = p.getUniqueId();
		final String name = p.getName();
		
		new NewfriendCreate(name, uuid).bootstrap();
	}
	
	/**
	 * This simple (hah) runnable encapsulates the prior logic in a safe, but asynchronous fashion.
	 * The code in this runnable is always called synchronously. It keeps track of which combination (name, name+1, name+2) that has been tried, and
	 * triggers the next attempt if prior has failed. If it runs out of tries, it gracefully ends.
	 * 
	 * Note that at every step it checks if group exists before creating. This adds some tick-time overhead but should work well.
	 * 
	 * All this is off the main thread (the actual database calls) so trauma should be low.
	 * 
	 * @author ProgrammerDan
	 *
	 */
	private static class NewfriendCreate extends RunnableOnGroup {
		private Integer inc = null;
		private final String name;
		private final UUID uuid;
		
		NewfriendCreate(final String name, final UUID uuid) {
			this.name = name;
			this.uuid = uuid;
		}
		
		public void bootstrap() {
			GroupManager gm = NameAPI.getGroupManager();
			gm.createGroupAsync(new Group(name, uuid, false, null, -1, System.currentTimeMillis()), this, true);
		}
		
		@Override
		public void run() {
			Group g = getGroup();
			if (g.getGroupId() == -1) { // now try + num
				NameLayerPlugin.log(Level.WARNING, "Newfriend automatic group creation failed for " + g.getName() + " " + uuid);
				GroupManager gm = NameAPI.getGroupManager();
				if (inc == null) {
					inc = 0;
				} else {
					inc ++;
				}
				if (inc < 20) {
					String newName = name + String.valueOf(inc);
					gm.createGroupAsync(new Group(newName, uuid, false, null, -1, System.currentTimeMillis()), this, true);
				}
			} else {
				NameLayerPlugin.log(Level.WARNING, "Newfriend automatic group creation succeeded for " + g.getName() + " " + uuid);
				g.setDefaultGroup(uuid);
			}
		}

	}
}
