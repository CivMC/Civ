package com.aleksey.castlegates.utils;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.types.PowerResult;

public class PowerResultHelper {
	public static void showStatus(Location location, List<Player> players, PowerResult result) {
		String message = switch(result.status) {
			case Blocked -> getBlockedMessage(result);
			case Broken -> getBrokenMessage(result);
			case CannotDrawGear -> getCannotDrawGearMessage(result);
			case NotInCitadelGroup -> ChatColor.RED + "Citadel has prevented that operation.";
			case DifferentCitadelGroup -> ChatColor.RED + "All blocks must be reinforced by the same group.";
			case BastionBlocked -> ChatColor.RED + "A bastion has prevented the bridge/gate from undrawing.";
			case Locked -> ChatColor.RED + "The gearblock is locked.";
			default -> null;
		};

		if(message != null) {
			for(Player player : players) {
				player.sendMessage(message);
			}
		}
		else if(result.status == PowerResult.Status.Drawn || result.status == PowerResult.Status.Undrawn){
			playSound(location, result.status);
		}

		if(result.block != null) {
			ParticleHelper.spawn(result.block, ParticleHelper.Type.Warning);
		}
	}

	public static void playSound(Location location, PowerResult.Status status) {
		Sound sound = status == PowerResult.Status.Drawn
				? Sound.BLOCK_PISTON_CONTRACT
				: Sound.BLOCK_PISTON_EXTEND;

		location.getWorld().playSound(location, sound, 0.7f, 1);
	}

	private static String getBlockedMessage(PowerResult result) {
		Location location = result.block.getLocation();

		return ChatColor.RED +
			"The bridge/gate undraw path is blocked at [" + location.getBlockX() +
			" " + location.getBlockY() +
			" " + location.getBlockZ() +
			"]."
			;
	}

	private static String getBrokenMessage(PowerResult result) {
		Location location = result.block.getLocation();

		if(result.block.getType() == Material.AIR) {
			return ChatColor.RED +
					"The bridge/gate is broken at [" + location.getBlockX() +
					" " + location.getBlockY() +
					" " + location.getBlockZ() +
					"]."
					;
		}

		return ChatColor.RED +
			"The " + result.block.getType() + " block at [" + location.getBlockX() +
			" " + location.getBlockY() +
			" " + location.getBlockZ() +
			"] is not allowed to be used as a part of bridges/gates."
			;
	}

	private static String getCannotDrawGearMessage(PowerResult result) {
		Location location = result.block.getLocation();

		return ChatColor.RED +
			"The gearblock at [" + location.getBlockX() +
			" " + location.getBlockY() +
			" " + location.getBlockZ() +
			"] is not allowed to be used as a part of bridges/gates."
			;
	}
}
