package com.untamedears.itemexchange.utility;

import co.aikar.commands.InvalidCommandArgument;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import org.bukkit.entity.Player;

/**
 * This is a utility to be used within modifier command handlers.
 */
public class ModifierHandler<T extends ModifierData> extends RuleHandler {

	private final T template;
	private T modifier;

	/**
	 * Creates a new modifier handler.
	 *
	 * @param player The player who's invoked a modifier command handler.
	 * @param template The template modifier to base this handler around.
	 */
	public ModifierHandler(Player player, T template) {
		super(player);
		if (ItemExchangePlugin.modifierRegistrar().getModifier(template.getClass()) != template) {
			throw new InvalidCommandArgument("Could not match that modifier.", false);
		}
		this.template = template;
		this.modifier = getRule().getModifiers().get(template);
	}

	/**
	 * Gets the current instance of this modifier.
	 *
	 * @return Returns the current instance of this modifier.
	 */
	public final T getModifier() {
		return this.modifier;
	}

	/**
	 * Ensures that there's a modifier instance of the template.
	 *
	 * @return Returns the current instance of this modifier, or creates a new one if it didn't already exist.
	 */
	@SuppressWarnings("unchecked")
	public final T ensureModifier() {
		if (this.modifier == null) {
			this.modifier = (T) template.construct();
		}
		return this.modifier;
	}

	/**
	 * Sets the current instance of this modifier.
	 *
	 * @param modifier The modifier to set. If set to null will remove this modifier.
	 */
	public final void setModifier(T modifier) {
		this.modifier = modifier;
	}

	@Override
	public void close() {
		if (this.modifier == null) {
			getRule().getModifiers().remove(this.template);
		}
		else {
			getRule().getModifiers().put(this.modifier);
		}
		super.close();
	}

}
