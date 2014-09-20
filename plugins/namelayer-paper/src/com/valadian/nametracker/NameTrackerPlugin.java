package com.valadian.nametracker;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.valadian.nametracker.database.AssociationList;

public class NameTrackerPlugin extends JavaPlugin implements Listener {
	public static AssociationList associations;

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig();
		new ConfigManager().setConfigOptions(config);;
		saveConfig();
		associations = new AssociationList(this, config);
	    getServer().getPluginManager().registerEvents(this, this);
	} 
	
	public void onDisable() {
	}
	
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void OnPlayerLogin(AsyncPlayerPreLoginEvent event)
	{
		
	}
	
	// sets the player name in the gameprofile
	@EventHandler(priority=EventPriority.LOWEST)
	public void loginEvent(PlayerLoginEvent event){
		Player player = event.getPlayer();
		associations.addPlayer(player.getName(), player.getUniqueId());
		String name = associations.getCurrentName(player.getUniqueId());
		try {
			// start of getting the GameProfile
			CraftHumanEntity craftHuman = (CraftHumanEntity) player;
			EntityHuman human = craftHuman.getHandle();
			Field fieldName = EntityHuman.class.getDeclaredField("i");
			fieldName.setAccessible(true);
			GameProfile prof = (GameProfile) fieldName.get(human);
			// End

			// Start of adding a new name
			Field nameUpdate = prof.getClass().getDeclaredField("name");

			setFinalStatic(nameUpdate, name, prof);
			// end
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.setDisplayName(name);
		player.setPlayerListName(name);
		player.setCustomName(name);
	}
	
	static void setFinalStatic(Field field, Object newValue, GameProfile prof) {
		try {
			field.setAccessible(true);

			// remove final modifier from field
			Field modifiersField;
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField
					.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(prof, newValue);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
