package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.properties.CompactorProperties;
import com.github.igotyou.FactoryMod.properties.IFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class Compactor extends ABaseFactory {

    private CompactorProperties cp;
    private CompactorMode mode;
    
   
    public Compactor(Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
                boolean active, CompactorProperties compactorProperties) {
        super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active, FactoryType.COMPACTOR, "Compactor");
        cp = compactorProperties;
        mode = CompactorMode.REPAIR;
    }
    
    public Compactor(Location factoryLocation,
            Location factoryInventoryLocation, Location factoryPowerSource,
            boolean active, CompactorProperties compactorProperties,
            CompactorMode mode, double currentRepair, long timeDisrepair) {
            super(factoryLocation, factoryInventoryLocation, factoryPowerSource, FactoryType.COMPACTOR, active,
                    "Compactor", 0, 0, currentRepair, timeDisrepair);
        cp = compactorProperties;
        this.mode = mode;
    }
    
    public boolean isRepairing() {
        return mode.equals(CompactorMode.REPAIR);
    }
    
    public ItemList<NamedItemStack> getFuel() {
        return cp.getFuel();
    }
    
    public double getEnergyTime() {
        return cp.getEnergyTime();
    }
    
    public double getProductionTime() {
		if (mode.equals(CompactorMode.REPAIR)) {
			return cp.getRepairTime();
		} else {
			return cp.getProductionTime();
		}
    }
    
    public ItemList<NamedItemStack> getInputs() {
        ItemList<NamedItemStack> inputs = new ItemList<NamedItemStack>();
        Inventory inv = getInventory();
        if(mode.equals(CompactorMode.DECOMPACT)) {
            for(ItemStack is : inv.getContents()) {
                if(canDecompact(is)) {
                	NamedItemStack clone = new NamedItemStack(is.clone(), is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString());
                	clone.setAmount(1);
                	inputs.add(clone);

                    return inputs;
                }
            }
        } else if (mode.equals(CompactorMode.COMPACT)) {
            for(ItemStack is : inv.getContents()) {
                if(canCompact(is)) {
                	NamedItemStack clone = new NamedItemStack(is.clone(), is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString());
                    inputs.add(clone); 

                    inputs.addAll(cp.getRecipeMaterials());
                    return inputs;
                }
            }
        }
        return inputs;
    }
    
    /**
     * Returns true is the item stack is not null, if the item is a full sized stack, with size
     * more than one (to prevent simply adding lore to single items), that the item does NOT have
     * lore, and that the item has simple metadata (not a book, fireworks, banners, beacons, other
     * "special" items that generate a host of edge cases like duplication and cheap dye and other
     * problems.)
     * 
     * @param is the ItemStack to check validity of compaction
     * @return true if can be compacted, false otherwise
     */
    private boolean canCompact(ItemStack is) {
    	return is != null && is.getAmount() == is.getMaxStackSize() && is.getAmount() > 1 && 
    			!is.getItemMeta().hasLore() && is.getItemMeta().getClass().getSuperclass().equals(java.lang.Object.class);
    	
    	/* bit of a hack at the end, but effectively only items with "simple" meta, where the implementation
    	 * is strictly a subclass of Object, and not a subclass of bukkit's CraftMetaItem. */    	
    }
    
    /**
     * Returns true if the item stack is not null, and has lore where the lore contains
     * the special lore of this factory, and where the item has simple metadata (see canCompact).
     * @param is the ItemStack to check validity of decompaction
     * @return true if can be decompacted, false otherwise
     */
    private boolean canDecompact(ItemStack is) {
    	return is != null && is.getItemMeta().hasLore() && 
    			is.getItemMeta().getLore().contains(cp.getCompactLore()) &&
    			is.getItemMeta().getClass().getSuperclass().equals(java.lang.Object.class);
    }
    
    public ItemList<NamedItemStack> getOutputs() {
        ItemList<NamedItemStack> outputs = new ItemList<NamedItemStack>();
        Inventory inv = getInventory();
        if (mode.equals( CompactorMode.DECOMPACT )) {
            for(ItemStack is : inv.getContents()) {
                if(canDecompact(is)) {
                	NamedItemStack clone = new NamedItemStack(is.clone(), is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString());
                	clone.setAmount(clone.getMaxStackSize());
                    ItemMeta cloneMeta = clone.getItemMeta();
                    cloneMeta.setLore(null);
                    clone.setItemMeta(cloneMeta);
                    outputs.add(clone); 
                	
                    return outputs;
                }
            }
        } else if (mode.equals( CompactorMode.COMPACT )) {
            for (ItemStack is : inv.getContents()) {
                if(canCompact(is)) {
                	NamedItemStack clone = new NamedItemStack(is.clone(), is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : is.getType().toString());
                	clone.setAmount(1);
                    ItemMeta cloneMeta = clone.getItemMeta();
                    List<String> lore = new ArrayList<String>();
                    lore.add(cp.getCompactLore());
                    cloneMeta.setLore(lore);
                    clone.setItemMeta(cloneMeta);
                    
                    outputs.add(clone);
                    return outputs;
                }
            }
        }
        return outputs;
    }
    
    public ItemList<NamedItemStack> getRepairs() {
		if (mode.equals( CompactorMode.REPAIR)) {
			return cp.getRepairMaterials();
		} else {
			return new ItemList<NamedItemStack>();
		}
    }
    
    public void update() {
        if(active) {
            if(checkHasMaterials()) {
                if(currentProductionTimer < getProductionTime()) {
                    if(isFuelAvailable()) {
                        if(currentEnergyTimer == getEnergyTime() - 1) {
                            getFuel().removeFrom(getPowerSourceInventory());
                            currentEnergyTimer = 0;
                            fuelConsumed();
                        } else {
                            currentEnergyTimer++;
                        }
                        currentProductionTimer++;
                        postUpdate();
                    } else {
                        powerOff();
                    }
                } else if(currentProductionTimer >= getProductionTime()) {
                	if (mode.equals(CompactorMode.REPAIR)) { 
                		repair(getRepairs().removeMaxFrom(getInventory(), (int)currentRepair));
                	} else if (mode.equals(CompactorMode.COMPACT) || mode.equals(CompactorMode.DECOMPACT)) {
                        // consumeInputs(); one or the other :(
                        
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
	public boolean checkHasMaterials() {
    	if (mode.equals(CompactorMode.REPAIR)) {
    		return getAllInputs().allIn(getInventory());
    	} else {
    		if (getInputs().isEmpty()) {
    			return false;
    		} else {
    			return true;
    		}
    	}
	}
    
    protected void recipeFinished() {
    	ItemList<NamedItemStack> output = getOutputs(); //.putIn(getInventory());
        getInputs().removeFrom(getInventory());
        output.putIn(getInventory());
        
    }
 
    public int getMaxRepair() {
        return cp.getRepair();
    }
    
    public List<InteractionResponse> getCentralBlockResponse() {
        List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
        if (!active) {
            CompactorMode mode = this.mode.getNext();
            this.mode = mode;
            responses.add(new InteractionResponse(InteractionResult.SUCCESS, "-----------------------------------------------------"));
            responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Switched recipe to " + this.mode.getDescription() + "."));
        } else {
            responses.add(new InteractionResponse(InteractionResult.FAILURE, "You can't change recipes while the factory is on! Turn it off first."));
        }
        return responses;
    }
    
    public List<InteractionResponse> getChestResponse(){
        List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
        String status=active ? "On" : "Off";
        int maxRepair = getMaxRepair();
        boolean maintenanceActive = maxRepair!=0;
        String response = "Current costs are : "; // the response specific to the mode.
        if (mode.equals(CompactorMode.REPAIR)){
            response += getRepairs().toString();
        } else if (mode.equals(CompactorMode.COMPACT) ) {
        	ItemList<NamedItemStack> inputs = getInputs();
        	response += (inputs.isEmpty() ? "Nothing to compact." : inputs.toString() );
        } else if (mode.equals(CompactorMode.DECOMPACT)){
        	ItemList<NamedItemStack> inputs = getInputs();
            response += (inputs.isEmpty() ? "Nothing to decompact." : inputs.toString() + " " + cp.getCompactLore() );
        }
        
        String percentDone=status.equals("On") ? " - "+Math.round(currentProductionTimer*100/getProductionTime())+"% done." : "";
        int health =(!maintenanceActive) ? 100 : (int) Math.round(100*(1-currentRepair/(maxRepair)));
        responses.add(new InteractionResponse(InteractionResult.SUCCESS, cp.getName()+": "+status+" with "+String.valueOf(health)+"% health."));
        responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Current mode: " + mode.getDescription()));
        responses.add(new InteractionResponse(InteractionResult.SUCCESS, response));
        responses.add(new InteractionResponse(InteractionResult.SUCCESS, percentDone));
        
        if(!getRepairs().isEmpty()&&maintenanceActive&& mode == CompactorMode.REPAIR)
        {
            int amountAvailable=getRepairs().amountAvailable(getInventory());
            int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
            int percentRepaired=(int) (( (double) amountRepaired)/maxRepair*100);
            responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+getRepairs().getMultiple(amountRepaired).toString()+"."));
        }
        
        return responses;
    }
    
    public enum CompactorMode {
        REPAIR(0, "Repair Factory"),
        COMPACT(1, "Compact Items"),
        DECOMPACT(2, "De-Compact Items");
        
        private static final int MAX_ID = 3;
        private int id;
        private String description;
        
        private CompactorMode(int id, String description) {
            this.id = id;
            this.description = description;
        }
        
        public static CompactorMode byId(int id) {
            for(CompactorMode mode : CompactorMode.values()) {
                if(mode.getId() == id) {
                    return mode;
                }
            }
            return null;
        }
        
        public int getId() {
            return id;
        }
        
        public String getDescription() {
            return description;
        }
        
        public CompactorMode getNext() {
            int nextId = (getId() + 1) % MAX_ID;
            return byId(nextId);
        }
    }
    
    public CompactorMode getMode() {
        return mode;
    }
    
    public IFactoryProperties getProperties() {
        return cp;
    }
}
