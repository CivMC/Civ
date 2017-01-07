/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.types.TimerOperation;

public class Helper {
	public static TimerOperation parseTimerOperation(String name) {
		if(name == null) {
			return null;
		}
		
		switch(name.toLowerCase()) {
		case "draw":
			return TimerOperation.DRAW;
		case "undraw":
			return TimerOperation.UNDRAW;
		case "revert":
			return TimerOperation.REVERT;
		default:
			return null;
		}
	}
	
	public static String getLore(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		if(!meta.hasLore()) return null;
		
		StringBuilder lore = new StringBuilder();
		
		for (String line: meta.getLore()) {
			if(lore.length() > 0) {
				lore.append("\n");
			}
			
			lore.append(line);
		}
		
		return lore.toString();
	}
	
	public static void setLore(ItemStack item, String lore) {
		if (lore == null || lore.length() == 0) return;
		
		ItemMeta meta = item.getItemMeta();
		List<String> array = Arrays.asList(lore.split("\n"));
		meta.setLore(array);
		item.setItemMeta(meta);
	}
	
	public static List<Player> getNearbyPlayers(Location location) {
		double distance = CastleGates.getCitadelManager().getMaxRedstoneDistance();
        double minX = location.getX() - distance;
        double minZ = location.getZ() - distance;
        double maxX = location.getX() + distance;
        double maxZ = location.getZ() + distance;

        List<Player> players = new ArrayList<Player>();
		
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead())  continue;

            Location playerLocation = player.getLocation();
            
            if(!playerLocation.getWorld().equals(location.getWorld())) continue;
            
            double playerX = playerLocation.getX();
            double playerZ = playerLocation.getZ();
            
            if (playerX < minX || playerX > maxX
                    || playerZ < minZ || playerZ > maxZ)
            {
                continue;
            }
            
            double playerDistance = playerLocation.distance(location);
            
            if (playerDistance <= distance) {
                players.add(player);
            }
        }
        
        return players;
	}
	
	public static List<Integer> getConsumeSlots(Player player, ItemStack consumeItem) {
		if(consumeItem == null) return null;
		
		List<Integer> list = new ArrayList<Integer>();
		PlayerInventory inventory = player.getInventory();
		int inventorySize = inventory.getSize();
		int requiredAmount = consumeItem.getAmount();
		
		try {
			for(int i = 0; i < inventorySize && requiredAmount > 0; i++) {
				ItemStack item = inventory.getItem(i);
				
				if(item == null || !consumeItem.isSimilar(item)) {
					continue;
				}
				
				list.add(i);
				
				requiredAmount -= item.getAmount();
			}
        } catch (Exception ex) {
        }
		
		return requiredAmount > 0 ? null :list;
	}
	
	public static void consumeItem(Player player, ItemStack consumeItem, List<Integer> list) {
		if(list == null) return;
		
		PlayerInventory inventory = player.getInventory();
		int requiredAmount = consumeItem.getAmount();
		
		for(int i = 0; i < list.size() && requiredAmount > 0; i++) {
			int slot = list.get(i);
			ItemStack item = inventory.getItem(slot);
			int itemAmount = item.getAmount();
			int amountToRemove = Math.min(itemAmount, requiredAmount);
			
			if(itemAmount > amountToRemove) {
				item.setAmount(itemAmount - amountToRemove);
			} else {
				inventory.clear(slot);
			}
			
			requiredAmount -= amountToRemove;
		}
	}
	
	public static void putItemToInventoryOrDrop(Player player, Location dropLocation, ItemStack dropItem) {
        if (player != null){
            Inventory inv = player.getInventory();

            for(ItemStack leftover : inv.addItem(dropItem).values()) {
            	dropItemAtLocation(dropLocation, leftover);
            }
        }
        else {
        	dropItemAtLocation(dropLocation, dropItem);
        }
	}
	
	private static void dropItemAtLocation(final Location dropLocation, final ItemStack dropItem) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(CastleGates.getInstance(), new Runnable() {
			@Override
			public void run() {
				try {
					Location newDropLocation = dropLocation.add(0.5, 0.5, 0.5);
					dropLocation.getWorld().dropItem(newDropLocation, dropItem).setVelocity(new Vector(0, 0.05, 0));
				} catch (Exception e) {
					CastleGates.getPluginLogger().log(Level.WARNING, "dropItemAtLocation called but errored: ", e);
				}
			}
		}, 1);
	}
}