package vg.civcraft.mc.namelayer.misc;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;

public interface ProfileInterface {

	public void setPlayerProfle(Player player);
	public void setFinalStatic(Field field, Object newValue, Object prof);
}
