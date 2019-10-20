package com.devotedmc.ExilePearl.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminExileAny extends PearlCommand {

	public CmdAdminExileAny(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("exileany");

		this.setHelpShort("Exiles a player.");

		this.commandArgs.add(requiredPlayerOrUUID("player"));
		this.commandArgs.add(optionalPlayerOrUUID("killed by"));
		this.commandArgs.add(optional("world"));
		this.commandArgs.add(optional("x"));
		this.commandArgs.add(optional("y"));
		this.commandArgs.add(optional("z"));

		this.permission = Permission.EXILE_ANY.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		UUID playerId = argAsPlayerOrUUID(0);
		UUID killerId = argAsPlayerOrUUID(1);
		Inventory inv = null;

		if (playerId == null) {
			msg(Lang.unknownPlayer);
			return;
		}
		String playerName = plugin.getRealPlayerName(playerId);

		// Use the command sender as killer if none is specified
		if(args.size() < 2) {
			if(senderIsConsole) {
				msg("<i>You must specify a killing player when performing this command from console.");
				return;
			} else {
				killerId = player().getUniqueId();
			}
		}

		ExilePearl pearl = plugin.getPearl(playerId);
		if (pearl != null) {
			if(pearl.getFreedOffline()) {
				// The player has been freed but the pearl still exists because they haven't logged in yet.
				// This will force free the pearl so they can be re-pearled
				plugin.freePearl(pearl, PearlFreeReason.FORCE_FREED_BY_ADMIN);
			} else {
				msg("<i>The player <c>%s <i>is already exiled and is %s.", pearl.getPlayerName(), pearl.getLocationDescription());
				return;
			}
		}

		if (killerId == null) {
			killerId = player().getUniqueId();
		}

		if (killerId == null) {
			msg(Lang.unknownPlayer);
			return;
		}

		if (senderIsConsole || args.size() > 2) {
			if (args.size() < 6) {
				if (senderIsConsole) {
					msg("<i>You must specify a location when performing this command from console.");
				} else {
					msg("<i>Invalid location.");
					return;
				}
			}

			World world = Bukkit.getWorld(argAsString(2));
			Integer x = argAsInt(3);
			Integer y = argAsInt(4);
			Integer z = argAsInt(5);

			if (world == null || x == null || y == null || z == null) {
				msg("<i>Invalid location");
				return;
			}

			Block b = world.getBlockAt(x, y, z);
			BlockState bs = b.getState();
			if (bs == null || (!(bs instanceof InventoryHolder))) {
				msg(Lang.locNotInventory);
				return;
			}

			inv = ((InventoryHolder)bs).getInventory();

			if (inv.firstEmpty() == -1) {
				msg("<i>That inventory is full.");
				return;
			}

			pearl = plugin.exilePlayer(playerId, killerId, b.getLocation());
		} else {
			inv = player().getInventory();

			if (inv.firstEmpty() == -1) {
				msg("<i>You need an open inventory slot to do that.");
				return;
			}

			pearl = plugin.exilePlayer(playerId, killerId, new PlayerHolder(player()));
		}

		if (pearl == null) {
			msg("<b>Tried to exile player <c>%s <b>but the operation failed.", playerName);
			return;
		}

		// Place the pearl in the inventory
		inv.setItem(inv.firstEmpty(), pearl.createItemStack());
		msg("<g>You exiled the player <c>%s", pearl.getPlayerName());
	}
}
