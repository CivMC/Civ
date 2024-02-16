package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.Lang;

/**
 * Command that lets exiled players kill themselves
 * @author Gordon
 */
public class CmdSuicide extends PearlCommand {

	public CmdSuicide(ExilePearlApi pearlApi) {
		super(pearlApi);
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

		if (!plugin.isPlayerExiled(player().getUniqueId())) {
			msg(Lang.onlyExiledPlayers);
			return;
		}

		plugin.getSuicideHandler().addPlayer(player());
	}
}
