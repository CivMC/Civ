package vg.civcraft.mc.namelayer.command.commands;

import java.util.Collections;
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
		newNames = Collections
				.synchronizedSortedMap(new TreeMap<UUID, String>());
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
							if (fetchedNames == null) {
								p.sendMessage(ChatColor.RED
										+ "An error occured. Try again later");
								return;
							}
							String newName = fetchedNames.get(uuid);
							if (newName == null) {
								p.sendMessage(ChatColor.RED
										+ "An error occured. Try again later");
								return;
							}
							if (NameAPI.getUUID(newName) != null) {
								p.sendMessage(ChatColor.RED
										+ "Someone already has the new name of your minecraft account on this server. Because of that you may not update your name");
								return;
							}
							p.sendMessage(ChatColor.GREEN
									+ "The current name of your minecraft account is \""
									+ newName
									+ "\". Run \"/nlun CONFIRM\" to update your name on the server to this name. Be careful though as this change can not be reverted!");
							newNames.put(uuid, newName);
						}
					});
			return true;
		} else {
			String newName = newNames.get(uuid);
			if (newName == null) {
				sender.sendMessage(ChatColor.RED
						+ "Run \"/nlun\" first to initiate the name changes process");
				return true;
			}
			if (!args[0].equals("CONFIRM")) {
				sender.sendMessage(ChatColor.RED + "Run \"/nlun CONFIRM\" to confirm your name change to \"" + newName + "\"");
				return true;
			}
			NameLayerPlugin.getGroupManagerDao().logNameChange(uuid, oldName,
					newName);
			//uncomment following to directly change name
			// NameAPI.getAssociationList().changePlayer(newName, uuid);
			// NameAPI.resetCache(uuid);
			sender.sendMessage(ChatColor.GREEN
					+ "Your name was changed to \""
					+ newName
					+ "\". This change will be applied together with all other name changes at a previously announced date.");
		}
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		if (newNames.get(((Player) sender).getUniqueId()) != null) {
			List<String> conf = new LinkedList<String>();
			conf.add("CONFIRM");
			return conf;
		}
		return null;
	}

}
