package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.rules.ExchangeRule.Type;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.utilities.Validation;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * This class represents a specific trade within a shop, an input and output pair, or a donation.
 */
public final class TradeRule implements Validation {

	private ExchangeRule input;

	private ExchangeRule output;

	private Inventory inventory;

	public TradeRule(Inventory inventory) {
		setInventory(inventory);
	}

	@Override
	public boolean isValid() {
		if (this.input == null) {
			return false;
		}
		if (this.input.isBroken()) {
			return false;
		}
		if (this.input.getType() != Type.INPUT) {
			return false;
		}
		if (this.output != null) {
			if (this.output.isBroken()) {
				return false;
			}
			if (this.output.getType() != Type.OUTPUT) {
				return false;
			}
		}
		if (!InventoryUtils.isValidInventory(this.inventory)) {
			return false;
		}
		if (!WorldUtils.isValidLocation(this.inventory.getLocation())) {
			return false;
		}
		return true;
	}

	/**
	 * Determines how many times this rule matches with the given inventory.
	 *
	 * @return The number of trades that can be performed.
	 */
	public int calculateStock() {
		return output.calculateStock(this.inventory);
	}

	/**
	 * Gets the trade's input rule.
	 *
	 * @return The trade's input rule.
	 */
	public ExchangeRule getInput() {
		return this.input;
	}

	/**
	 * Sets the trade's input rule.
	 *
	 * @param input The input rule to set.
	 */
	public void setInput(ExchangeRule input) {
		this.input = input;
	}

	/**
	 * Checks whether the trade has an output.
	 *
	 * @return Returns true if the trade has an output rule.
	 */
	public boolean hasOutput() {
		return this.output != null;
	}

	/**
	 * Gets the trade's output rule.
	 *
	 * @return The trade's output rule.
	 */
	public ExchangeRule getOutput() {
		return this.output;
	}

	/**
	 * Sets the trade's output rule.
	 *
	 * @param output The output rule to set.
	 */
	public void setOutput(ExchangeRule output) {
		this.output = output;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public Block getBlock() {
		return Objects.requireNonNull(this.inventory.getLocation()).getBlock();
	}

}
