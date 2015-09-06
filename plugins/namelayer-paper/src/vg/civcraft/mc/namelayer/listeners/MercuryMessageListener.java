package vg.civcraft.mc.namelayer.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.events.GroupTransferEvent;
import vg.civcraft.mc.namelayer.group.Group;

public class MercuryMessageListener implements Listener{
	
	private GroupManager gm = NameAPI.getGroupManager();
	private NameLayerPlugin nl = NameLayerPlugin.getInstance();
	
	public MercuryMessageListener() {
		MercuryAPI.instance.registerPluginMessageChannel("namelayer");
		NameLayerPlugin.setOnlineAllServers(new HashMap<String,String>());
		MercuryAPI.instance.sendMessage("all", "whoonline "+MercuryPlugin.name, "namelayer");
		nl.getLogger().info("Requested player lists");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(nl, new Runnable() {

			@Override
			public void run() {
				StringBuilder message = new StringBuilder();
				message.append("sync " + MercuryPlugin.name + " ");
				for (Player p: Bukkit.getOnlinePlayers())
					message.append(p.getName() + ";");
			}
			
		}, 10, 100);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equalsIgnoreCase("namelayer"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];
		if (reason.equals("whoonline")){
			String playerlist = "";
			for(Player p : Bukkit.getOnlinePlayers()){
				playerlist = playerlist+p.getDisplayName()+";";
			}
			if (playerlist.isEmpty()){return;}
			playerlist = playerlist.substring(0, playerlist.length()-1);
			MercuryPlugin.handler.sendMessage(message[1], "sync "+MercuryPlugin.name+" "+playerlist, "namelayer");
			NameLayerPlugin.getInstance().getLogger().info("Responded to server '"+message[1]+"' sync request");
			return;
		} else if (reason.equals("login")){
			NameLayerPlugin.getOnlineAllServers().put(message[2].toLowerCase(),message[1]);
			NameLayerPlugin.getInstance().getLogger().info("Player "+message[2]+" has logged in on server: "+message[1]);
			return;
		} else if (reason.equals("logoff")){
			NameLayerPlugin.getOnlineAllServers().remove(message[2]);
			NameLayerPlugin.getInstance().getLogger().info("Player "+message[2]+" has logged off on server: "+message[1]);
			return;
		} else if (reason.equals("sync")){
			String[] players = message[2].split(";");
			String allsynced = "";
			for (String player : players){
				if (!NameLayerPlugin.getOnlineAllServers().containsKey(player))
					NameLayerPlugin.getOnlineAllServers().put(player.toLowerCase(), message[1]);
				allsynced = allsynced+player+" ,";
			}
			if (allsynced.isEmpty()){return;}
			allsynced = allsynced.substring(0, allsynced.length()-2);
			NameLayerPlugin.getInstance().getLogger().info("Synced players from '"+message[1]+"': "+allsynced);
			return;
		}
		
		String group = message[1];
		Group g = GroupManager.getGroup(group);
		if (reason.equals("recache")){
			gm.invalidateCache(g.getName());
		}
		else if (reason.equals("delete")){
			GroupDeleteEvent e = new GroupDeleteEvent(g, true);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
		}
		else if (reason.equals("merge")){
			GroupMergeEvent e = new GroupMergeEvent(g, GroupManager.getGroup(message[2]), true);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
			gm.invalidateCache(message[2]);
		}
		else if (reason.equals("transfer")){
			GroupTransferEvent e = new GroupTransferEvent(g, UUID.fromString(message[2]));
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerJoinEvent event){
		MercuryPlugin.handler.sendMessage("all", "login "+MercuryPlugin.name+" "+event.getPlayer().getDisplayName(), "namelayer");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogoff(PlayerQuitEvent event){
		MercuryPlugin.handler.sendMessage("all", "logoff "+MercuryPlugin.name+" "+event.getPlayer().getDisplayName(), "namelayer");
	}

}
