package com.programmerdan.minecraft.civspy.util;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Attachable;
import org.bukkit.material.Banner;
import org.bukkit.material.Bed;
import org.bukkit.material.Button;
import org.bukkit.material.Cake;
import org.bukkit.material.Cauldron;
import org.bukkit.material.Coal;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Colorable;
import org.bukkit.material.Command;
import org.bukkit.material.Comparator;
import org.bukkit.material.Crops;
import org.bukkit.material.DetectorRail;
import org.bukkit.material.Diode;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.Dye;
import org.bukkit.material.Gate;
import org.bukkit.material.Hopper;
import org.bukkit.material.Ladder;
import org.bukkit.material.Leaves;
import org.bukkit.material.Lever;
import org.bukkit.material.LongGrass;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Mushroom;
import org.bukkit.material.NetherWarts;
import org.bukkit.material.Openable;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.material.PoweredRail;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.PressureSensor;
import org.bukkit.material.Pumpkin;
import org.bukkit.material.Rails;
import org.bukkit.material.Redstone;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.material.RedstoneWire;
import org.bukkit.material.Sandstone;
import org.bukkit.material.Sapling;
import org.bukkit.material.Sign;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.material.TexturedMaterial;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;
import org.bukkit.material.Tree;
import org.bukkit.material.Tripwire;
import org.bukkit.material.TripwireHook;
import org.bukkit.material.Vine;
import org.bukkit.material.Wood;
import org.bukkit.material.WoodenStep;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

// TODO: integrate with CivModCore and use NiceNames api.
public class ItemStackToString {

	private ItemStackToString(){}

	/**
	 * For 1.10.2 ItemStacks.
	 */
	public static String toString(ItemStack itemStack) {
		if (itemStack == null) return "Nothing";
		StringBuilder toString = new StringBuilder();

		Material material = itemStack.getType();
		ItemMeta meta = itemStack.getItemMeta();
		int amount = itemStack.getAmount();
		short durability = itemStack.getDurability();
				
		if (amount != 1) {
			toString.append(amount).append("x");
		}
		
		toString.append(material.toString());
		toString.append(":").append(durability);
		
		if (meta != null) {
			Map<Enchantment, Integer> enchants = meta.getEnchants();
			String customName = meta.getDisplayName();
			// Set<ItemFlag> flags = meta.getItemFlags(); // maybe some other time.
			List<String> lore = meta.getLore();
			
			if (meta.hasDisplayName()) {
				toString.append('/').append(customName);
			}
			
			if (meta.hasEnchants()) {
				toString.append('[');
				for (Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
					toString.append(enchant.getKey().getName()).append('/')
							.append(enchant.getValue()).append('|');
				}
				toString.deleteCharAt(toString.length() - 1);
				toString.append(']');
			}
			
			if (meta.hasLore()) {
				toString.append('{');
				for (String line : lore) {
					toString.append(line).append('/');
				}
				toString.deleteCharAt(toString.length() - 1);
				toString.append('}');
			}
			
			// custom metas
			if (meta instanceof BannerMeta) {
				BannerMeta banner = (BannerMeta) meta;
				toString.append('/').append("Pattern")
						.append(':').append(banner.getBaseColor());
				if (banner.numberOfPatterns() > 0) {
					toString.append('[');
					for (Pattern pattern : banner.getPatterns()) {
						toString.append(pattern.getColor())
								.append(':').append((pattern.getPattern() != null ? pattern.getPattern().getIdentifier() : pattern.toString()))
								.append('|');
					}
					toString.deleteCharAt(toString.length() - 1);
					toString.append(']');
 				}
			} else if (meta instanceof BlockStateMeta) {
				BlockStateMeta bsm = (BlockStateMeta) meta;
				if (bsm.hasBlockState()) {
					toString.append('/').append(toString(bsm.getBlockState()));
				}
			} else if (meta instanceof BookMeta) {
				BookMeta bookmeta = (BookMeta) meta;
				toString.append('/');
				if (bookmeta.hasTitle()) {
					toString.append('"');
					toString.append(bookmeta.getTitle());
					toString.append('"');
				}
				if (bookmeta.hasAuthor()) {
					toString.append("by_").append(bookmeta.getAuthor());
				}
				if (bookmeta.hasGeneration()) {
					if (bookmeta.getGeneration() != BookMeta.Generation.ORIGINAL)
					toString.append("_").append(bookmeta.getGeneration().toString());
				}
				if (bookmeta.hasPages()) {
					toString.append("_").append(bookmeta.getPageCount()).append("pgs");
				}
			} else if (meta instanceof EnchantmentStorageMeta) {
				toString.append("_Stored");
				toString.append('[');
				EnchantmentStorageMeta esm = (EnchantmentStorageMeta) meta;
				if (esm.hasStoredEnchants()) {
					for (Map.Entry<Enchantment, Integer> enchant : esm.getStoredEnchants().entrySet()) {
						toString.append(enchant.getKey().getName()).append('/')
								.append(enchant.getValue()).append('|');
					}
					toString.deleteCharAt(toString.length() - 1);
				}
				toString.append(']');
			} else if (meta instanceof FireworkEffectMeta) {
				FireworkEffectMeta fe = (FireworkEffectMeta) meta;
				toString.append("_").append(fe.toString());
			} else if (meta instanceof LeatherArmorMeta) {
				toString.append('/').append("Color:").append(((LeatherArmorMeta) meta).getColor().toString());
			} else if (meta instanceof MapMeta) {
				if (((MapMeta) meta).isScaling()) {
					toString.append("_Scaling");
				}
			} else if (meta instanceof PotionMeta) {
				PotionMeta potionMeta = (PotionMeta) meta;
				PotionData baseData = potionMeta.getBasePotionData();
				toString.append("_").append(baseData.getType());
				if (baseData.isExtended()) {
					toString.append("_Ext");
				} else if (baseData.isUpgraded()) {
					toString.append("_Upd");
				}
				if (potionMeta.hasCustomEffects()) {
					toString.append('[');
					for (PotionEffect effect : potionMeta.getCustomEffects()){
						toString.append(effect.getType().getName())
								.append("x").append(effect.getAmplifier())
								.append("t").append(effect.getDuration())
								.append("c").append(effect.getColor());
						if (effect.isAmbient()) {
							toString.append('/').append("Ambient");
						} else if (effect.hasParticles()) {
							toString.append('/').append("Particles");
						}
						toString.append('|');
					}
					toString.deleteCharAt(toString.length() - 1);
					toString.append(']');
				}
			} else if (meta instanceof SkullMeta) {
				SkullMeta skull = (SkullMeta) meta;
				if (skull.hasOwner()) {
					toString.append('/').append("Owner:").append(skull.getOwner());
				}
			}
		}
		
		return toString.toString();
	}
	
	/**
	 * For 1.10.2 Blocks
	 * @param block
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String toString(BlockState block) {
		if (block == null) return "Empty";
		StringBuilder toString = new StringBuilder();

		Material material = block.getType();
		MaterialData data = block.getData();
		
		if (data instanceof Directional) {
			toString.append(((Directional) data).getFacing().name()).append("_");
		}

		if (data instanceof Attachable) {
			toString.append("On").append(((Attachable) data).getAttachedFace().name()).append("_");
		}
		
		if (data instanceof Redstone) {
			toString.append(((Redstone) data).isPowered() ? "Powered_" : "Unpowered_");
		}

		if (data instanceof Colorable) {
			toString.append(((Colorable) data).getColor().name()).append("_");
		}

		if (data instanceof Openable) {
			toString.append(((Openable) data).isOpen() ? "Open_" : "Closed_");
		}
		
		if (data instanceof PressureSensor) {
			toString.append(((PressureSensor) data).isPressed() ? "Pressed_" : "Unpressed_");
		}


		if (data instanceof Banner) {
			Banner banner = (Banner) data;
			if (banner.isWallBanner()) {
				toString.append("Wall");
			}
			toString.append("Banner");
		} else if (data instanceof Bed) {
			toString.append("Bed");
		} else if (data instanceof Cake) {
			Cake cake = (Cake) data;
			toString.append(cake.getSlicesEaten()).append("/").append(cake.getSlicesRemaining()).append("_Cake");
		} else if (data instanceof Cauldron) {
			Cauldron cauldron = (Cauldron) data;
			if (cauldron.isEmpty()) {
				toString.append("Empty_");
			} else if (cauldron.isFull()) {
				toString.append("Full_");
			}
			toString.append("Cauldron");
		} else if (data instanceof Coal) {
			toString.append(((Coal) data).getType().name());
		} else if (data instanceof CocoaPlant) {
			toString.append(((CocoaPlant) data).getSize().name());
			toString.append("_CocoaPlant");
		} else if (data instanceof Command) {
			toString.append("CommandBlock");
		} else if (data instanceof Comparator) {
			if (((Comparator) data).isSubtractionMode()) {
				toString.append("Subtracting_");
			}
			toString.append("Comparator");
		} else if (data instanceof Crops) {
			toString.append(((Crops)data).getState().name());
			toString.append("_").append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Diode) {
			toString.append(((Diode)data).getDelay()).append("t_Delay");
			toString.append("_Diode");
		} else if (data instanceof Door) {
			Door door = (Door) data;
			if (door.getHinge()) {
				toString.append("Right_");
			} else {
				toString.append("Left_");
			}
			toString.append("_").append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Dye) {
			toString.append("Dye_").append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Gate) {
			toString.append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Hopper) {
			toString.append(((Hopper) data).isActive() ? "Active_" : "Inactive_").append("Hopper");
		} else if (data instanceof LongGrass) {
			toString.append(((LongGrass) data).getSpecies().name()).append("_LongGrass");
		} else if (data instanceof Mushroom) {
			toString.append(((Mushroom) data).getBlockTexture().name()).append("_GiantMushroom");
		} else if (data instanceof NetherWarts) {
			toString.append(((NetherWarts) data).getState().name()).append("_NetherWarts");
		} else if (data instanceof PistonBaseMaterial) {
			toString.append(((PistonBaseMaterial) data).isSticky() ? "StickyPiston" : "Piston");
		} else if (data instanceof PistonExtensionMaterial) {
			toString.append(((PistonExtensionMaterial) data).isSticky() ? "StickyPistonExtension" : "PistonExtension");
		} else if (data instanceof PressurePlate) {
			toString.append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Pumpkin) {
			toString.append(((Pumpkin) data).isLit() ? "LitPumpkin" : "Pumpkin");
		} else if (data instanceof Rails) {
			Rails rails = (Rails) data;
			toString.append(rails.isCurve() ? "Curved_" : "Straight_")
					.append(rails.getDirection().toString()).append("_")
					.append(rails.isOnSlope() ? "Sloped_" : "Flat_");
			if (rails instanceof DetectorRail) {
				toString.append("DetectorRail");
			} else if (rails instanceof PoweredRail) {
				toString.append("PoweredRail");
			} else {
				toString.append("Rail");
			}
		} else if (data instanceof RedstoneWire) {
			toString.append("RedstoneWire");
		} else if (data instanceof Sandstone) {
			toString.append(((Sandstone) data).getType().toString())
					.append("_Sandstone");
		} else if (data instanceof Sign) {
			toString.append(((Sign)data).isWallSign() ? "OnWall_" : "Standing_")
					.append("Sign");
		} else if (data instanceof Button) {
			toString.append(material.toString());
			toString.append("_Button");
		} else if (data instanceof Ladder) {
			toString.append("Ladder");
		} else if (data instanceof Lever) {
			toString.append("Lever");
		} else if (data instanceof RedstoneTorch) {
			toString.append("RedstoneTorch");
		} else if (data instanceof Torch) {
			toString.append("Torch");
		} else if (data instanceof TrapDoor) {
			if (((TrapDoor) data).isInverted()) {
				toString.append("Inverted_");
			}
			toString.append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof TripwireHook) {
			TripwireHook hook = (TripwireHook) data;
			toString.append(hook.isActivated() ? "Activated_" : "Inactive_")
					.append(hook.isConnected() ? "Connected_" : "Disconnected_")
					.append("TripwireHook");
		} else if (data instanceof Stairs) {
			Stairs stairs = (Stairs) data;
			if (stairs.isInverted()) {
				toString.append("Inverted_");
			}
			toString.append("Ascends").append(stairs.getAscendingDirection().toString()).append("_");
			toString.append("Descends").append(stairs.getDescendingDirection().toString()).append("_");
			toString.append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof TexturedMaterial) {
			if (data instanceof Step) {
				if (((Step) data).isInverted()) {
					toString.append("Inverted_");
				}
			}
			toString.append("Textured").append(((TexturedMaterial)data).getMaterial().toString());
			toString.append("_").append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Tripwire) {
			Tripwire tripwire = (Tripwire) data;
			toString.append(tripwire.isActivated() ? "Activated_" : "Inactivated_");
			toString.append(tripwire.isObjectTriggering() ? "ObjectTriggered_": "ObjectNotTriggered_");
			toString.append("Tripwire");
		} else if (data instanceof Vine) {
			toString.append("Vine");
		} else if (data instanceof Wood) {
			toString.append(((Wood) data).getSpecies().toString()).append("_");
			if (data instanceof Leaves) {
				Leaves leaves = (Leaves) data;
				if (leaves.isDecaying()) {
					toString.append("Decaying_");
				} else if (leaves.isDecayable()) {
					toString.append("Decayable_");
				}
			} else if (data instanceof Sapling) {
				if (((Sapling) data).isInstantGrowable()) {
					toString.append("InstantGrowable_");
				}
			} else if (data instanceof Tree) {
				toString.append(((Tree)data).getDirection().toString()).append("_");
			} else if (data instanceof WoodenStep) {
				if (((WoodenStep)data).isInverted()) {
					toString.append("Inverted_");
				}
			}
			toString.append(material.toString());
			if (data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		} else if (data instanceof Wool) {
			toString.append("Wool");
		} else {
			toString.append(material != null ? material.toString() : "Null?");
			if (data != null && data.getData() != 0) {
				toString.append(":").append(data.getData());
			}
		}
		return toString.toString();
	}
}
