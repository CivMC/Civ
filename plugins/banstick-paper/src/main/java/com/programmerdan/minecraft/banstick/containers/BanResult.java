package com.programmerdan.minecraft.banstick.containers;

import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSShare;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;

/**
 * Used to store bans issued and then transmit the results to various parties.
 * Basically a logic wrapper.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanResult {
	private Set<BSPlayer> playerBans;
	private Set<BSBan> bans;
	
	public BanResult() {
		playerBans = new HashSet<>();
		bans = new HashSet<>();
	}
	
	public static SimpleDateFormat getUsualDateTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Let the command sender know the result of the ban(s) and player(s).
	 * @param sender the sender of commands.
	 */
	public void informCommandSender(CommandSender sender) {
		if (bans.isEmpty() && playerBans.isEmpty()) {
			sender.sendMessage("No bans issued.");
		}
		StringBuilder sb = new StringBuilder();
		if (playerBans.size() > 1) {
			sb.append(playerBans.size()).append(" player bans issued.\n");
		}
		for (BSPlayer banned : playerBans) {
			BSBan ban = banned.getBan();
			sb.append(" Banned ").append(banned.getName()).append(" for ").append(ban.getMessage());
			if (ban.getBanEndTime() != null) {
				sb.append(" until ").append(getUsualDateTime().format(ban.getBanEndTime())).append("\n");
			} else {
				sb.append(" forever\n");
			}	
		}
		if (bans.size() > 1) {
			sb.append(bans.size()).append(" other bans issued.\n");
		}
		for (BSBan banned : bans) {
			sb.append(" Banned ");
			BSIP bip = banned.getIPBan();
			if (bip != null) {
				sb.append(" IP ").append(bip.toFullString(sender.hasPermission("banstick.ips")));
			}
			BSIPData vip = banned.getProxyBan();
			if (vip != null) {
				sb.append(" VPN ").append(vip.toFullString(sender.hasPermission("banstick.ips")));
			}
			BSShare sid = banned.getShareBan();
			if (sid != null) {
				sb.append(" Share ").append(sid.toFullString(sender.hasPermission("banstick.ips")));
			}
			if (banned.getBanEndTime() != null) {
				sb.append(" until ").append(getUsualDateTime().format(banned.getBanEndTime())).append("\n");
			} else {
				sb.append(" forever\n");
			}
		}
		sender.sendMessage(sb.toString());
	}

	public void addPlayer(BSPlayer player) {
		playerBans.add(player);
	}
	
	public void addBan(BSBan ban) {
		bans.add(ban);
	}
}
