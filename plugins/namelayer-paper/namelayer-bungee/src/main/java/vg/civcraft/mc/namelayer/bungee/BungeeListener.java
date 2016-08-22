package vg.civcraft.mc.namelayer.bungee;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.packet.LoginRequest;

public class BungeeListener implements Listener{

	private DataBaseManager db;
	private NameLayerBungee plugin;
	
	public BungeeListener(DataBaseManager db) {
		this.db = db;
		plugin = NameLayerBungee.getInstance();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void postLoginEvent(LoginEvent event) {
		PendingConnection pending = event.getConnection();
		InitialHandler handle = (InitialHandler) pending;
		UUID uuid = pending.getUniqueId();
		db.addPlayer(pending.getName(), uuid);
		String name = db.getCurrentName(uuid);
		try {
			Field loginField = InitialHandler.class.getDeclaredField("loginRequest");
			loginField.setAccessible(true);
			LoginRequest request = (LoginRequest) loginField.get(handle);
			request.setData(name);
			//setFinalStatic(nameField, name, con);
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
