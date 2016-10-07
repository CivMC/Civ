package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.Lang;

/**
 * Command that lets exiled players kill themselves
 * @author Gordon
 */
public class CmdSuicide extends PearlCommand {
	
	public CmdSuicide(ExilePearlPlugin p) {
		super(p);
		this.aliases.add("suicide");
		
		this.senderMustBePlayer = true;
		this.errorOnToManyArgs = false;
		this.visibility = CommandVisibility.INVISIBLE;
	}

	@Override
	public void perform() {
		if(!plugin.getPearlConfig().canPerform(ExileRule.SUICIDE)) {
			msg(Lang.unknownCommand);
			return;
		}
		
		if (!pearlApi.isPlayerExiled(me().getUniqueId())) {
			msg(Lang.onlyExiledPlayers);
			return;
		}
		
		plugin.getSuicideHandler().addPlayer(me());
	}
}
