package vg.civcraft.mc.namelayer.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.events.GroupTransferEvent;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.Group;

public class MercuryMessageListener extends BukkitRunnable implements Listener{
	
	private GroupManager gm = NameAPI.getGroupManager();
	public MercuryMessageListener() {}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equalsIgnoreCase("name_layer"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];
		if (reason.equals("whoonline")){
			String playerlist = "";
			for(Player p : NameLayerPlugin.getInstance().getServer().getOnlinePlayers()){
				playerlist = playerlist+p.getDisplayName()+";";
			}
			if (playerlist.isEmpty()){return;}
			playerlist = playerlist.substring(0, playerlist.length()-1);
			MercuryPlugin.handler.sendMessage(message[1], "name_layer", "sync "+playerlist);
			NameLayerPlugin.getInstance().getLogger().info("Responded to server '"+message[1]+"' sync request");
			return;
		} else if (reason.equals("login")){
			NameLayerPlugin.getOnlineAllServers().add(message[1]);
			NameLayerPlugin.getInstance().getLogger().info("Player "+message[1]+" has logged in on other server");
			return;
		} else if (reason.equals("logoff")){
			NameLayerPlugin.getOnlineAllServers().remove(message[1]);
			NameLayerPlugin.getInstance().getLogger().info("Player "+message[1]+" has logged off on other server");
			return;
		} else if (reason.equals("sync")){
			String[] players = message[1].split(";");
			String allsynced = "";
			for (String player : players){
				NameLayerPlugin.getOnlineAllServers().add(player);
				allsynced = allsynced+player+" ,";
			}
			if (allsynced.isEmpty()){return;}
			allsynced = allsynced.substring(0, allsynced.length()-1);
			NameLayerPlugin.getInstance().getLogger().info("Synced players: "+allsynced);
			return;
		}
		
		String group = message[1];
		Group g = gm.getGroup(group);
		if (reason.equals("recache")){
			gm.invalidateCache(g.getName());
		}
		else if (reason.equals("delete")){
			GroupDeleteEvent e = new GroupDeleteEvent(g, true);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
		}
		else if (reason.equals("merge")){
			GroupMergeEvent e = new GroupMergeEvent(g, gm.getGroup(message[2]), true);
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
		MercuryPlugin.handler.sendMessage("all", "name_layer", "login "+event.getPlayer().getDisplayName());
		Bukkit.getLogger().info("LOGIN");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogoff(PlayerQuitEvent event){
		MercuryPlugin.handler.sendMessage("all", "name_layer", "logoff "+event.getPlayer().getDisplayName());
		Bukkit.getLogger().info("LOGOFF");
	}

	@Override
	public void run() {
		NameLayerPlugin nl = NameLayerPlugin.getInstance();
		nl.isMercuryEnabled = Bukkit.getPluginManager().isPluginEnabled("Mercury");
		if (nl.isMercuryEnabled){
			nl.getServer().getPluginManager().registerEvents(this, nl);
			nl.setOnlineAllServers(new ArrayList<String>());
			MercuryPlugin.handler.sendMessage("all", "name_layer", "whoonline "+MercuryPlugin.name);
			nl.getLogger().info("Requested player lists");
		}
		
	}
}
