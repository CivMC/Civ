/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PrintNoteRecipe extends PrintBookRecipe {
	private static class BookInfo {
		public String title;
		public List<String> lines;
	}
	
	private static final String pamphletName = "Pamphlet";
	private static final String secureNoteName = "Secure Note";
	
	private boolean secureNote;
	private String title;
	
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
		super(identifier, name, productionTime, input, printingPlate, outputAmount);

		this.secureNote = secureNote;
		
		if(title != null && title.length() > 0) {
			this.title = title;
		} else {
			this.title = secureNote ? secureNoteName: pamphletName;
		}
	}	

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		
		ItemStack printingPlateStack = getPrintingPlateItemStack(i, getPrintingPlate());
		ItemMap toRemove = this.input.clone();

		if (printingPlateStack != null
				&& toRemove.isContainedIn(i)
				&& toRemove.removeSafelyFrom(i)
				)
		{
			BookInfo info = getBookInfo(printingPlateStack);
			ItemStack paper = new ItemStack(Material.PAPER, getOutputAmount());
			
			ItemMeta paperMeta = paper.getItemMeta();
			paperMeta.setLore(info.lines);
			paperMeta.setDisplayName(ChatColor.RESET + info.title);
			paper.setItemMeta(paperMeta);

			i.addItem(paper);
		}
		
		logAfterRecipeRun(i, fccf);
	}
	
	private BookInfo getBookInfo(ItemStack printingPlateStack) {
		ItemStack book = createBook(printingPlateStack, 1);
		BookMeta bookMeta = (BookMeta)book.getItemMeta();
		String text = bookMeta.getPageCount() > 0 ? bookMeta.getPage(1): "";
		String[] lines = text.split("\n");
		List<String> fixedLines = new ArrayList<String>();
		
		for(String line : lines) {
			String fixedLine = line.replaceAll("§0", ChatColor.GRAY.toString());
			
			if(fixedLines.size() == 0) {
				fixedLine = ChatColor.GRAY + fixedLine;
			}
			
			fixedLines.add(fixedLine);
		}
		
		String bookTitle = bookMeta.getTitle();
		
		BookInfo info = new BookInfo();
		info.lines = fixedLines;
		info.title = bookTitle != null && bookTitle.length() > 0 ? bookTitle: this.title;
		
		if(this.secureNote) {
			TagManager printingPlateTag = new TagManager(printingPlateStack);
			String serialNumber = printingPlateTag.getString("SN");
			info.lines.add(serialNumber);
		}

		return info;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack paper = new ItemStack(Material.PAPER, getOutputAmount());
		ISUtils.setName(paper, this.title);
		
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(paper);
		stacks.add(getPrintingPlateRepresentation(getPrintingPlate(), PrintingPlateRecipe.itemName));

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
