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

## Active Hacks

### BadBoyWatch

This hack keeps track of who breaks what, and what they broke to get there. I've got
bigger plans for more heuristics but for now, that's it. Please, feel free to improve
on this.

### CTAnnounce

An integration into CombatTagPlus, this announces to in-game operators when combat
is under way. Invaluable when you like your admins to monitor in-game fights.

### Experimental

This is a catch-all for anything micro that has no other home. Useful features like
`/serialize` command live here too, but mostly its logging trackers that are great
for debugging edge cases and useless for normal runtime.

### GameFeatures

The principle focus here is on enabling and disabling game features, like elytra or
skulker boxes or those sorts of things. Try to keep the focus on boolean or otherwise
simple on / off or discrete state options that relate to existing Minecraft game 
mechanics.

### GameFixes

Sometimes things in Minecraft just... don't work. Or don't work the way you want them
to. Put those fixes here.

### GameTuning

Similar to Game Fixes, but distinct in that the intent here is on hacks that _tune_
or alter existing mechanics without fully disabling them or without fixing them. 
Things like altering armor damage reduction or tool impact would be good fits.

### HackBot

An experimental module whose complexity is approaching divorce from SAH, this is
a bit of mid-work code that allows operators to define static Bots that show up
like "real" players but aren't.

### Introbook

Allows the creation of customizeable, helpful first join books that are distributed
to players when they first join the server. Safe to turn off but super useful
if you have a core set of additional "good to know" information or introductory
material you'd like players to receive.

### InvControl

Host of the `/invsee` command, gives insight into active player inventory, armor,
health and more. I hope / intend to add another portion to this that allows online
and offline inventory management via `/invmod` but this is as of now not started.

### InvisibleFix and InvisibleFixTwo

My hope is to remove these in time, but for 1.9 and 1.10 minecraft players
go invisible all the time. Both of these hacks use different approaches to help
preserve the visiblity of players. The second hack is the more invasive, sending
explicit location packets when the situation indicates its usefulness.

### NewfriendAssist

A new join tracker, that also doubles as an introkit delivery mechanism.
It also exposes the `introkit` command you can use to send specific people a new
introkit if they lost theirs.

### ReinforcedChestBreak

Another useful alerting hack, sends a message to online operators when players
are actively breaking reinforced containers. 

### SanityHack

This one is pretty Devoted iteration 3 specific. Allows tracking server wide of
all breaks or builds under a specific y level.

### TimingsHack

This is mature enough to be its own plugin now, but for the moment I'm leaving it in
because IMHO it is an essential component for debugging and maintaining your server.

This hack begins where /timings and warmroast end. It generates in-game visualizations
of per-tick time utilization, and allows you to investigate with extreme effectiveness
exactly what is causing instantaneous or irregular lag. Things that average out, but lead
to bad experiences for players.

To use it, there are a number of important commands.

First:

 * `/showtimings` This gives the requester a "map" object that paints a heatmap, heightmap,
    and linegraph of TPS. Each vertical pixel is a full 20 ticks; each dot in the heatmap
    is a single tick. The heightmap above the heatmap (heatmap is on the bottom) is both
    colored and sized based on time-in-tick. At the very top is line graph of relative "health"
    of the tick. The closer to the grey line the better; the closer to the top, the worse.
 * `/thresholdtimings` This starts a very high resolution (1ms) stack inspection tracker.
    It keeps track _per tick_ with usually 50 samples per tick of which methods and classes
    are being taking up time within the tick. Give it a threshold factor -- please, something more
    then 1, 5 to 10 works well -- where any tick that takes longer then threshold factor * average
    tick length, dumps the class/method list to requestor (either console or in-game character).
    The list is in sorted order of class where most time is spent down to class where least time
    is spent. 
 * `/listtimings` In you already suspect something, you can use this command to begin a spammy
    dump of all classes encountered during high-resolution tracking. Only new classes are printed,
    and it shuts itself off if nothing new has been printed for a full second.
 * `/bindtimings` This generates an in-game map displaying (like `/showtimings`) a heatmap, heightmap,
    and line graph of per-tick utilization of any Classes that _contain_ whatever is passed in after 
    bindtimings. Note this is saved, and on reload of the server if HQ timings are restarted those
    maps will re-bind to the saved bindings. This is a massively useful deep inspection tool;
    you can put the "based" class of a suspect plugin as the parameter, and watch its actual per-tick
    time utilization in real-time. Destroy the map object to release the drain on overall TPS that
    results from the comparison functions used to update the map.
 * `/stoptimings` This turns off any HQ functions. Has the impact of _pausing_ any bindtimings maps, 
    and releasing all HQ functions from impacting Tick (also stops tracking data, so data tracking will
    reset if HQ restarted by calling any other HQ-tagged command).

In game example with some descriptions:

 * From live server: http://imgur.com/a/zWNWo

## Conclusion

There is room for many more hacks and the list grows regularly. The pattern is simple; 
check any of the existing hacks for details by example.

