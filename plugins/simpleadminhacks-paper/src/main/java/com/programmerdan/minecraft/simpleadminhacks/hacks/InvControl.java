package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.UUID;

import vg.civcraft.mc.namelayer.NameAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvControlConfig;

public class InvControl extends SimpleHack<InvControlConfig> implements CommandExecutor, Listener {

	public static final String NAME = "InvControl";
	
	private boolean hasNameAPI;
	
	public InvControl(SimpleAdminHacks plugin, InvControlConfig config) {
		super(plugin, config);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		String playername = args[0];
		Player admin = (Player) sender;
		Player player = null;
		if (this.hasNameAPI) {
			UUID playerUID = NameAPI.getUUID(playername);
			player = Bukkit.getPlayer(playerUID);
		}
		if (player == null) { // no NameAPI or failure.
			player = Bukkit.getPlayer(playername);
		}
		if (player == null) { // By name failed... is it a UUID?
			try {
				player = Bukkit.getPlayer(UUID.fromString(playername));
			} catch (IllegalArgumentException iae) {
				player = null;
			}
		}
		if (player == null) { // Nothing left to try...
			admin.sendMessage("Player not found");
			return true;
		}
		if (command.getName().equalsIgnoreCase("invsee")) { // see
			final PlayerInventory pl_inv = player.getInventory();
			if (sender instanceof ConsoleCommandSender) { // send text only.
				
			} else {
			    final Inventory inv = Bukkit.createInventory(
			        admin, 45, playername + "'s Inventory");
			    for (int slot = 0; slot < 36; slot++) {
			      final ItemStack it = pl_inv.getItem(slot);
			      inv.setItem(slot, it);
			    }
			    inv.setItem(36, pl_inv.getItemInOffHand());
			    inv.setItem(38, pl_inv.getHelmet());
			    inv.setItem(39, pl_inv.getChestplate());
			    inv.setItem(40, pl_inv.getLeggings());
			    inv.setItem(41, pl_inv.getBoots());
			    ItemStack health = new ItemStack(Material.APPLE, (int)player.getHealth()*2);
			    ItemMeta hdata = health.getItemMeta();
			    hdata.setDisplayName("Player Health");
			    health.setItemMeta(hdata);
			    inv.setItem(43, health);
			    ItemStack hunger = new ItemStack(Material.COOKED_BEEF, (int)player.getFoodLevel());
			    hdata = hunger.getItemMeta();
			    hdata.setDisplayName("Player Hunger");
			    hunger.setItemMeta(hdata);
			    inv.setItem(44, hunger);
			    admin.openInventory(inv);
			    admin.updateInventory();
			}
		} else if (command.getName().equalsIgnoreCase("invmod")) { // mod
			if (sender instanceof ConsoleCommandSender) { // send text only.
				sender.sendMessage(ChatColor.RED + "Apologies, this is only for in-game operators");
			} else {
				// I do not know what this will do.
				admin.openInventory(player.getOpenInventory());
			}
		}
		return false;
	}

	@Override
	public void registerListeners() {
		
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().log("Registering invsee and invmod commands");
			plugin().registerCommand("invsee", this);
			plugin().registerCommand("invmod", this);
		}
	}

	@Override
	public void dataBootstrap() {
		if (plugin().serverHasPlugin("NameLayer")) {
			this.hasNameAPI = true;
		} else {
			this.hasNameAPI = false;
		}
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
	}

	@Override
	public String status() {
		if (config.isEnabled()) {
			return "Listening for invsee and invmod commands";
		} else {
			return "Inventory Control disabled.";
		}
	}

}
