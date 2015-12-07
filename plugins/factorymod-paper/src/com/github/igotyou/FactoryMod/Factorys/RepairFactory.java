package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.server.v1_8_R3.ItemStack;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import com.github.igotyou.FactoryMod.properties.IFactoryProperties;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class RepairFactory extends ABaseFactory{

	private RepairFactoryProperties rfp;
	private RepairFactoryMode mode;
	
	private static Logger log = Logger.getLogger(RepairFactory.class.getName());
	
	// Constructor for when initial creation of factory.
	public RepairFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, RepairFactoryProperties repairFactoryProperties) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				FactoryType.REPAIR_FACTORY, "Repair Factory");
		this.rfp = repairFactoryProperties;
		this.mode = RepairFactoryMode.REPAIR;
	}
	
	public RepairFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, RepairFactoryProperties repairFactoryProperties, 
			RepairFactoryMode mode, double currentRepair, long timeDisrepair) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, FactoryType.REPAIR_FACTORY, active,
				"Repair Factory", 0, 0, currentRepair, timeDisrepair);
		this.rfp = repairFactoryProperties;
		this.mode = mode;
	}
	
	@Override
	public boolean isRepairing() {
		return mode == RepairFactoryMode.REPAIR;
	}

	@Override
	public ItemList<AdvancedItemStack> getFuel() {
		return rfp.getFuel();
	}
	
	@Override
	public void update()
	{
		//if factory is turned on
		if (active){
			//if the materials required to produce the current recipe are in the factory inventory
			if (checkHasMaterials()){
				//if the factory has been working for less than the required time for the recipe
				if (currentProductionTimer < getProductionTime()){
					//if the factory power source inventory has enough fuel for at least 1 energyCycle
					if (isFuelAvailable()){
						//if the time since fuel was last consumed is equal to how often fuel needs to be consumed
						if (currentEnergyTimer == getEnergyTime()-1){
							//remove one fuel.
							getFuel().removeFrom(getPowerSourceInventory());
							//0 seconds since last fuel consumption
							currentEnergyTimer = 0;
							fuelConsumed();
						} else {
							currentEnergyTimer++;
						}
						//increment the production timer
						currentProductionTimer ++;
						postUpdate();
					} else { //if there is no fuel Available turn off the factory
						powerOff();
					}
				} else if (currentProductionTimer >= getProductionTime()){
					//if the production timer has reached the recipes production time remove input from chest, and add output material

					if (mode == RepairFactoryMode.REPAIR) {
						//Repairs the factory
						repair(getRepairs().removeMaxFrom(getInventory(),(int)currentRepair));
					} else if (mode == RepairFactoryMode.RESET_ITEMS) {
						consumeInputs(); // consumes the items needed.
						
						// Sets all the items to the miniumum needed repair cost.
						recipeFinished();
					}
					
					currentProductionTimer = 0;
					currentEnergyTimer = 0;
					powerOff();
				}
			} else {
				powerOff();
			}
		}
	}
	
	@Override
	public List<InteractionResponse> togglePower(){
		log.info("Repair factory at " + this.factoryLocation.toString() + " power toggle attempt");
		List<InteractionResponse> response=new ArrayList<InteractionResponse>();
		//if the factory is turned off
		if (!active) {
			//if the factory isn't broken or the current recipe can repair it
			if ( !isBroken() || isRepairing() ) {
				//is there fuel enough for at least once energy cycle?
				if ( isFuelAvailable() ) {
					//are there enough materials for the current recipe in the chest?
					if ( checkHasMaterials() ) {
						//turn the factory on
						powerOn();
						//return a success message
						response.add(new InteractionResponse(InteractionResult.SUCCESS, "Factory activated!"));
						return response;
					} else { //there are not enough materials for the recipe!
						//return a failure message, containing which materials are needed for the recipe
						//[Requires the following: Amount Name, Amount Name.]
						//[Requires one of the following: Amount Name, Amount Name.]
						
						ItemList<AdvancedItemStack> needAll=new ItemList<AdvancedItemStack>();
						ItemList<AdvancedItemStack> allInputs = null;
						
						if (mode.equals(RepairFactoryMode.REPAIR)){
							allInputs = rfp.getRepairMaterials();
						}
						else if (mode.equals(RepairFactoryMode.RESET_ITEMS)){
							allInputs = rfp.getRecipeMaterials();
						}
						
						needAll.addAll(allInputs.getDifference(getInventory()));
						if(!needAll.isEmpty())
						{
							response.add(new InteractionResponse(InteractionResult.FAILURE,"You need all of the following: "+needAll.toString()+"."));
						} else if (allInputs == null || allInputs.isEmpty()) {
							System.out.println("getAllInputs() returned null or empty; recipe is returning no expectation of input!");
						}
						return response;
					}
				} else { //if there isn't enough fuel for at least one energy cycle
					//return a error message
					int multiplesRequired=(int)Math.ceil(getProductionTime()/(double)getEnergyTime());
					response.add(new InteractionResponse(InteractionResult.FAILURE, "Factory is missing fuel! ("+getFuel().getMultiple(multiplesRequired).toString()+")"));
					return response;
				}
			} else {
				response.add(new InteractionResponse(InteractionResult.FAILURE, "Factory is in disrepair!"));
				return response;
			}			
		} else { //if the factory is on already
			//turn the factory off
			powerOff();
			//return success message
			response.add(new InteractionResponse(InteractionResult.FAILURE, "Factory has been deactivated!"));
			return response;
		}
	}

	@Override
	public double getEnergyTime() {
		return rfp.getEnergyTime();
	}

	@Override
	public double getProductionTime() {
		if (mode.equals(RepairFactoryMode.REPAIR)) {
			return rfp.getRepairTime();
		} else {
			return rfp.getProductionTime();
		}
	}

	@Override
	public ItemList<AdvancedItemStack> getInputs() {
		if (mode.equals(RepairFactoryMode.RESET_ITEMS)) {
			return rfp.getRecipeMaterials();
		} else {
			return new ItemList<AdvancedItemStack>();
		}
	}

	@Override
	public ItemList<AdvancedItemStack> getOutputs() {
		return new ItemList<AdvancedItemStack>();
	}

	@Override
	public ItemList<AdvancedItemStack> getRepairs() {
		if (mode.equals( RepairFactoryMode.REPAIR)) {
			return rfp.getRepairMaterials();
		} else {
			return new ItemList<AdvancedItemStack>();
		}
	}

	@Override
	protected void recipeFinished() {
		log.finer("Recipe Finished");
		//getInputs().removeOneFrom(getInventory()); // Don't double dip! Parent consume already ate recipe costs.
		org.bukkit.inventory.ItemStack[] contents = getInventory().getContents();
		for (int x = 0; x < contents.length; x++){
			org.bukkit.inventory.ItemStack stack = contents[x];
			if (stack == null) {
				continue;
			}
			if (repairable(stack)) {
				log.fine("Found repairable: " + stack.getType());
				ItemStack s = CraftItemStack.asNMSCopy(stack);
				s.setRepairCost(rfp.getResetLevel());
				getInventory().setItem(x, CraftItemStack.asBukkitCopy(s));
			} else {
				log.fine("Found non-repairable: " + stack.getType());
			}
		}
	}

	private boolean repairable(org.bukkit.inventory.ItemStack stack) {
		return rfp.getAllowedRepairable().contains(stack.getType());		
	}
	
	@Override
	public int getMaxRepair() {
		return rfp.getRepair();
	}

	@Override
	public List<InteractionResponse> getCentralBlockResponse() {
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		if (!active) {
			RepairFactoryMode mode = this.mode.getNext();
			this.mode = mode;
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "-----------------------------------------------------"));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Switched recipe to " + this.mode.getDescription() + "."));
		} else {
			responses.add(new InteractionResponse(InteractionResult.FAILURE, "You can't change recipes while the factory is on! Turn it off first."));
		}
		return responses;
	}
	
	@Override
	public List<InteractionResponse> getChestResponse(){
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		String status=active ? "On" : "Off";
		//double time = 0;
		int maxRepair = getMaxRepair();
		boolean maintenanceActive = maxRepair!=0;
		String response = "Current costs are : "; // the response specific to the mode.
		if (mode.equals(RepairFactoryMode.REPAIR)){
			response += getRepairs().toString();
		} else if (mode.equals(RepairFactoryMode.RESET_ITEMS)){
			response += getInputs().toString();
		}
		
		String percentDone=status.equals("On") ? " - "+Math.round(currentProductionTimer*100/getProductionTime())+"% done." : "";
		int health =(!maintenanceActive) ? 100 : (int) Math.round(100*(1-currentRepair/(maxRepair)));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, rfp.getName()+": "+status+" with "+String.valueOf(health)+"% health."));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Current mode: " + mode.getDescription()));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, response));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, percentDone));
		
		if(!getRepairs().isEmpty()&&maintenanceActive&& mode == RepairFactoryMode.REPAIR)
		{
			// amountAvailable() is pretty broken, so addressing it in config for now.
			int amountAvailable= getRepairs().amountAvailable(getInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/maxRepair*100);
			log.finer(String.format("Repair mode: available %d repaired %d maxrepair %d percentRepaired %d", amountAvailable, amountRepaired, maxRepair, percentRepaired));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+getRepairs().getMultiple(amountRepaired).toString()+"."));
		}
		
		return responses;
	}
	
	public enum RepairFactoryMode {
		REPAIR(0, "Repair Factory"),
		RESET_ITEMS(1, "Reset Items");
		
		private static final int MAX_ID = 2;
		private int id;
		private String description;
		
		private RepairFactoryMode(int id, String description){
			this.id = id;
			this.description = description;
		}
		
		public static RepairFactoryMode byId(int id){
			for (RepairFactoryMode mode: RepairFactoryMode.values())
				if (mode.getId() == id)
					return mode;
			return null;
		}
		
		public String getDescription() {
			return description;
		}
		
		public int getId() {
			return id;
		}
		
		public RepairFactoryMode getNext(){
			int nextId = (getId() + 1) % MAX_ID;
			return byId(nextId);
		}
	}
	
	public RepairFactoryMode getMode(){
		return mode;
	}
	
	public IFactoryProperties getProperties(){
		return rfp;
	}
}
