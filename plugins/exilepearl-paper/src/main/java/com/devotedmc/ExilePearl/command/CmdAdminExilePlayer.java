package com.devotedmc.ExilePearl.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;

public class CmdAdminExilePlayer extends PearlCommand {

	public CmdAdminExilePlayer(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("exile");

		this.setHelpShort("Exiles a player.");
		
		this.requiredArgs.add("player");
		this.requiredArgs.add("killed by");
		this.optionalArgs.put("world", "?");
		this.optionalArgs.put("x", "?");
		this.optionalArgs.put("y", "?");
		this.optionalArgs.put("z", "?");
	}

	@Override
	public void perform() {		
		String name = argAsString(0);
		String killedByName = argAsString(1);
		Inventory inv = null;
		
		ExilePearl pearl = pearlApi.getPearl(name);
		if (pearl != null) {
			msg("<i>The player <c>%s <i>is already exiled and is %s.", name, pearl.getLocationDescription());
			return;
		}
		
		PearlPlayer player = pearlApi.getPearlPlayer(name);
		if (player == null) {
			msg(Lang.unknownPlayer);
			return;
		}
		
		PearlPlayer killedBy = pearlApi.getPearlPlayer(killedByName);
		if (killedBy == null) {
			msg(Lang.unknownPlayer);
			return;
		}
		
		if (senderIsConsole) {
			if (args.size() < 6) {
				msg("<i>You must specify a location when performing this command from console.");
				return;
			}
			
			World world = Bukkit.getWorld(argAsString(2));
			Integer x = argAsInt(3);
			Integer y = argAsInt(4);
			Integer z = argAsInt(5);
			
			if (world == null || x == null || y == null || z == null) {
				msg("<i>Invalid location");
				return;
			}
			
			Block b = new Location(world, x, y, z).getBlock();			
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
		}
		
		pearl = pearlApi.exilePlayer(player, killedBy);
		if (pearl == null) {
			msg("<b>Tried to exile player <c>%s but the operation failed.", name);
			return;
		}
		
		if (!senderIsConsole) {
			inv = me().getInventory();
			
			if (inv.firstEmpty() == -1) {
				msg("<i>You need an open inventory slot to do that.");
				return;
			}
		}
		
		// Place the pearl in the inventory
		inv.setItem(inv.firstEmpty(), pearl.createItemStack());
		msg("<g>You exiled the player <c>%s", pearl.getPlayerName());
	}
}
