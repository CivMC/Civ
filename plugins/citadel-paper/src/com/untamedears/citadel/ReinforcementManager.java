package com.untamedears.citadel;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.untamedears.citadel.entity.Reinforcement;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class ReinforcementManager {
	
	private ReinforcementStorage storage;

	public ReinforcementManager(){}
	
	public ReinforcementStorage getStorage(){
		return this.storage;
	}
	
	public void setStorage(ReinforcementStorage storage){
		this.storage = storage;
	}

	public Reinforcement getReinforcement(Block block){
		return this.storage.findReinforcement(block);
	}

	public Reinforcement getReinforcement(Location location) {
		return getReinforcement(location.getBlock());
	}
	
	public Set<Reinforcement> getReinforcementsByGroup(String groupName){
		return this.storage.findReinforcementsByGroup(groupName);
	}
	
	public void moveReinforcements(String from, String target){
		this.storage.moveReinforcements(from, target);
	}
	
	public void addReinforcement(Reinforcement reinforcement){
		this.storage.addReinforcement(reinforcement);
	}
	
	public void removeReinforcement(Reinforcement reinforcement){
		this.storage.removeReinforcement(reinforcement);
	}
	
	public int getReinforcementsAmount(){
		return this.storage.findReinforcementsAmount();
	}
}
