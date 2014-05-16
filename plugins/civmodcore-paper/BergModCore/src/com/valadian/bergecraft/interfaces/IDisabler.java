package com.valadian.bergecraft.interfaces;
import org.bukkit.entity.Player;

public interface IDisabler extends ICompat
{
    public boolean isBergecraftDisabledFor(Player player);
}
