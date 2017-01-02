package com.programmerdan.minecraft.banstick.handler;

import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.containers.BanResult;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSPlayer;

/**
 * A series of static utility classes to facilitate issuing bans.
 * 
 * @author ProgrammerDan
 *
 */
public class BanHandler {
	private BanHandler() {}

	/**
	 * Issues a ban against a specific UUID.
	 * 
	 * If the player is online, kicks them.
	 * 
	 * This uses the default message. See {@link #doUUIDBan(UUID, String)} for custom message,
	 * or {@link #doUUIDBan(UUID, Date)} for end time, or {@link #doUUIDBan(UUID, String, Date)} for
	 * both.
	 * 
	 * @param playerId
	 *  The UUID of the player to ban.
	 * @return
	 *  A summary of who was banned.
	 */
	public static BanResult doUUIDBan(UUID playerId) {
		return doUUIDBan(playerId, null, null);
	}
	
	/**
	 * Issues a ban against a specific UUID.
	 * 
	 * If the player is online, kicks them.
	 * 
	 * This uses the default message but a custom end time.
	 * 
	 * @param playerId
	 *  The UUID of the player to ban.
	 * @param banEnd
	 *  The time the ban should end.
	 * @return
	 *  A summary of who was banned.
	 */
	public static BanResult doUUIDBan(UUID playerId, Date banEnd) {
		return doUUIDBan(playerId, null, banEnd);
	}
	
	/**
	 * Issues a ban against a specific UUID.
	 * 
	 * If the player is online, kicks them.
	 * 
	 * This uses a custom message.
	 * 
	 * @param playerId
	 *  The UUID of the player to ban.
	 * @param message
	 *  The message to display when a player attempts to rejoin.
	 * @return
	 *  A summary of who was banned.
	 */
	public static BanResult doUUIDBan(UUID playerId, String message) {
		return doUUIDBan(playerId, message, null);
	}
	
	/**
	 * Issues a ban against a specific UUID.
	 * 
	 * If the player is online, kicks them.
	 * 
	 * This uses a custom message and end time.
	 * 
	 * @param playerId
	 *  The UUID of the player to ban.
	 * @param message
	 *  The message to display when a player attempts to rejoin.
	 * @param banEnd
	 *  The time the ban should end.
	 * @return
	 *  A summary of who was banned.
	 */
	public static BanResult doUUIDBan(UUID playerId, String message, Date banEnd) {
		try {
			Player spigotPlayer = Bukkit.getPlayer(playerId);
			BSPlayer player = BSPlayer.byUUID(playerId);
			if (player == null) {
				if (spigotPlayer != null) {
					player = BSPlayer.create(spigotPlayer);
				} else {
					player = BSPlayer.create(playerId);
				}
			}
			BSBan ban = BSBan.create(message, banEnd); // general ban.
			player.setBan(ban);
			
			if (spigotPlayer != null) {
				spigotPlayer.kickPlayer(message);
			}
		} catch (Exception e) {
			BanStick.getPlugin().warning("Failed to issue UUID ban: ", e);
		}
	}
	
	
}
