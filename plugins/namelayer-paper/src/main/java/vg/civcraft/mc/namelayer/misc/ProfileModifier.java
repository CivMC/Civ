package vg.civcraft.mc.namelayer.misc;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class ProfileModifier implements ProfileInterface {

    private static final Logger LOGGER = Logger.getLogger(ProfileModifier.class.getSimpleName());

    @Override
    public void setPlayerProfile(final Player player, final String name) {
        final String oldName = player.getName();
        PlayerProfile oldProfile = player.getPlayerProfile();
        PlayerProfile newProfile = new CraftPlayerProfile(player.getUniqueId(), name);
        if (name.length() > 16) {
            LOGGER.info(String.format("The player %s (%s) was kicked from the server due to their "
                    + "name already existing but now becoming over 16 characters.",
                name, player.getUniqueId()));
        }
        newProfile.setProperties(oldProfile.getProperties());
        player.setPlayerProfile(newProfile);
        player.displayName(Component.text(name));
        player.playerListName(Component.text(name));
        player.customName(Component.text(name));
        LOGGER.info(String.format("The player \"%s\" has had their name changed to \"%s\"", oldName, name));
    }

}
