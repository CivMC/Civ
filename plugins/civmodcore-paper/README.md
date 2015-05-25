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
    
Then it is just a matter of adding CivConfigs and CivConfig Decorators on PUBLIC methods:

Types available are defined in vg.civcraft.mc.civmodcore.annotations.CivConfigType:

    public enum CivConfigType {
	     Bool,
	     Double,
	     Int,
	     String
    }
    
For Example, if you want to toggle a functionality on/off:

    @CivConfig(name="mycapability", def="false", type = CivConfigType.Bool)
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (config_.get("mycapability").getBool())
        {
        
        }
    }
    
since def="false", then by DEFAULT this capability will be disabled.

You can also have a group of CivConfig items on a single method:

    @CivConfigs({
	     @CivConfig(name="mycapability",      def="false", type = CivConfigType.Bool),
	     @CivConfig(name="cooldown",          def="3000",  type = CivConfigType.Int),
	     @CivConfig(name="mycapability2",     def="true",  type = CivConfigType.Bool),
	     @CivConfig(name="damage_bonus",      def="0.66",  type = CivConfigType.Double),
	     @CivConfig(name="mycapability3",     def="true",  type = CivConfigType.Bool),
    	  @CivConfig(name="damage_multiplier", def="1.5",   type = CivConfigType.Double)
    })
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event)
    {
        if (config_.get("mycapability").getBool())
        {
        		//Make some cooldown
        }
        
        if (config_.get("mycapability2").getBool())
        {
            double newDamage = event.getDamage() + config_.get("damage_bonus").getDouble();
            event.setDamage(newDamage);
        }
        
        if (config_.get("mycapability3").getBool())
        {
            double newDamage = event.getDamage() * config_.get("damage_multiplier").getDouble();
            event.setDamage(newDamage);
        }
    }
