/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.utility;

import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Brian Landry
 */
public class ItemExchange {
	private List<ExchangeRule> inputs;
	private List<ExchangeRule> outputs;
	
	public ItemExchange(List<ExchangeRule> inputs, List<ExchangeRule> ouputs){
		this.inputs=inputs;
		this.outputs=outputs;
	}
	public static ItemExchange getItemExchange(Inventory inventory)
	{
		ItemStack[] contents=inventory.getContents();
		List<ExchangeRule> inputs=new ArrayList<ExchangeRule>();
		List<ExchangeRule> outputs=new ArrayList<ExchangeRule>();
		for(ItemStack itemStack:inventory.getContents())
		{
			try{
				ExchangeRule exchangeRule=ExchangeRule.parseRuleBlock(itemStack);
				if(exchangeRule.getType()==ExchangeRule.RuleType.INPUT){
					inputs.add(exchangeRule);
				}
				else if(exchangeRule.getType()==ExchangeRule.RuleType.OUTPUT){
					outputs.add(exchangeRule);
				}
			}
			catch (ExchangeRuleParseException e){
			}
		}
		return new ItemExchange(inputs,outputs);
	}
	public List<ExchangeRule> getInputs(){
		return inputs;
	}
	
	public List<ExchangeRule> getOutputs(){
		return outputs;
	}
	/*
	 * Checks if the exchange has both at least one input and one output
	 */
	public boolean isValid(){
		return inputs.size()>0 && outputs.size()>0;
	}
}
