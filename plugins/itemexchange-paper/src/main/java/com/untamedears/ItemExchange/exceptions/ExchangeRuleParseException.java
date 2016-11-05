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
	private static final long serialVersionUID = -8077197735510230988L;

	public ExchangeRuleParseException(String message) {
		super(message);
	}

	public ExchangeRuleParseException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
