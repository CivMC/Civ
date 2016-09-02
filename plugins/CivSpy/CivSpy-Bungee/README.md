### CivSpy for Bungee
-------------

CivSpy for Bungee is still fairly nascient. Although like CivSpy for Bukkit it leverages the power of HikariCP's connection pool, at present it lacks any of the scaled-out infrastructure of CivSpy Bukkit. 

Currently, it simply records login, logout, and session times at the network level. 

------------------
#### Ready to contribute?

Check out the javadocs for best details on getting started with CivSpy-Bungee.

Having trouble loading the javadocs? Clone this repository locally and you'll have them available -- just navigate to the index.html file inside `apidocs` folder.


-----------------
#### Catalogue of Current `stat_key` elements

As of Sept 2 2016:

`bungee.playercount` - Periodic count of players online. Period controlled by 'interval' in config.
`bungee.login` - Point data on players joining the network
`bungee.logout` - Point data on players leaving the network
`bungee.session` - Point data on how long inbetween join and leave (session length). Recorded at leave or shutdown.

