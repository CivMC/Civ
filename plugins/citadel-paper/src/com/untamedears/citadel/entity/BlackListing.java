package com.untamedears.citadel.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="blacklist", uniqueConstraints = {
		@UniqueConstraint(columnNames={"faction", "player"})})
public class BlackListing {

	@Id private String player;
	@Id private String faction;
	public BlackListing(){
		
	}
	public BlackListing (String player, String faction){
		this.faction = faction;
		this.player = player;
	}
	
	public String getFaction(){
		return faction;
	}
	
	public void setFaction(String faction){
		this.faction = faction;
	}
	
	public void setPlayer(String player){
		this.player = player;
	}
	
	public String getPlayer(){
		return player;
	}
}
