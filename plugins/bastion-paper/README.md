Bastion
=======
A minecraft mod designed for use with [/r/civcraft](https://old.reddit.com/r/Civcraft/) and continually improved by [/r/devoted](https://old.reddit.com/r/devoted/). Currently in use on [/r/civclassics](https://old.reddit.com/r/Civclassics/) at mc.civclassic.com (1.16.5)

For a more extensive and updated list of features see https://github.com/CivClassic/Bastion/wiki

Features:
 * Destroys blocks placed in a cylinder or cuboid of configurable radius starting at or just above a reinforced target block (Bastion block) at the price of a configurable amount of reinforcement if the placer is not a member of the reinforcing group
 * Prevents dispensers dispensing water, lava, and flint and steel if the owner of there reinforcement is not allowed to place blocks
 * Prevents pistons pushing into the Bastion field (Area where block placement is restricted) if the owner of the reinforcement would not be allowed to place there.
 * Prevents teleportation through the the Bastion field by non-members (Note this is highly experimental and may be better disabled)
 * Multiple Bastion types with unique properties

Modes:
* INFO
  * When clicking a block inside a Bastion field tells you if you have access
  * When clicking a bastion block gives some basic information on it
* DELETE
  * When a Bastion block is clicked remove the bastion field while maintaing the reinforcement
* NORMAl
  * Defualt clicking on blocks does nothing
* BASTION
  * Any target block reinforced will become a Bastion Block
* CREATE
  * Any reinforced target block clicked will becomea  Bastion if you have access


Commands:
 * /bsi Puts the player into info mode
 * /bsd Puts the player into delete mode
 * /bso Puts the player into normal mode
 * /bsc puts the player into create mode
 * /bsb puts the player into bastion mode

Maturity:
 * When a bastion is first created it starts much weaker
 * While it matures any blocks placed within the field do much more damage
 * Until mature ender pearls are not blocked.
 * INFO mode gives some information on the time till maturity

Use:
 * Place the "target block" and reinforce it
 * Enter /bsb and click the block
 * A bastion block has been created


To install:
  * Add Bastion.jar to the server /plugins directory
  * Confirm that Citadel is also installed
  * Launch the server or copy the default configuration file
  * Create a Mysql database and account for the plugin
  * Specify in the configuration file
  * Relaunch the server

Permissions:
 *  Bastion.normal
   * Ability to use basic set of commands
 * Bastion.admin
   * Gives access to /bsm to mature a bastion instantly
   * sets Bastion.bypass to true
 * Bastion.bypass
   * Bastions should not effect you (not well tested)
 * Bastion.dev
   * /bsi gives extra info 

To compile:
 * Download Paper
 * Download Citadel
 * Download Namelayer
 * Download CivModCore
 * Link to Paper, Citadel, Namelayer, and CivModCore then compile
