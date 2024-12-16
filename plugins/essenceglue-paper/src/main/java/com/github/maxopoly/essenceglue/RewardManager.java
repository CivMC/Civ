package com.github.maxopoly.essenceglue;

import com.github.maxopoly.essenceglue.reward.Rewarder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RewardManager {

    private final Rewarder rewarder;
    private final int dailyReward;
    private final int voteReward;

    public RewardManager(Rewarder rewarder, int dailyReward, int voteReward) {
        this.rewarder = rewarder;
        this.dailyReward = dailyReward;
        this.voteReward = voteReward;
    }

    public void giveLoginReward(Player p, int streak) {
        p.sendMessage(ChatColor.GREEN + "You've received your daily login reward");
        rewarder.reward(p, dailyReward * streak);
    }

    public void giveVoteReward(Player p, String page) {
        p.sendMessage(ChatColor.GREEN + "You received a reward for voting on " + page);
        rewarder.reward(p, voteReward);
    }

}
