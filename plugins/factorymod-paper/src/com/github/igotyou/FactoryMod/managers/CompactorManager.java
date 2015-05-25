package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.Compactor;
import com.github.igotyou.FactoryMod.Factorys.Compactor.CompactorMode;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class CompactorManager implements Manager {

    private FactoryModPlugin plugin;
    private ReinforcementManager rm = Citadel.getReinforcementManager();
    private List<Compactor> compactors;
    private boolean isLogging = true;
    private long repairTime;
    
    public CompactorManager(FactoryModPlugin plugin) {
        this.plugin = plugin;
        compactors = new ArrayList<Compactor>();
        updateFactorys();
    }

    @Override
    public void save(File file) throws IOException {
        repairTime = System.currentTimeMillis();
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int version = 1;
        oos.writeInt(version);
        oos.writeInt(compactors.size());
        for(Compactor factory : compactors) {
            Location centerLocation = factory.getCenterLocation();
            Location inventoryLocation = factory.getInventoryLocation();
            Location powerLocation = factory.getPowerSourceLocation();
            
            oos.writeUTF(centerLocation.getWorld().getName());
            
            oos.writeInt(centerLocation.getBlockX());
            oos.writeInt(centerLocation.getBlockY());
            oos.writeInt(centerLocation.getBlockZ());
            
            oos.writeInt(inventoryLocation.getBlockX());
            oos.writeInt(inventoryLocation.getBlockY());
            oos.writeInt(inventoryLocation.getBlockZ());

            oos.writeInt(powerLocation.getBlockX());
            oos.writeInt(powerLocation.getBlockY());
            oos.writeInt(powerLocation.getBlockZ());
            
            oos.writeInt(factory.getMode().getId());
            oos.writeDouble(factory.getCurrentRepair());
            oos.writeLong(factory.getTimeDisrepair());
        }
        oos.flush();
        fos.close();
    }

    @Override
    public void load(File file) throws IOException {
        isLogging = false;
        repairTime = System.currentTimeMillis();
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        int version = ois.readInt();
        int count = ois.readInt();
        for(int i = 0; i < count; i++) {
            String worldName = ois.readUTF();
            World world = Bukkit.getWorld(worldName);
            
            Location centerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
            Location inventoryLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
            Location powerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
            
            CompactorMode mode = CompactorMode.byId(ois.readInt());
            double currentRepair = ois.readDouble();
            long timeDisrepair  = ois.readLong();
            
            Compactor factory = new Compactor(centerLocation, inventoryLocation, powerLocation, false, plugin.getCompactorProperties(), 
                    this, mode, currentRepair, timeDisrepair);
            addFactory(factory);
        }
        fis.close();
    }

    @Override
    public void updateFactorys() {

    }

    @Override
    public InteractionResponse createFactory(Location factoryLocation,
            Location inventoryLocation, Location powerLocation) {
        return null;
    }

    @Override
    public InteractionResponse addFactory(Factory factory) {
        Compactor compactor = (Compactor) factory;
        if(compactor.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) &&
                (!factoryExistsAt(compactor.getCenterLocation())
                || !factoryExistsAt(compactor.getInventoryLocation())
                || !factoryExistsAt(compactor.getPowerSourceLocation()))) {
            compactors.add(compactor);
            if(isLogging) {
                FactoryModPlugin.sendConsoleMessage("Compactor created: " + compactor.getProperties().getName());
            }
            return new InteractionResponse(InteractionResult.SUCCESS, "");
        } else {
            FactoryModPlugin.sendConsoleMessage("Compactor failed to create: " + compactor.getProperties().getName());
            return new InteractionResponse(InteractionResult.FAILURE, "");
        }
    }

    @Override
    public Factory getFactory(Location factoryLocation) {
        for(Compactor factory : compactors) {
            if(factory.getCenterLocation().equals(factoryLocation)
                    || factory.getInventoryLocation().equals(factoryLocation)
                    || factory.getPowerSourceLocation().equals(factoryLocation)) {
                return factory;
            }
        }
        return null;
    }

    @Override
    public boolean factoryExistsAt(Location factoryLocation) {
        return false;
    }

    @Override
    public boolean factoryWholeAt(Location factoryLocation) {
        return (getFactory(factoryLocation) != null);
    }

    @Override
    public void removeFactory(Factory factory) {
        if(!(factory instanceof Compactor)) {
            FactoryModPlugin.sendConsoleMessage("Could not remove unexpected factory type: " + factory.getClass().getName());
            return;
        }
        
        Compactor compactor = (Compactor) factory;
        
        FactoryModPlugin.sendConsoleMessage(new StringBuilder("Compactor removed: ")
        .append(compactor.getProperties().getName())
        .append(" at ")
        .append(StringUtils.formatCoords(compactor.getCenterLocation()))
        .toString());
        
        compactors.remove(compactor);
    }

    @Override
    public String getSavesFileName() {
        return FactoryModPlugin.COMPACTOR_SAVE_FILE;
    }
}
