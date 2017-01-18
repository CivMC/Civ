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
		String message;
		
		switch(result.status) {
		case Blocked:
			message = getBlockedMessage(result);
			break;
		case Broken:
			message = getBrokenMessage(result);
			break;
		case CannotDrawGear:
			message = getCannotDrawGearMessage(result);
			break;
		case NotInCitadelGroup:
			message = ChatColor.RED + "Citadel prevent this operation";
			break;
		case BastionBlocked:
			message = ChatColor.RED + "Bastion prevent undrawing";
			break;
		case Locked:
			message = ChatColor.RED + "Gearblock locked";
			break;
		default:
			message = null;
			break;
		}
		
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
			"Undraw path is blocked at x = " + location.getBlockX() +
			", y = " + location.getBlockY() +
			", z = " + location.getBlockZ()
			;
	}

	private static String getBrokenMessage(PowerResult result) {
		Location location = result.block.getLocation();
		
		if(result.block.getType() == Material.AIR) {
			return ChatColor.RED +
					"Bridge/gates is broken at x = " + location.getBlockX() +
					", y = " + location.getBlockY() +
					", z = " + location.getBlockZ()
					;
		}
		
		return ChatColor.RED +
			"Material " + result.block.getType() + " at x = " + location.getBlockX() +
			", y = " + location.getBlockY() +
			", z = " + location.getBlockZ() +
			" is not allowed to use as part of bridge/gates"
			;
	}

	private static String getCannotDrawGearMessage(PowerResult result) {
		Location location = result.block.getLocation();
		
		return ChatColor.RED +
			"Gearblock is placed as part of bridge/gates at x = " + location.getBlockX() +
			", y = " + location.getBlockY() +
			", z = " + location.getBlockZ() +
			" but this is not allowed"
			;
	}
}