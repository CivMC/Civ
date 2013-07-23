/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.exceptions;

/**
 *
 * @author Brian
 */
public class ExchangeRuleParseException extends Exception {
	public ExchangeRuleParseException(String message) {
		super(message);
	}

	public ExchangeRuleParseException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
