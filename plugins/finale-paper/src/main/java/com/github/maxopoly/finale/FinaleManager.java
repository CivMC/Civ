package com.github.maxopoly.finale;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.maxopoly.finale.combat.AsyncPacketHandler;
import com.github.maxopoly.finale.combat.CPSHandler;
import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.combat.SprintHandler;
import com.github.maxopoly.finale.misc.*;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import com.github.maxopoly.finale.misc.arrow.ArrowHandler;
import com.github.maxopoly.finale.misc.crossbow.CrossbowHandler;
import com.github.maxopoly.finale.misc.warpfruit.WarpFruitTracker;
import com.github.maxopoly.finale.potion.PotionHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
	private AllyHandler allyHandler;
	private ArrowHandler arrowHandler;
	private TridentHandler tridentHandler;
	private ShieldHandler shieldHandler;
	private CrossbowHandler crossbowHandler;
	private GappleHandler gappleHandler;
	private TippedArrowModifier tippedArrowModifier;
	private BlockRestrictionHandler blockRestrictionHandler;
	private Set<UUID> chemtrails = new HashSet<>();

	private boolean invulTicksEnabled;
	private Map<EntityDamageEvent.DamageCause, Integer> invulnerableTicks;

	private WarpFruitTracker warpFruitTracker;

	private SprintHandler sprintHandler;
	private CPSHandler cpsHandler;
	private CombatConfig combatConfig;
	private AsyncPacketHandler combatHandler;

	public FinaleManager(boolean debug, boolean attackSpeedEnabled, double attackSpeed, boolean invulTicksEnabled, Map<EntityDamageEvent.DamageCause, Integer> invulnerableTicks, boolean regenHandlerEnabled,
			boolean ctpOnLogin, SaturationHealthRegenHandler regenHandler, WeaponModifier weaponModifier, ArmourModifier armourModifier,
						 AllyHandler allyHandler, ArrowHandler arrowHandler, TridentHandler tridentHandler, ShieldHandler shieldHandler, CrossbowHandler crossbowHandler,
						 GappleHandler gappleHandler, PotionHandler potionHandler, TippedArrowModifier tippedArrowModifier, BlockRestrictionHandler blockRestrictionHandler,
						 CombatConfig combatConfig, WarpFruitTracker warpFruitTracker) {
		this.debug = debug;
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.weaponModifier = weaponModifier;
		this.armourModifier = armourModifier;
		this.potionHandler = potionHandler;
		this.allyHandler = allyHandler;
		this.arrowHandler = arrowHandler;
		this.tridentHandler = tridentHandler;
		this.shieldHandler = shieldHandler;
		this.crossbowHandler = crossbowHandler;
		this.gappleHandler = gappleHandler;
		this.tippedArrowModifier = tippedArrowModifier;
		this.blockRestrictionHandler = blockRestrictionHandler;
		this.combatConfig = combatConfig;
		this.invulTicksEnabled = invulTicksEnabled;
		this.invulnerableTicks = invulnerableTicks;
		this.ctpOnLogin = ctpOnLogin;
		this.warpFruitTracker = warpFruitTracker;
		
		this.cpsHandler = new CPSHandler();
		this.sprintHandler = new SprintHandler();

		Bukkit.getScheduler().runTaskAsynchronously(Finale.getPlugin(), () -> ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(combatHandler = new AsyncPacketHandler(combatConfig)).start());
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

	public SprintHandler getSprintHandler() {
		return sprintHandler;
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

	public AllyHandler getAllyHandler() {
		return allyHandler;
	}

	public ArrowHandler getArrowHandler() {
		return arrowHandler;
	}

	public TridentHandler getTridentHandler() {
		return tridentHandler;
	}

	public ShieldHandler getShieldHandler() {
		return shieldHandler;
	}

	public CrossbowHandler getCrossbowHandler() {
		return crossbowHandler;
	}

	public GappleHandler getGappleHandler() {
		return gappleHandler;
	}

	public TippedArrowModifier getTippedArrowModifier() {
		return tippedArrowModifier;
	}

	public BlockRestrictionHandler getBlockRestrictionHandler() {
		return blockRestrictionHandler;
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

	public WarpFruitTracker getWarpFruitTracker() {
		return warpFruitTracker;
	}

	public Set<UUID> getChemtrails() {
		return chemtrails;
	}
}
