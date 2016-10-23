/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagReader;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PrintNoteRecipe extends InputRecipe {
	public static final String pamphletName = "Pamphlet";
	public static final String secureNoteName = "Secure Note";
	
	private ItemMap printingPlate;
	private int outputAmount;
	private boolean secureNote;
	private String title;
	
	public ItemMap getPrintingPlate() {
		return this.printingPlate;
	}
	
	public int getOutputAmount() {
		return this.outputAmount;
	}

	public boolean isSecurityNote() {
		return this.secureNote;
	}

	public String getTitle() {
		return this.title;
	}

	public PrintNoteRecipe(
			String identifier,
			String name,
			int productionTime,
			ItemMap input, 
			ItemMap printingPlate,
			int outputAmount,
			boolean secureNote,
			String title
			)
	{
		super(identifier, name, productionTime, input);
		this.printingPlate = printingPlate;
		this.outputAmount = outputAmount;
		this.secureNote = secureNote;
		
		if(title != null && title.length() > 0) {
			this.title = title;
		} else {
			this.title = secureNote ? secureNoteName: pamphletName;
		}
	}	

	public boolean enoughMaterialAvailable(Inventory i) {
		return this.input.isContainedIn(i) && PrintBookRecipe.getPrintingPlate(i, this.printingPlate) != null;
	}

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		
		ItemStack printingPlateStack = PrintBookRecipe.getPrintingPlate(i, this.printingPlate);
		ItemMap toRemove = this.input.clone();

		if (printingPlateStack != null
				&& toRemove.isContainedIn(i)
				&& toRemove.removeSafelyFrom(i)
				)
		{
			List<String> textLines = new ArrayList<String>();
			
			if(this.secureNote) {			
				TagReader reader = new TagReader(printingPlateStack);
				String serialNumber = (String)reader.getString("sn");
				textLines.add(serialNumber);
			}
			
			addTextLines(printingPlateStack, textLines);

			ItemStack paper = new ItemStack(Material.PAPER, this.outputAmount);
			
			ItemMeta paperMeta = paper.getItemMeta();
			paperMeta.setLore(textLines);
			paperMeta.setDisplayName(this.title);
			paper.setItemMeta(paperMeta);

			i.addItem(paper);
		}
		
		logAfterRecipeRun(i, fccf);
	}
	
	private static void addTextLines(ItemStack printingPlateStack, List<String> result) {
		ItemStack book = PrintBookRecipe.createBook(printingPlateStack, 1);
		BookMeta bookMeta = (BookMeta)book.getItemMeta();
		String text = bookMeta.getPageCount() > 0 ? bookMeta.getPage(1): "";
		String[] lines = text.split("§0\n");
		
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i];
			
			if(i > 0) {
				line = line.substring(2);
			}
			
			result.add(ChatColor.GRAY + line);
		}
	}
	
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<ItemStack>();

		if (i == null) {
			ItemStack is = PrintingPlateRecipe.getPrintingPlateRepresentation(this.printingPlate, PrintingPlateRecipe.itemName);
			
			result.addAll(this.input.getItemStackRepresentation());
			result.add(is);
			return result;
		}
		
		result = createLoredStacksForInfo(i);
		
		ItemStack printingPlateStack = PrintBookRecipe.getPrintingPlate(i, this.printingPlate);
		
		if(printingPlateStack != null) {
			result.add(printingPlateStack.clone());
		}
		
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack paper = new ItemStack(Material.PAPER, this.outputAmount);
		ISUtils.setName(paper, this.title);
		
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(paper);
		stacks.add(PrintingPlateRecipe.getPrintingPlateRepresentation(this.printingPlate, PrintingPlateRecipe.itemName));

		if (i == null) {
			return stacks;
		}
		
		int possibleRuns = input.getMultiplesContainedIn(i);
		
		for (ItemStack is : stacks) {
			ISUtils.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}
		
		return stacks;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.PAPER);
		
		ISUtils.setName(res, getName());
		
		return res;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "PRINTNOTE";
	}
}
