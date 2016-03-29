package vg.civcraft.mc.namelayer.command.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.misc.NameFetcher;

public class UpdateName extends PlayerCommandMiddle {

	private Map<UUID, String> newNames;

	public UpdateName(String name) {
		super(name);
		setIdentifier("nlun");
		setDescription("Updates your name on this server to the one your minecraft account currently has");
		setUsage("/nlun [CONFIRM]");
		setArguments(0, 1);
		newNames = new TreeMap<UUID, String>();
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE
					+ "NO NO NO NO NO NO NO NO");
			return true;
		}
		final Player p = (Player) sender;
		final UUID uuid = p.getUniqueId();
		String oldName = NameAPI.getCurrentName(uuid);
		
		if (NameLayerPlugin.getGroupManagerDao().hasChangedNameBefore(uuid)) {
			p.sendMessage(ChatColor.RED + "You already changed your name");
			return true;
		}

		if (args.length == 0) {
			Bukkit.getScheduler().runTaskAsynchronously(
					NameLayerPlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							List<UUID> uuids = new LinkedList<UUID>();
							uuids.add(uuid);
							NameFetcher fetcher = new NameFetcher(uuids);
							Map<UUID, String> fetchedNames = null;
							try {
								fetchedNames = fetcher.call();
							} catch (Exception e) {
								e.printStackTrace();
							}
							String newName = fetchedNames.get(uuid);
							if (newName == null) {
								p.sendMessage(ChatColor.RED + "An error occured. Try again later");
							}
							p.sendMessage(ChatColor.GREEN
									+ "The current name of your minecraft account is \""
									+ newName
									+ "\". Run \"/nlun CONFIRM\" to update your name on the server to this name. Be careful though as this changes can not be reverted!");
							newNames.put(uuid, newName);
						}
					});
			return true;
		} else {
			if (!args[0].equals("CONFIRM")) {
				return false;
			}
			String newName = newNames.get(uuid);
			if (newName == null) {
				sender.sendMessage(ChatColor.RED
						+ "Run \"/nlun\" first to initiate the name changes process");
				return true;
			}
			NameLayerPlugin.getGroupManagerDao().logNameChance(uuid, oldName,
					newName);
			NameAPI.getAssociationList().changePlayer(newName, uuid);
			NameAPI.resetCache(uuid);
			sender.sendMessage(ChatColor.GREEN + "Your name was changed to \""
					+ newName + "\". Relog to have it apply");
		}
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
