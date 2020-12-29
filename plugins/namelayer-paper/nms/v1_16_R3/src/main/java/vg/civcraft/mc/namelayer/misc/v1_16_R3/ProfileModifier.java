package vg.civcraft.mc.namelayer.misc.v1_16_R3;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import net.minecraft.server.v1_16_R3.EntityHuman;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.misc.ProfileInterface;

import com.mojang.authlib.GameProfile;

public class ProfileModifier implements ProfileInterface {

	private static final Logger log = Logger.getLogger(ProfileModifier.class.getSimpleName());

	@Override
	public void setPlayerProfle(Player player, String name) {
		String oldName = player.getName();
		if (name.length() > 16) {
			log.info(String.format("The player %s (%s) was kicked from the server due to his "
							+ "name already existing but now becoming over 16 characters.",
					name, player.getUniqueId().toString()));
		}
		try {
			// start of getting the GameProfile
			CraftHumanEntity craftHuman = (CraftHumanEntity) player;
			EntityHuman human = craftHuman.getHandle();
			GameProfile prof = human.getProfile();
			// End

			// Start of adding a new name
			Field nameUpdate = prof.getClass().getDeclaredField("name");

			setFinalStatic(nameUpdate, name, prof);
			
			((CraftServer)Bukkit.getServer()).getServer().getUserCache().a(prof);
			// end
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		player.setDisplayName(name);
		player.setPlayerListName(name);
		player.setCustomName(name);
		log.info(String.format("The player %s has had his name changed to %s.", oldName, name));
	}

	public void setFinalStatic(Field field, Object newValue, Object profile) {
		GameProfile prof = (GameProfile) profile;
		try {
			field.setAccessible(true);

			// remove final modifier from field
			Field modifiersField;
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField
					.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(prof, newValue);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
