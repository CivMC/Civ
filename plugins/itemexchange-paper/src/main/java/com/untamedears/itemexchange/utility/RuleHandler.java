package com.untamedears.itemexchange.utility;

import co.aikar.commands.InvalidCommandArgument;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.io.Closeable;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;

public class RuleHandler implements Closeable {

	private final Player player;
	private final ExchangeRule rule;
	private final List<String> messages;

	public RuleHandler(Player player) {
		if (player == null || !player.isOnline()) {
			throw new InvalidCommandArgument("You must be a player to do that.", false);
		}
		this.player = player;
		this.rule = ExchangeRule.fromItem(player.getInventory().getItemInMainHand());
		if (this.rule == null) {
			throw new InvalidCommandArgument("You must be holding an exchange rule.", false);
		}
		this.messages = Lists.newArrayList();
	}

	public final Player getPlayer() {
		return this.player;
	}

	public final ExchangeRule getRule() {
		return this.rule;
	}

	public final void relay(String message) {
		this.messages.add(message);
	}

	@Override
	public void close() {
		ItemStack item = this.rule.toItem();
		if (!ItemAPI.isValidItem(item)) {
			throw new InvalidCommandArgument("Could not replace that rule.", false);
		}
		this.player.getInventory().setItemInMainHand(item);
		for (String message : messages) {
			if (!Strings.isNullOrEmpty(message)) {
				this.player.sendMessage(message);
			}
		}
	}

}
