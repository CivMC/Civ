package com.github.igotyou.FactoryMod.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import sun.misc.Regexp;

import com.github.igotyou.FactoryMod.FactoryModPlugin;

public class NoteStackListener implements Listener {
	private FactoryModPlugin plugin;
	private static final Pattern stackableRegexp = Pattern.compile("^(§2.*?)( \\(x([1-9][0-9]*)\\))?$");
	private static final Pattern nameRegexp = Pattern.compile("^(.*?)( §2x([1-9][0-9]*))?$");
	private static final int SCALE_FACTOR = 4;
	private static final int MAX_SCALE = 64;

	public NoteStackListener(FactoryModPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void playerInteractionEvent(PlayerInteractEvent e)
	{
		Block clicked = e.getClickedBlock();
		Player player = e.getPlayer();
		
		ItemStack item = player.getItemInHand();
		if (item == null) return;
		if (!item.getType().equals(Material.PAPER)) return; 
		
		//if the player left clicked a block
		if (e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			if (clicked.getType().equals(Material.ENCHANTMENT_TABLE)) {
				// Break notes
				int multiplier = getMultiplier(player.getItemInHand()); 
				if (multiplier >= SCALE_FACTOR) {
					int count = item.getAmount() * SCALE_FACTOR;
					int newMultiplier = multiplier / SCALE_FACTOR;
					player.setItemInHand(new ItemStack(Material.AIR));
					ItemStack newMultipleStack = setMultiplier(item, newMultiplier);
					
					while (count > 0) {
						ItemStack toAdd = new ItemStack(newMultipleStack);
						if (count > 64) {
							toAdd.setAmount(64);
						} else {
							toAdd.setAmount(count);
						}
						// Try to add to hand first when breaking over 64
						if (count > 64 && player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
							player.setItemInHand(toAdd);
						} else {
							Map<Integer, ItemStack> overflow = player.getInventory().addItem(toAdd);
							if (overflow != null && overflow.size() > 0) {
								for (ItemStack spill : overflow.values()) {
									player.getWorld().dropItem(player.getLocation(), spill);
								}
							}
						}
						count = count - 64;
					}
					
					player.updateInventory();
				}
			}
		} else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (clicked.getType().equals(Material.ENCHANTMENT_TABLE)) {
				// Combine notes
				int multiplier = getMultiplier(item); 
				if (multiplier > 0 && item.getAmount() >= SCALE_FACTOR) {
					int count = item.getAmount() / SCALE_FACTOR;
					int residual = item.getAmount() - count * SCALE_FACTOR;
					int newMultiplier = multiplier * SCALE_FACTOR;
					if (newMultiplier > MAX_SCALE) {
						return;
					}
					if (residual > 0) {
						ItemStack singleRemainder = new ItemStack(item);
						singleRemainder.setAmount(residual);
						player.setItemInHand(singleRemainder);
					} else {
						player.setItemInHand(new ItemStack(Material.AIR));
					}
					ItemStack newMultipleStack = setMultiplier(item, newMultiplier);
					
					while (count > 0) {
						ItemStack toAdd = new ItemStack(newMultipleStack);
						if (count > 64) {
							toAdd.setAmount(64);
						} else {
							toAdd.setAmount(count);
						}
						// Try to add to hand first
						Map<Integer, ItemStack> overflow = player.getInventory().addItem(toAdd);
						if (overflow != null && overflow.size() > 0) {
							for (ItemStack spill : overflow.values()) {
								player.getWorld().dropItem(player.getLocation(), spill);
							}
						}
						count = count - 64;
					}
					
					e.setCancelled(true);
					player.updateInventory();
				}
			}
		}
	}
	
	private int getMultiplier(ItemStack item) {
		List<String> lore = item.getItemMeta().getLore();
		if (item.getType().equals(Material.PAPER)) {
			if (lore != null && lore.size() > 0) {
				String lastLore = lore.get(lore.size() - 1);
				Matcher matcher = stackableRegexp.matcher(lastLore);
				if (matcher.find()) {
					String digits = matcher.group(3);
					if (digits != null) {
						return Integer.parseInt(digits);
					} else {
						return 1;
					}
				}
				return 0;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	private ItemStack setMultiplier(ItemStack item, int multiplier) {
		String name = item.getItemMeta().getDisplayName(); 
		List<String> lore = item.getItemMeta().getLore(); 
		if (item.getType().equals(Material.PAPER)) {
			if (name != null) {
				Matcher matcher = nameRegexp.matcher(name);
				if (matcher.find()) {
					String newName;
					if (multiplier == 1) {
						newName = matcher.group(1);
					} else {
						newName = matcher.group(1) + " §2x" + Integer.toString(multiplier);
					}
					
					ItemMeta newMeta = item.getItemMeta().clone();
					newMeta.setDisplayName(newName);
					item = new ItemStack(item);
					item.setItemMeta(newMeta);
				}
			}
			
			if (lore != null && lore.size() > 0) {
				String lastLore = lore.get(lore.size() - 1);
				Matcher matcher = stackableRegexp.matcher(lastLore);
				List<String> newLore = new ArrayList<String>();
				if (matcher.find()) {
					for (int i = 0; i < lore.size() - 1; i++) {
						newLore.add(lore.get(i));
					}
					if (multiplier == 1) {
						newLore.add(matcher.group(1));
					} else {
						newLore.add(matcher.group(1) + " (x" + Integer.toString(multiplier) + ")");
					}
					
					ItemMeta newMeta = item.getItemMeta().clone();
					newMeta.setLore(newLore);
					item = new ItemStack(item);
					item.setItemMeta(newMeta);
				}
			}
		}
		return item;
	}
}
