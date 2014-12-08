package vg.civcraft.mc.citadel.misc;

public class LoadingCacheNullException extends RuntimeException{

	public LoadingCacheNullException(String message){
		super(message);
	}
	
	public LoadingCacheNullException(){
		super();
	}
}
