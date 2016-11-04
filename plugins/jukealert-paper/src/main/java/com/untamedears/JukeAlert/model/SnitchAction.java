package com.untamedears.JukeAlert.model;

import java.util.Date;

import org.bukkit.Material;

public class SnitchAction {

	private int snitchActionId;
	private int snitchId;
	private Date date;
	private LoggedAction action;
	private String initiateUser;
	private String victim;
	private int x;
	private int y;
	private int z;
	private Material material;

	public SnitchAction(int snitchActionId, int snitchId, Date date, LoggedAction action, String initiateUser,
			String victim, int x, int y, int z, Material material) {
		this.snitchActionId = snitchActionId;
		this.snitchId = snitchId;
		this.date = date;
		this.action = action;
		this.initiateUser = initiateUser;
		this.victim = victim;
		this.x = x;
		this.y = y;
		this.z = z;
		this.material = material;
	}

	public int getSnitchActionId() {
		return snitchActionId;
	}

	public int getSnitchId() {
		return snitchId;
	}

	public Date getDate() {
		return date;
	}

	public LoggedAction getAction() {
		return action;
	}

	public String getInitiateUser() {
		return initiateUser;
	}

	public String getVictim() {
		return victim;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public Material getMaterial() {
		return material;
	}

}
