package com.untamedears.citadel.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class MineCartListener implements Listener {
	/* commenting out this code as it is not needed for Civ craft. leaving it for others
	private CitadelDao dao;
	private Map<String, Faction> currentGroup = new HashMap<String, Faction>();
	public MineCartListener(CitadelDao dao){
		this.dao = dao;
	}
	@EventHandler
	public void enter(VehicleEnterEvent event){
		Entity ent = event.getEntered();
		Player player = null;
		if (ent instanceof Player){
			player = (Player) ent;
		}
		if (player == null) return;
		Location loc = event.getVehicle().getLocation();
		IReinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);
		Faction groupName = null;
		if (rein instanceof PlayerReinforcement) {
			groupName = ((PlayerReinforcement) rein).getOwner();
		}
		if (groupName == null){
			return;
		}
		currentGroup.put(player.getName(), groupName);
		boolean cancel = dao.blackListPlayer(player.getName(), groupName);
		event.setCancelled(cancel);
	}
	
	@EventHandler
	public void travel(VehicleMoveEvent event){
		// stole this method from Juke Alert, it was easier
		Location from = event.getFrom();
        Location to = event.getTo();
		 if (from.getBlockX() == to.getBlockX()
	                && from.getBlockY() == to.getBlockY()
	                && from.getBlockZ() == to.getBlockZ()
	                && from.getWorld().equals(to.getWorld())) {
	            // Player didn't move by at least one block.
	            return;
	        }
		Location loc = event.getVehicle().getLocation();
		IReinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);
		Faction groupName = null;
		if (rein instanceof PlayerReinforcement) {
			groupName = ((PlayerReinforcement) rein).getOwner();
		}
		if (groupName == null) return;
		Player player;
		if (event.getVehicle().getPassenger() instanceof Player){
			player = (Player) event.getVehicle().getPassenger();
			}
		else return;
		if (currentGroup.get(player.getName()) == null){
			boolean cancel = dao.blackListPlayer(player.getName(), groupName);
			if (cancel){
				player.leaveVehicle();
				return;
			}
		}
		if (groupName.equals(currentGroup.get(player.getName()))) return;
		boolean cancel = dao.blackListPlayer(player.getName(), groupName);
		if (cancel) player.leaveVehicle();
		currentGroup.put(player.getName(), groupName);
	}
	
	@EventHandler
	public void exit(VehicleExitEvent event){
		Player player = null;
		if (event.getExited() instanceof Player){
			player = (Player) event.getVehicle().getPassenger();
			}
		currentGroup.remove(player.getName());
	}
	*/
}
