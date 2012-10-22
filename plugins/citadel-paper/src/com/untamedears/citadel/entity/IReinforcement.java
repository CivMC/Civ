package com.untamedears.citadel.entity;

import org.bukkit.block.Block;

public abstract interface IReinforcement extends
        Comparable<IReinforcement> {
    public ReinforcementKey getId();
    public void setId(ReinforcementKey id);
    public Block getBlock();
    public int getDurability();
    public void setDurability(int durability);
    public double getHealth();
    public String getHealthText();
    public String getStatus();
}
