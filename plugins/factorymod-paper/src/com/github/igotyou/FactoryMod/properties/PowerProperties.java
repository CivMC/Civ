package com.github.igotyou.FactoryMod.properties;


import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;



public class PowerProperties implements Properties
{
	private ItemList<NamedItemStack> inputs;
	
	public PowerProperties(ItemList<NamedItemStack> inputs)
	{
		this.inputs = inputs;
	}
	
	public ItemList<NamedItemStack> getInputs() 
	{
		return inputs;
	}

}
