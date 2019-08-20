package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvControlConfig;

import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagList;
import net.minecraft.server.v1_14_R1.WorldNBTStorage;
import vg.civcraft.mc.namelayer.NameAPI;

public class InvControl extends SimpleHack<InvControlConfig> implements CommandExecutor, Listener {

	public static final String NAME = "InvControl";

	private boolean hasNameAPI;

	private Set<UUID> adminsWithInv;

	public InvControl(SimpleAdminHacks plugin, InvControlConfig config) {
		super(plugin, config);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		String playername = args[0];
		UUID playerUID = null;
		Player player = null;
		if (this.hasNameAPI) {
			plugin().debug("Using NameAPI to look up {0}", playername);
			playerUID = NameAPI.getUUID(playername);
			plugin().debug("Found UUID match: {0}", playerUID);
			player = Bukkit.getPlayer(playerUID);
		}
		if (player == null) { // no NameAPI or failure.
			plugin().debug("Using Bukkit byname to look up {0}", playername);
			player = Bukkit.getPlayer(playername);
		}
		if (player == null) { // By name failed... is it a UUID?
			try {
				plugin().debug("Using Bukkit by UUID to look up {0}", playername);
				player = Bukkit.getPlayer(UUID.fromString(playername));
				playerUID = UUID.fromString(playername);
			} catch (IllegalArgumentException iae) {
				player = null;
			}
		}
		if (player == null && playerUID != null) { // Go deep into NBT.
			WorldNBTStorage storage = ((CraftServer) plugin().getServer()).getServer().getWorlds().
					iterator().next().getDataManager();
			NBTTagCompound rawPlayer = storage.getPlayerData(playerUID.toString());

			if (rawPlayer != null) {
				plugin().debug("Player {0} found in NBT data, read-only access enabled.", playername);
				sender.sendMessage("Player found via alternate lookup, read-only access enabled.");
			} else {
				sender.sendMessage("Player " + playername + " does not exist or cannot be opened.");
				return false;
			}

			float health = rawPlayer.getFloat("Health");
			int food = rawPlayer.getInt("foodLevel");

			// Fun NMS inventory reconstruction from file data.
			net.minecraft.server.v1_14_R1.PlayerInventory nms_pl_inv = new net.minecraft.server.v1_14_R1.PlayerInventory(null);
			NBTTagList inv = rawPlayer.getList("Inventory", rawPlayer.getTypeId());
			nms_pl_inv.b(inv); // We use this to bypass the Craft code which requires a player object, unlike NMS.
			PlayerInventory pl_inv = new CraftInventoryPlayer(nms_pl_inv);

			invSee(sender, pl_inv, health, food, playername);
			return true;
		}
		if (player == null) {
			sender.sendMessage("Player " + playername + " does not exist or cannot be opened.");
			return false;
		}
		if (command.getName().equalsIgnoreCase("invsee")) { // see
			if (player.equals(sender)) {
				sender.sendMessage(ChatColor.RED + " Just open your inventory, silly.");
				return true;
			}
			final PlayerInventory pl_inv = player.getInventory();
			invSee(sender, pl_inv, player.getHealth(), player.getFoodLevel(), playername);
		} else if (command.getName().equalsIgnoreCase("invmod")) { // mod
			if (!(sender instanceof Player)) { // send text only.
				sender.sendMessage(ChatColor.RED + "Apologies, this is only for in-game operators");
			} else {
				Player admin = (Player) sender;

				if (admin.equals(player)) {
					sender.sendMessage(ChatColor.RED + "You cannot modify your own inventory in this manner.");
				} else {
					sender.sendMessage(ChatColor.RED + "Feature not yet implemented");
					//this.adminsWithInv.add(admin.getUniqueId());
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public void adminCloseInventory(InventoryCloseEvent event) {
		if (this.adminsWithInv.contains(event.getPlayer().getUniqueId())) {
			this.adminsWithInv.remove(event.getPlayer().getUniqueId());
		}
	}

	private void invSee(CommandSender sender, PlayerInventory pl_inv, double health, int food, String playername) {
		if (!(sender instanceof Player)) { // send text only.
			StringBuilder sb = new StringBuilder();
			sb.append(playername).append("'s\n   Health: ").append((int)health*2);
			sb.append("\n   Food: ").append(food);
			sb.append("\n   Inventory: ");
			sb.append("\n      Offhand: ").append(pl_inv.getItemInOffHand());
			sb.append("\n      Helmet: ").append(pl_inv.getHelmet());
			sb.append("\n      Chest: ").append(pl_inv.getChestplate());
			sb.append("\n      Legs: ").append(pl_inv.getLeggings());
			sb.append("\n      Feet: ").append(pl_inv.getBoots());
			for (int slot = 0; slot < 36; slot++) {
				final ItemStack it = pl_inv.getItem(slot);
				sb.append("\n      ").append(slot).append(":").append(it).append(ChatColor.RESET);
			}
			sender.sendMessage(sb.toString());
		} else {
			Player admin = (Player) sender;
		    Inventory inv = Bukkit.createInventory(
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
		    ItemStack ihealth = new ItemStack(Material.APPLE, (int)health*2);
		    ItemMeta hdata = ihealth.getItemMeta();
		    hdata.setDisplayName("Player Health");
		    ihealth.setItemMeta(hdata);
		    inv.setItem(43, ihealth);
		    ItemStack hunger = new ItemStack(Material.COOKED_BEEF, food);
		    hdata = hunger.getItemMeta();
		    hdata.setDisplayName("Player Hunger");
		    hunger.setItemMeta(hdata);
		    inv.setItem(44, hunger);
		    admin.openInventory(inv);
		    admin.updateInventory();
		}
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
		if (config.isEnabled()) {
			if (plugin().serverHasPlugin("NameLayer")) {
				this.hasNameAPI = true;
			} else {
				this.hasNameAPI = false;
			}
			this.adminsWithInv = new HashSet<UUID>();
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

	public static InvControlConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new InvControlConfig(plugin, config);
	}
}
