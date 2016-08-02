package com.github.maxopoly.finale;

import java.util.Map;

import org.bukkit.Material;

import com.github.maxopoly.finale.external.ProtocolLibManager;
import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;

public class FinaleManager {
	
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private SaturationHealthRegenHandler regenHandler;
	private Map <Material, Integer> attackDamageChanges;
	private boolean protocolLibEnabled;
	
	public FinaleManager(boolean attackSpeedEnabled, double attackSpeed, boolean regenHandlerEnabled,
			SaturationHealthRegenHandler regenHandler,Map <Material, Integer> attackDamageChanges, boolean protocolLibEnabled) {
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.protocolLibEnabled = protocolLibEnabled;
		this.attackDamageChanges = attackDamageChanges;
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
	
	public Integer getAdjustAttackDamage(Material m) {
	    if (m == null) {
		return null;
	    }
	    return attackDamageChanges.get(m);
	}
	
	public boolean protocolLibEnabled() {
		return protocolLibEnabled;
	}
}
