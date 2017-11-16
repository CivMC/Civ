package vg.civcraft.mc.citadel.misc;

public class LoadingCacheNullException extends RuntimeException{

	private static final long serialVersionUID = -1664671367153338518L;

	public LoadingCacheNullException(String message){
		super(message);
	}
	
	public LoadingCacheNullException(){
		super();
	}
}
