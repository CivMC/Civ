package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class PrintingPress extends BaseFactory {
	
	public enum OperationMode {
		REPAIR(0, "Repair", 200),
		SET_PLATES(1, "Set plates", 200),
		PRINT_BOOKS(2, "Print books", Integer.MAX_VALUE),
		PRINT_PAMPHLETS(3, "Print pamphlets", Integer.MAX_VALUE),
		PRINT_SECURITY(4, "Print security notes", Integer.MAX_VALUE);
		
		private static final int MAX_ID = 3;
		private int id;
		private String description;
		private int productionTime;
		
		private OperationMode(int id, String description, int productionTime) {
			this.id = id;
			this.description = description;
			this.productionTime = productionTime;
		}
		
		public String getDescription() {
			return description;
		}

		public static OperationMode byId(int id) {
			for (OperationMode mode : OperationMode.values()) {
				if (mode.getId() == id)
					return mode;
			}
			return null;
		}
		
		private int getId() {
			return id;
		}

		public OperationMode getNext() {
			int nextId = (getId() + 1) % MAX_ID;
			return OperationMode.byId(nextId);
		}
	}
	
	private PrintingPressProperties printingPressProperties;
	private OperationMode mode;

	public PrintingPress(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, PrintingPressProperties printingPressProperties) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				FactoryType.PRINTING_PRESS, "press");
		this.mode = OperationMode.REPAIR;
		this.printingPressProperties = printingPressProperties;
	}
	
	

	public PrintingPress(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active,
			int currentProductionTimer, int currentEnergyTimer,
			double currentMaintenance, long timeDisrepair, OperationMode mode,
			PrintingPressProperties printingPressProperties) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				FactoryType.PRINTING_PRESS, active, "Printing Press", currentProductionTimer,
				currentEnergyTimer, currentMaintenance, timeDisrepair);
		this.mode = mode;
		this.active = active;
		this.printingPressProperties = printingPressProperties;
	}

	@Override
	public ItemList<NamedItemStack> getFuel() {
		return printingPressProperties.getFuel();
	}

	@Override
	public double getEnergyTime() {
		return 2;
	}

	@Override
	public double getProductionTime() {
		return 20;
	}

	@Override
	public ItemList<NamedItemStack> getInputs() {
		ItemList<NamedItemStack> inputs = new ItemList<NamedItemStack>();
		return inputs;
	}

	@Override
	public ItemList<NamedItemStack> getOutputs() {
		ItemList<NamedItemStack> outputs = new ItemList<NamedItemStack>();
		NamedItemStack pages = new NamedItemStack(Material.PAPER, 64, (short) 0, "pages");
		pages.getItemMeta().setDisplayName("Test Page");
		outputs.add(pages);
		return outputs;
	}

	@Override
	public ItemList<NamedItemStack> getRepairs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void recipeFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxRepair() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the center block, with the InteractionMaterial
	 */
	public List<InteractionResponse> getCentralBlockResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		//Is the factory off
		if (!active)
		{
			//is the recipe is initiated
			if (mode == null) {
				mode = OperationMode.REPAIR;
			} else {		
				mode = mode.getNext();
			}
			
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "-----------------------------------------------------"));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Switched mode to: " + mode.getDescription()+"."));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Next mode is: "+mode.getNext().getDescription()+"."));
		}
		//if the factory is on, return error message
		else
		{
			responses.add(new InteractionResponse(InteractionResult.FAILURE, "You can't change modes while the press is on! Turn it off first."));
		}
		return responses;
	}
	
	public List<InteractionResponse> getChestResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		String status=active ? "On" : "Off";
		//Name: Status with XX% health.
		int maxRepair = printingPressProperties.getMaxRepair();
		boolean maintenanceActive = maxRepair!=0;
		int health =(!maintenanceActive) ? 100 : (int) Math.round(100*(1-currentRepair/(maxRepair)));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, printingPressProperties.getName()+": "+status+" with "+String.valueOf(health)+"% health."));
		//RecipeName: X seconds(Y ticks)[ - XX% done.]
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, mode.getDescription()));
		//[Inputs: amount Name, amount Name.]
		if(!getInputs().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Input: "+getInputs().toString()+"."));
		}
		//[Outputs: amount Name, amount Name.]
		if(!getOutputs().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Output: "+getOutputs().toString()+"."));
		}
		//[Will repair XX% of the factory]
		if(!getRepairs().isEmpty()&&maintenanceActive)
		{
			int amountAvailable=getRepairs().amountAvailable(getPowerSourceInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/printingPressProperties.getMaxRepair()*100);
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+getRepairs().getMultiple(amountRepaired).toString()+"."));
		}
		return responses;
	}
	
	private class PrintResult {
		private static final int PAGE_LORE_LENGTH_LIMIT = 140;
		private List<String> pages;
		private String title;
		private String author;
		private int watermark;
		
		PrintResult() {
			Pattern printPlateRE = Pattern.compile("^Print plates #([0-9]{4})$");
			Inventory inventory = getInventory();
			for (ItemStack stack : inventory.getContents()) {
				if (stack.getType().equals(Material.BOOK_AND_QUILL) ||
						stack.getType().equals(Material.WRITTEN_BOOK)) {
					ItemMeta meta = stack.getItemMeta();
					List<String> lore = meta.getLore();
					if (!lore.isEmpty()) {
						String firstLore = lore.get(0);
						Matcher match = printPlateRE.matcher(firstLore);
						if (match.matches()) {
							if (meta instanceof BookMeta) {
								BookMeta bookData = (BookMeta) meta;
								title = bookData.getTitle();
								author = bookData.getAuthor();
								watermark = Integer.parseInt(match.group(1)); 
								pages = new ArrayList<String>(bookData.getPages());
							}
						}
					}
				}
			}
		}
		
		public int pageCount() {
			return pages.size();
		}
		
		public ItemStack toBook() {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta meta = (BookMeta) book.getItemMeta();
			meta.setDisplayName(title);
			List<String> lore = new ArrayList<String>();
			lore.add(author);
			meta.setLore(lore);
			meta.setTitle(title);
			meta.setAuthor(author);
			meta.setPages(pages);
			book.setItemMeta(meta);
			return book;
		}
		
		public ItemStack toPamphlet() {
			ItemStack book = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = book.getItemMeta();
			meta.setDisplayName(title);
			List<String> lore = new ArrayList<String>();
			if (pages.size() > 0) {
				lore.add(limitPageLore(pages.get(0)));
			}
			meta.setLore(lore);
			return book;
		}
		
		public ItemStack toSecurityNote() {
			ItemStack book = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = book.getItemMeta();
			meta.setDisplayName(title);
			List<String> lore = new ArrayList<String>();
			if (pages.size() > 0) {
				lore.add(pages.get(0));
			}
			meta.setLore(lore);
			return book;
		}
		
		private String limitPageLore(String in) {
			if (in.length() > PAGE_LORE_LENGTH_LIMIT) {
				return in.substring(0, PAGE_LORE_LENGTH_LIMIT - 3) + "...";
			} else {
				return in;
			}
		}
	}
	
	private PrintResult getPrintResult() {
		return new PrintResult();
	}
}
