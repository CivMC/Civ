package com.github.maxopoly;

import com.github.maxopoly.external.ProtocolLibManager;
import com.github.maxopoly.misc.SaturationHealthRegenHandler;

public class FinaleManager {
	
	private double attackSpeed;
	private SaturationHealthRegenHandler regenHandler;
	private boolean protocolLibEnabled;
	
	public FinaleManager(double attackSpeed, SaturationHealthRegenHandler regenHandler, boolean protocolLibEnabled) {
		this.attackSpeed = attackSpeed;
		this.regenHandler = regenHandler;
		this.protocolLibEnabled = protocolLibEnabled;
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
