package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.rules.ExchangeRule.Type;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * This class represents a specific trade within a shop, an input and output pair, or a donation.
 */
public final class TradeRule implements Validation {

	private ExchangeRule input;

	private ExchangeRule output;

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
		return true;
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

}
