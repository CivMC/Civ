package vg.civcraft.mc.namelayer.misc;

import java.lang.reflect.Field;

import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.entity.Player;

public interface ProfileInterface {

	public void setPlayerProfle(Player player);
	public void setFinalStatic(Field field, Object newValue, GameProfile prof);
}
