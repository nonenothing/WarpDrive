package cr0s.warpdrive.event;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class ClientHandler {
	
	private boolean isSneaking;
	private boolean isCreativeMode;
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltipEvent(final ItemTooltipEvent event) {
		if (event.entityPlayer == null) {
			return;
		}
		if (Dictionary.ITEMS_BREATHING_HELMET.contains(event.itemStack.getItem()) && WarpDriveConfig.isIndustrialCraft2Loaded) {
			Commons.addTooltip(event.toolTip, StatCollector.translateToLocalFormatted("warpdrive.tooltip.itemTag.breathingHelmet"));
		}
		if (Dictionary.ITEMS_FLYINSPACE.contains(event.itemStack.getItem())) {
			Commons.addTooltip(event.toolTip, StatCollector.translateToLocalFormatted("warpdrive.tooltip.itemTag.flyInSpace"));
		}
		if (Dictionary.ITEMS_NOFALLDAMAGE.contains(event.itemStack.getItem())) {
			Commons.addTooltip(event.toolTip, StatCollector.translateToLocalFormatted("warpdrive.tooltip.itemTag.noFallDamage"));
		}
		
		isSneaking = event.entityPlayer.isSneaking();
		isCreativeMode = event.entityPlayer.capabilities.isCreativeMode;
		
		// add block/items details
		final Block block = Block.getBlockFromItem(event.itemStack.getItem());
		if (block != Blocks.air) {
			addBlockDetails(event, block);
		} else {
			addItemDetails(event, event.itemStack.getItem());
		}
		
		// add burn time details (vanilla only register server side?)
		if (WarpDriveConfig.CLIENT_TOOLTIP_BURN_TIME.isEnabled(isSneaking, isCreativeMode)) {
			final int fuelValue = GameRegistry.getFuelValue(event.itemStack);
			if (fuelValue > 0) {
				Commons.addTooltip(event.toolTip, String.format("Burn time is %d (%.1f ores)", fuelValue, fuelValue / 200.0F));
			}
		}
		
		// add ore dictionary names
		if (WarpDriveConfig.CLIENT_TOOLTIP_ORE_DICTIONARY_NAME.isEnabled(isSneaking, isCreativeMode)) {
			final int[] idOres = OreDictionary.getOreIDs(event.itemStack);
			if (idOres.length != 0) {
				Commons.addTooltip(event.toolTip, "Ore dictionary names:");
				for (final int idOre : idOres) {
					final String nameOre = OreDictionary.getOreName(idOre);
					Commons.addTooltip(event.toolTip, "- " + nameOre);
				}
			}
		}
	}
	
	public void addBlockDetails(final ItemTooltipEvent event, final Block block) {
		// registry name
		if (WarpDriveConfig.CLIENT_TOOLTIP_REGISTRY_NAME.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final String uniqueName = Block.blockRegistry.getNameForObject(block);
				if (uniqueName != null) {
					Commons.addTooltip(event.toolTip, uniqueName);
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// tool related stats
		if (WarpDriveConfig.CLIENT_TOOLTIP_HARVESTING.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final String harvestTool = block.getHarvestTool(event.itemStack.getItemDamage());
				if (harvestTool != null) {
					Commons.addTooltip(event.toolTip, String.format("Harvest with %s (%d)",
					                                                harvestTool,
					                                                block.getHarvestLevel(event.itemStack.getItemDamage())));
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// generic properties
		if (WarpDriveConfig.CLIENT_TOOLTIP_OPACITY.isEnabled(isSneaking, isCreativeMode)) {
			Commons.addTooltip(event.toolTip, String.format("Light opacity is %d", block.getLightOpacity()));
		}
		
		if (WarpDriveConfig.CLIENT_TOOLTIP_HARDNESS.isEnabled(isSneaking, isCreativeMode)) {
			try {
				Commons.addTooltip(event.toolTip, String.format("Hardness is %.1f", (float) WarpDrive.fieldBlockHardness.get(block)));
			} catch (final Exception exception) {
				// no operation
			}
			Commons.addTooltip(event.toolTip, String.format("Explosion resistance is %.1f", + block.getExplosionResistance(null)));
		}
		
		// flammability
		if (WarpDriveConfig.CLIENT_TOOLTIP_FLAMMABILITY.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final int flammability = Blocks.fire.getFlammability(block);
				final int fireSpread = Blocks.fire.getEncouragement(block);
				if (flammability > 0) {
					Commons.addTooltip(event.toolTip, String.format("Flammable: %d, spread %d", flammability, fireSpread));
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// fluid stats
		if (WarpDriveConfig.CLIENT_TOOLTIP_FLUID.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
				if (fluid != null) {
					if (fluid.isGaseous()) {
						Commons.addTooltip(event.toolTip, String.format("Gaz viscosity is %d", fluid.getViscosity()));
						Commons.addTooltip(event.toolTip, String.format("Gaz density is %d", fluid.getDensity()));
					} else {
						Commons.addTooltip(event.toolTip, String.format("Liquid viscosity is %d", fluid.getViscosity()));
						Commons.addTooltip(event.toolTip, String.format("Liquid density is %d", fluid.getDensity()));
					}
					Commons.addTooltip(event.toolTip, String.format("Temperature is %d K", fluid.getTemperature()));
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
	}
	
	public void addItemDetails(final ItemTooltipEvent event, final Item item) {
		// registry name
		if (WarpDriveConfig.CLIENT_TOOLTIP_REGISTRY_NAME.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final String uniqueName = Item.itemRegistry.getNameForObject(item);
				if (uniqueName != null) {
					Commons.addTooltip(event.toolTip, uniqueName);
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// (item duration can't be directly understood => out)
		
		// durability
		if (WarpDriveConfig.CLIENT_TOOLTIP_DURABILITY.isEnabled(isSneaking, isCreativeMode)) {
			try {
				if (event.itemStack.isItemStackDamageable()) {
					Commons.addTooltip(event.toolTip, String.format("Durability: %d / %d",
					                                                event.itemStack.getMaxDamage() - event.itemStack.getItemDamage(),
					                                                event.itemStack.getMaxDamage()));
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// armor stats
		if (WarpDriveConfig.CLIENT_TOOLTIP_ARMOR.isEnabled(isSneaking, isCreativeMode)) {
			try {
				if (item instanceof ItemArmor) {
					Commons.addTooltip(event.toolTip, String.format("Armor points: %d",
					                                                ((ItemArmor) item).damageReduceAmount));
					final ArmorMaterial armorMaterial = ((ItemArmor) item).getArmorMaterial();
					Commons.addTooltip(event.toolTip, String.format("Enchantability: %d",
					                                                armorMaterial.getEnchantability()));
					
					if (WarpDriveConfig.CLIENT_TOOLTIP_REPAIR_WITH.isEnabled(isSneaking, isCreativeMode)) {
						final Item itemRepair = armorMaterial.func_151685_b();
						if (itemRepair != null) {
							Commons.addTooltip(event.toolTip, String.format("Repair with %s",
							                                                armorMaterial.func_151685_b().getUnlocalizedName()));
						}
					}
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
	}
}
