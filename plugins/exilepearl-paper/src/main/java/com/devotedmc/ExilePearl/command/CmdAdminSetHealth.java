package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;
import java.util.UUID;

public class CmdAdminSetHealth extends PearlCommand {

    public CmdAdminSetHealth(ExilePearlApi pearlApi) {
        super(pearlApi);
        this.aliases.add("sethealth");

        this.setHelpShort("Sets the health value of a pearl.");

        this.commandArgs.add(requiredPearlPlayer());
        this.commandArgs.add(required("health", autoTab("", "Enter the desired health")));

        this.permission = Permission.SET_HEALTH.node;
        this.visibility = CommandVisibility.SECRET;
    }

    @Override
    public void perform() {
        UUID playerId = argAsPlayerOrUUID(0);
        if (playerId == null) {
            msg("<i>No player was found matching <c>%s", argAsString(0));
            return;
        }

        ExilePearl pearl = plugin.getPearl(playerId);
        if (pearl == null) {
            msg("<i>No pearl was found matching <c>%s", argAsString(0));
            return;
        }

        Integer health = argAsInt(1);
        if (health == null) {
            msg("<b>Pearl health must be an integer");
            return;
        }

        pearl.setHealth(health);
        msg("<g>You updated the pearl health of player %s to %d", pearl.getPlayerName(), health);
    }
}
