package sh.okx.railswitch;

import org.bukkit.event.Listener;
import sh.okx.railswitch.glue.CitadelGlue;
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
        commandManager = new CommandManager(this);
        commandManager.init();
        registerCommands();
    }

    private void registerCommands() {

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
