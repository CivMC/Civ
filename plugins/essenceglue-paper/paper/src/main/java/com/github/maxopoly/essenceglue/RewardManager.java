package com.github.maxopoly.essenceglue;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class RewardManager {

	private final ItemMap dailyReward;
	private final ItemMap voteReward;

	public RewardManager(ItemMap dailyReward, ItemMap voteReward) {
		this.dailyReward = dailyReward;
		this.voteReward = voteReward;
	}

	public void giveLoginReward(Player p, int streak) {
		p.sendMessage(ChatColor.GREEN + "You've received your daily login reward");
		ItemMap reward = dailyReward.clone();
		reward.multiplyContent(streak);
		giveItems(p, reward);
	}

	public void giveVoteReward(Player p, String page) {
		p.sendMessage(ChatColor.GREEN + "You received a reward for voting on " + page);
		giveItems(p, voteReward);
	}

	private static void giveItems(Player p, ItemMap items) {
		if (items.fitsIn(p.getInventory())) {
			for (ItemStack is : items.getItemStackRepresentation()) {
				HashMap<Integer, ItemStack> notAdded = p.getInventory().addItem(is);
				for (ItemStack toDrop : notAdded.values()) {
					p.sendMessage(ChatColor.GREEN + "Your inventory was full, so it was dropped on the ground");
					p.getWorld().dropItemNaturally(p.getLocation(), toDrop);
				}
			}
		} else {
			p.sendMessage(ChatColor.GREEN + "Your inventory was full, so it was dropped on the ground");
			for (ItemStack is : items.getItemStackRepresentation()) {
				p.getWorld().dropItemNaturally(p.getLocation(), is);
			}
		}
	}

}
