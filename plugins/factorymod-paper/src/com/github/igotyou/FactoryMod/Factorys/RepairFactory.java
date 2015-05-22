package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R2.ItemStack;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory.NetherOperationMode;
import com.github.igotyou.FactoryMod.managers.RepairFactoryManager;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class RepairFactory extends BaseFactory{

	private ReinforcementManager rm = Citadel.getReinforcementManager();
	private RepairFactoryProperties rfp;
	private RepairFactoryMode mode;
	
	// Constructor for when initial creation of factory.
	public RepairFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, RepairFactoryProperties repairFactoryProperties, RepairFactoryManager repairFactoryManager) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				FactoryType.REPAIR_FACTORY, "Repair Factory");
		this.rfp = repairFactoryProperties;
		this.mode = RepairFactoryMode.REPAIR;
	}
	
	public RepairFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, RepairFactoryProperties repairFactoryProperties, RepairFactoryManager repairFactoryManager,
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
	public ItemList<NamedItemStack> getFuel() {
		return rfp.getFuel();
	}
	
	@Override
	public void update()
	{
		if (mode == RepairFactoryMode.REPAIR){
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
							}
							else {
								currentEnergyTimer++;
							}
							//increment the production timer
							currentProductionTimer ++;
							postUpdate();
						}
						//if there is no fuel Available turn off the factory
						else {
							powerOff();
						}
					}
					//if the production timer has reached the recipes production time remove input from chest, and add output material
					else if (currentProductionTimer >= getProductionTime()){
						//Repairs the factory
						repair(getRepairs().removeMaxFrom(getInventory(),(int)currentRepair));
						
						currentProductionTimer = 0;
						powerOff();
					}
				}
				else {
					powerOff();
				}
			}
		}
		else if (mode == RepairFactoryMode.RESET_ITEMS) {
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
							}
							else {
								currentEnergyTimer++;
							}
							//increment the production timer
							currentProductionTimer ++;
							postUpdate();
						}
						//if there is no fuel Available turn off the factory
						else {
							powerOff();
						}
					}
					//if the production timer has reached the recipes production time remove input from chest, and add output material
					else if (currentProductionTimer >= getProductionTime()){
						consumeInputs(); // consumes the items needed.
						
						// Sets all the items to the miniumum needed repair cost.
						recipeFinished();
						
						currentProductionTimer = 0;
						powerOff();
					}
				}
				else {
					powerOff();
				}
			}
		}
	}

	@Override
	public double getEnergyTime() {
		return rfp.getEnergyTime();
	}

	@Override
	public double getProductionTime() {
		return rfp.getProductionTime();
	}

	@Override
	public ItemList<NamedItemStack> getInputs() {
		return rfp.getRecipeMaterials();
	}

	@Override
	public ItemList<NamedItemStack> getOutputs() {
		return new ItemList<NamedItemStack>();
	}

	@Override
	public ItemList<NamedItemStack> getRepairs() {
		return rfp.getRepairMaterials();
	}

	@Override
	protected void recipeFinished() {
		getInputs().removeOneFrom(getInventory());
		org.bukkit.inventory.ItemStack[] contents = getInventory().getContents();
		for (int x = 0; x < contents.length; x++){
			org.bukkit.inventory.ItemStack stack = contents[x];
			if (stack == null)
				continue;
			ItemStack s = CraftItemStack.asNMSCopy(stack);
			s.setRepairCost(1);
			getInventory().setItem(x, CraftItemStack.asBukkitCopy(s));
		}
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
		double time = 0;
		int maxRepair = getMaxRepair();
		boolean maintenanceActive = maxRepair!=0;
		String response = "Current costs are : "; // the response specific to the mode.
		if (mode.getId() == 0){
			time = getEnergyTime();
			response += getRepairs().toString();
		}
		else if (mode.getId() == 1){
			time = getProductionTime();
			response += getInputs().toString();
		}
		String percentDone=status.equals("On") ? " - "+Math.round(currentProductionTimer*100/time)+"% done." : "";
		int health =(!maintenanceActive) ? 100 : (int) Math.round(100*(1-currentRepair/(maxRepair)));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, rfp.getName()+": "+status+" with "+String.valueOf(health)+"% health."));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Current mode: " + mode.getDescription()));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, response));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, percentDone));
		
		if(!getRepairs().isEmpty()&&maintenanceActive)
		{
			int amountAvailable=getRepairs().amountAvailable(getInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/maxRepair*100);
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
	
	public RepairFactoryProperties getProperties(){
		return rfp;
	}
}
