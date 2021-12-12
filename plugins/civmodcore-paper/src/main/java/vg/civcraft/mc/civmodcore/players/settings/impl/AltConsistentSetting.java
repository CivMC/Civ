package vg.civcraft.mc.civmodcore.players.settings.impl;

import java.util.UUID;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.players.settings.AltRequestEvent;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;

/**
 * 
 * Wrapper for a PlayerSetting which will keep values consistent across alts.
 * Whenever a request is made based on UUID this class will remap the UUID based
 * on the result of an AltRequestEvent, which must be filled by the appropriate
 * alt handling plugin.
 * 
 * Uses the wrapped PlayerSetting as storage, but this instance for the GUI
 *
 * @param <C> Type of the PlayerSetting to wrap
 * @param <T> Type of the values stored in the wrapped PlayerSetting
 */
public class AltConsistentSetting<C extends PlayerSetting<T>, T> extends PlayerSetting<T> {

	private C wrappedSetting;

	/**
	 * Wrap an existing PlayerSetting. Use this by creating a normal PlayerSetting,
	 * passing it to this constructor and then only registering the instance of
	 * AltConsistentSetting in PlayerSettingAPI
	 * 
	 * @param wrappedSetting PlayerSetting to wrap
	 */
	public AltConsistentSetting(C wrappedSetting) {
		super(wrappedSetting.getOwningPlugin(), wrappedSetting.getDefaultValue(), wrappedSetting.getNiceName(),
				wrappedSetting.getIdentifier(), wrappedSetting.getVisualization(), wrappedSetting.getDescription(),
				true);
		this.wrappedSetting = wrappedSetting;
	}

	public C getWrappedSetting() {
		return wrappedSetting;
	}

	@Override
	public T getValue(UUID player) {
		return wrappedSetting.getValue(getMain(player));
	}

	@Override
	public void setValue(UUID uuid, T value) {
		super.setValue(getMain(uuid), value);
	}

	private static UUID getMain(UUID account) {
		AltRequestEvent event = new AltRequestEvent(account);
		Bukkit.getPluginManager().callEvent(event);
		return event.getMain();
	}

	@Override
	public T deserialize(String serial) {
		return wrappedSetting.deserialize(serial);
	}

	@Override
	public boolean isValidValue(String input) {
		return wrappedSetting.isValidValue(input);
	}

	@Override
	public String serialize(T value) {
		return wrappedSetting.serialize(value);
	}

	@Override
	public String toText(T value) {
		return wrappedSetting.toText(value);
	}

}
