import vg.civcraft.mc.civmodcore.ACivMod;

public class Dummy extends ACivMod{
	protected String getPluginName(){
		return "CivModCore";
	}
	@Override
	public void onLoad()
	{
		//Don't want it to load config
	}
	@Override
	public void onEnable()
	{
		//Don't want it to load commands/events
	}
}
