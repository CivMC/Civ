### CivSpy
-------------

Better targeted recording of very specific actions and data into mostly aggregate but also point records.

For easy reading on how to add Samplers and Listeners to CivSpy-Bukkit, see [here for Samplers](CivSpy-Bukkit/src/main/java/com/programmerdan/minecraft/civspy/samplers/package.html) and [here for Listeners](CivSpy-Bukkit/src/main/java/com/programmerdan/minecraft/civspy/listeners/package.html).

For CivSpy-Bungee, just follow the examples as shown in [the main class](CivSpy-Bungee/src/main/java/com/programmerdan/minecraft/civspy/CivSpyBungee.java).

#### Basic Configuration

CivSpy (both bungee and bukkit) sits on top of a HikariCP connection pool. This pool needs to be well-configured or you risk data loss. 

For more details see:

* [CivSpy-API README](CivSpy-API/README.md)
* [CivSpy-Bukkit README](CivSpy-Bukkit/README.md)
* [CivSpy-Bungee README](CivSpy-Bungee/README.md)
