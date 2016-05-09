package com.github.maxopoly;

import java.util.HashMap;

import org.bukkit.Material;

import com.github.maxopoly.listeners.ProtocolLibPacketListener;
import com.github.maxopoly.misc.SaturationHealthRegenHandler;

public class FinaleManager {
	
	private double attackSpeed;
	private SaturationHealthRegenHandler regenHandler;
	private boolean protocolLibEnabled;
	
	public FinaleManager(double attackSpeed, SaturationHealthRegenHandler regenHandler, boolean protocolLibEnabled) {
		this.attackSpeed = attackSpeed;
		this.regenHandler = regenHandler;
		this.protocolLibEnabled = protocolLibEnabled;
		if (protocolLibEnabled) {
			new ProtocolLibPacketListener(new HashMap<Material, Long>());
		}
	}
	
	public double getAttackSpeed() {
		return attackSpeed;
	}
	
	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}
	
	public boolean protocolLibEnabled() {
		return protocolLibEnabled;
	}
}
