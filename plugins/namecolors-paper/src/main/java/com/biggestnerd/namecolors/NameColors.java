package com.biggestnerd.namecolors;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

public class NameColors extends ACivMod implements Listener {

	private Map<UUID, String> names;
	private FileConfiguration config;
	
	public void onEnable() {
		super.onEnable();
		names = new HashMap<UUID, String>();
		saveDefaultConfig();
		reloadConfig();
		config = getConfig();
		for(String key : config.getKeys(false)) {
			UUID id = UUID.fromString(key);
			names.put(id, config.getString(key));
		}
		getServer().getPluginManager().registerEvents(this, this);
		CivChatManager.init();
	}
	
	public void onDisable() {
		for(Entry<UUID, String> entry : names.entrySet()) {
			config.set(entry.getKey().toString(), entry.getValue());
		}
		try {
			config.save(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(names.containsKey(event.getPlayer().getUniqueId())) {
			if(!event.getPlayer().hasPermission("namecolor.use")) {
				names.remove(event.getPlayer().getUniqueId());
				return;
			}
			updatePlayerName(event.getPlayer(), names.get(event.getPlayer().getUniqueId()));	
		}
	}
	
	private void updatePlayerName(Player player, String name) {
		if(name == player.getDisplayName()) {
			names.remove(player.getUniqueId());
			player.setPlayerListName(ChatColor.stripColor(player.getDisplayName()));
			player.sendMessage("Your name color has been cleared");
		} else {
			names.put(player.getUniqueId(), name);
			player.setPlayerListName(names.get(player.getUniqueId()));
			player.sendMessage("Your name has been changed to '" + name + "'");
		}
		CivChatManager.updatePlayerName(player.getUniqueId(), name);
		try { player.closeInventory(); } catch (NullPointerException npe) {} //exception only happens on login nbd
	}
	
	private void updatePlayerName(Player player, ChatColor color) {
		updatePlayerName(player, color + player.getDisplayName());
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			ClickableInventory inv = new ClickableInventory(InventoryType.CHEST, "Change Name Color");
			ItemStack redWool = new ItemStack(Material.WOOL, 1, (short)14);
			ISUtils.setName(redWool, ChatColor.DARK_RED + "Red");
			inv.addSlot(new Clickable(redWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.DARK_RED);
				}
			});
			ItemStack greenWool = new ItemStack(Material.WOOL, 1, (short)13);
			ISUtils.setName(greenWool, ChatColor.DARK_GREEN + "Dark Green");
			inv.addSlot(new Clickable(greenWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.DARK_GREEN);
				}
			});
			ItemStack darkblueWool = new ItemStack(Material.WOOL, 1, (short)11);
			ISUtils.setName(darkblueWool, ChatColor.BLUE + "Dark Blue");
			inv.addSlot(new Clickable(darkblueWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.BLUE);
				}
			});
			ItemStack purpleWool = new ItemStack(Material.WOOL, 1, (short)10);
			ISUtils.setName(purpleWool, ChatColor.DARK_PURPLE + "Purple");
			inv.addSlot(new Clickable(purpleWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.DARK_PURPLE);
				}
			});
			ItemStack tealWool = new ItemStack(Material.WOOL, 1, (short)9);
			ISUtils.setName(tealWool, ChatColor.DARK_AQUA + "Teal");
			inv.addSlot(new Clickable(tealWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.DARK_AQUA);
				}
			});
			ItemStack lightGrayWool = new ItemStack(Material.WOOL, 1, (short)8);
			ISUtils.setName(lightGrayWool, ChatColor.GRAY + "Gray");
			inv.addSlot(new Clickable(lightGrayWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.GRAY);
				}
			});
			ItemStack darkGrayWool = new ItemStack(Material.WOOL, 1, (short)7);
			ISUtils.setName(darkGrayWool, ChatColor.DARK_GRAY + "Dark Gray");
			inv.addSlot(new Clickable(darkGrayWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.DARK_GRAY);
				}
			});
			ItemStack limeWool = new ItemStack(Material.WOOL, 1, (short)5);
			ISUtils.setName(limeWool, ChatColor.GREEN + "Light Green");
			inv.addSlot(new Clickable(limeWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.GREEN);
				}
			});
			ItemStack yellowWool = new ItemStack(Material.WOOL, 1, (short)4);
			ISUtils.setName(yellowWool, ChatColor.YELLOW + "Yellow");
			inv.addSlot(new Clickable(yellowWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.YELLOW);
				}
			});
			ItemStack lightBlueWool = new ItemStack(Material.WOOL, 1, (short)3);
			ISUtils.setName(lightBlueWool, ChatColor.AQUA + "Light Blue");
			inv.addSlot(new Clickable(lightBlueWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.AQUA);
				}
			});
			ItemStack GoldWool = new ItemStack(Material.WOOL, 1, (short)1);
			ISUtils.setName(GoldWool, ChatColor.GOLD + "Gold");
			inv.addSlot(new Clickable(GoldWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.GOLD);
				}
			});
			ItemStack pinkWool = new ItemStack(Material.WOOL, 1, (short)2);
			ISUtils.setName(pinkWool, ChatColor.LIGHT_PURPLE + "Pink");
			inv.addSlot(new Clickable(pinkWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.LIGHT_PURPLE);
				}
			});
			if(sender.hasPermission("namecolor.rainbow")) {
				ItemStack brownWool = new ItemStack(Material.WOOL, 1, (short)12);
				ISUtils.setName(brownWool, ChatColor.LIGHT_PURPLE + "Rainbow");
				inv.addSlot(new Clickable(brownWool) {
					public void clicked(Player player) {
						char[] letters = player.getDisplayName().toCharArray();
						StringBuilder nameBuilder = new StringBuilder();
						for(int i = 0; i < letters.length; i++) {
							nameBuilder.append(rainbow[i%rainbow.length]).append(letters[i]);
						}
						updatePlayerName(player, nameBuilder.toString());
					}
				});
			}
			ItemStack whiteWool = new ItemStack(Material.WOOL, 1);
			ISUtils.setName(whiteWool, "Reset");
			inv.addSlot(new Clickable(whiteWool) {
				public void clicked(Player player) {
					updatePlayerName(player, ChatColor.RESET);
				}
			});
			inv.showInventory((Player)sender);
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "Only players can change their name color");
		}
		return true;
	}
	
	static final ChatColor[] rainbow  = {
			ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE
	};
	
	public String getPluginName() {
		return "NameColors";
	}
}
