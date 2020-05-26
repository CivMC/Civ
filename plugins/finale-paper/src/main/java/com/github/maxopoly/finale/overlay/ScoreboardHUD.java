package com.github.maxopoly.finale.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.FinaleSettingManager;

import vg.civcraft.mc.civmodcore.api.PotionAPI;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.SettingChangeListener;
import vg.civcraft.mc.civmodcore.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardAPI;

public class ScoreboardHUD implements Listener {

	private List<CivScoreBoard> scoreBoards;
	private FinaleSettingManager settingsMan;

	public ScoreboardHUD(FinaleSettingManager settingsMan) {
		scoreBoards = new ArrayList<>();
		this.settingsMan = settingsMan;
		for (int i = 0; i < 10; i++) {
			scoreBoards.add(ScoreBoardAPI.createBoard("finaleArmor" + i));
		}
		Bukkit.getScheduler().runTaskTimer(Finale.getPlugin(), () -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (settingsMan.showArmorDurability(p.getUniqueId())) {
					updateAllArmor(p);
				}
				if (settingsMan.showPotionEffects(p.getUniqueId())) {
					updateAllPotionEffects(p);
				}
				if (settingsMan.getGammaBrightSetting().getValue(p)) {
					updateGammaBright(p);
				}
			}
		}, 5L, 5L);
		settingsMan.getArmorSetting().registerListener(new SettingChangeListener<Boolean>() {

			@Override
			public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
				Player p = Bukkit.getPlayer(player);
				if (p == null) {
					return;
				}
				if (newValue) {
					updateAllArmor(p);
				} else {
					for (int i = 0; i < 4; i++) {
						scoreBoards.get(i).set(p, null);
					}
				}
			}
		});
		settingsMan.getGammaBrightSetting().registerListener(new SettingChangeListener<Boolean>() {

			@Override
			public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
				Player p = Bukkit.getPlayer(player);
				if (p == null) {
					return;
				}
				if (newValue) {
					updateGammaBright(p);
				} else {
					p.removePotionEffect(PotionEffectType.NIGHT_VISION);
				}
			}
		});
		settingsMan.getPotionSetting().registerListener(new SettingChangeListener<Boolean>() {

			@Override
			public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
				Player p = Bukkit.getPlayer(player);
				if (p == null) {
					return;
				}
				if (newValue) {
					updateAllPotionEffects(p);
				} else {
					for (int i = 4; i < 10; i++) {
						scoreBoards.get(i).set(p, null);
					}
				}
			}
		});
	}

	private void updateGammaBright(Player player) {
		player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 1, false, false, false));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent e) {
		if (settingsMan.showArmorDurability(e.getPlayer().getUniqueId())) {
			updateAllArmor(e.getPlayer());
		}
		if (settingsMan.showPotionEffects(e.getPlayer().getUniqueId())) {
			updateAllPotionEffects(e.getPlayer());
		}
		if (settingsMan.getGammaBrightSetting().getValue(e.getPlayer())) {
			updateGammaBright(e.getPlayer());
		}
	}

	private void updateAllPotionEffects(Player p) {
		int boardIndex = 4;
		for (PotionEffect pot : p.getActivePotionEffects()) {
			if (boardIndex >= 10) {
				break;
			}
			if (pot.getType().equals(PotionEffectType.NIGHT_VISION) && settingsMan.getGammaBrightSetting().getValue(p)) {
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
			String name = PotionAPI.getNiceName(pot.getType());
			String formatted = String.format("%s %s%s %d | %d:%s", sortingPrefix, effectColor, name, level, minutes,
					seconds);
			scoreBoards.get(boardIndex).set(p, formatted);
			boardIndex++;
		}
		while (boardIndex < 10) {
			scoreBoards.get(boardIndex).set(p, null);
			boardIndex++;
		}
	}

	private static Map<PotionEffectType, ChatColor> colorMapping;

	private static ChatColor getMatchingColor(PotionEffectType pet) {
		if (colorMapping == null) {
			colorMapping = new HashMap<>();
			colorMapping.put(PotionEffectType.FIRE_RESISTANCE, ChatColor.GOLD);
			colorMapping.put(PotionEffectType.SPEED, ChatColor.AQUA);
			colorMapping.put(PotionEffectType.INCREASE_DAMAGE, ChatColor.RED);
			colorMapping.put(PotionEffectType.REGENERATION, ChatColor.LIGHT_PURPLE);
			colorMapping.put(PotionEffectType.FAST_DIGGING, ChatColor.AQUA);
			colorMapping.put(PotionEffectType.SLOW, ChatColor.GRAY);
		}
		ChatColor color = colorMapping.get(pet);
		if (color != null) {
			return color;
		}
		return ChatColor.YELLOW;
	}

	private void updateAllArmor(Player p) {
		scoreBoards.get(0).set(p, updateArmorPiece(p, "Helmet", 0, 3));
		scoreBoards.get(1).set(p, updateArmorPiece(p, "Chestplate", 1, 2));
		scoreBoards.get(2).set(p, updateArmorPiece(p, "Leggings", 2, 1));
		scoreBoards.get(3).set(p, updateArmorPiece(p, "Boots", 3, 0));
	}

	private String updateArmorPiece(Player p, String prefix, int order, int slot) {
		if (!settingsMan.showArmorDurability(p.getUniqueId())) {
			return null;
		}
		ItemStack is = p.getInventory().getArmorContents()[slot];
		if (is == null) {
			return null;
		}
		ItemMeta im = is.getItemMeta();
		if (!(im instanceof Damageable)) {
			return null;
		}
		int maxDura = is.getType().getMaxDurability();
		int damage = ((Damageable) im).getDamage();
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

}
