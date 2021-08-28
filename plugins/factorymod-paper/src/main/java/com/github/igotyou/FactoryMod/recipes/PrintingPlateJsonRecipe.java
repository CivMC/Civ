package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class PrintingPlateJsonRecipe extends PrintingPlateRecipe {
	/*
	 * WARNING
	 *
	 * This inherits from PrintingPlateRecipe
	 *                            ^^^^^
	 * Not PrintingPressRecipe, as would normally be expected.
	 *             ^^^^^
	 *
	 * This is because this recipe creates a printing plate, like the super class.
	 */

	public PrintingPlateJsonRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap output) {
		super(identifier, name, productionTime, input, output);
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory inputInv) {
		String[] pages = String.join("", ((BookMeta) getBook(inputInv).getItemMeta()).getPages()).split("<<PAGE>>");

		for (String page : pages) {
			try {
				Gson gson = new Gson();
				JsonElement element = gson.fromJson(page, JsonElement.class);

				String result = checkForIllegalSections(element);
				if (result != null) {
					factioryError(inputInv, "Banned Tag Error", "Error Message: " + result);

					return false;
				}
			} catch (JsonSyntaxException e) {
				factioryError(inputInv, "JSON Syntax Error", e.toString());

				return false;
			}
		}

		return this.input.isContainedIn(inputInv) && getBook(inputInv) != null;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);

		ItemStack book = getBook(inputInv);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		if (!bookMeta.hasGeneration()) {
			bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
		}
		String serialNumber = UUID.randomUUID().toString();

		String[] pages = String.join("", bookMeta.getPages()).split("<<PAGE>>");
		NBTTagList pagesNBT = new NBTTagList();

		for (String page : pages) {
			pagesNBT.add(NBTTagString.a(page));
		}

		NBTTagCompound bookNBT = new NBTTagCompound();
		bookNBT.setInt("generation", bookMeta.getGeneration().ordinal());
		bookNBT.setString("author", bookMeta.getAuthor());
		bookNBT.setString("title", bookMeta.getTitle());
		bookNBT.set("pages", pagesNBT);

		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();

		if (toRemove.isContainedIn(inputInv) && toRemove.removeSafelyFrom(inputInv)) {
			for (ItemStack is : toAdd.getItemStackRepresentation()) {
				is = addTags(serialNumber, is, bookNBT);

				ItemUtils.setDisplayName(is, itemName);
				ItemUtils.setLore(is,
						serialNumber,
						ChatColor.WHITE + bookMeta.getTitle(),
						ChatColor.GRAY + "by " + bookMeta.getAuthor(),
						ChatColor.GRAY + getGenerationName(bookMeta.getGeneration()),
						ChatColor.GRAY + "(JSON)"
				);
				is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
				is.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
				outputInv.addItem(is);
			}
		}

		logAfterRecipeRun(combo, fccf);
		return true;
	}

	@Override
	public String getTypeIdentifier() {
		return "PRINTINGPLATEJSON";
	}

	/**
	 * Maximum recursion of raw JSON text allowed before rejecting text.
	 *
	 * Very large because recursion is an important feature of JSON text.
	 */
	private final int MAX_ITER_COUNT = 15;

	/**
	 * Validates if the text to be put into a book is safe.
	 *
	 * Will check if the book text contains a scoreboard value, an entity selector, an NBT path, a clickable URL/File,
	 *     a clickable command (clickable command suggestions are explicitly allowed), clipboard copying (can exploit
	 *     windows visibilities), selectors, or entity hovers.
	 *
	 * @param bookText The Raw JSON Text Format to check.
	 * @return Why the text is banned, or null if it is legal.
	 * @throws StackOverflowError If the text contains too much recursion.
	 */
	public String checkForIllegalSections(JsonElement bookText) {
		return checkForIllegalSections(bookText, 0);
	}

	private String checkForIllegalSections(JsonElement bookText, int iterCount) {
		if (iterCount > MAX_ITER_COUNT) {
			return "maximum number of nested elements reached, please simplify the json structure: " + iterCount;
		}

		if (bookText == null) {
			return "Book does not contain any text.";
		}

		if (bookText.isJsonObject()) {
			return checkForIllegalSections((JsonObject) bookText, iterCount + 1);
		} else if (bookText.isJsonArray()) {
			JsonArray array = bookText.getAsJsonArray();
			for (JsonElement element : array) {
				String result = checkForIllegalSections(element, iterCount + 1);
				if (result != null) {
					return result;
				}
			}
		}

		// The booktext is a safe type or a array with no bad elements
		return null;
	}

	private String checkForIllegalSections(JsonObject bookText, int iterCount) {
		if (iterCount > MAX_ITER_COUNT) {
			return "maximum number of nested elements reached, please simplify the json structure: " + iterCount;
		}

		if (bookText.has("extra")) {
			JsonElement extra = bookText.get("extra");

			String result = checkForIllegalSections(extra, iterCount + 1);
			if (result != null) {
				return result;
			}
		}

		// List of keys that are not allowed at all. Always can be used to cheat, with no legit uses.
		String[] simpleForbiddenKeys = {
				"score", // gets the values from the scoreboard. At worst exposes secrets, at best useless.
				"selector", // gets lists of all entities in the world that fit a condition
				"nbt" // gets any nbt key in the entire world
		};

		for (String forbiddenKey : simpleForbiddenKeys) {
			if (bookText.has(forbiddenKey)) {
				return "Contains banned key: " + forbiddenKey;
			}
		}

		if (bookText.has("clickEvent")) {
			JsonElement clickEvent = bookText.get("clickEvent");

			if (clickEvent.isJsonObject()) {
				JsonObject clickEventObject = clickEvent.getAsJsonObject();

				if (clickEventObject.has("action")) {
					JsonElement action = clickEventObject.get("action");
					if (action.isJsonPrimitive() && action.getAsJsonPrimitive().isString()) {
						String actionString = action.getAsString();

						String[] forbiddenActions = {
								"open_url",
								"open_file",
								"run_command",
								"copy_to_clipboard"
						};

						/*
						 * Allowed actions:
						 *
						 * "suggest_command": Just fills in the chat box, doesn't do anything unless the player presses enter.
						 *
						 * "change_page": Changes the currently selected page to the value page.
						 */

						for (String forbiddenAction : forbiddenActions) {
							if (actionString.equals(forbiddenAction)) {
								return "Contains forbidden action for clickEvent: " + forbiddenAction;
							}
						}
					} else {
						return "Invalid Tag-Level Syntax: action of clickEvent is not a string";
					}
				} else {
					return "Invalid Tag-Level Syntax: clickEvent does not have an action.";
				}
			} else {
				return "Invalid Tag-Level Syntax: clickEvent is not an object.";
			}
		}

		if (bookText.has("hoverEvent")) {
			JsonElement hoverEventElement = bookText.get("hoverEvent");
			if (hoverEventElement.isJsonObject()) {
				JsonObject hoverEvent = hoverEventElement.getAsJsonObject();
				if (hoverEvent.has("action")) {
					JsonElement actionElement = hoverEvent.get("action");
					if (actionElement.isJsonPrimitive() && actionElement.getAsJsonPrimitive().isString()) {
						String action = actionElement.getAsString();

						if (action.equals("show_entity")) {
							// Shows the details about an entity in the world, I think.
							return "Contains banned action for hoverEvent: show_entity";
						}

						if (action.equals("show_text")) {
							if (hoverEvent.has("contents")) {
								JsonElement contents = hoverEvent.get("contents");

								String result = checkForIllegalSections(contents, iterCount + 1);
								if (result != null) {
									return result;
								}
							}

							if (hoverEvent.has("value")) {
								JsonElement value = hoverEvent.get("value");

								String result = checkForIllegalSections(value, iterCount + 1);
								if (result != null) {
									return result;
								}
							}
						}

						// "show_item" is allowed because it can only show a fixed NBT, and can not get any info
						// from the game world.
					} else {
						return "Tag-Level Syntax Error: action of hoverEvent is not a string.";
					}
				} else {
					return "Tag-Level Syntax Error: hoverEvent does not have action.";
				}
			} else {
				return "Tag-Level Syntax Error: hoverEvent is not an object.";
			}
		}

		if (bookText.has("translate") && bookText.has("with")) {
			JsonElement withElement = bookText.get("with");
			if (withElement.isJsonArray()) {
				for (JsonElement element : withElement.getAsJsonArray()) {
					String result = checkForIllegalSections(element, iterCount + 1);
					if (result != null) {
						return result;
					}
				}
			} else {
				return "Tag-Level Syntax Error: with is not an array";
			}
		}

		/*
		 * List of explicitly allowed keys:
		 *
		 * "text": Literally just a string.
		 * "translate": A string from the user's language files. Has no cheating use, although a screenshot of the
		 *     book will expose the user's set language. However, any screenshot of the interface
		 *     will expose the user's set languate
		 * "keybind": Shows the user's configured keybind for a specific key. A screensot will expose the user's
		 *     set keybind, but not a big deal.
		 * "color", "font", "bold", "italic", "underlined", "strikethrough", "obfuscated":
		 *     Already accessible with the standard color codes, except for the new 24-bit color and custom fonts.
		 */

		return null;
	}

	/**
	 * Puts an error button into the factory's inventory.
	 *
	 * Kind of gross, but there aren't many other ways to give feedback to the player about errors.
	 * @param inventory The factory chest.
	 * @param errorTitle The name of the button put into the inventory.
	 * @param errorDetails The lore of the button.
	 */
	public void factioryError(Inventory inventory, String errorTitle, String... errorDetails) {
		ItemStack invalidSyntaxExplanationButton = new ItemStack(Material.STONE_BUTTON, 1);

		ItemMeta meta;
		if (!invalidSyntaxExplanationButton.hasItemMeta()) {
			meta = FactoryMod.getInstance().getServer().getItemFactory().getItemMeta(Material.STONE_BUTTON);
		} else {
			meta = invalidSyntaxExplanationButton.getItemMeta();
		}

		assert meta != null;
		meta.setLore(Arrays.asList(errorDetails));

		meta.setDisplayName(errorTitle);

		invalidSyntaxExplanationButton.setItemMeta(meta);
		inventory.addItem(invalidSyntaxExplanationButton);
		// Kinda gross, but there aren't many other ways to give feedback to the player about errors.
	}
}
