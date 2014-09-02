package com.untamedears.citadel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.untamedears.citadel.entity.Member;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class MemberManager {

	private Map<String, Player> online = new HashMap<String, Player>();
	private MemberStorage storage;
	
	public MemberManager(){}
	
	public MemberStorage getStorage(){
		return this.storage;
	}
	
	public void setStorage(MemberStorage storage){
		this.storage = storage;
	}
	
	public void addOnlinePlayer(Player player){
		String name = player.getName().toLowerCase();
		if(this.online.containsKey(name)){
			return;
		}
		this.online.put(name, player);
	}

	public void removeOnlinePlayer(Player player) {
		String name = player.getName().toLowerCase();
		if(!this.online.containsKey(name)){
			return;
		}
		this.online.remove(player);
	}
	
	public boolean isOnline(String playerName){
		return this.online.containsKey(playerName.toLowerCase());
	}
	
	public Player getOnlinePlayer(String playerName){
		return this.online.get(playerName.toLowerCase());
	}
	
	public Collection<Player> getOnlinePlayers(){
		return this.online.values();
	}
	
	public void addMember(Player player){
		Member member = new Member(player.getName());
		addMember(member);
	}
	
	public void addMember(Member member){
		this.storage.addMember(member);
	}
	
	public void removeMember(Player player){
		removeMember(getMember(player));
	}
	
	public void removeMember(Member member){
		this.storage.removeMember(member);
	}
	
	public boolean hasMember(Player player){
		if(this.storage.getMember(player.getName()) != null){
			return true;
		}
		return false;
	}
	
	public Member getMember(Player player){
		return getMember(player.getName());
	}
	
	public Member getMember(String memberName){
		return this.storage.getMember(memberName);
	}
}
