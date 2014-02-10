package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.PrettyLore;

public class PrintingPress extends BaseFactory {
	
	private PrintingPressProperties printingPressProperties;
	private OperationMode mode;
	public OperationMode getMode() {
		return mode;
	}

	private int containedPaper;
	private int containedBindings;
	private int containedSecurityMaterials;
	private int[] processQueue;
	private int processQueueOffset;
	private int lockedResultCode;

	public PrintingPress(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, PrintingPressProperties printingPressProperties) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				FactoryType.PRINTING_PRESS, "press");
		this.mode = OperationMode.REPAIR;
		this.printingPressProperties = printingPressProperties;
		this.containedPaper = 0;
		this.containedBindings = 0;
		this.containedSecurityMaterials = 0;
		this.processQueue = new int[1];
		this.processQueueOffset = 0;
		this.lockedResultCode = 0;
	}

	public PrintingPress(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active,
			int currentProductionTimer, int currentEnergyTimer,
			double currentMaintenance, long timeDisrepair, OperationMode mode,
			PrintingPressProperties printingPressProperties,
			int containedPaper, int containedBindings, int containedSecurityMaterials,
			int[] processQueue, int lockedResultCode) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				FactoryType.PRINTING_PRESS, active, "Printing Press", currentProductionTimer,
				currentEnergyTimer, currentMaintenance, timeDisrepair);
		this.mode = mode;
		this.active = active;
		this.printingPressProperties = printingPressProperties;
		this.containedPaper = containedPaper;
		this.containedBindings = containedBindings;
		this.containedSecurityMaterials = containedSecurityMaterials;
		this.containedPaper = 0;
		this.containedBindings = 0;
		this.containedSecurityMaterials = 0;
		this.processQueue = processQueue;
		this.processQueueOffset = 0;
		this.lockedResultCode = lockedResultCode;
	}

	public int getLockedResultCode() {
		return lockedResultCode;
	}

	@Override
	public ItemList<NamedItemStack> getFuel() {
		return printingPressProperties.getFuel();
	}
	
	public int getContainedPaper() {
		return containedPaper;
	}

	public int getContainedBindings() {
		return containedBindings;
	}

	public int getContainedSecurityMaterials() {
		return containedSecurityMaterials;
	}

	@Override
	public double getEnergyTime() {
		return printingPressProperties.getEnergyTime();
	}

	@Override
	public double getProductionTime() {
		switch(mode) {
			case SET_PLATES:
				NamedItemStack plates = getPlateResult();
				int pageCount = 1;
				if (plates != null) {
					pageCount = Math.max(1, ((BookMeta) plates.getItemMeta()).getPageCount());
				}
				pageCount = Math.min(pageCount, printingPressProperties.getBookPagesCap());
				return printingPressProperties.getSetPlateTime() * pageCount;
			case REPAIR:
				return printingPressProperties.getRepairTime();
			default:
				// Continuous recipes -> 1 year limit at 1 update per second
				return 3600 * 24 * 365;
		}
	}

	@Override
	public ItemList<NamedItemStack> getInputs() {
		ItemList<NamedItemStack> inputs = new ItemList<NamedItemStack>();
		switch(mode) {
		case SET_PLATES:
			NamedItemStack plates = getPlateResult();
			if (plates != null) {
				int pageCount = ((BookMeta) plates.getItemMeta()).getPageCount();
				pageCount = Math.min(pageCount, printingPressProperties.getBookPagesCap());
				inputs.addAll(printingPressProperties.getPlateMaterials().getMultiple(pageCount));
			}
			break;
		}
		return inputs;
	}

	@Override
	public ItemList<NamedItemStack> getOutputs() {
		ItemList<NamedItemStack> outputs = new ItemList<NamedItemStack>();
		switch(mode) {
		case SET_PLATES:
			NamedItemStack plates = getPlateResult();
			if (plates != null) {
				outputs.add(plates);
			}
			break;
		}
		return outputs;
	}

	@Override
	public ItemList<NamedItemStack> getRepairs() {
		ItemList<NamedItemStack> inputs = new ItemList<NamedItemStack>();
		switch(mode) {
		case REPAIR:
			inputs.addAll(printingPressProperties.getRepairMaterials());
			break;
		}
		return inputs;
	}

	@Override
	public int getMaxRepair() {
		return printingPressProperties.getMaxRepair();
	}
	
	@Override
	public void powerOn() {
		super.powerOn();
		this.containedPaper = 0;
		this.containedBindings = 0;
		this.containedSecurityMaterials = 0;
		int outputDelay = printingPressProperties.getPageLead();
		this.processQueue = new int[outputDelay];
		this.processQueueOffset = 0;
		
		if (mode == OperationMode.PRINT_BOOKS ||
				mode == OperationMode.PRINT_PAMPHLETS ||
				mode == OperationMode.PRINT_SECURITY) {
			// Require product
			if (!getPrintResult().isValid()) {
				powerOff();
			} else {
				this.lockedResultCode = getPrintResult().hashCode();
			}
		}
	}
	
	@Override
	public void fuelConsumed() {
		// Check for sneaky plate swaps, shut down
		if (mode == OperationMode.PRINT_BOOKS ||
				mode == OperationMode.PRINT_PAMPHLETS ||
				mode == OperationMode.PRINT_SECURITY) {
			// Require product
			int expectedResultCode = getPrintResult().hashCode();
			if (this.lockedResultCode != expectedResultCode) {
				powerOff();
				return;
			}
		}
		
		switch(mode) {
		case PRINT_BOOKS:
			printBooksUpdate();
			break;
		case PRINT_PAMPHLETS:
			printPamphletsUpdate();
			break;
		case PRINT_SECURITY:
			printSecurityUpdate();
			break;
		}
	}
	
	public void printBooksUpdate() {
		// Output finished results
		int finished = processQueue[processQueueOffset];
		if (finished > 0) {
			NamedItemStack result = getPrintResult().toBook();
			ItemList<NamedItemStack> set = new ItemList<NamedItemStack>();
			set.add(result);
			set = set.getMultiple(finished);
			set.putIn(getInventory());
		}
		
		// Load materials
		ItemList<NamedItemStack> pages = printingPressProperties.getPageMaterials();
		boolean hasPages = pages.allIn(getInventory());
		boolean inputStall = false;
		
		int pageCount = getPrintResult().pageCount();
		pageCount = Math.min(pageCount, printingPressProperties.getBookPagesCap());
		
		if (hasPages) {
			// Check bindings
			int expectedBindings = (int) Math.floor((double) (containedPaper + printingPressProperties.getPagesPerLot()) / (double) pageCount);
			boolean hasBindings = true;
			ItemList<NamedItemStack> allBindings = new ItemList<NamedItemStack>();
			if (expectedBindings > containedBindings) {
				int neededBindings = expectedBindings - containedBindings;
				allBindings = printingPressProperties.getBindingMaterials().getMultiple(neededBindings);
				hasBindings = allBindings.allIn(getInventory());
			}
			
			if (hasBindings) {
				pages.removeFrom(getInventory());
				containedPaper += printingPressProperties.getPagesPerLot();
				
				while (containedBindings < expectedBindings) {
					if (printingPressProperties.getBindingMaterials().allIn(getInventory())) {
						printingPressProperties.getBindingMaterials().removeFrom(getInventory());
						containedBindings += 1;
					}
				}
			} else {
				inputStall = true;
			}
		} else {
			inputStall = true;
		}
		
		// Put materials in queue
		int booksInPages = containedPaper / pageCount;
		int copiesIn = Math.min(booksInPages, containedBindings);
		containedPaper -= copiesIn * pageCount;
		containedBindings -= copiesIn;
		processQueue[processQueueOffset] = copiesIn;
		
		if (inputStall) {
			stopIfEmpty();
		}
		
		// Rotate on queue
		processQueueOffset += 1;
		if (processQueueOffset >= processQueue.length) {
			processQueueOffset = 0;
		}
	}
	
	private void stopIfEmpty() {// Check if queue is empty
		boolean queueEmpty = true;
		for (int amount : processQueue) {
			if (amount > 0) {
				queueEmpty = false;
				break;
			}
		}
		if (queueEmpty) {
			// Stalled and empty
			powerOff();
		}
	}

	public void printPamphletsUpdate() {
		// Output finished results
		int finished = processQueue[processQueueOffset];
		if (finished > 0) {
			NamedItemStack result = getPrintResult().toPamphlet();
			ItemList<NamedItemStack> set = new ItemList<NamedItemStack>();
			set.add(result);
			set = set.getMultiple(finished);
			set.putIn(getInventory());
		}
		
		// Load materials
		ItemList<NamedItemStack> pages = printingPressProperties.getPamphletMaterials();
		boolean hasPages = pages.allIn(getInventory());
		if (hasPages) {
			pages.removeFrom(getInventory());
			processQueue[processQueueOffset] = printingPressProperties.getPamphletsPerLot();
		} else {
			processQueue[processQueueOffset] = 0;
			stopIfEmpty();
		}
		
		// Rotate on queue
		processQueueOffset += 1;
		if (processQueueOffset >= processQueue.length) {
			processQueueOffset = 0;
		}
	}
	
	public void printSecurityUpdate() {
		// Output finished results
		int finished = processQueue[processQueueOffset];
		if (finished > 0) {
			NamedItemStack result = getPrintResult().toSecurityNote();
			ItemList<NamedItemStack> set = new ItemList<NamedItemStack>();
			set.add(result);
			set = set.getMultiple(finished);
			set.putIn(getInventory());
		}
		
		// Load materials
		ItemList<NamedItemStack> pages = printingPressProperties.getPamphletMaterials();
		boolean hasPages = pages.allIn(getInventory());
		boolean inputStall = false;
		if (hasPages) {
			// Check security materials
			int expectedExtras = (int) Math.ceil((double) containedPaper + printingPressProperties.getPamphletsPerLot());
			boolean hasExtras = true;
			ItemList<NamedItemStack> allSecurityMaterials = new ItemList<NamedItemStack>();
			if (expectedExtras > containedSecurityMaterials) {
				int neededExtras = expectedExtras - containedSecurityMaterials;
				int neededExtraLots = (int) Math.ceil((double) neededExtras / (double) printingPressProperties.getSecurityNotesPerLot());
				allSecurityMaterials = printingPressProperties.getSecurityMaterials().getMultiple(neededExtraLots);
				hasExtras = allSecurityMaterials.allIn(getInventory());
			}
			
			if (hasExtras) {
				pages.removeFrom(getInventory());
				containedPaper += printingPressProperties.getPamphletsPerLot();
				
				// Load security materials if security notes
				while (containedSecurityMaterials < containedPaper) {
					if (printingPressProperties.getSecurityMaterials().allIn(getInventory())) {
						printingPressProperties.getSecurityMaterials().removeFrom(getInventory());
						containedSecurityMaterials += printingPressProperties.getSecurityNotesPerLot();
					}
				}
			} else {
				inputStall = true;
			}
		} else {
			inputStall = true;
		}
		
		// Put materials in queue
		int copiesIn = containedPaper;
		containedPaper -= copiesIn;
		containedSecurityMaterials -= copiesIn;
		processQueue[processQueueOffset] = copiesIn;
		
		if (inputStall) {
			stopIfEmpty();
		}
		
		// Rotate on queue
		processQueueOffset += 1;
		if (processQueueOffset >= processQueue.length) {
			processQueueOffset = 0;
		}
	}
	
	public int[] getProcessQueue() {
		// Rotate so that current place in ring buffer is 0
		int[] newQ = new int[processQueue.length];
		int toEnd = processQueue.length - processQueueOffset;
		System.arraycopy(processQueue, processQueueOffset, newQ, 0, toEnd);
		if (processQueueOffset > 0) {
			System.arraycopy(processQueue, 0, newQ, toEnd, processQueueOffset);
		}
		return newQ;
	}

	public boolean isRepairing() {
		return mode == OperationMode.REPAIR;
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
			int amountAvailable=getRepairs().amountAvailable(getInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/printingPressProperties.getMaxRepair()*100);
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+getRepairs().getMultiple(amountRepaired).toString()+"."));
		}
		return responses;
	}
	
	private PrintResult getPrintResult() {
		return new PrintResult();
	}
	
	private NamedItemStack getPlateResult() {
		for (ItemStack stack : getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			if (stack.getType().equals(Material.BOOK_AND_QUILL) ||
					stack.getType().equals(Material.WRITTEN_BOOK)) {
				ItemMeta meta = stack.getItemMeta();
				if (meta instanceof BookMeta) {
					// Found a book
					BookMeta bookData = (BookMeta) meta;
					String title = bookData.getTitle();
					String author = bookData.getAuthor();
					if (author == null) {
						author = "";
					}
					List<String> pages = new ArrayList<String>(bookData.getPages());
					
					NamedItemStack plates = new NamedItemStack(Material.WRITTEN_BOOK, 1, (short) 0, "plate");
					BookMeta plateMeta = (BookMeta) plates.getItemMeta();
					plateMeta.setTitle(title);
					plateMeta.setAuthor(author);
					plateMeta.setPages(pages);
					int watermark = new Random().nextInt(9000) + 1000;
					List<String> lore = new ArrayList<String>();
					lore.add("Print plates #" + Integer.toString(watermark));
					plateMeta.setLore(lore);
					plates.setItemMeta(plateMeta);
					return plates;
				}
			}
		}
		return null;
	}
	
	private class PrintResult {
		private static final int PAGE_LORE_LENGTH_LIMIT = 140;
		private static final int PAGE_LORE_LINE_LIMIT = 35;
		private List<String> pages;
		private String title;
		private String author;
		private int watermark;
		private boolean valid;
		
		PrintResult() {
			Pattern printPlateRE = Pattern.compile("^Print plates #([0-9]{4})$");
			Inventory inventory = getInventory();
			
			title = "";
			author = "";
			watermark = 0;
			valid = false;
			pages = new ArrayList<String>();
			
			for (ItemStack stack : inventory.getContents()) {
				if (stack == null) {
					continue;
				}
				
				if (stack.getType().equals(Material.BOOK_AND_QUILL) ||
						stack.getType().equals(Material.WRITTEN_BOOK)) {
					ItemMeta meta = stack.getItemMeta();
					List<String> lore = meta.getLore();
					if (lore != null && !lore.isEmpty()) {
						String firstLore = lore.get(0);
						Matcher match = printPlateRE.matcher(firstLore);
						if (match.matches()) {
							if (meta instanceof BookMeta) {
								BookMeta bookData = (BookMeta) meta;
								title = bookData.getTitle();
								author = bookData.getAuthor();
								if (author == null) {
									author = "";
								}
								watermark = Integer.parseInt(match.group(1)); 
								pages = new ArrayList<String>(bookData.getPages());
								valid = true;
								break;
							}
						}
					}
				}
			}
		}
		
		public boolean isValid() {
			return valid;
		}

		public int pageCount() {
			return pages.size();
		}
		
		public NamedItemStack toBook() {
			NamedItemStack book = new NamedItemStack(Material.WRITTEN_BOOK, 1, (short) 0, "book");
			BookMeta meta = (BookMeta) book.getItemMeta();
			meta.setTitle(title);
			meta.setAuthor(author);
			meta.setPages(pages);
			book.setItemMeta(meta);
			return book;
		}
		
		public NamedItemStack toPamphlet() {
			NamedItemStack book = new NamedItemStack(Material.PAPER, 1, (short) 0, "pamphlet");
			ItemMeta meta = book.getItemMeta();
			meta.setDisplayName(title);
			List<String> lore = new ArrayList<String>();
			if (pages.size() > 0) {
				lore.addAll(filterPageLore(pages.get(0)));
			}
			meta.setLore(lore);
			book.setItemMeta(meta);
			return book;
		}
		
		public NamedItemStack toSecurityNote() {
			NamedItemStack book = new NamedItemStack(Material.PAPER, 1, (short) 0, "note");
			ItemMeta meta = book.getItemMeta();
			meta.setDisplayName(title);
			List<String> lore = new ArrayList<String>();
			if (pages.size() > 0) {
				lore.addAll(filterPageLore(pages.get(0)));
			}
			if (author.equals("")) {
				lore.add(String.format("§2#%d", watermark));
			} else {
				lore.add(String.format("§2%s #%d", author, watermark));
			}
			meta.setLore(lore);
			book.setItemMeta(meta);
			return book;
		}
		
		private List<String> filterPageLore(String lore) {
			// Remove green
			lore = lore.replace("§2", "");
			
			// Remove line breaks
			lore = lore.replaceAll("[ \r\n]+", " ");
			
			// Limit length
			lore = PrettyLore.limitLengthEllipsis(lore, PAGE_LORE_LENGTH_LIMIT);
			
			// Split in to lines based on length
			List<String> lines = PrettyLore.splitLines(lore, PAGE_LORE_LINE_LIMIT);
			
			return lines;
		}
		
		public int hashCode() {
			int code = watermark;
			code = code ^ title.hashCode();
			code += 349525;
			code = code ^ author.hashCode();
			code += 349525;
			for (String page : pages) {
				code = code ^ page.hashCode();
				code += 349525;
			}
			return code;
		}
	}
	
	public enum OperationMode {
		REPAIR(0, "Repair"),
		SET_PLATES(1, "Set plates"),
		PRINT_BOOKS(2, "Print books"),
		PRINT_PAMPHLETS(3, "Print pamphlets"),
		PRINT_SECURITY(4, "Print security notes");
		
		private static final int MAX_ID = 5;
		private int id;
		private String description;

		private OperationMode(int id, String description) {
			this.id = id;
			this.description = description;
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
		
		public int getId() {
			return id;
		}

		public OperationMode getNext() {
			int nextId = (getId() + 1) % MAX_ID;
			return OperationMode.byId(nextId);
		}
	}

	@Override
	protected void recipeFinished() {
		// TODO Auto-generated method stub
		
	}
}
