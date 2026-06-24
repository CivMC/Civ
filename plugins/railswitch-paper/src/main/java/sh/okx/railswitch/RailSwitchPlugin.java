package sh.okx.railswitch;

import org.bukkit.event.Listener;
import sh.okx.railswitch.commands.DestinationCommand;
import sh.okx.railswitch.glue.CitadelGlue;
import sh.okx.railswitch.settings.SettingsListener;
import sh.okx.railswitch.settings.SettingsManager;
import sh.okx.railswitch.switches.SwitchListener;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

/**
 * The Rail Switch plugin class
 */
public final class RailSwitchPlugin extends ACivMod implements Listener {

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        super.onEnable();
        SettingsManager.init(this);
        registerListener(new CitadelGlue(this));
        registerListener(new SwitchListener());
        registerListener(new SettingsListener());
        commandManager = new CommandManager(this);
        commandManager.init();
        commandManager.registerCommand(new DestinationCommand());
    }

    @Override
    public void onDisable() {
        SettingsManager.reset();
        if (commandManager != null) {
            commandManager.reset();
            commandManager = null;
        }
        super.onDisable();
    }

}
