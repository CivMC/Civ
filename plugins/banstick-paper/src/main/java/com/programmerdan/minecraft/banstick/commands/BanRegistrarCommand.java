package com.programmerdan.minecraft.banstick.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSRegistrars;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.handler.BanHandler;

import vg.civcraft.mc.namelayer.NameAPI;

public class BanRegistrarCommand implements CommandExecutor {

	public static final String name = "banprovider";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You must specify a player whose last provider will be banned");
			return true;
		}
		UUID uuid = NameAPI.getUUID(args[0]);
		if (uuid == null) {
			sender.sendMessage(ChatColor.RED + "No player " + args[0] + " is known");
			return true;
		}
		BSPlayer player = BSPlayer.byUUID(uuid);
		BSSession lastSession = player.getLatestSession();
		BSIP ip = lastSession.getIP();
		List<BSIPData> proxyChecks = BSIPData.allByIP(ip);
		if (proxyChecks.isEmpty()) {

		}
		BSRegistrars handler = BanStick.getPlugin().getRegistrarHandler();
		for (BSIPData data : proxyChecks) {
			if (data.getRegisteredAs() == null || data.getRegisteredAs().isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Can not ban registrar, because none was known");
				continue;
			}
			handler.banRegistrar(data);
			sender.sendMessage(
					ChatColor.GREEN + "Banning registrar " + data.getRegisteredAs() + " of " + data.toString());
		}
		//also give them an ip ban on the way if they dont have one already
		BanHandler.doIPBan(ip, null, null, true, false);
		return true;
	}

}
