/*
 * A simple Object to connect a name to display to the player with an ItemStack.
 * This is required since the actual name displayed is taken care of client side
 * so different languages can be used and therefor it is unavailable to the
 * plugin. Bukkit does not provide a reliable naming
 * structure and does not take into account different names for different damage
 * values of items. This could instead be stored in the display name field, but
 * then the display name field would have to be ignored on item comparisons and
 * couldn't be used to designate unique items. So this is needed :(
 */
package com.github.igotyou.FactoryMod.utility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Brian Landry
 */
public class NamedItemStack extends ItemStack{
	//Name to be printed to the user, not necessarily the DisplayName
	private final String commonName;
	public NamedItemStack (final Material type, final int amount, final short damage,final String commonName)
	{
		super(type,amount,damage);
		this.commonName=commonName;
	}
	public NamedItemStack clone()
	{
		try{
			NamedItemStack namedItemStack=(NamedItemStack) super.clone();
			return namedItemStack;
		}
		catch (Error e) {
		throw e;
		}
	}
	public String toString()
	{
		return String.valueOf(getAmount())+" "+commonName;
	}
	
	public String getCommonName()
	{
		return commonName;
	}
}
