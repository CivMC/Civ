package com.untamedears.citadel;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Reinforcement;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class ReinforcementStorage {

	private CitadelDao dao;
	
	public ReinforcementStorage(CitadelDao dao) {
		this.dao = dao;
	}

	public void addReinforcement(Reinforcement reinforcement){
		this.dao.save(reinforcement);
	}

	public void removeReinforcement(Reinforcement reinforcement) {
		this.dao.delete(reinforcement);
	}
	
	public Reinforcement findReinforcement(Block block){
		return this.dao.findReinforcement(block);
	}

	public Reinforcement findReinforcement(Location location) {
		return this.dao.findReinforcement(location);
	}
	
	public Set<Reinforcement> findReinforcementsByGroup(String groupName){
		return this.dao.findReinforcementsByGroup(groupName);
	}
	
	public void moveReinforcements(String from, String target){
		this.dao.moveReinforcements(from, target);
	}
	
	public int findReinforcementsAmount(){
		return this.dao.countReinforcements();
	}
}
