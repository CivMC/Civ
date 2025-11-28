package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.actions.internal.DestroySnitchAction;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class ShowOwnerOnDestroyAppender extends AbstractSnitchAppender {

    public static final String ID = "showownerondestroy";

    public ShowOwnerOnDestroyAppender(Snitch snitch) {
        super(snitch);
    }

    @Override
    public boolean runWhenSnitchInactive() {
        return true;
    }

    @Override
    public void acceptAction(SnitchAction action) {
        if (!action.isLifeCycleEvent()) {
            return;
        }
        if (!(action instanceof DestroySnitchAction dsa)) {
            return;
        }
        UUID destroyerUUID = dsa.getPlayer();
        if (destroyerUUID == null) {
            return;
        }
        Player player = Bukkit.getPlayer(destroyerUUID);
        if (player == null) {
            return;
        }
        Group group = snitch.getGroup();
        if (group == null) {
            player.sendMessage(Component.empty()
                .append(Component.text(snitch.getType().getName(), NamedTextColor.GOLD))
                .append(Component.text(" is on an unknown group.", NamedTextColor.YELLOW)));
            return;
        }

        UUID placer = action.getSnitch().getPlacer();
        String ownerName = Objects.requireNonNullElse(NameLayerAPI.getCurrentName(group.getOwner()), group.getOwner().toString());
        String groupName = group.getName();

        if (placer == null) {
            player.sendMessage(String.format("%s%s %swas reinforced on %s%s%s owned by %s%s", ChatColor.GOLD,
                snitch.getType().getName(), ChatColor.YELLOW, ChatColor.GREEN, groupName, ChatColor.YELLOW,
                ChatColor.LIGHT_PURPLE, ownerName));
        } else {
            player.sendMessage(Component.empty()
                .append(Component.text(snitch.getType().getName(), NamedTextColor.GOLD))
                .append(Component.text(" was placed by ", NamedTextColor.YELLOW))
                .append(Component.text(Objects.requireNonNullElse(NameLayerAPI.getCurrentName(placer), placer.toString()), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(", and was reinforced on ", NamedTextColor.YELLOW))
                .append(Component.text(groupName, NamedTextColor.GREEN))
                .append(Component.text(" owned by ", NamedTextColor.YELLOW))
                .append(Component.text(ownerName, NamedTextColor.LIGHT_PURPLE)));
        }
    }

}
