package com.github.maxopoly.external;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.TagManager;

public class CombatTagPlusManager {
	
	private TagManager tagManager;
	
	public CombatTagPlusManager() {
		CombatTagPlus ctp = (CombatTagPlus) Bukkit.getPluginManager().getPlugin("CombatTagPlus");
		this.tagManager = ctp.getTagManager();
	}
	
	public void tag(Player victim, Player attacker) {
		tagManager.tag(victim, attacker);
	}
	
	public boolean isTagged(Player p) {
		return tagManager.isTagged(p.getUniqueId());
	}
	
	public long getExpireTime(Player p) {
		return tagManager.getTag(p.getUniqueId()).getExpireTime();
	}


}
