package com.github.maxopoly;

import com.github.maxopoly.external.ProtocolLibManager;
import com.github.maxopoly.misc.SaturationHealthRegenHandler;

public class FinaleManager {
	
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private SaturationHealthRegenHandler regenHandler;
	private boolean protocolLibEnabled;
	
	public FinaleManager(boolean attackSpeedEnabled, double attackSpeed, boolean regenHandlerEnabled,
			SaturationHealthRegenHandler regenHandler, boolean protocolLibEnabled) {
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.protocolLibEnabled = protocolLibEnabled;
	}
	
	public boolean isAttackSpeedEnabled() {
		return attackSpeedEnabled;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}
	
	public boolean isRegenHandlerEnabled() {
		return regenHandlerEnabled;
	}

	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}
	
	public boolean protocolLibEnabled() {
		return protocolLibEnabled;
	}
}
