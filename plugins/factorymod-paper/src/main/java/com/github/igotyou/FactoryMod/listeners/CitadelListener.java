package com.github.igotyou.FactoryMod.listeners;

import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;

public class CitadelListener implements Listener {
	
	private FactoryModManager manager;
	private Random rng;
	
	public CitadelListener() {
		this.manager = FactoryMod.getManager();
		this.rng = new Random();
	}
	
	@EventHandler
	public void reinDamage(ReinforcementDamageEvent e) {
		Factory f = manager.getFactoryAt(e.getBlock());
		if (!(f instanceof FurnCraftChestFactory)) {
			return;
		}
		FurnCraftChestFactory fccf = (FurnCraftChestFactory) f;
		if (fccf.getMultiBlockStructure().getCenter().equals(e.getBlock().getLocation())) {
			if (rng.nextDouble() > fccf.getCitadelBreakReduction()) {
				e.setCancelled(true);
			}
		}
	}
}
