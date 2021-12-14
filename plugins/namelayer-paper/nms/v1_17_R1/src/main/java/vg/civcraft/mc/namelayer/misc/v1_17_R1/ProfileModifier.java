package vg.civcraft.mc.namelayer.misc.v1_17_R1;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.misc.ProfileInterface;

public class ProfileModifier implements ProfileInterface {

	private static final Logger LOGGER = Logger.getLogger(ProfileModifier.class.getSimpleName());

	@Override
	public void setPlayerProfile(final Player player, final String name) {
		final String oldName = player.getName();
		if (name.length() > 16) {
			LOGGER.info(String.format("The player %s (%s) was kicked from the server due to their "
							+ "name already existing but now becoming over 16 characters.",
					name, player.getUniqueId()));
		}
		player.setPlayerProfile(new CraftPlayerProfile(player.getUniqueId(), name));
		player.displayName(Component.text(name));
		player.playerListName(Component.text(name));
		player.customName(Component.text(name));
		LOGGER.info(String.format("The player \"%s\" has had their name changed to \"%s\"", oldName, name));
	}

}
