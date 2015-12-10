package com.github.igotyou.FactoryMod.eggs;

import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;

public interface IFactoryEgg {
	public Factory hatch(MultiBlockStructure mbs,Player p);
	
	public String getName();
}
