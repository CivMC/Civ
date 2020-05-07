package com.github.maxopoly.essenceglue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VotifierListener implements Listener {

	private RewardManager rewardMan;

	public VotifierListener(RewardManager rewardMan) {
		this.rewardMan = rewardMan;
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        Player player = Bukkit.getPlayer(vote.getUsername());
        if (player != null) {
        	 rewardMan.giveVoteReward(player, vote.getServiceName());
        }
    }

}
