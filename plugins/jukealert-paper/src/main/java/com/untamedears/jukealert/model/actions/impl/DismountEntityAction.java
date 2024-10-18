package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import java.util.UUID;
import com.untamedears.jukealert.util.JAUtility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class DismountEntityAction extends LoggablePlayerVictimAction {

    public static final String ID = "DISMOUNT_ENTITY";

    public DismountEntityAction(long time, Snitch snitch, UUID player, Location location, String victim) {
        super(time, snitch, player, location, victim);
    }

    @Override
    public IClickable getGUIRepresentation() {
        ItemStack is = new ItemStack(getVehicle());
        super.enrichGUIItem(is);
        return new DecorationStack(is);
    }

    /**
     * @return Material of the dismounted entity
     */
    public Material getVehicle() {
        return JAUtility.getVehicle(victim);
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public String getChatRepresentationIdentifier() {
        return "Dismounted " + getVictim();
    }

}
