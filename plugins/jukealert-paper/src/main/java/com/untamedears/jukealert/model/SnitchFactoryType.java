package com.untamedears.jukealert.model;

import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;
import com.untamedears.jukealert.model.field.SingleCuboidRangeManager;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class SnitchFactoryType {

	private final int id;
	private final ItemStack item;
	private final String name;
	private final int range;

	private final List<Function<Snitch, AbstractSnitchAppender>> appenders;

	public SnitchFactoryType(ItemStack item, int range, String name, int id,
			List<Function<Snitch, AbstractSnitchAppender>> appenders) {
		this.item = item;
		this.name = name;
		this.id = id;
		this.range = range;
		this.appenders = appenders;
	}

	public Snitch create(int snitchID, Location location, String name, int groupID, boolean isNew) {
		Snitch snitch = new Snitch(snitchID, location, isNew, groupID, s -> new SingleCuboidRangeManager(range, s),
				this, name);
		for(Function<Snitch, AbstractSnitchAppender> appenderFunc : appenders) {
			AbstractSnitchAppender appender = appenderFunc.apply(snitch);
			if (appender != null) {
				snitch.addAppender(appender);
			}
		}
		return snitch;
	}

	/**
	 * @return Identifying id of this config which will identify its instances even
	 *         across config changes
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return Human readable name of this config
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Item used to create instances of this snitch
	 */
	public ItemStack getItem() {
		return item.clone();
	}
}
