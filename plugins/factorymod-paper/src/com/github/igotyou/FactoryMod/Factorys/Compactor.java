package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.properties.CompactorProperties;
import com.github.igotyou.FactoryMod.properties.IFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class Compactor extends ABaseFactory {

    private ReinforcementManager rm = Citadel.getReinforcementManager();
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
        return mode == CompactorMode.REPAIR;
    }
    
    public ItemList<NamedItemStack> getFuel() {
        return cp.getFuel();
    }
    
    public double getEnergyTime() {
        return cp.getEnergyTime();
    }
    
    public double getProductionTime() {
        return cp.getProductionTime();
    }
    
    public ItemList<NamedItemStack> getInputs() {
        ItemList<NamedItemStack> inputs = new ItemList<NamedItemStack>();
        Inventory inv = getInventory();
        if(mode == CompactorMode.DECOMPACT) {
            for(ItemStack is : inv.getContents()) {
                if(is.getItemMeta().hasLore() && is.getItemMeta().getLore().contains(cp.getCompactLore())) {
                    inputs.add(new NamedItemStack(is.getType(), is.getMaxStackSize(), is.getDurability(), is.getItemMeta().getDisplayName()));
                    return inputs;
                }
            }
            inputs.add(new NamedItemStack(Material.AIR, 1, (short)1, "Compacted Item"));
        } else if (mode == CompactorMode.COMPACT) {
            for(ItemStack is : inv.getContents()) {
                if(is.getAmount() == is.getMaxStackSize()) {
                    if(!is.getItemMeta().hasLore()) {
                        inputs.add(new NamedItemStack(is.getType(), is.getAmount(), is.getDurability(), is.getItemMeta().getDisplayName()));
                        inputs.addAll(cp.getRecipeMaterials());
                        return inputs;
                    }
                }
            }
            return cp.getRecipeMaterials();
        }
        return inputs;
    }
    
    public ItemList<NamedItemStack> getOutputs() {
        ItemList<NamedItemStack> outputs = new ItemList<NamedItemStack>();
        Inventory inv = getInventory();
        if(mode == CompactorMode.DECOMPACT) {
            for(ItemStack is : inv.getContents()) {
                if(is.getItemMeta().hasLore() && is.getItemMeta().getLore().contains(cp.getCompactLore())) {
                    outputs.add(new NamedItemStack(is.getType(), is.getMaxStackSize(), is.getDurability(), is.getItemMeta().getDisplayName()));
                    return outputs;
                }
            }
            outputs.add(new NamedItemStack(Material.AIR, 64, (short)1, "Decompacted Items"));
        } else if (mode == CompactorMode.COMPACT) {
            for(ItemStack is : inv.getContents()) {
                if(is.getAmount() == is.getMaxStackSize()) {
                    NamedItemStack compactedItemStack = new NamedItemStack(is.getType(), 1, is.getDurability(), is.getItemMeta().getDisplayName());
                    compactedItemStack.getItemMeta().getLore().add(cp.getCompactLore());
                    outputs.add(compactedItemStack);
                    return outputs;
                }
            }
            outputs.add(new NamedItemStack(Material.AIR, 1, (short)1, "Compacted Item"));
        }
        return outputs;
    }
    
    public ItemList<NamedItemStack> getRepairs() {
        return cp.getRepairMaterials();
    }
    
    public void update() {
        if(mode == CompactorMode.REPAIR) {
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
                        repair(getRepairs().removeMaxFrom(getInventory(), (int)currentRepair));
                        currentProductionTimer = 0;
                        powerOff();
                    }
                } else {
                    powerOff();
                }
            }
        } else  if(mode == CompactorMode.COMPACT) {
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
                        consumeInputs();
                        
                        recipeFinished();
                        
                        currentProductionTimer = 0;
                        powerOff();
                    }
                } else {
                    powerOff();
                }
            }
        } else if(mode == CompactorMode.DECOMPACT) {
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
                        consumeInputs();
                        
                        recipeFinished();
                        
                        currentProductionTimer = 0;
                        powerOff();
                    }
                } else {
                    powerOff();
                }
            }
        }
    }
    
    protected void recipeFinished() {
        getInputs().removeOneFrom(getInventory());
        getOutputs().putIn(getInventory());
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
