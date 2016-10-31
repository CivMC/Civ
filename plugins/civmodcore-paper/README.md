CivModCore
===========

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
