package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.sql.JukeAlertLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlert extends JavaPlugin {

    private JukeAlertLogger jaLogger;
    private Map<World, Map<Location, JukeAlertSnitch>> snitches = new HashMap<World, Map<Location, JukeAlertSnitch>>();

    @Override
    public void onEnable() {
        jaLogger = new JukeAlertLogger(this);

        JukeAlertCommands commands = new JukeAlertCommands(this);
        for (String command : getDescription().getCommands().keySet()) {

            getCommand(command).setExecutor(commands);
        }
    }

    @Override
    public void onDisable() {
        //TODO: Make sure everything saves properly and does save.
    }

    //Gets the JaLogger.
    public JukeAlertLogger getJaLogger() {
        return jaLogger;
    }

    public List<JukeAlertSnitch> getSnitches(World world) {
        return (List<JukeAlertSnitch>) snitches.get(world).values();
    }

    public JukeAlertSnitch getSnitch(World world, Location location) {
        return snitches.get(world).get(location);
    }

    //Logs a message with the level of Info.
    public void log(String message) {
        this.getLogger().log(Level.INFO, message);
    }
}
