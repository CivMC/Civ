package com.github.civcraft.donum.commands;
import com.github.civcraft.donum.commands.commands.Deliver;
import com.github.civcraft.donum.commands.commands.DeliverDeath;
import com.github.civcraft.donum.commands.commands.OpenDeliveries;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class DonumCommandHandler extends CommandHandler {

    @Override
    public void registerCommands() {
    	addCommands(new OpenDeliveries("openDeliveries"));
    	addCommands(new Deliver("adminDeliver"));
    	addCommands(new DeliverDeath("deliverDeath"));
    }
    

}
