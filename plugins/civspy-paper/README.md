### CivSpy for Bukkit
-------------

CivSpy for Bukkit is focused on several core functional and operational imperatives:

* Don't lag the server

* Don't lag the database

* Focus on aggregation, not massive data volume

* Simple Periodic Sampling

* Simple Listener Support

* Good documentation

A little extra detail on each main point follows.

-----------------
#### Don't Lag The Server

While CivSpy's architecture can't prevent contributors from developing very poorly behaved `DataListeners` and `DataSamplers`, it does everything in its power to make sure that the flow of point or sampled data from the moment it leaves the listeners and samplers, is moved as quickly and efficiently as possible on its way to eventual database insertion.

Internally, this is accomplished by two main unbounded queues, some memory structures, and a whole host of worker threads focused on Keeping Things Moving.

*The Data Sample Queue* - When a Sampler's `sample()` method returns or a Listener calls `record()`, those records are added to the end of the data sample queue. This is a very fast, very lightweight operation. The Listener can get right back to listening, and the Sampler can get right back to waiting on its next invocation.

*The Aggregation Windows* - Meanwhile, a bunch of dequeue workers are busy pulling DataSamples off the front of the data sample queue, and putting them into special windowed aggregation containers. About half a dozen of these windows (standard configuration) are available, to deal with volume and rate-flow issues. Dequeued DataSamples are summation-aggregated into the appropriate window based on their creation timestamps. Note that PeriodDataSamples are handled special; they are passed directly to the next stage without aggregation. Normal DataSamples, however, must wait a while in their Window, until the window gets old enough that no more data is accepted into it. At that point, a special worker takes all the aggregated data and pushes it on to the batching queue.

*The Batching Queue* - Aggregate data is queued into the end of a special aggregate data queue by various workers from the aggregation window stage. Now final-stage workers grab those aggregations and compile them into batches. These batches are then transmitted to the database for insertion. A typical batch will be about 100 records. If a ton of aggregate data is dumped onto the queue at once, additional final-stage workers are turned on to help deal with the influx. When the calamity has passed, they are released gracefully. These batch workers are watched by a manager, who ensures that should they die or time out, at least one worker is always on the job with more ready to go. They leverage a pool of database connections, so there is little to no latency in submitting their data batches once it is compiled.

Some things are described in general or approximate terms above; check the source code for additional comments and details.

--------------------------
#### Don't Lag The Database

Both the application and database can waste a ton of time renegotiating connections. CivSpy-Bukkit uses HikariCP to keep a pool of connections on hand at all times so that connection establishment isn't happening during high-volume insertions. 

Additionally, all of CivSpy-Bukkit's insertions are strictly batched. The uniformity of sample data enables this highly efficient communication practice to be applied universally, yet by keeping batch sizes relatively small no undue burden is placed on the database or network infrastructure.

-------------------------
#### Focus on Aggregation, Not Massive Data Volume

Pull Requests adding listeners or samplers that generate large amounts of high granularity data will be rejected. If that's what you need, look into Devotion or similar tracks-everything platforms.

The focus here is on periodic low-volume data, or high-volume listeners that yield summation-aggregateable timeslice data. That's a fancy way of saying, listeners and samplers should generate data that has meaning when counted.

Strict adherance to this underlying philosophy lets CivSpy-Bukkit stay lean and effecient.

--------------------------
#### Simple Periodic Sampling

Contributing a new DataSampler is shockingly easy. 

* Follow the [instructions in the com.programmerdan.minecraft.civspy.samplers API](/apidocs/com/programmerdan/minecraft/civspy/samplers/package-summary.html) and make your code.

* Add new dependencies to the [pom](pom.xml). 

* If you depend on or sample another Bukkit plugin, be sure that:
   * Check the plugin exists using the Bukkit tests in your Sampler constructor, and call `deactivate` from your `sample` method if it isn't.
   * Add the plugin dependency to the [soft-depends list in plugin.yml](/src/main/resources/plugin.yml)

* Put the source file in package `com.programmerdan.minecraft.civspy.samplers.impl`.

All done.

-------------------------
#### Simple Listener Support

Similarly, Listeners are very well supported and familiar.

* Follow the [instructions in the com.programmerdan.minecraft.civspy.listeners API](/apidocs/com/programmerdan/minecraft/civspy/listeners/package-summary.html) and make your code.

* Add new dependencies to the [pom](pom.xml). Make sure to give them a scope of provided or compile, as appropriate.

* If you depend on or listen to events from another Bukkit plugin, be sure to add the plugin dependency to the [soft-depends list in plugin.yml](/src/main/resources/plugin.yml). 
   * If that plugin isn't available, the listener will gracefully fail to start.
   * Do not add any hard dependencies; only soft-depends.

* Put the source file in package `com.programmerdan.minecraft.civspy.listeners.impl`.

All done.

-------------------------
#### Good Documentation

I hope you agree that the documentation is clear and comprehensive. If anything is missing or unclear, open an issue on the repository.


----------------------------
### I'm Ready to Contribute

Fork the repo, and get hacking!

Clone locally to check out the javadocs for even more details on getting started with CivSpy. Just navigate to the index.html file inside `apidocs` folder on your local clone from your favorite web-browser.

-----------------
#### Catalog of Current `stat_key` elements

As of April 22, 2017:

##### Aggregate Statistics

`player.blockbreak` - Breaks down based on string_value of encoded block attributes and count within aggregation period.

`player.blockplace` - When a player places a block. UUID is recorded, and Block serialization is stored in the string value field.s

`block.drop.TYPE` - When the block broken drops items. TYPE is the material type of the block that is dropping stuff.

`player.craft` - When player crafts something. What was crafted size stored in number value, serialized string in string value.

`player.craft.custom` - For RecipeManager unique crafting with recipe name as string value.

`player.movement` - Breaks down based on string_value of movement type (walking, sneaking, vehicle, etc.) within aggregation period. Includes Vehicle movement, and resets prior location on teleport.

`player.drop` - Records item dropped when a person drops something

`player.consume` - Records when a player eats / consumes something.

`block.dispense.TYPE` - When a dispenser/dropper launches an item. TYPE is the material type of the block that is dispensing.

`entity.death.TYPE`
`entity.death.drop.TYPE`
`entity.death.xp.TYPE` - where TYPE is the EntityType.name() of what died; If killer is a player and isn't empty, UUID is filled for all. Otherwise is null. If creature has a custom name, recorded in string value field for death and XP; not for drop (itemstack serialized recorded there).

`player.fishing` - records fishing data, records location, player, and item / amount retrieved.

`player.killed` - used for PVP kills or death by entities, the UUID of the player or TYPE/Name of the entity responsible for killing is in the string value field. The UUID field holds the player killed in all cases.

`player.killed.drop` - the serialized items dropped by player on death

`player.died` - records player deaths when killed by some non-strictly-entity cause (drowning, etc.). The `drop` contributions are similar to entity death, but the UUID recorded is the player that died.

`player.died.drop` - The serialized items dropped by player on death 

`player.damage.TYPE` - Damage from PVP, where TYPE is nature of damage dealt, as discernable. Uuid is the player delivering the damage; string\_value is the player or entity receiving the damage; and numeric\_value is the damage dealt.

`player.damaged` - Damage from PVE, with string\_value being the nature of the damage received, and numeric\_value the damage amount.

`player.pickup` - Records when a person picks up something

`entity.pickup.TYPE` - Records when an entity picks up something

`inventory.pickup.TYPE` - An inventory holder picks up an item. TYPE is the Hopper or HopperMinecart that picked up the item.

`player.bucket.empty`
`player.bucket.fill` - Records when a bucket is emptied or filled, and who did it.

`inventory.moveto.TYPE` - Records when items move to an inventory, paired with...

`inventory.movefrom.TYPE` - Records when items leaves an inventory. String value is the item moved, Numeric Value is how many.

`player.INV.slot.ACTION` - Records when a player interacts with an inventory slot.

`player.INV.cursor.ACTION` - Records when a player interacts with their cursor. For both, INV is source inventory type; ACTION is the nature of the interaction. String value is the item moved, Numeric Value is how many.

`player.open` - when a player opens an inventory holder. String value is the TYPE of inventory opened.

`player.citadel.damage.group`
`player.citadel.damage.TYPE` - When a Citadel reinforcement is damaged, records what kind was damaged and what group was damaged. Also records who did it and what kind of block the citadel reinforcement is defending.

`player.citadel.create.group`
`player.citadel.create.TYPE` - When a Citadel reinforcement is created, records what kind it is and to what group it is bound. Also records who did it and what kind of block is now being protected

`player.citadel.change.group`
`player.citadel.change.TYPE` - Reinforcements can be "swapped" which triggers a change event. This captures the new group, the TYPE form captures the old reinf as TYPE and the new reinf as string_value

`player.citadel.acid.TYPE` - Records as TYPE the acid type and in string_value the reinforcement destroyed.

`player.citadel.acid.destroyed.group` - Records the group whose reinforcement was destroyed.

`cropcontrol.drop.TYPE` - Records when crop control triggers a drop. Covers all types of drops including player sponsored. Records which item(s) were dropped in the string value field. TYPE is the kind of break -- check the crop control documentation for which values are valid.

`hiddenore.drop` - Records when a person triggers a drop from hiddenore

`hiddenore.gen`
`hiddenore.replace` - When mining triggers a generation task. Both what was generated (.gen) and what it replaced (.replace) are recorded.

`crazycrates.crate.TYPE` 
`crazycrates.prize.TYPE`
`crazycrates.drop.TYPE` - When crazy crates awards a prize. Covers all types of crates. Records which item(s) were given out in the string
 value field for .drop, .crate stores crate name, .prize records prize name. TYPE is the kind of crate -- check the CrazyCrates documentation for which values are valid.
 
`exilepearl.pearl` - Records a player was pearled. The player data field is the player pearled, the String field is the player doing the pearling.

`exilepearl.moveto` - The pearl was moved to a new contained. String field is the name of the container.

`exilepearl.freed` - The pearl was freed. String field is the reason for freeing.

`redstone.current.increase`
`redstone.current.decrease`
`redstone.current.stable` - Track Redstone changes by location and motion. String_value is the redstone item that activated.


##### Periodic Statistics

`server.playercount` - default sampling period of every minute, records players online the server.

`world.playercount` - default sampling period of every minute, records players online per world on the server.

`server.tick.min` - In sampling period, the minimum tick in milliseconds (shortest observed).

`server.tick.max` - In sampling period, the maximum tick in milliseconds (longest observed).

`server.tick.average` - In sampling period, the average tick in milliseconds.
