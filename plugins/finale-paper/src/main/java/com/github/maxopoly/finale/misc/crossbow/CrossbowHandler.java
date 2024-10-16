package com.github.maxopoly.finale.misc.crossbow;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.CooldownHandler;
import com.github.maxopoly.finale.misc.ItemUtil;
import dev.jayms.arsenal.Arsenal;
import dev.jayms.arsenal.artillery.Artillery;
import dev.jayms.arsenal.artillery.ArtilleryManager;
import dev.jayms.arsenal.artillery.event.MissileExplodeEvent;
import dev.jayms.arsenal.util.LocationTools;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class CrossbowHandler {

	private boolean enabled;
	private int radius;
	private float artilleryDamage;
	private double reinforcementDamage;
	private double bastionDamage;

	private Map<String, AntiAirMissile> antiAirMissiles;

	private CooldownHandler cooldowns;

	public CrossbowHandler(boolean enabled, int radius, float artilleryDamage, double reinforcementDamage, double bastionDamage, long cooldown, Map<String, AntiAirMissile> antiAirMissiles) {
		this.enabled = enabled;
		this.radius = radius;
		this.artilleryDamage = artilleryDamage;
		this.reinforcementDamage = reinforcementDamage;
		this.bastionDamage = bastionDamage;

		this.antiAirMissiles = antiAirMissiles;

		this.cooldowns = new CooldownHandler("crossbowCooldown", cooldown, (player, cooldowns) ->
				ChatColor.DARK_RED + "" + ChatColor.BOLD + "Crossbow: " +
						ChatColor.RED + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);

		new BukkitRunnable() {

			@Override
			public void run() {
				for (AntiAirMissile antiAirMissile : antiAirMissiles.values()) {
					antiAirMissile.progressInstances();
				}
			}

		}.runTaskTimer(Finale.getPlugin(), 0L, 1L);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public double getRadius() {
		return radius;
	}

	public float getArtilleryDamage() {
		return artilleryDamage;
	}

	public double getReinforcementDamage() {
		return reinforcementDamage;
	}

	public double getBastionDamage() {
		return bastionDamage;
	}

	public boolean onCooldown(Player shooter) {
		return cooldowns.onCooldown(shooter);
	}

	public void putOnCooldown(Player shooter) {
		cooldowns.putOnCooldown(shooter);
	}

	public AntiAirMissile getAntiAirMissile(ItemStack is) {
		String key = ItemUtil.getAAKey(is);
		return getAntiAirMissile(key);
	}

	public AntiAirMissile getAntiAirMissile(String key) {
		return antiAirMissiles.get(key);
	}

	private Random random = new Random();

	public void handleFireworkExplode(FireworkExplodeEvent event) {
		Firework firework = event.getEntity();
		Player shooter = (Player) firework.getShooter();
		Location loc = firework.getLocation();

		BastionBlockManager bastionBlockManager = Bastion.getBastionManager();
		Set<BastionBlock> bastions = bastionBlockManager.getBlockingBastions(loc);
		if (!bastions.isEmpty()) {
			bastionBlockManager.erodeFromPlace(shooter, bastions);
		}

		List<Location> explodeLocs = LocationTools.getCircle(loc, radius, radius, false, true, 0);
		List<Block> explodeBlocks = new ArrayList<>();
		for (Location explodeLoc : explodeLocs) {
			Block explodeBlock = explodeLoc.getBlock();
			if (explodeBlock.getType() != Material.AIR && explodeBlock.getType() != Material.BEDROCK && explodeBlock.getType() != Material.BARRIER) {
				ArtilleryManager artilleryManager = Arsenal.getInstance().getArtilleryManager();
				Artillery artillery = artilleryManager.getArtillery(explodeLoc);
				if (artillery != null) {
					artillery.damage(getArtilleryDamage());
				}
				Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(explodeBlock);
				if (rein != null) {
					for (int i = 0; i < reinforcementDamage; i++) {
						if (rein.isBroken()) {
							break;
						}
						float damage = ReinforcementLogic.getDamageApplied(rein);
						ReinforcementLogic.damageReinforcement(rein, damage, shooter);
					}
				} else {
					Material type = Material.AIR;

					if (explodeBlock.getRelative(BlockFace.DOWN, 1).getType().isSolid() && random.nextInt(3) == 0) {
						type = Material.FIRE;
					}

					explodeBlock.setType(type);
					explodeBlocks.add(explodeBlock);
				}
			}
		}

		MissileExplodeEvent missileExplodeEvent = new MissileExplodeEvent(explodeBlocks);
		Bukkit.getPluginManager().callEvent(missileExplodeEvent);
	}

}
