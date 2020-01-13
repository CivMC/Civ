CivModCore
===========

Versions:

* 1.7.8 - Spigot 1.14.4 (No explicit support for 1.14 prior to 1.14.4)

* 1.7.0 - Spigot 1.13.2 (No explicit support for 1.13 or 1.13.1)

* 1.6.1 - Spigot 1.12 (Mercury Removed -- incompatible with plugins that rely on Mercury hooks)

* 1.6.0 - Spigot 1.12

* 1.5.10 - Spigot 1.11 or higher.

* 1.5.9 - Spigot 1.10 / 1.10.x

No explicit backwards support is offered.

-------

Common Plugin Core derived from Humbug

To use CivModCore, your Main Plugin class must extend ACivMod:

    public class MyNewPlugin extends ACivMod
    {
    
    }
    
CivModCore implements onEnable/onLoad, and as such an extending plugin must Override and call super:

https://github.com/DevotedMC/CivModCore/blob/master/src/main/java/vg/civcraft/mc/civmodcore/ACivMod.java#L34

    @Override
    public void onEnable()
    {
        super.onEnable();
        //Do your stuff here that you need to do.
    }
