package isaac.bastion;

import isaac.bastion.manager.BastionBlockManager;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class EnderPearlListener implements Listener {
	int taskId;
	Server server;
	BastionBlockManager manager;
 	public EnderPearlListener(){
 		server=Bastion.getPlugin().getServer();
 		manager=Bastion.getBastionManager();
 		
 		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 		taskId=scheduler.scheduleSyncRepeatingTask(Bastion.getPlugin(),
				new BukkitRunnable(){
			public void run(){
				tick();
			}
		},
		5,5);
 	}
	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void handleEnderPearlLanded(PlayerTeleportEvent event){
 		if(event.isCancelled())
 			return;
 		
 		manager.handleEnderPearlLanded(event);
 	}
 	
 	private void tick(){
 		for(World world : server.getWorlds()){
 			tickForWorld(world);
 		}
 	}
 	private void tickForWorld(World world){
 		Collection<EnderPearl> flying=world.getEntitiesByClass(EnderPearl.class);
 		for(EnderPearl pearl : flying){
 			manager.handleEnderPearlThrown(pearl);
 		}
 	}
}
