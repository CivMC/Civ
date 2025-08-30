---
title: FAQ
description: Server FAQ
---

# Frequently Asked Questions

This is a compilation of frequently asked questions that many new or old players ask.

## Getting Started

### What is CivMC?

CivMC is the latest Civ-genre Minecraft server, utilizing various mechanics to create an emergent simulation of civilization within Minecraft.

### What is a Civ-genre minecraft server?

Somewhat between Anarchy and SMP, the Civ genre is focused on creating an emergent simulation of civilization within Minecraft. These civilizations are player-run and the players own government, economy, justice, and more. 

### How do I play/what's the IP?

The IP is `play.civmc.net`.

### What versions can I play on?

At the time of writing, the server officially supports clients on 1.21.4, Java edition -- but any client between 1.21 and above can connect. 

Please note that you should only submit a bug report on the officially supported version, 1.21.4.

*This may be out of date; this was last updated on February 27, 2025*.

### Can I play on Bedrock?

Currently, the admins [have no plans to make the server compatible with Bedrock Edition.](https://www.reddit.com/r/CivMC/comments/1ixls5c/is_civmc_ever_going_to_allow_bedrock/)

### Where should I play?

There are a bunch of nations that advertise themselves within the *#nation-ads* forum [on the official discord.](https://discord.gg/HkD79GfmQQ)

### How do I start/claim land for a nation?

While you *can* start a new nation, many players highly recommend (read: *extremely recommend*) to join an established nation (see above) to learn [the intricate Civ-specific mechanics](https://wiki.civmc.com).

If you want to claim land for a new nation though, it is commonplace for players to claim some open land present on the most recent claims map provided it doesn't conflict with any other nation, and post your claims [on the subreddit](https://reddit.com/r/civmc).

### People are asking me to teleport to them!

You're able to use your one time teleport to them via `/ott <player>` ([more information here](/pages/new-player-guide#nations-and-getting-started). Once you teleport to someone, you won't be able to teleport again. It is usually wise to find someone who willing to teleport who is in a nation you'd like to be in, as they will likely help you learn about Civ.

## Communication

### How do I talk to people?

You can talk within a "group" (that's the messages prepended with a bracket set, like `[!]`) by typing `/gc <group name>` in game. So for the `!` group, which is the global group (that you automatically join when you first log in), you would type `/gc !`.

In local chat, you're restricted to only a 1000 block radius. You can type in that chat by just typing `/gc`.

If you'd like to message someone directly in-game, `/message <player> [message]` initiates a conversation and `/reply [message]` is a shortcut to your last DM.

### How do I mute chat?

You can mute a group with `/`.

You can mute a person with `/mute`.

### How do I talk to people offline?

Join the [server discord](https://discord.gg/HkD79GfmQQ). The general channel is usually pretty active, but there also is a relay to the global chat group `!` within the discord.

Nearly every nation and other multinational organizations also have discords too, you can check the `#nation-ads` channel within the server discord.

## Tools

### Is there a map / is there a dynmap?

There is no dynmap installed. There are player-run, asynchronous maps showing claims and various landmarks but are only synced with the live server every couple of months; two commonly used ones are:

* https://civmc-map.github.io, which provides more landmarks & filters.
* https://civmap.com, which provides more map visualization features.

### Is there Towny?

No. Claiming land is less of a game mechanic here and more of a loose, informal structure, where claims are only valid if you can enforce your claim, and is not restricted by any mechanic.

### What mods can I use?

While there is a specific list of restrictions of what mods (and bots) can and cannot do, the short version is that it is only restricted to what a vanilla player can do without a screen.

A (community) shortlisted version of mods is available on the [Mods page on CivWiki](https://civwiki.org/wiki/Mods), although this is not vetted by the admins.

## Mechanics

While this will give a general overview of frequently asked questions on the server's mechanics, fuller explanations can be found on [CivMC's wiki.](https://wiki.civmc.net/pages/plugins/unique/)

### What is a pearl? I have died and was sent to the nether!

Ender pearls on Civ are used as a "capture" kind of like a Pokeball; a player will have to kill you while a pearl is in their hotbar. While captured, you'll be sent to the Nether. If you're pearled, you likely have done something that another player finds bad.

More information can be found on the [ExilePearl wiki page](/pages/plugins/essential/exilepearl).

### How do I reinforce something? / How can I protect my stuff?

You can use stone, iron, or diamond to reinforce a block you placed on the server by 50, 300, or 2000 times respectively; this will mean it will take that number of block breaks for another player (besides you) to break that block.

More information can be found on the [Citadel wiki page](/pages/plugins/essential/citadel).

### I can't place a block here / I received a message saying "Bastion blocked"

This means you are within a Bastion field, which prevents blocks from being placed. Either ask for permission to place blocks from the owners of the city, or build somewhere else.

More information can be found on the [Bastion wiki page](/pages/plugins/unique/bastions).

### How do I find diamonds?

Diamonds can be found in "veins" that mimic the real world where ores are found in long, distributed veins, underground; while finding a vein is through a similar process in Minecraft, mining a diamond vein to completion can be tricky on your first try. A common y-level to start hunting for diamonds is at -30, but note that veins can commonly sprawl vertically.

[A guide on how to mine these veins are here.](/pages/new-player-guide#hiddenore)

### I'm holding too much stuff, are there shulker boxes?

No, but there are two in-game mechanics that you can use to transport large amounts of materials quickly.

- Compactors are factories (which also act as decompactors) can "compress" a stack of blocks into one compacted item (ci), meaning you can hold 64 * 64 items in one compacted stack (cs).
- Backpacks are extremely high value items that allow you to have an expanded inventory.

### How do I make `<insert item here>`?

Try looking at https://civtechtree.netlify.app/items.

## Navigation & Structures

### How do I get around the server?

A simple way is the rail network. There are [several rails that dot around the server](https://civmc-map.github.io/). Of those, there are three prevailing systems of navigation:

* Direct, which is a bi-directional rail sending you directly to one destination to another
* OneDest, an interconnected rail network that serves multiple destinations with a universal location identifier for each
* Great Overland Rail (GOR), a singular trunk line primarily going through Yoahtlan territory

For the latter two, you will need to set a `/dest` command to route to your location: [OneDest locations] and [GOR locations] can be found at the links provided.

There are other ways of getting around, such as ice roads like NordStream.

### Do you have elytra/ice boating/any other faster form of transportation?

We don't; these lag out the server infrastructure too much.

### There is a deep hole with obsidian

This is what's known as a [vault](https://civwiki.org/wiki/Vault), a structure designed to be intentionally hard to break into for storing pearls or high-value items. *Do not jump into the hole, unless you are the owner of the vault.*

### I jumped into the deep hold

[What's wrong with you](https://www.youtube.com/watch?v=KLXgG77zbMM)? Depending on which vault hole you jumped into, either someone will help free you from the area or you will be pearled. Ask on Discord.

## Admin Stuff

### I got banned because of a VPN, what do I do?

[Create a ticket on Discord, under "Resolve VPN Ban"](./media/vpn-unban.png) If you're unable to access the discord, [open a modmail request on Reddit](https://www.reddit.com/message/compose?to=%2Fr%2FCivMC&subject=&message=).

### I got banned for logging in "under association" / same account as another person, what do I do?

[Create a ticket on Discord, under "Resolve Multi-account Ban"](./media/multi-account-unban.png). If you're unable to access the discord, [open a modmail request on Reddit](https://www.reddit.com/message/compose?to=%2Fr%2FCivMC&subject=&message=). *(We do this to prevent [alternate accounts](https://www.youtube.com/watch?v=jfoaR3CdIJI).)*

### I got banned for `<issue not mentioned above>`. What do I do?

[Open a modmail request on Reddit.](https://www.reddit.com/message/compose?to=%2Fr%2FCivMC&subject=&message=)

### I found a (possible) bug, what do I do?

[Create a ticket on Discord, under "Report a bug"](./media/bug-report.png). *Do not use this knowledge to gain an advantage within the game; you will be banned for doing so.*

### I need to report a player!

[Create a ticket on Discord, under "Report a player"](./media/player-report.png)

### The admins/moderators have not responded to my ticket in a while.

Practice good etiquette and be patient. Sometimes, admins must deliberate for your case if you are banned under suspicious circumstances, so please don't bug them -- they are usually very busy.

### The admins/moderators won't tell me why I'm banned.

Unfortunately, it is server policy to not disclose why you're banned; players in the game will also never know the reason why you are banned. It is worth it to appeal or modmail to check if they will specifically disclose why, but it is within their right to withhold that information.

### Nobody can see my post on Reddit, even though I posted it!

That means you've probably recently made a new account; a Reddit moderator will approve it soon.

### I like the admins and this server, how do I help?

There are a couple ways:

- [Donate to their Patreon](https://www.patreon.com/Civ_MC)! There are some pretty generous tiers.
- If you have Discord Nitro, support [the discord](https://discord.gg/HkD79GfmQQ) with one of your boosts!

## Other

### I suddenly got kicked from the server!

#### ...and it is around 10pm Pacific / 1am Eastern / 6:00 GMT / 16:00 Australia

This is the normal daily restart, the server should be back online in roughly 25 to 45 minutes.

#### ...and there is a message in #announcements on Discord

Admins are likely doing maintenance to the server right now or applying an update. It usually takes around 15 minutes  or less for a server update, but could be longer.

#### ...and none of the above applies

Uh oh. That's not good. Nothing to do but wait, I guess

#### What's the history of this server and genre?

It's a 10+ year old server genre with a rich history; You can find out more with pages on https://civwiki.org.

#### What's been happening on the server recently?

There's a monthly newsletter at https://civwiki.news.


