package com.untamedears.citadel;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.IReinforcement;

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

	public IReinforcement addReinforcement(IReinforcement reinforcement){
		return (IReinforcement)this.dao.save(reinforcement);
	}

	public void removeReinforcement(IReinforcement reinforcement) {
		this.dao.delete(reinforcement);
	}
	
	public IReinforcement findReinforcement(Block block){
		return this.dao.findReinforcement(block);
	}

	public IReinforcement findReinforcement(Location location) {
		return this.dao.findReinforcement(location);
	}
	
	public Set<IReinforcement> findReinforcementsByGroup(String groupName){
		return this.dao.findReinforcementsByGroup(groupName);
	}
	
	public void moveReinforcements(String from, String target){
		this.dao.moveReinforcements(from, target);
	}
	
	public int findReinforcementsAmount(){
		return this.dao.countReinforcements();
	}
}
