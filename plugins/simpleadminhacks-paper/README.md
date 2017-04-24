Simple Admin Hacks
==================

Landing spot for fixes, tuning, micro game modifications that have no other home.

This is tweak central. 

Brought to you by DevotedMC, we're taking a pretty inclusive and permissive view
for this plugin; if you need a particular "hack" to get things working in your
use case, send us a PR for inclusion, we'd love to host it.

This plugin is designed from the ground up to support easy creation of "microplugins"
-- things with featuresets too small to make sense for a full sized plugin, or fixes
for game problems or simple heuristic loggers.

Making a new "Hack" is always an option, but take a look at this list of active
Hacks and see if your contribution might fit with an existing module.

##Active Hacks

###BadBoyWatch

This hack keeps track of who breaks what, and what they broke to get there. I've got
bigger plans for more heuristics but for now, that's it. Please, feel free to improve
on this.

###CTAnnounce

An integration into CombatTagPlus, this announces to in-game operators when combat
is under way. Invaluable when you like your admins to monitor in-game fights.

###Experimental

This is a catch-all for anything micro that has no other home. Useful features like
`/serialize` command live here too, but mostly its logging trackers that are great
for debugging edge cases and useless for normal runtime.

###GameFeatures

The principle focus here is on enabling and disabling game features, like elytra or
skulker boxes or those sorts of things. Try to keep the focus on boolean or otherwise
simple on / off or discrete state options that relate to existing Minecraft game 
mechanics.

###GameFixes

Sometimes things in Minecraft just... don't work. Or don't work the way you want them
to. Put those fixes here.

###GameTuning

Similar to Game Fixes, but distinct in that the intent here is on hacks that _tune_
or alter existing mechanics without fully disabling them or without fixing them. 
Things like altering armor damage reduction or tool impact would be good fits.

###HackBot

An experimental module whose complexity is approaching divorce from SAH, this is
a bit of mid-work code that allows operators to define static Bots that show up
like "real" players but aren't.

###Introbook

Allows the creation of customizeable, helpful first join books that are distributed
to players when they first join the server. Safe to turn off but super useful
if you have a core set of additional "good to know" information or introductory
material you'd like players to receive.

###InvControl

Host of the `/invsee` command, gives insight into active player inventory, armor,
health and more. I hope / intend to add another portion to this that allows online
and offline inventory management via `/invmod` but this is as of now not started.

###InvisibleFix and InvisibleFixTwo

My hope is to remove these in time, but for 1.9 and 1.10 minecraft players
go invisible all the time. Both of these hacks use different approaches to help
preserve the visiblity of players. The second hack is the more invasive, sending
explicit location packets when the situation indicates its usefulness.

###NewfriendAssist

A new join tracker, that also doubles as an introkit delivery mechanism.
It also exposes the `introkit` command you can use to send specific people a new
introkit if they lost theirs.

###ReinforcedChestBreak

Another useful alerting hack, sends a message to online operators when players
are actively breaking reinforced containers. 

###SanityHack

This one is pretty Devoted iteration 3 specific. Allows tracking server wide of
all breaks or builds under a specific y level.

## Conclusion

There is room for many more hacks and the list grows regularly. The pattern is simple; 
check any of the existing hacks for details by example.

