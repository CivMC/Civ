package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.Collections;
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
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.misc.NameFetcher;

@CommandAlias("nlun")
public class UpdateName extends BaseCommandMiddle {

	private Map<UUID, String> newNames = Collections.synchronizedSortedMap(new TreeMap<UUID, String>());

	@Syntax("/nlun [CONFIRM]")
	@Description("Updates your name on this server to the one your minecraft account currently has")
	public void execute(CommandSender sender, String newNameOrConfirm) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE
					+ "NO NO NO NO NO NO NO NO");
			return;
		}
		final Player p = (Player) sender;
		final UUID uuid = p.getUniqueId();
		final String oldName = NameAPI.getCurrentName(uuid);

		if (NameLayerPlugin.getGroupManagerDao().hasChangedNameBefore(uuid)) {
			p.sendMessage(ChatColor.RED + "You already changed your name");
			return;
		}

		if (newNameOrConfirm.isEmpty()) {
			Bukkit.getScheduler().runTaskAsynchronously(
					NameLayerPlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							NameFetcher fetcher = new NameFetcher(Collections.singletonList(uuid));
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
							UUID existingNameUUID = NameAPI.getUUID(newName);
							if (existingNameUUID != null) {
								if (!uuid.equals(existingNameUUID)) {
									// different person has the name
									p.sendMessage(ChatColor.RED
											+ "Someone already has the new name of your minecraft account on this server. Because of that you may not update your name");
									return;
								}
								if (oldName.equals(newName)) {
									// name hasnt changed
									p.sendMessage(ChatColor.RED
											+ "The name of your minecraft account is the same one as on this server");
									return;
								}
								// Player wants to change name to one which is
								// the same one as his current name, but with
								// different capitalization. We'll allow it
							}

							p.sendMessage(ChatColor.GREEN
									+ "The current name of your minecraft account is \""
									+ newName
									+ "\". Run \"/nlun CONFIRM\" to update your name on the server to this name. Be careful though as this change can not be reverted!");
							newNames.put(uuid, newName);
						}
					});
			return;
		} else {
			String newName = newNames.get(uuid);
			if (newName == null) {
				sender.sendMessage(ChatColor.RED
						+ "Run \"/nlun\" first to initiate the name changes process");
				return;
			}
			if (!newNameOrConfirm.equals("CONFIRM")) {
				sender.sendMessage(ChatColor.RED
						+ "Run \"/nlun CONFIRM\" to confirm your name change to \""
						+ newName + "\"");
				return;
			}
			NameLayerPlugin.getGroupManagerDao().logNameChange(uuid, oldName,
					newName);
			// uncomment following to directly change name
			// NameAPI.getAssociationList().changePlayer(newName, uuid);
			// NameAPI.resetCache(uuid);
			sender.sendMessage(ChatColor.GREEN
					+ "Your name was changed to \""
					+ newName
					+ "\". This change will be applied together with all other name changes at a previously announced date.");
		}
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		if (newNames.get(((Player) sender).getUniqueId()) != null) {
			return Collections.singletonList("CONFIRM");
		}
		return null;
	}

}
