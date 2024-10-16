package com.untamedears.realisticbiomes.model.gauss;

import com.untamedears.realisticbiomes.model.ltree.BlockTransformation;
import org.bukkit.configuration.ConfigurationSection;

public class GaussTreeConfig {
	
	private String identifier;
	
	private GaussProperty preCanopyHeight;
	private GaussProperty stemAngleStep;
	private double stemAngleCap;
	private GaussProperty stemRadius;
	private BlockTransformation stemTransform;
	private BlockTransformation leafTransform;

	private GaussProperty canopyHeight;
	private GaussProperty leafAmount;
	private GaussProperty branchChance;
	private GaussProperty branchLength;
	private GaussProperty branchLogInterval;
	private GaussProperty branchAngle;
	
	public GaussTreeConfig(ConfigurationSection config) {
		if (!config.isString("id")) {
			throw new IllegalArgumentException("Config did not have an id");
		}
		this.identifier = config.getString("id");
		this.preCanopyHeight = parseGaussProperty(config, "stem_height");
		this.stemAngleStep = parseGaussProperty(config, "stem_angle_step");
		this.stemRadius = parseGaussProperty(config, "stem_radius");
		this.canopyHeight = parseGaussProperty(config, "canopy_height");
		this.leafAmount = parseGaussProperty(config, "leaf_amount");
		this.branchLength = parseGaussProperty(config, "branch_length");
		this.branchLogInterval = parseGaussProperty(config, "branch_log_interval");
		this.branchAngle = parseGaussProperty(config, "stem_height");
		this.branchChance = parseGaussProperty(config, "branch_chance");
	}
	
	private GaussProperty parseGaussProperty(ConfigurationSection config, String key) {
		if (!config.isConfigurationSection(key)) {
			return GaussProperty.constructProperty(new DeviatingDouble(0.0, 1.0, 0.1), 0.15);
		}
		config = config.getConfigurationSection(key);
		if (!config.isConfigurationSection("deviation")) {
			throw new IllegalArgumentException(config.getCurrentPath() + " did not specify deviation");
		}
		double lowerCap = config.getDouble("lower_cap", 0);
		double upperCap = config.getDouble("upper_cap");
		double deviation = config.getDouble("deviation", 0.1);
		DeviatingDouble deviatingDouble = new DeviatingDouble(lowerCap, upperCap, deviation);
		double sigma = config.getDouble("sigma", 0.15);
		return GaussProperty.constructProperty(deviatingDouble, sigma);
	}
	
	
	
	
}
