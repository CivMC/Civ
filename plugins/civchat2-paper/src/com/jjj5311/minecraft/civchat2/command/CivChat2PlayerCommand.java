package com.jjj5311.minecraft.civchat2.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.jjj5311.minecraft.civchat2.CivChat2;
import com.jjj5311.minecraft.civchat2.CivChat2Manager;
import com.jjj5311.minecraft.civchat2.utility.CivChat2Log;


public abstract class CivChat2PlayerCommand implements CivChat2Command{
	
	protected CivChat2Manager chatMan = CivChat2.getCivChat2Manager();
	protected CivChat2Log logger = CivChat2.getCivChat2Log();
	private String name = "";
	private String description = "";
	private String usage = "";
	private String identifier = "";
	private int min = 0;
	private int max = 0;
	
	public CivChat2PlayerCommand(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getMaxArguments() {
		return max;
	}

	public int getMinArguments() {
		return min;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setDescription(String desc){
		Bukkit.getPluginCommand(identifier).setDescription(desc);
		this.description = desc;
	}
	
	public void setUsage(String usage){
		Bukkit.getPluginCommand(identifier).setUsage(usage);
		this.usage = usage;
	}
	
	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}

	public void setArgs(int min, int max){
		this.min = min;
		this.max = max;
	}
}
