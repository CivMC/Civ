package com.github.maxopoly.essenceglue;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class RewardManager {
	
	private ItemMap dailyReward;
	private ItemMap voteReward;
	
	public RewardManager(ItemMap dailyReward, ItemMap voteReward) {
		this.dailyReward = dailyReward;
		this.voteReward = voteReward;
	}
	
	public void giveLoginReward(Player p, int streak) {
		p.sendMessage(ChatColor.GREEN + "You've received your daily login reward");
		ItemMap reward = dailyReward.clone();
		reward.multiplyContent(streak);
		if (reward.fitsIn(p.getInventory())) {
			for(ItemStack is : reward.getItemStackRepresentation()) {
				p.getInventory().addItem(is);
			}
		}
		else {
			p.sendMessage(ChatColor.GREEN + "Your inventory was full, so it was dropped on the ground");
			for(ItemStack is : reward.getItemStackRepresentation()) {
				p.getWorld().dropItemNaturally(p.getLocation(), is);
			}
		}
	}
	
	public void giveVoteReward(Player p, String page) {
		p.sendMessage(ChatColor.GREEN + "You received a reward for voting on " + page);
		if (voteReward.fitsIn(p.getInventory())) {
			for(ItemStack is : voteReward.getItemStackRepresentation()) {
				p.getInventory().addItem(is);
			}
		}
		else {
			p.sendMessage(ChatColor.GREEN + "Your inventory was full, so it was dropped on the ground");
			for(ItemStack is : voteReward.getItemStackRepresentation()) {
				p.getWorld().dropItemNaturally(p.getLocation(), is);
			}
		}
	}

}
