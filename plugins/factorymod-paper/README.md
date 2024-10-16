# FactoryMod 

FactoryMod is a Minecraft plugin with specialized crafting machines that can consume and produce any goods. From this basis FactoryMod's flexible configurability means admins can drastically improve upon a vanilla Minecraft economy: 

* Tech trees with precise control over depth and difficulty
* Encouraging cooperation through permanence of capital
* Or simply a way for players to obtain survival unobtainable items

FactoryMod is currently running on [r/civclassics](https://old.reddit.com/r/civclassics/) at mc.civclassic.com (1.16.5)

---
Any number of factories can be configured, each with unique purposes, and upgradable to other factories. Factories are a static structure created by placing a furnace, crafting bench, and chest together. The ingredients and outputs for a recipe are placed inside the chest. Clicking on the crafting table with a stick allows for selection of a recipe while clicking on the furnace with a stick runs the factory. Before a factory can be run it must be created : the setup cost of goods is deposited in the chest. Factories have a configurable repair cost, which essentially works as an upkeep cost. 

The diamond pick factory in the Civclassic config is an example of a simple factory, that for input diamonds creates diamond picks at a 1/3 vanilla cost.

    diamond_pick:
      type: FCC
      name: Diamond Pickaxe Smith
      citadelBreakReduction: 1.0
      setupcost:
        diamond:
          material: DIAMOND
          amount: 96
      recipes:
       - make_diamond_pick
       - repair_diamond_pick_factory

Lower in the config file each recipe is defined:

    make_diamond_pick:
      production_time: 4s
      name: Make Diamond Pick
      type: PRODUCTION
      input:
        diamond:
          material: DIAMOND
          amount: 15
      output:
        diamond_pick:
          material: DIAMOND_PICKAXE
          amount: 15
          
Factories can do more than increase yield. The preset compactor factory turns a stack of items into a single lored item or visa versa. This gives another method for transporting bulk goods. There are many more configurable options, such as how much of startup cost factories return when broken. For a more comprehensive (WIP) users guide see [Civclassic Wiki](https://civclassic.miraheze.org/wiki/Comprehensive_Guide#Factorymod)

---

Parts of this README adapted from earlier guides such as [Civcraft FactoryMod](https://github.com/civcraft/FactoryMod/wiki)
