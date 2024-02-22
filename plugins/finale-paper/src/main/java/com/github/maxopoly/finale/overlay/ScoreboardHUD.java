package com.github.maxopoly.finale.overlay;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.FinaleSettingManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.PotionUtils;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;

public class ScoreboardHUD implements Listener {

	//TODO Make this a configuration item so it can be changed at runtime.
	private final static long UPDATE_PERIOD_IN_TICKS = 20L * 5L; // Every 5 seconds.

	private final static Map<PotionEffectType, ChatColor> colorMapping = new HashMap<>();

	static {
		colorMapping.put(PotionEffectType.FIRE_RESISTANCE, ChatColor.GOLD);
		colorMapping.put(PotionEffectType.SPEED, ChatColor.AQUA);
		colorMapping.put(PotionEffectType.INCREASE_DAMAGE, ChatColor.RED);
		colorMapping.put(PotionEffectType.REGENERATION, ChatColor.LIGHT_PURPLE);
		colorMapping.put(PotionEffectType.FAST_DIGGING, ChatColor.AQUA);
		colorMapping.put(PotionEffectType.SLOW, ChatColor.GRAY);
	}

	private List<CivScoreBoard> scoreBoards;
	private FinaleSettingManager settingsMan;
	private BottomLine coordsBottomLine;

	public ScoreboardHUD(FinaleSettingManager settingsMan) {
		scoreBoards = new ArrayList<>();
		this.coordsBottomLine = BottomLineAPI.createBottomLine("Location", 4);
		this.settingsMan = settingsMan;
		for (int i = 0; i < 12; i++) {
			scoreBoards.add(ScoreBoardAPI.createBoard("finaleArmor" + i));
		}
		Bukkit.getScheduler().runTaskTimer(Finale.getPlugin(), () -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				updateDurabilities(p);
				updateCoordinates(p, settingsMan.getCoordsLocation());
				if (settingsMan.showPotionEffects(p.getUniqueId())) {
					updateAllPotionEffects(p);
				}
			}
		}, UPDATE_PERIOD_IN_TICKS, UPDATE_PERIOD_IN_TICKS);
		settingsMan.getArmorSetting().registerListener((player, setting, oldValue, newValue) -> {
			Player p = Bukkit.getPlayer(player);
			if (p == null) {
				return;
			}
			if (newValue) {
				updateDurabilities(p);
			} else {
				for (int i = 1; i < 5; i++) {
					scoreBoards.get(i).set(p, null);
				}
			}
		});
		settingsMan.getToolSetting().registerListener((player, setting, oldValue, newValue) -> {
			Player p = Bukkit.getPlayer(player);
			if (p == null) {
				return;
			}
			if (newValue) {
				updateDurabilities(p);
			} else {
				scoreBoards.get(0).set(p, null);
			}
		});
		settingsMan.getGammaBrightSetting().registerListener((player, setting, oldValue, newValue) -> {
			Player p = Bukkit.getPlayer(player);
			if (p == null) {
				return;
			}
			if (newValue) {
				updateGammaBright(p);
			} else {
				p.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
		});
		settingsMan.getPotionSetting().registerListener((player, setting, oldValue, newValue) -> {
			Player p = Bukkit.getPlayer(player);
			if (p == null) {
				return;
			}
			if (newValue) {
				updateAllPotionEffects(p);
			} else {
				for (int i = 5; i < 11; i++) {
					scoreBoards.get(i).set(p, null);
				}
			}
		});

		settingsMan.getCoordsLocation().registerListener((player, playerSetting, s, t1) -> {
			Player p = Bukkit.getPlayer(player);
			if (p == null) {
				return;
			}
			updateCoordinates(Bukkit.getPlayer(player), settingsMan.getCoordsLocation());
		});
	}

	private void updateGammaBright(Player player) {
		player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		//after being logged in for 14 days straight, players may have to relog to refresh the effect
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 60 * 24 * 14, 1, false, false, false));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent e) {
		updateDurabilities(e.getPlayer());
		if (settingsMan.showPotionEffects(e.getPlayer().getUniqueId())) {
			updateAllPotionEffects(e.getPlayer());
		}
		if (settingsMan.getGammaBrightSetting().getValue(e.getPlayer())) {
			updateGammaBright(e.getPlayer());
		}
		updateCoordinates(e.getPlayer(), settingsMan.getCoordsLocation());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void playerRespawn(PlayerRespawnEvent e) {
		Bukkit.getScheduler().runTask(Finale.getPlugin(), () -> {
			if (settingsMan.getGammaBrightSetting().getValue(e.getPlayer())) {
				updateGammaBright(e.getPlayer());
			}
		});
	}

	private void updateAllPotionEffects(Player p) {
		int boardIndex = 5;
		for (PotionEffect pot : p.getActivePotionEffects()) {
			if (boardIndex >= 11) {
				break;
			}
			if (pot.getType().equals(PotionEffectType.NIGHT_VISION)
				&& settingsMan.getGammaBrightSetting().getValue(p)) {
				continue;
			}
			String sortingPrefix = ChatColor.BLACK + "|" + (char) ('a' + boardIndex);
			String effectColor = getMatchingColor(pot.getType()).toString();
			int level = pot.getAmplifier() + 1;
			int durationInSeconds = pot.getDuration() / 20;
			int minutes = durationInSeconds / 60;
			String seconds = String.valueOf(durationInSeconds % 60);
			if (seconds.length() == 1) {
				seconds = "0" + seconds;
			}

			//TODO check deprecated methods
			String name = PotionUtils.getEffectNiceName(pot.getType());
			String formatted = String.format("%s %s%s %d | %d:%s", sortingPrefix, effectColor, name, level, minutes,
				seconds);
			scoreBoards.get(boardIndex).set(p, formatted);
			boardIndex++;
		}
		while (boardIndex < 11) {
			scoreBoards.get(boardIndex).set(p, null);
			boardIndex++;
		}
	}

	private static ChatColor getMatchingColor(PotionEffectType pet) {
		return colorMapping.getOrDefault(pet, ChatColor.YELLOW);
	}

	private void updateDurabilities(Player p) {
		if (settingsMan.showToolDurability(p.getUniqueId())) {
			scoreBoards.get(0).set(p, updateArmorPiece(p, "Tool", 0, -1));
		}
		if (settingsMan.showArmorDurability(p.getUniqueId())) {
			scoreBoards.get(1).set(p, updateArmorPiece(p, "Helmet", 1, 3));
			scoreBoards.get(2).set(p, updateArmorPiece(p, "Chestplate", 2, 2));
			scoreBoards.get(3).set(p, updateArmorPiece(p, "Leggings", 3, 1));
			scoreBoards.get(4).set(p, updateArmorPiece(p, "Boots", 4, 0));
		}
	}

	private String updateArmorPiece(Player p, String prefix, int order, int slot) {
		if (slot >= 0) {
			if (!settingsMan.showArmorDurability(p.getUniqueId())) {
				return null;
			}
		} else {
			if (!settingsMan.showToolDurability(p.getUniqueId())) {
				return null;
			}
		}
		ItemStack is;
		if (slot < 0) {
			is = p.getInventory().getItemInMainHand();
		} else {
			is = p.getInventory().getArmorContents()[slot];
		}
		if (is == null) {
			return null;
		}
		Damageable damageable = ItemUtils.getDamageable(is);
		if (damageable == null) {
			return null;
		}
		int maxDura = is.getType().getMaxDurability();
		int damage = damageable.getDamage();
		int remainingHealth = maxDura - damage;
		float damagedRatio = ((float) remainingHealth) / maxDura;
		String sortingPrefix = ChatColor.BLACK + "|" + order;
		String colorPrefix;
		if (damagedRatio >= 1.0) {
			colorPrefix = ChatColor.AQUA.toString();
		} else if (damagedRatio > 0.75) {
			colorPrefix = ChatColor.GREEN.toString();
		} else if (damagedRatio > 0.5) {
			colorPrefix = ChatColor.YELLOW.toString();
		} else if (damagedRatio > 0.25) {
			colorPrefix = ChatColor.RED.toString();
		} else {
			colorPrefix = ChatColor.RED.toString() + ChatColor.BOLD.toString();
		}
		return String.format("%s %s%s: %s%d%s/%d", sortingPrefix, ChatColor.AQUA, prefix, colorPrefix, remainingHealth,
			ChatColor.AQUA, maxDura);
	}

	private void updateCoordinates(Player p, DisplayLocationSetting setting) {

	}
}
