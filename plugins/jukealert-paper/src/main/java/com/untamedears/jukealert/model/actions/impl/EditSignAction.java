package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;

public class EditSignAction extends LoggableBlockAction {

    public static final String ID = "EDIT_SIGN";

    public EditSignAction(long time, Snitch snitch, UUID player, Location location, Material material) {
        super(time, snitch, player, location, material);
    }

    @Override
    public String getChatRepresentationIdentifier() {
        return "Edited";
    }


    @Override
    public String getIdentifier() {
        return ID;
    }

}
