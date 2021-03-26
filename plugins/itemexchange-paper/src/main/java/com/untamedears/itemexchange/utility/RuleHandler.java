package com.untamedears.itemexchange.utility;

import co.aikar.commands.InvalidCommandArgument;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.io.Closeable;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * This is a utility to be used within the default commands.
 */
public class RuleHandler implements Closeable {

	private final Player player;
	private final ExchangeRule rule;
	private final List<String> messages;
	private boolean saveChanges;

	/**
	 * Creates a new modifier handler.
	 *
	 * @param player The player who's invoked a default command handler.
	 */
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
		this.saveChanges = true;
	}

	/**
	 * @return Returns the player who invoked the command, which is never null.
	 */
	public final Player getPlayer() {
		return this.player;
	}

	/**
	 * @return Returns the rule the player is holding, which is never null.
	 */
	public final ExchangeRule getRule() {
		return this.rule;
	}

	/**
	 * Use this to send responses to the player. The messages will be stored and sent <i>after</i> the command has been
	 * handled successfully and without error. This allows you to relay messages without worrying about a later thrown
	 * exception, since that exception will prevent the messages from being sent.
	 *
	 * @param message The message to send to the player.
	 */
	public final void relay(String message) {
		this.messages.add(message);
	}

	/**
	 * Use this to enable/disable saving changes to the rule onto the player's held rule.
	 *
	 * @param shouldSave Set to false to disable the saving of changes.
	 */
	public final void saveChanges(boolean shouldSave) {
		this.saveChanges = shouldSave;
	}

	@Override
	public void close() {
		ItemStack item = this.rule.toItem();
		if (!ItemUtils.isValidItem(item)) {
			throw new InvalidCommandArgument("Could not replace that rule.", false);
		}
		if (this.saveChanges) {
			this.player.getInventory().setItemInMainHand(item);
		}
		for (String message : messages) {
			if (!Strings.isNullOrEmpty(message)) {
				this.player.sendMessage(message);
			}
		}
	}

}
