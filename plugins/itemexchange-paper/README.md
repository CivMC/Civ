#Item Exchange
 
Item Exchange is a Minecraft server mod that allows players to set up chests and dispensers as shops. It is intended to be used in conjunction with Citadel to lock the container that is being used this way. With a few exceptions for technical reasons, any item can be set up to be traded for any item. A single shop can offer multiple exchanges, for customers to choose between.
 
Chests, double chests, trapped chests, double trapped chests and dispensers can all be used as shops.
 
**[Video tutorial of the basics](http://www.youtube.com/watch?v=uLIy3UlvAz0)**
 
##Using a shop as a customer
 
To see the input and output of the first exchange a chest or dispenser offers, just left click it. If the shop offers more exchanges (it will say so), you can click it with an empty hand to cycle through them. To complete a trade, cycle to the exchange you want, then left click the shop with the input item in hand
 
![Image showing the message a shop displays when you left click it.](http://i.imgur.com/gP4Rjfa.png)
 
##Creating a shop
 
An Item Exchange shop is any chest or dispenser with exchange rules inside. Exchange rules consist of stone buttons that Item Exchange has attached extra information to. Anyone can use Item Exchange to generate these buttons, to make their own shop with.
 
An exchange rule shows (almost) all of its information in its tooltip. To increase usability, the English displayed name is used in place of the Bukkit material name and durability value, wherever possible.
 
A single exchange rule is either an input rule, defining items that the shop accepts, or an output rule, defining items that the shop gives in return. A single exchange consists of one input rule and one output rule. These are paired by the order that they're held in the shop's inventory; the first input rule is matched to the first output rule and so on.
 
![Image showing an input exchange rule tooltip in a shop inventory.](http://i.imgur.com/rC77hfy.png)
![Image showing an output exchange rule tooltip in a shop inventory.](http://i.imgur.com/hPzVh9n.png)
 
The stone button items that represent the exchange rules are created using the **/iecreate** (or **/iec**) command. There are three ways of doing this.
 
- Look at a chest or dispenser containing two different types of items (it's okay if they're spread across multiple stacks), and use **/iec**. This will create an input rule matching the first item type and its quantity, and an output rule matching the second item type and its quantity. Both are placed inside the container, so this shop is immediately ready to be stocked and used.
- Hold an item in your hand, then use **`/iec <input or output>`**. This will create an input or output rule matching the held item, and its quantity
- Use **`/iec <input or output> <common name or ID:durability> [amount]`**. This will create an input or output rule for the specified item, and optionally the specified amount.
 
For the second and third options, you need to make sure you have both an input and an output exchange rule, then place them in a suitable container to form an exchange. By adding more input and output rules, you can add more exchanges to your shop, but keep in mind how they are paired.
 
Remember to reinforce your shop!
 
##Editing an exchange rule
 
An existing exchange rule can be edited using the **/ieset** (or **/ies**) command. This is the only way to access some advanced features. Using this command will edit the exchange rule held in hand.
 
- **/ies commonname** (or **c**) **<<i>common name</i>>** changes the item in the exchange rule to the one specified by its common name.
- **/ies material** (or **m**) **<<i>common name</i> or <i>ID</i>>** changes the item in the exchange rule to the one specified by its common name or ID, without changing the durability.
- **/ies durability** (or **d**) **<<i>number</i>>** changes the durability of the item exchanged.
- **/ies amount** (or **a**) **<<i>number</i>>** changes the amount of the item exchanged.
- **/ies enchantment** (or **e**) **<<i>enchantment rule</i>>** adds an enchantment restriction to the exchange rule.
  Enchantment restrictions have the format of **<+/?/-><<i>enchantment</i>><<i>level</i>>**. **+** makes the enchantment required for the item, **-** makes it forbidden, and **?** removes the restriction. **<i>level</i>** specifies which level the enchantment should be. **<i>enchantment</i>** needs to be one of the following case-sensitive codes:
  - **E** for Efficiency
  - **U** for Unbreaking
  - **F** for Fortune
  - **ST** for Silk Touch
  - **S** for Sharpness
  - **Sm** for Smite
  - **BoA** for Bane of Arthropods
  - **K** for Knockback
  - **FA** for Fire Aspect
  - **L** for Looting
  - **Po** for Power
  - **Pu** for Punch
  - **Fl** for Flame
  - **I** for Infinity
  - **P** for Protection
  - **FP** for Fire Protection
  - **BP** for Blast Protection
  - **PP** for Projectile Protection
  - **FF** for Feather Fall
  - **T** for Thorns
  - **R** for Respiration
  - **AA** for Aqua Affinity
 
  For example, **/ies e +P5**, followed by **/ies e -T1** would specify the item needs to have Protection 5, but is not allowed to have Thorns 1. Note that if the exchange rule is set to disallow all enchantments not explicitly required (see below), forbidding an enchantment is redundant.
- **/ies allowenchantments** sets the exchange to allow all enchantments not explicitly specified by **/ies enchantment**.
- **/ies denyenchantments** sets the exchange to disallow all enchantments not explicitly specified by **/ies enchantment**. This is the default setting for newly created rules.
- **/ies displayname** (or **n**) **[<i>name</i>]** sets the display name of the item in the exchange rule. If used without a name, it only matches items with no display name. A display name is a name given to an item by an anvil.
- **/ies lore** (or **l**) **[<i>lore</i>]** sets the lore of the item in the exchange rule. If used without any lore specified, it only matches items with no lore. Multiple lines of lore can be entered by placing semicolons (;) in place of line breaks.
- **/ies switchio** (or **s**) toggles the exchange rule between input and output.
- **/ies group** (or **g**) **[<i>group name</i>]** restricts an exchange to members of the specified Citadel group. If no group name is given, it removes the group restriction. This only works on input rules.
 
The amount of an item specified in an exchange rule can also be increased or decreased by shift left clicking or shift right clicking on the exchange rule respectively.
 
##Bulk exchange rules
 
It is possible to merge multiple exchange rules into one item by putting them in a crafting grid together. This will create a bulk exchange rule. Bulk exchange rules can have more exchange rules merged into them, including other bulk exchange rules. This means any number of exchanges can be stored in a single item, which in turn allows a shop to hold any number of exchanges while only using one inventory slot.
 
The order of all exchange rules inside a bulk exchange rule is determined by how the components were placed on the crafting grid. When merging bulk exchange rules, one is essentially appended to the other.
 
When forming input and output pairs, all exchange rules in the shop are considered, regardless of whether the rule is part of a bulk exchange rule or not. The first input rule is matched to the first output rule, the second to the second and so on, even if they are not in the same bulk exchange rule.
 
As an example of that, a chest containing only an input rule for 1 stone, and a bulk rule that contains an input rule for 1 wood and an output rule for 1 charcoal, in that order, will have an exchange selling charcoal for stone. The wood input rule cannot be matched to anything, and won't be a part of any exchange.
 
When a bulk exchange rule is dropped onto the ground, it splits into its component exchange rules.
 
##Redstone support
If there is a button attached to the block behind the chest, it will emit a redstone signal on a successful transaction. This currently only works for chests.
![Image showing a button on a block behind a chest.](http://imgur.com/OQaoaVu.png)
![Another image showing a button on a block behind a chest.](http://imgur.com/nGnu83v.png)
