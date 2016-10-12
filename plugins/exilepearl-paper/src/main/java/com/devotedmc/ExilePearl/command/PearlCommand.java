package com.devotedmc.ExilePearl.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.PearlPlayer;

public abstract class PearlCommand extends BaseCommand<ExilePearlPlugin> {
	
	protected final ExilePearlApi pearlApi;
	
	public PearlCommand(ExilePearlPlugin plugin) {
		super(plugin);
		
		pearlApi = plugin;
	}
	

	
	/**
	 * Gets the sender instance
	 * @return The sender instance
	 */
	protected PearlPlayer me() {
		return plugin.getPearlPlayer(player().getUniqueId());
	}
	
	@Override
	protected List<String> getCustomAutoTab(String tabName, String pattern) {
		List<String> tabList = new ArrayList<String>();
		
		switch(tabName) {
		case "exile_rule":
			for(ExileRule v : new ArrayList<ExileRule>(Arrays.asList(ExileRule.values()))) {
				if (v.toString().toLowerCase().startsWith(pattern.toLowerCase())) {
					tabList.add(v.toString());
				}
			}
			
			break;
		default:
			break;
		
		}
		return tabList;
	}
}
