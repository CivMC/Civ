package com.github.maxopoly.finale;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.maxopoly.finale.combat.AsyncPacketHandler;
import com.github.maxopoly.finale.combat.CPSHandler;
import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;
import com.github.maxopoly.finale.potion.PotionHandler;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent;

public class FinaleManager {

	private boolean debug;
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private boolean ctpOnLogin;
	private SaturationHealthRegenHandler regenHandler;
	private WeaponModifier weaponModifier;
	private ArmourModifier armourModifier;
	private PotionHandler potionHandler;
	private boolean invulTicksEnabled;
	private Map<EntityDamageEvent.DamageCause, Integer> invulnerableTicks;
	
	private CPSHandler cpsHandler;
	private CombatConfig combatConfig;
	private AsyncPacketHandler combatHandler;

	public FinaleManager(boolean debug, boolean attackSpeedEnabled, double attackSpeed, boolean invulTicksEnabled, Map<EntityDamageEvent.DamageCause, Integer> invulnerableTicks, boolean regenHandlerEnabled,
			boolean ctpOnLogin, SaturationHealthRegenHandler regenHandler, WeaponModifier weaponModifier, ArmourModifier armourModifier, PotionHandler potionHandler, CombatConfig combatConfig) {
		this.debug = debug;
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.weaponModifier = weaponModifier;
		this.armourModifier = armourModifier;
		this.potionHandler = potionHandler;
		this.combatConfig = combatConfig;
		this.invulTicksEnabled = invulTicksEnabled;
		this.invulnerableTicks = invulnerableTicks;
		this.ctpOnLogin = ctpOnLogin;
		
		this.cpsHandler = new CPSHandler();
		
		Bukkit.getScheduler().runTaskAsynchronously(Finale.getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(combatHandler = new AsyncPacketHandler()).start();
			}
			
		});
	}
	
	public AsyncPacketHandler getCombatHandler() {
		return combatHandler;
	}

	public boolean isInvulTicksEnabled() {
		return invulTicksEnabled;
	}
	
	public Map<EntityDamageEvent.DamageCause, Integer> getInvulnerableTicks() {
		return invulnerableTicks;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public boolean getCTPOnLogin() {
		return ctpOnLogin;
	}

	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}
	
	public CPSHandler getCPSHandler() {
		return cpsHandler;
	}
	
	public CombatConfig getCombatConfig() {
		return combatConfig;
	}

	public PotionHandler getPotionHandler() {
		return potionHandler;
	}
	
	public ArmourModifier getArmourModifier() {
		return armourModifier;
	}

	public WeaponModifier getWeaponModifer() {
		return weaponModifier;
	}

	public boolean isAttackSpeedEnabled() {
		return attackSpeedEnabled;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isRegenHandlerEnabled() {
		return regenHandlerEnabled;
	}
}
