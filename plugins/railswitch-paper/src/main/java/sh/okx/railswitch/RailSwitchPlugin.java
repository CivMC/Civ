package sh.okx.railswitch;

import org.bukkit.event.Listener;
import sh.okx.railswitch.commands.DestinationCommand;
import sh.okx.railswitch.glue.CitadelGlue;
import sh.okx.railswitch.settings.SettingsManager;
import sh.okx.railswitch.switches.SwitchListener;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;

/**
 * The Rail Switch plugin class
 */
public final class RailSwitchPlugin extends ACivMod implements Listener {

    private static AikarCommandManager commands;

    @Override
    public void onEnable() {
        useNewCommandHandler = false;
        super.onEnable();
        SettingsManager.init(this);
        registerListener(new CitadelGlue());
        registerListener(new SwitchListener());
        commands = new AikarCommandManager(this) {
            @Override
            public void registerCommands() {
                registerCommand(new DestinationCommand());
            }
        };
    }

    @Override
    public void onDisable() {
        SettingsManager.reset();
        if (commands != null) {
            commands.reset();
            commands = null;
        }
        super.onDisable();
    }

}
