JukeAlert
=========

Additional updates requested for Devoted:

Snitch network command and control block. Essentially a change to jukealert requiring a stationary block that will relay snitch entries. Until that is set up on the snitch group jukeboxes will only record actions by /jainfo, breaking snitches will reveal parts of the 'relay's' location, allowing you to find an enemy's relay (and destroy it) after breaking a couple snitches.

Breaking that down:

1: New block (let's use a Sea Lantern for development) that is required to be on a group with a snitch before sending snitch 'entry' alerts:

   public void playerJoinEvent(PlayerJoinEvent event)
   public void handlePlayerExit(PlayerEvent event)
   private void handleSnitchEntry(Player player)

2: If a snitch is broken and a relay is reinforced on that group, it will display the following in the player who broke the snitch's chat:

"Relayed snitch broken. Relay located at [world XXX XXX XXX]"

With 5 of the X's randomly revealed. So for instance:

"Relayed snitch broken. Relay located at [world X3X 21 1X2]"

This allows players to track down Relays, if they break enough snitches.
