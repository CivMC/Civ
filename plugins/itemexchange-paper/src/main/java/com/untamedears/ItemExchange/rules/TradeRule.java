package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.rules.ExchangeRule.Type;
import com.untamedears.itemexchange.rules.interfaces.BaseRule;

/**
 * This class represents a specific trade within a shop, an input and output pair, or a donation.
 */
public final class TradeRule extends BaseRule {

    private ExchangeRule input;
    private ExchangeRule output;

    @Override
    protected void onLock() {
        if (this.input != null) {
            this.input.lock();
        }
        if (this.output != null) {
            this.output.lock();
        }
    }

    /**
     * Determines whether the trade is valid.
     *
     * @return Returns true if the trade is valid.
     */
    public boolean isValid() {
        if (this.input == null) {
            return false;
        }
        if (!this.input.isValid()) {
            return false;
        }
        if (this.input.getType() != Type.INPUT) {
            return false;
        }
        if (this.output != null) {
            if (!this.output.isValid()) {
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
        checkLocked();
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
        checkLocked();
        this.output = output;
    }

}
