/// Lets civ-plugins communicate with supporting client mods via plugin-message channels. These are messages
/// specifically intended for the client, not for any proxy. Do **NOT** use this for sharding. ALL packets are
/// serialised to/from JSON so that they can be readable to server owners.
package vg.civcraft.mc.civmodcore.modsupport.packets;
