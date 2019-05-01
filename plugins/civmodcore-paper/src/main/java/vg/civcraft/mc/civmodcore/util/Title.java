package vg.civcraft.mc.civmodcore.util;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R2.PlayerConnection;

/**
 * Allows sending titles to players with full customization, which the bukkit/spigot API doesnt offer. A title consists
 * of two parts, the main head line text and a second sub title line, which is displayed smaller than the main title.
 * When sending the title, it'll first have a "fade in" period, during which the title will fade in, at it's end the
 * title displayed will be completly opaque. It'll stay like that for the stay period of time defined and after that
 * take the fade out time to disappear again completly. When overlapping titles, the later one will completly override
 * the previous one.
 *
 */
public class Title {
	private String title;
	private String subtitle;
	private int fadeIn;
	private int stay;
	private int fadeOut;

	public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		super();
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	/**
	 * @return Main title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the main big title
	 * 
	 * @param title
	 *            Title to set to
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Subtitle shown in smaller font below the main title
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * Sets the smaller sub title
	 * 
	 * @param subtitle
	 *            New subtitle text
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	/**
	 * @return Time it takes for the title to fade in, measured in ticks
	 */
	public int getFadeIn() {
		return fadeIn;
	}

	/**
	 * Sets how long the title takes to fade in
	 * 
	 * @param fadeIn
	 *            Time in ticks to fade in
	 */
	public void setFadeIn(int fadeIn) {
		this.fadeIn = fadeIn;
	}

	/**
	 * @return Time the title will stay fully visible, measured in ticks
	 */
	public int getStay() {
		return stay;
	}

	/**
	 * Sets how long the title stays fully visible
	 * 
	 * @param stay
	 *            Time the title stays, measured in ticks
	 */
	public void setStay(int stay) {
		this.stay = stay;
	}

	/**
	 * @return Time the title takes to fade out, measured in ticks
	 */
	public int getFadeOut() {
		return fadeOut;
	}

	/**
	 * Sets how long it takes for the title to fade out
	 * 
	 * @param fadeOut
	 *            Fade out time in ticks
	 */
	public void setFadeOut(int fadeOut) {
		this.fadeOut = fadeOut;
	}

	/**
	 * Sends the title to the given player, according to the configuration in this instance
	 * 
	 * @param p
	 *            Player to send the title to
	 */
	public void sendTitle(Player p) {
		PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn,
				stay, fadeOut);
		connection.sendPacket(packet);
		IChatBaseComponent sub = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
		packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, sub);
		connection.sendPacket(packet);
		IChatBaseComponent main = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
		packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, main);
		connection.sendPacket(packet);
	}
}
