package com.programmerdan.minecraft.simpleadminhacks.hacks;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.ExperimentalConfig;

/**
 * This is a grab-bag class to hold any _tuning_ related configurations that impact the 
 * game, server-wide.
 * 
 * It's part of a series of focused hacks.
 *
 * {@link GameFixes} is focused on things that are broken or don't work, and attempts to fix them.
 * {@link GameFeatures} focuses on enabling and disabling features, like elytra, various potion states.
 * {@link GameTuning} neither fixes nor disables, but rather adjusts and reconfigures.
 *
 * Currently you can control the following:
 *  - BlockEntity limits per chunk
public class GameTuning extends SimpleHack<GameTuningConfig> implements Listener, CommandExecutor {

}
