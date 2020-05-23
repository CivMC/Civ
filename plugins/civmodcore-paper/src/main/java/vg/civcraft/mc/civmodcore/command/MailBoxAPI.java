package vg.civcraft.mc.civmodcore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Preconditions;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.impl.collection.ListSetting;

public class MailBoxAPI {

	private static ListSetting<String> mail;

	/**
	 * Internal setup, should only be called when CivModCore is enabling
	 */
	public static void setup() {
		if (mail != null) {
			throw new IllegalStateException("Was already registed");
		}
		/*mail = new ListSetting<>(CivModCorePlugin.getInstance(), "Mail box", "cmcMailBox",
				new ItemStack(Material.STICK), null, String.class, false); */
		PlayerSettingAPI.registerSetting(mail, null);
	}

	/**
	 * Adds a new messages to the players mail box
	 * @param player UUID of the player to send message to
	 * @param msg Message to add
	 */
	public static void addMail(UUID player, String msg) {
		Preconditions.checkNotNull(player, "Player may not be null");
		Preconditions.checkNotNull(msg, "Message to add may not be null");
		mail.addElement(player, msg);
	}

	/**
	 * Gets all pending messages in a players mail box
	 * @param player Player to mail box of
	 * @return Messages in the players mail box
	 */
	public static List<String> getMail(UUID player) {
		Preconditions.checkNotNull(player, "Player may not be null");
		return mail.getValue(player);
	}

	/**
	 * Clears all mail a player has
	 * @param player
	 */
	public static void clearMail(UUID player) {
		Preconditions.checkNotNull(player, "Player may not be null");
		mail.setValue(player, new ArrayList<>());
	}

}
