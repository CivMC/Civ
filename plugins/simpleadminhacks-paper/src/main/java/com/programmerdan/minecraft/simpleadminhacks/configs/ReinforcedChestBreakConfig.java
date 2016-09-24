package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReinforcedChestBreakConfig extends SimpleHackConfig{

    /**
     * This list contains the admins specified in the config
     */
    private List<UUID> admins;

    /**
     * This is the delay between messages
     */
    private int delay;

    /**
     * The message if someone is raiding a reinforced chest
     */
    private String message;

    public ReinforcedChestBreakConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
        super(plugin, base);
    }

    @Override
    protected void wireup(ConfigurationSection config) {
        if (config.getList("admins") == null)
        {
            admins = new ArrayList<>();

            for (OfflinePlayer offlinePlayer: Bukkit.getOfflinePlayers())
            {
                //are these your igns?
                if (offlinePlayer.getName().equals("BonKill") || offlinePlayer.getName().equals("ProgrammerDan"))
                {
                    admins.add(offlinePlayer.getUniqueId());
                }
            }
        }
        else
        {
            this.admins = (List<UUID>) config.getList("admins");
        }

        this.delay = config.getInt("delay", 180);
        this.message = config.getString("message", "&4%Player% is raiding a chest at %x% %y% %z%.");
    }

    public List<UUID> getAdmins()
    {
        return  admins;
    }

    public int getDelay()
    {
        return delay;
    }

    public String getMessage()
    {
        return message;
    }
}
