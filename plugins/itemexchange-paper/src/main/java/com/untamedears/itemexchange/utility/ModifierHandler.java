package com.untamedears.itemexchange.utility;

import co.aikar.commands.InvalidCommandArgument;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModifierHandler<T extends ModifierData<T>> extends RuleHandler {

	private final T template;
	private T modifier;

	@SuppressWarnings("unchecked")
	public ModifierHandler(Player player, T template) {
		super(player);
		if (ItemExchangePlugin.getModifierRegistrar().getModifier(template.getClass()) != template) {
			throw new InvalidCommandArgument("Could not match that modifier.", false);
		}
		this.template = template;
		this.modifier = (T) getRule().getModifiers().get(template);
	}

	public final T getModifier() {
		return this.modifier;
	}

	public final T ensureModifier() {
		if (this.modifier == null) {
			this.modifier = template.construct();
		}
		return this.modifier;
	}

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
