---
title: Item Exchange
description: Item exchange plugin
---
An Item Exchange is a mechanic that allows users to trade items at predefined rates at a chest.

### Mechanics
Shop chests are created when they contain "Exchange Rules" (encoded buttons which specify the rulse of an exchange). Exchanges are made of pairs of inputs and outputs (though an output rule is not strictly necessary and a "donation" item exchange uses only an input rule). Bulk exchange rules are a single encoded button which contains within it one or more pairs of Exchange Rules. You can create a bulk exchange rule by placing an input and output button within a crafting grid. You can add bulk exchange buttons to other bulk exchange buttons to save space in your shop chest.

Players can view and cycle through a shop's available exchanges by punching a shop chest, preferably with an empty hand. If the player then punches the chest on a given exchange with a matching input, if the shop chest is stocked and has space available, then the exchange is transacted. 

Shops can, by default, be made with chests, trapped chests, barrels, dispensers, and droppers. You can check which containers are supported using the `/ieinfo shopblocks` command.

![ShopBlocks SS](https://github.com/user-attachments/assets/0c4830ae-0af9-4b37-be15-2243dbcbd2f7)

ItemExchange differs from other market and trade plugins in that it functions on a series of rules and criteria, rather than selling specific items. An Exchange Rule that ***ONLY*** specifies that the output is a Diamond Pickaxe will be able to output ***ANY*** Diamond Pickaxe, enchanted or not, used or not, repaired or not, named or not, etc. 

### Creating an Exchange Rule
There are three ways to create an Exchange Rule, each via the `/iecreate` command (`/iec`):

- `/iereate <input/output>` while holding an item in your hand will create a rule with its criteria as close to matching that item as possible.
- `/iecreate <input/output> <material> [amount]` will create a barebones rule of just the material and the amount, which defaults to 1 if not specified. See [Paper's Material list](https://jd.papermc.io/paper/1.21.5/org/bukkit/Material.html) for reference.
- `/iecreate` while looking at a chest (or other supported container) containing one or two stacks will add a corresponding input and output rule for those items. If the container is reinforced, you must have the required permissions for this command to function.

### Modifying an Exchange Rule

Exchange Rules can be modified using the `/ieset` command (`/ies`) while holding the button:
- `/ieset material <material>` will update the material. See [Paper's Material list](https://jd.papermc.io/paper/1.21.5/org/bukkit/Material.html) for reference.
- `/ieset amount <amount>` will update the amount, which must be higher than zero.
- `/ieset switchio` will filp the exchange rule from an input to an output, and vice versa.

Exchange Rules can also contain *modifiers*, which are additional bits of critieria that can increase the specificity of an exchange:

- ```/ieset durability [damage]```  
  Updates how damaged the item should be.  
  - Not specifying the damage (or inputting `ANY`) will ignore durability.
  - Inputting `USED` will only accept damaged items.

- ```/ieset repair [level]```  
  Sets the repair level of the item.  
  - Not specifying the level ignores it.
  - Prefixing with `@` (e.g., `@9`) accepts only exactly level 9.
  - Using just `9` accepts level 9 or lower.

- ```/ieset ignoreenchants```  
  Ignores all enchantments.

- ```/ieset allowenchants```  
  Ignores unspecified enchantments.

- ```/ieset denyenchants```  
  Ensures only specified enchants are allowed.

- ```/ieset enchant <+/?/-><enchantment>[level]```  
  Reset enchantment rules.  
  - Example: `/ieset enchant +DURABILITY3` adds a required enchant.
  - Example: `/ieset enchant -DURABILITY` excludes it.
  - Example: `/ieset enchant ?DURABILITY` resets the rule.  
  See [Paper's Enchantment List](https://jd.papermc.io/paper/1.21.5/org/bukkit/enchantments/Enchantment.html) for valid enchant names.

- ```/ieset ignoredisplayname```  
  Ignores item display names.

- ```/ieset displayname [name]```  
  Sets required display name. Omitting name ignores display names.

- ```/ieset ignorelore```  
  Ignores item lore.

- ```/ieset lore [lore]```  
  Sets required lore.  
  Use `;` to separate lines, e.g.  
  ```/ieset lore First Line;Second Line```

- ```/ieset group [group]```  
  Sets a [NameLayer](https://civmc.net/wiki/plugins/essential/namelayer.html) group requirement (only for input rules).  
  Omitting the group disables the requirement.

### Relays

Ender chests are used as ***relays***, which when clicked search for shop chests within 4 blocks and return their contents. The ender chest needs to connected to the shop chest with transparent blocks. They can be used to add a moderate layer of protection to shop chests selling valuable goods, by putting distance between the shop chest, itself andt he relay, where other players interact with it.


![RelayChest](https://github.com/user-attachments/assets/ec4b0a76-2160-4850-b283-6047fb781194)
