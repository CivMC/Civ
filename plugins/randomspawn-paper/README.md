Random-Spawn
============

Random Spawn plugin for Bukkit


Originaly by  Josvth

This plugin allows you to setup random spawning for players when joining or dieing including overriding the return 
to bed on death.

Sponsored by DevotedMC (by ProgrammerDan):
 * Adding new types of spawning for new players.
     * firstjoin and newplayer
     * firstjoin just enabled Spawn Point spawning for first joining players
     * newplayer is a time-configurable "grace period" where Spawn Points continue to be available to new players, in case their first point of spawn is ... not friendly, for instance.
 * New event NewPlayerSpawn for other plugins to cancel prospective new player spawning events ( prevent from spawning in an area for instance ) but only for the new Spawn Points.
 * Command /rs addspawn to add named spawn points with a spawn radius and exclusion; spawn type determines how they are used.
 * Command /rs removespawn to remove a named spawn point.

