package vg.civcraft.mc.namelayer.misc.v1_8_R3;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.AssociationList;
import vg.civcraft.mc.namelayer.misc.ProfileInterface;

import com.mojang.authlib.GameProfile;

public class ProfileModifier implements ProfileInterface{ // meh package change, when i get rid of 1.7 compatibility come here

	private AssociationList associations = NameAPI.getAssociationList();

	public void setPlayerProfle(Player player) {
		String name = associations.getCurrentName(player.getUniqueId());
		String oldName = player.getName();
		if (name.length() > 16) {
			NameLayerPlugin
					.log(Level.INFO,
							String.format(
									"The player %s (%s) was kicked from the server due to his "
											+ "name already existing but now becoming over 16 characters.",
									name, player.getUniqueId().toString()));
		}
		try {
			// start of getting the GameProfile
			CraftHumanEntity craftHuman = (CraftHumanEntity) player;
			EntityHuman human = craftHuman.getHandle();
			Field fieldName = EntityHuman.class.getDeclaredField("bH");
			fieldName.setAccessible(true);
			GameProfile prof = (GameProfile) fieldName.get(human);
			// End

			// Start of adding a new name
			Field nameUpdate = prof.getClass().getDeclaredField("name");

			setFinalStatic(nameUpdate, name, prof);
			
			MinecraftServer.getServer().getUserCache().a(prof);
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
		NameLayerPlugin.log(Level.INFO, String.format("The player %s has had his name changed to %s.", oldName, name));
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
