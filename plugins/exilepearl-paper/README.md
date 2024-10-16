# ExilePearl
A minecraft plugin that allows some self-moderation by letting players 'exile' other players through the use of ender pearls. 

When a player kills another player with an ender pearl in his hotbar, the player becomes exiled and the pearl is transformed into an exile pearl. Exiled players have a restricted set of in-game abilities which prevents them from griefing and doing damage to other players.

Exile pearls have a health value that decays over time, so they must be repaired periodically or else the player will be freed. 

Exiled players can't come within a set radius of their pearl, so the only way they can be freed is to convince the pearl owner to free him, or if other players forcefully free the pearl.

Exile pearls can also be upgraded to prison pearls, which imprison the pearled player in the end, for a higher upkeep cost.

### Player Commands
    /ep                           The root command
    /ep help                      Displays plugin help
    /ep locate                    Locates your pearl location
    /ep free                      Frees a pearl in your hand
    /ep upgrade                   Upgrades an Exile Pearl to a Prison Pearl
    /ep downgrade                 Downgrades a Prison Pearl
    /ep summon                    Requests to summon the player in the pearl in your hand
    /ep confirm                   Confirms a summon request
    /ep broadcast                 Broadcasts your pearl's location to a group or player
    /ep accept                    Accepts a broadcast request from another player
    /ep silence                   Silences pearl broadcasts from a player
    
### Admin Commands
    /ep config                    Config root command
    /ep config list               Lists the config rules
    /ep config load               Reloads the configuration
    /ep config save               Saves the configuration
    /ep config set                Sets a configuration rule value
    /ep decay                     Performs a decay operation on all pearls
    /ep exileany                  Exiles any player
    /ep freeany                   Frees any player
    /ep sethealth                 Sets the health of a pearl
    /ep settype                   Sets the type of a pearl
    /ep setkiller                 Sets the killer of a pearl
    /ep check                     Checks if a player is exiled
    /ep list                      Lists all the exiled players
    /ep reload                    Reloads the entire plugin
    
### Permissions
  The player commands are enabled by default for all players
  
    exilepearl.check              Grants access to '/ep check'
    exilepearl.decay              Grants access to '/ep decay'
    exilepearl.exileany           Grants access to '/ep exileany'
    exilepearl.freeany            Grants access to '/ep freeany'
    exilepearl.list               Grants access to '/ep list'
    exilepearl.reload             Grants access to '/ep reload'
    exilepearl.sethealth          Grants access to '/ep sethealth'
    exilepearl.settype            Grants access to '/ep settype'
    exilepearl.setkiller          Grants access to '/ep setkiller'
    exilepearl.config             Grants access to '/ep config'
