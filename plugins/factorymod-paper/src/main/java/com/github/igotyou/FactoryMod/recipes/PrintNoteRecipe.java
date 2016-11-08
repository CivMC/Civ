/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PrintNoteRecipe extends PrintBookRecipe {
	public static final String pamphletName = "Pamphlet";
	public static final String secureNoteName = "Secure Note";
	
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
			List<String> textLines = new ArrayList<String>();
			TagManager printingPlateTag = new TagManager(printingPlateStack);
			
			if(this.secureNote) {
				String serialNumber = printingPlateTag.getString("SN");
				textLines.add(serialNumber);
			}
			
			addTextLines(printingPlateStack, textLines);

			ItemStack paper = new ItemStack(Material.PAPER, getOutputAmount());
			
			ItemMeta paperMeta = paper.getItemMeta();
			paperMeta.setLore(textLines);
			paperMeta.setDisplayName(ChatColor.RESET + printingPlateTag.getCompound("Book").getString("title"));
			paper.setItemMeta(paperMeta);

			i.addItem(paper);
		}
		
		logAfterRecipeRun(i, fccf);
	}
	
	private void addTextLines(ItemStack printingPlateStack, List<String> result) {
		ItemStack book = createBook(printingPlateStack, 1);
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
