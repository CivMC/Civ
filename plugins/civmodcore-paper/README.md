CivModCore
===========

Versions:

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

In addition to that, you must override getPluginName for log messages:

    protected String getPluginName() {
        return "MyPluginsName";
    }
    
CivModCore implements onEnable/onLoad, and as such an extending plugin must Override and call super:

https://github.com/Bergecraft/CivModCore/blob/master/src/vg/civcraft/mc/civmodcore/ACivMod.java#L108

    @Override
    public void onEnable()
    {
        super.onEnable();
        //Do your stuff here that you need to do.
    }
