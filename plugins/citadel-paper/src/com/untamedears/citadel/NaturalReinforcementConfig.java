package com.untamedears.citadel;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.untamedears.citadel.Citadel;

public class NaturalReinforcementConfig {
    public static final int MAX_DURABILITY = 5000;

    private int materialId_;
    private int baseDurability_;
    private int minDurability_;
    private int maxDurability_;
    private int variance_;
    private double yAdjustment_;

    public NaturalReinforcementConfig(int materialId, int baseDurability) {
        setMaterialId(materialId);
        setBaseDurability(baseDurability);
        minDurability_ = 1;
        maxDurability_ = MAX_DURABILITY;
        variance_ = 0;
        yAdjustment_ = 0.0;
    }

    public NaturalReinforcementConfig(ConfigurationSection config) {
        String materialName = config.getName();
        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            setMaterialId(material.getId());
        } else {
            try {
                setMaterialId(Integer.parseInt(materialName));
            } catch (NumberFormatException e) {
                throw new Error(
                    "[Citadel] Invalid Natural Reinforcement Material: " +
                    materialName);
            }
        }
        int baseDurability = config.getInt("durability", Integer.MIN_VALUE);
        if (baseDurability == Integer.MIN_VALUE) {
            throw new Error(
                "[Citadel] Unconfigured Natural Reinforcement Durability for " +
                materialName);
        }
        setBaseDurability(baseDurability);
        setMinDurability(config.getInt("minimum", 1));
        setMaxDurability(config.getInt("maximum", MAX_DURABILITY));
        setVariance(config.getInt("variance", 0));
        setYAdjustment(config.getDouble("yadjust", 0.00000001));
    }

    public int getMaterialId()  { return materialId_; }
    public void setMaterialId(int materialId) {
        materialId_ = materialId;
    }
    public int getBaseDurability()  { return baseDurability_; }
    public void setBaseDurability(int baseDurability) {
        baseDurability_ = baseDurability;
    }
    public int getMinDurability()  { return minDurability_; }
    public void setMinDurability(int minDurability) {
        if (minDurability >= 1 && minDurability <= MAX_DURABILITY) {
            minDurability_ = minDurability;
        }
    }
    public int getMaxDurability()  { return maxDurability_; }
    public void setMaxDurability(int maxDurability) {
        if (maxDurability >= 1 && maxDurability <= MAX_DURABILITY) {
            maxDurability_ = maxDurability;
        }
    }
    public int getVariance()  { return variance_; }
    public void setVariance(int variance) {
        if (variance_ >= 0) {
            variance_ = variance;
        }
    }
    public double getYAdjustment()  { return yAdjustment_; }
    public void setYAdjustment(double yAdjustment) {
        yAdjustment_ = yAdjustment;
    }

    public int generateDurability(int blockY) {
        int durability = baseDurability_;
        if (variance_ > 0) {
            // variance_ is a max +- adjustment range to add to the durability
            durability += Citadel.getRandom().nextInt() % (variance_ * 2 + 1);
            durability -= variance_;
        }
        if (yAdjustment_ < 0.00001 || yAdjustment_ > 0.00001) {
            // yAdjustment_ is a gradient +- adjustment as the block's
            //  Y-value distances from 0. It is a float so the gradient
            //  can be gradual.
            durability += (int)((float)blockY * yAdjustment_);
        }
        if (durability < minDurability_) {
            return minDurability_;
        }
        if (durability > maxDurability_) {
            return maxDurability_;
        }
        return durability;
    }
}
