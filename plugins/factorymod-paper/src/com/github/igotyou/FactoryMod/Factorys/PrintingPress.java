package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;

public class PrintingPress extends ABaseFactory {
	
	private PrintingPressProperties printingPressProperties;

	private static Logger log = Logger.getLogger(PrintingPress.class.getName());
	
	public PrintingPressProperties getProperties() {
		return printingPressProperties;
	}
	
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
		this.processQueue = processQueue;
		this.processQueueOffset = 0;
		this.lockedResultCode = lockedResultCode;
	}

	public int getLockedResultCode() {
		return lockedResultCode;
	}

	@Override
	public ItemList<AdvancedItemStack> getFuel() {
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
				AdvancedItemStack plates = getPlateResult();
				int pageCount = 1;
				if (plates != null) {
					pageCount = Math.max(1, ((BookMeta) plates.getItemMeta()).getPageCount());
				} else {
					log.finer("getProductionTime(): plates == null");
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
	public ItemList<AdvancedItemStack> getInputs() {
		ItemList<AdvancedItemStack> inputs = new ItemList<AdvancedItemStack>();
		switch(mode) {
		case SET_PLATES:
			AdvancedItemStack plates = getPlateResult();
			if (plates != null) {
				int pageCount = ((BookMeta) plates.getItemMeta()).getPageCount();
				if (pageCount > 0) {
					pageCount = Math.min(pageCount, printingPressProperties.getBookPagesCap());
					inputs.addAll(printingPressProperties.getPlateMaterials().getMultiple(pageCount));
				}
			} else {
				log.finer("getInputs(): plates == null");
			}
			break;
		default:
			break;
		}
		return inputs;
	}

	@Override
	public ItemList<AdvancedItemStack> getOutputs() {
		ItemList<AdvancedItemStack> outputs = new ItemList<AdvancedItemStack>();
		switch(mode) {
		case SET_PLATES:
			AdvancedItemStack plates = getPlateResult();
			if (plates != null) {
				outputs.add(plates);
			} else {
				log.finer("getOutputs(): Plates results are null!");
			}
			break;
		default:
			break;
		}
		return outputs;
	}

	@Override
	public ItemList<AdvancedItemStack> getRepairs() {
		ItemList<AdvancedItemStack> inputs = new ItemList<AdvancedItemStack>();
		switch(mode) {
		case REPAIR:
			inputs.addAll(printingPressProperties.getRepairMaterials());
			break;
		default:
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
		default:
			break;
		}
	}
	
	public void printBooksUpdate() {
		// Output finished results

		PrintResult printResult = getPrintResult();
		
		int finished = processQueue[processQueueOffset];
		if (finished > 0) {
			AdvancedItemStack result = printResult.toBook();
			ItemList<AdvancedItemStack> set = new ItemList<AdvancedItemStack>();
			set.add(result);
			set = set.getMultiple(finished);
			set.putIn(getInventory());
		}
		
		// Load materials
		ItemList<AdvancedItemStack> pages = printingPressProperties.getPageMaterials();
		boolean hasPages = pages.allIn(getInventory());
		boolean inputStall = false;
		
		int pageCount = printResult.pageCount();
		pageCount = Math.min(pageCount, printingPressProperties.getBookPagesCap());
		
		if (hasPages) {
			// Check bindings
			int expectedBindings = pageCount == 0 ? containedPaper + printingPressProperties.getPagesPerLot() : (int) Math.floor((double) (containedPaper + printingPressProperties.getPagesPerLot()) / (double) pageCount);
			boolean hasBindings = true;
			ItemList<AdvancedItemStack> allBindings = new ItemList<AdvancedItemStack>();
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
		int booksInPages = pageCount == 0 ? containedPaper : containedPaper / pageCount;
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
			AdvancedItemStack result = getPrintResult().toPamphlet();
			ItemList<AdvancedItemStack> set = new ItemList<AdvancedItemStack>();
			set.add(result);
			set = set.getMultiple(finished);
			set.putIn(getInventory());
		}
		
		// Load materials
		ItemList<AdvancedItemStack> pages = printingPressProperties.getPamphletMaterials();
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
			AdvancedItemStack result = getPrintResult().toSecurityNote();
			ItemList<AdvancedItemStack> set = new ItemList<AdvancedItemStack>();
			set.add(result);
			set = set.getMultiple(finished);
			set.putIn(getInventory());
		}
		
		// Load materials
		ItemList<AdvancedItemStack> pages = printingPressProperties.getPamphletMaterials();
		boolean hasPages = pages.allIn(getInventory());
		boolean inputStall = false;
		if (hasPages) {
			// Check security materials
			int expectedExtras = (int) Math.ceil((double) containedPaper + printingPressProperties.getPamphletsPerLot());
			boolean hasExtras = true;
			ItemList<AdvancedItemStack> allSecurityMaterials = new ItemList<AdvancedItemStack>();
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
	
	/** 
	 * On interaction with the factory chest, return context-appropriate information
	 * concerning the current mode, relative to the inputs and outputs.
	 * 
	 * Adding detailed state on the active recipe now, if factory is on.
	 *
	 * @return A {@link List} of {@link InteractionResponse} objects containing
	 *   details on the current mode.
	 */
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

		// Never call the same function more than once, especially when this complex.
		ItemList<AdvancedItemStack> getInputs = getInputs();
		//[Inputs: amount Name, amount Name.]
		if(getInputs != null && !getInputs.isEmpty()) {
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Input: "+getInputs.toString()+"."));
		} else {
			log.fine("getChestResponse(): Inputs is null or empty!");
		}

		//[Outputs: amount Name, amount Name.]
		ItemList<AdvancedItemStack> getOutputs = getOutputs();
		if(getOutputs != null && !getOutputs.isEmpty()) {
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Output: "+getOutputs.toString()+"."));
		} else {
			log.fine("getChestResponse(): Outputs is null or empty!");
		}

		//[Will repair XX% of the factory]
		ItemList<AdvancedItemStack> getRepairs = getRepairs();
		if(getRepairs != null && !getRepairs.isEmpty() && maintenanceActive) {
			int amountAvailable = getRepairs.amountAvailable(getInventory());
			int amountRepaired = amountAvailable > currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired = (int) (( (double) amountRepaired) / printingPressProperties.getMaxRepair() * 100);
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Will repair " +
					String.valueOf(percentRepaired) + "% of the factory with " + 
					getRepairs.getMultiple(amountRepaired).toString() + "."));
		}


		//[% done if plates; queue status else]
		if (active && mode != OperationMode.REPAIR) {
			int readyCopies = 0;
			StringBuffer queueContents = new StringBuffer();
			if (mode == OperationMode.PRINT_BOOKS ||
				 mode == OperationMode.PRINT_PAMPHLETS ||
				 mode == OperationMode.PRINT_SECURITY) {
				queueContents.append("Queue Contents: [");
				for (int i = 0; i < processQueue.length; i++) {
					readyCopies += processQueue[i];
					if (i > 0) {
						queueContents.append(",");
					}
					if (i == processQueueOffset) {
						queueContents.append("{");
					}
					queueContents.append(processQueue[i]);
					if (i == processQueueOffset) {
						queueContents.append("}");
					}
				}
				queueContents.append("]");
			}
			switch(mode) {
			case PRINT_BOOKS:
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Printing Books with " +
						String.valueOf(readyCopies) + " in the queue."));
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, queueContents.toString()));
				break;
			case PRINT_PAMPHLETS:
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Printing Pamphlets with " +
						String.valueOf(readyCopies) + " in the queue."));
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, queueContents.toString()));
				break;
			case PRINT_SECURITY:
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Printing Security Notes with " +
						String.valueOf(readyCopies) + " in the queue."));
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, queueContents.toString()));
				break;
			case SET_PLATES:
				int setTime = printingPressProperties.getSetPlateTime() * getBookInventoryPages();
				int percentComplete = (int) (( (double) currentProductionTimer) / setTime * 100);
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Set Plates is " + 
						String.valueOf(percentComplete) + "% complete"));
				break;
			default:
				log.severe("getChestResponse(): Active set, but unknown mode: critical error.");
				break;
			}
		}

		return responses;
	}
	
	/**
	 * Generates a new PrintResult object based on the current production state of the factory,
	 *   reflecting the active mode's output. This does not include plates, which are handled
	 *   separately.
	 * 
	 * @return {@link PrintResult} object reflecting the current state production state of the
	 *   factory.
	 */
	private PrintResult getPrintResult() {
		return new PrintResult();
	}
	
	/**
	 * Lightweight page count function, for active recipes only. Useful for measuring current 
	 * progress in a recipe, for instance.
	 */
	private int getBookInventoryPages() {
		for (ItemStack stack : getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			if (stack.getType().equals(Material.BOOK_AND_QUILL) ||
					stack.getType().equals(Material.WRITTEN_BOOK)) {
				log.finer("getPlateResult(): Found a book in the factory.");
				ItemMeta meta = stack.getItemMeta();
				if (meta instanceof BookMeta) {
					BookMeta bookData = (BookMeta) meta;
					if (bookData.hasPages()) {
						return Math.max(1, bookData.getPageCount());
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Generates a NamedItemStack containing a 'plate' -- a signed book with lore that
	 *   uniquely reflects the book given as input. This plate can then be used to produce
	 *   copies of the book, security notes, pamphlets, and the like.
	 *
	 * @return {@link AdvancedItemStack} of a Signed Book with lore containing the contents of
	 *   the given input book, and other important metadata.
	 */
	private AdvancedItemStack getPlateResult() {
		for (ItemStack stack : getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			if (stack.getType().equals(Material.BOOK_AND_QUILL) ||
					stack.getType().equals(Material.WRITTEN_BOOK)) {
				log.finer("getPlateResult(): Found a book in the factory to use as plate source.");
				ItemMeta meta = stack.getItemMeta();
				if (meta instanceof BookMeta) {
					log.finer("getPlateResult(): Book found has Metadata associated.");
					// Found a book
					BookMeta bookData = (BookMeta) meta;
					String title = bookData.hasTitle() ? bookData.getTitle() : "";
					String author = bookData.hasAuthor() ? bookData.getAuthor() : "";
					if (author == null) {
						author = "";
					}
					if (title == null) {
						title = "";
					}
					List<String> pages = new ArrayList<String>();
					if (bookData.hasPages()) {
						pages.addAll(bookData.getPages());
					} else {
						pages.add("");
						log.finer("getPlateResult(): Book found has no pages; adding blank.");
					}
					
					AdvancedItemStack plates = new AdvancedItemStack(Material.WRITTEN_BOOK, 1, (short) 0, "plate");
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
				} else {
					log.warning("getPlateResult(): Book found, but no associated metadata.");
				}
			}
		}
		log.warning("getPlateResult(): No book found for plate source.");
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
		
		/**
		 * Initializes a PrintResult leveraging the contents and state of the 
		 * {@link PrintingPress}. Attempts to find a valid Print plate in the factory, then
		 * extracts the information from the Plate necessary to produce books,
		 * pamphlets, or security notes.
		 */ 
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
					log.finer("PrintResult(): Found a book item.");
					ItemMeta meta = stack.getItemMeta();
					List<String> lore = meta.getLore();
					if (lore != null && !lore.isEmpty()) {
						log.finer("PrintResult(): Found a lore item attached to the book.");
						String firstLore = lore.get(0);
						Matcher match = printPlateRE.matcher(firstLore);
						if (match.matches()) {
							log.finer("PrintResult(): Lore matches a print plate.");
							if (meta instanceof BookMeta) {
								log.finer("PrintResult(): Book has Associated Book Meta.");
								BookMeta bookData = (BookMeta) meta;
								if (bookData.hasTitle())
									title = bookData.getTitle();
								if (title == null)
									title = "";

								if (bookData.hasAuthor())
									author = bookData.getAuthor();
								if (author == null) {
									author = "";
								}
								watermark = Integer.parseInt(match.group(1));
								if (bookData.hasPages())
									pages = new ArrayList<String>(bookData.getPages());
								else {
									pages = new ArrayList<String>(0);
									pages.add(""); // Blank page.
								}
								valid = true;
								break;
							} else {
								log.fine("PrintResult(): No Book Meta found!");
							}
						} else {
							log.fine("PrintResult(): Lore indicates the book is not a print plate.");
						}
					} else {
						log.fine("PrintResult(): No lore item found with the book.");
					}
				}
			}

			if (!valid) {
				log.fine("PrintResult(): No valid plate found in the factory.");
			}
		}
		
		public boolean isValid() {
			return valid;
		}

		public int pageCount() {
			return pages.size();
		}
		
		public AdvancedItemStack toBook() {
			AdvancedItemStack book = new AdvancedItemStack(Material.WRITTEN_BOOK, 1, (short) 0, "book");
			BookMeta meta = (BookMeta) book.getItemMeta();
			meta.setTitle(title);
			meta.setAuthor(author);
			meta.setPages(pages);
			book.setItemMeta(meta);
			return book;
		}
		
		public AdvancedItemStack toPamphlet() {
			AdvancedItemStack book = new AdvancedItemStack(Material.PAPER, 1, (short) 0, "pamphlet");
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
		
		public AdvancedItemStack toSecurityNote() {
			AdvancedItemStack book = new AdvancedItemStack(Material.PAPER, 1, (short) 0, "note");
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
			lore = StringUtils.limitLengthEllipsis(lore, PAGE_LORE_LENGTH_LIMIT);
			
			// Split in to lines based on length
			List<String> lines = StringUtils.splitLines(lore, PAGE_LORE_LINE_LIMIT);
			
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
