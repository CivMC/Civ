package com.github.igotyou.FactoryMod.eggs;

import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.Factory;

public interface IFactoryEgg {
	public Factory hatch(Player p);

	public String getName();
}
