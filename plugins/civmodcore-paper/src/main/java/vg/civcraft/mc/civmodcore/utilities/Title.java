package vg.civcraft.mc.civmodcore.utilities;

import org.bukkit.entity.Player;

public class Title {

	private String title;
	private String subtitle;
	private int fadeIn;
	private int stay;
	private int fadeOut;

	public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}

	/**
	 * @return Main title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the main big title.
	 *
	 * @param title Title to set to.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Subtitle shown in smaller font below the main title.
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * Sets the smaller sub title.
	 *
	 * @param subtitle New subtitle text.
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	/**
	 * @return Time it takes for the title to fade in, measured in ticks.
	 */
	public int getFadeIn() {
		return fadeIn;
	}

	/**
	 * Sets how long the title takes to fade in.
	 *
	 * @param fadeIn Time in ticks to fade in.
	 */
	public void setFadeIn(int fadeIn) {
		this.fadeIn = fadeIn;
	}

	/**
	 * @return Time the title will stay fully visible, measured in ticks.
	 */
	public int getStay() {
		return stay;
	}

	/**
	 * Sets how long the title stays fully visible.
	 *
	 * @param stay Time the title stays, measured in ticks.
	 */
	public void setStay(int stay) {
		this.stay = stay;
	}

	/**
	 * @return Time the title takes to fade out, measured in ticks.
	 */
	public int getFadeOut() {
		return fadeOut;
	}

	/**
	 * Sets how long it takes for the title to fade out.
	 *
	 * @param fadeOut Fade out time in ticks.
	 */
	public void setFadeOut(int fadeOut) {
		this.fadeOut = fadeOut;
	}

	/**
	 * Sends the title to the given player, according to the configuration in this instance.
	 *
	 * @param player Player to send the title to.
	 */
	public void sendTitle(Player player) {
		player.sendTitle(this.title, this.subtitle, this.fadeIn, this.stay, this.fadeOut);
	}

}
