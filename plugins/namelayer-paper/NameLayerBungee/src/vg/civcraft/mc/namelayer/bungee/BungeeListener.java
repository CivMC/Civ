package vg.civcraft.mc.namelayer.bungee;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeListener implements Listener{

	private DataBaseManager db;
	private NameLayerBungee plugin;
	
	public BungeeListener(DataBaseManager db) {
		this.db = db;
		plugin = NameLayerBungee.getInstance();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void postLoginEvent(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		db.addPlayer(player.getName(), uuid);
		String name = db.getCurrentName(uuid);
		try {
			Field playerName = UserConnection.class.getDeclaredField("name");
			UserConnection con = (UserConnection) player;
			setFinalStatic(playerName, playerName, con);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.setDisplayName(name);
	}
	
	public void setFinalStatic(Field field, Object newValue, Object object) {
		try {
			field.setAccessible(true);

			// remove final modifier from field
			Field modifiersField;
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField
					.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(object, newValue);
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
