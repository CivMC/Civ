package com.aleksey.castlegates.jukealert;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import com.aleksey.castlegates.CastleGates;
import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

public class JukeAlertManager implements IJukeAlertManager {
    private SnitchManager _snitchManager = JukeAlert.getInstance().getSnitchManager();
	
	public boolean hasToggleLeverSnitchInRadius(Location loc, int groupId) {
		if(!CastleGates.getConfigManager().getInteractWithSnitches()) {
			return false;
		}
		
		World world = loc.getWorld();
		Set<Snitch> snitches = _snitchManager.findSnitches(world, loc);
		
		if(snitches.size() > 0) {
			double distance = CastleGates.getCitadelManager().getMaxRedstoneDistance();
			
			for(Snitch snitch : snitches) {
				if(snitch.getGroup().getGroupId() == groupId
						&& snitch.shouldToggleLevers()
						&& snitch.getLoc().distance(loc) <= distance
					)
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
