package com.github.maxopoly.finale;

import org.bukkit.Bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.maxopoly.finale.combat.AsyncPacketHandler;
import com.github.maxopoly.finale.combat.CPSHandler;
import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.combat.CombatRunnable;
import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;
import com.github.maxopoly.finale.potion.PotionHandler;

public class FinaleManager {

	private boolean debug;
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private int invulnerableTicks;
	private SaturationHealthRegenHandler regenHandler;
	private WeaponModifier weaponModifier;
	private PotionHandler potionHandler;
	
	private CPSHandler cpsHandler;
	private CombatRunnable combatRunnable;
	private CombatConfig combatConfig;

	public FinaleManager(boolean debug, boolean attackSpeedEnabled, double attackSpeed, int invulnerableTicks, boolean regenHandlerEnabled,
			SaturationHealthRegenHandler regenHandler, WeaponModifier weaponModifier, PotionHandler potionHandler, CombatConfig combatConfig) {
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.weaponModifier = weaponModifier;
		this.potionHandler = potionHandler;
		this.combatConfig = combatConfig;
		this.invulnerableTicks = invulnerableTicks;
		
		this.cpsHandler = new CPSHandler();
		this.combatRunnable = new CombatRunnable();
		Bukkit.getScheduler().runTaskTimer(Finale.getPlugin(), combatRunnable, 0L, 1L);
		
		Bukkit.getScheduler().runTaskAsynchronously(Finale.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new AsyncPacketHandler()).start();
			}
			
		});
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}
	
	public CPSHandler getCPSHandler() {
		return cpsHandler;
	}
	
	public CombatRunnable getCombatRunnable() {
		return combatRunnable;
	}
	
	public CombatConfig getCombatConfig() {
		return combatConfig;
	}

	public PotionHandler getPotionHandler() {
		return potionHandler;
	}

	public WeaponModifier getWeaponModifer() {
		return weaponModifier;
	}

	public boolean isAttackSpeedEnabled() {
		return attackSpeedEnabled;
	}

	public int getInvulnerableTicks() {
		return invulnerableTicks;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isRegenHandlerEnabled() {
		return regenHandlerEnabled;
	}
}
