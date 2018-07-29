package cr0s.warpdrive.event;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

public class ClientHandler {
	
	private boolean isSneaking;
	private boolean isCreativeMode;
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltipEvent_first(final ItemTooltipEvent event) {
		if (event.getEntityPlayer() == null) {
			return;
		}
		
		// add dictionary information
		if (Dictionary.ITEMS_BREATHING_HELMET.contains(event.getItemStack().getItem())) {
			Commons.addTooltip(event.getToolTip(), new TextComponentTranslation("warpdrive.tooltip.item_tag.breathing_helmet").getFormattedText());
		}
		if (Dictionary.ITEMS_FLYINSPACE.contains(event.getItemStack().getItem())) {
			Commons.addTooltip(event.getToolTip(), new TextComponentTranslation("warpdrive.tooltip.item_tag.fly_in_space").getFormattedText());
		}
		if (Dictionary.ITEMS_NOFALLDAMAGE.contains(event.getItemStack().getItem())) {
			Commons.addTooltip(event.getToolTip(), new TextComponentTranslation("warpdrive.tooltip.item_tag.no_fall_damage").getFormattedText());
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onTooltipEvent_last(final ItemTooltipEvent event) {
		if (event.getEntityPlayer() == null) {
			return;
		}
		
		isSneaking = event.getEntityPlayer().isSneaking();
		isCreativeMode = event.getEntityPlayer().capabilities.isCreativeMode;
		
		// cleanup the mess every mods add (notably the registry name)
		Commons.cleanupTooltip(event.getToolTip());
		
		// add block/items details
		final Block block = Block.getBlockFromItem(event.getItemStack().getItem());
		if (block != Blocks.AIR) {
			addBlockDetails(event, block);
		} else {
			addItemDetails(event, event.getItemStack().getItem());
		}
		
		// add burn time details
		if (WarpDriveConfig.CLIENT_TOOLTIP_BURN_TIME.isEnabled(isSneaking, isCreativeMode)) {
			final int fuelEvent = ForgeEventFactory.getItemBurnTime(event.getItemStack());
			final int fuelFurnace = Math.round(TileEntityFurnace.getItemBurnTime(event.getItemStack()));
			final int fuelValue = fuelEvent >= 0 ? 0 : fuelFurnace;
			if (fuelValue > 0) {
				Commons.addTooltip(event.getToolTip(), String.format("Fuel to burn %.1f ores", fuelValue / 200.0F));
			}
		}
		
		// add ore dictionary names
		if (WarpDriveConfig.CLIENT_TOOLTIP_ORE_DICTIONARY_NAME.isEnabled(isSneaking, isCreativeMode)) {
			final int[] idOres = OreDictionary.getOreIDs(event.getItemStack());
			if (idOres.length != 0) {
				Commons.addTooltip(event.getToolTip(), "Ore dictionary names:");
				for (final int idOre : idOres) {
					final String nameOre = OreDictionary.getOreName(idOre);
					Commons.addTooltip(event.getToolTip(), "- " + nameOre);
				}
			}
		}
	}
	
	public void addBlockDetails(final ItemTooltipEvent event, final Block block) {
		// item registry name
		final ResourceLocation registryNameItem = event.getItemStack().getItem().getRegistryName();
		if (registryNameItem == null) {
			Commons.addTooltip(event.getToolTip(), "§4Invalid item with no registry name!");
			return;
		}
		
		// registry name
		if (WarpDriveConfig.CLIENT_TOOLTIP_REGISTRY_NAME.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final ResourceLocation registryNameBlock = Block.REGISTRY.getNameForObject(block);
				// noinspection ConstantConditions
				if (registryNameBlock != null) {
					Commons.addTooltip(event.getToolTip(), "§8" + registryNameBlock);
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// tool related stats
		IBlockState blockState = null;
		try {
			blockState = block.getStateFromMeta(event.getItemStack().getItemDamage());
		} catch (final AssertionError assertionError) {
			// assertionError.printStackTrace();
			if (!registryNameItem.toString().equals("ic2:te")) {
				// noinspection ConstantConditions
				WarpDrive.logger.error(String.format("Assertion error on item stack %s with state %s",
				                                     event.getItemStack(), (blockState != null) ? blockState : "-null-"));
			}
		}
		if ( WarpDriveConfig.CLIENT_TOOLTIP_HARVESTING.isEnabled(isSneaking, isCreativeMode)
		  && blockState != null ) {
			try {
				final String harvestTool = block.getHarvestTool(blockState);
				if (harvestTool != null) {
					Commons.addTooltip(event.getToolTip(), String.format("Harvest with %s (%d)",
					                                                     harvestTool, 
					                                                     block.getHarvestLevel(blockState)));
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// generic properties
		if ( WarpDriveConfig.CLIENT_TOOLTIP_OPACITY.isEnabled(isSneaking, isCreativeMode)
		  && blockState != null ) {
			try {
				Commons.addTooltip(event.getToolTip(), String.format("§8Light opacity is %d",
				                                                     block.getLightOpacity(blockState)));
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		if (WarpDriveConfig.CLIENT_TOOLTIP_HARDNESS.isEnabled(isSneaking, isCreativeMode)) {
			try {
				Commons.addTooltip(event.getToolTip(), String.format("§8Hardness is %.1f",
				                                                     (float) WarpDrive.fieldBlockHardness.get(block)));
			} catch (final Exception exception) {
				// no operation
			}
			Commons.addTooltip(event.getToolTip(), String.format("§8Explosion resistance is %.1f",
			                                                     block.getExplosionResistance(null)));
		}
		
		// flammability
		if (WarpDriveConfig.CLIENT_TOOLTIP_FLAMMABILITY.isEnabled(isSneaking, isCreativeMode)) {
			try {
				final int flammability = Blocks.FIRE.getFlammability(block);
				final int fireSpread = Blocks.FIRE.getEncouragement(block);
				if (flammability > 0) {
					Commons.addTooltip(event.getToolTip(), String.format("§8Flammability is %d, spread %d",
					                                                     flammability, fireSpread));
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
						Commons.addTooltip(event.getToolTip(), String.format("Gaz viscosity is %d",
						                                                     fluid.getViscosity()));
						Commons.addTooltip(event.getToolTip(), String.format("Gaz density is %d",
						                                                     fluid.getDensity()));
					} else {
						Commons.addTooltip(event.getToolTip(), String.format("Liquid viscosity is %d",
						                                                     fluid.getViscosity()));
						Commons.addTooltip(event.getToolTip(), String.format("Liquid density is %d",
						                                                     fluid.getDensity()));
					}
					Commons.addTooltip(event.getToolTip(), String.format("Temperature is %d K",
					                                                     fluid.getTemperature()));
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
				final ResourceLocation registryNameItem = Item.REGISTRY.getNameForObject(item);
				if (registryNameItem == null) {
					Commons.addTooltip(event.getToolTip(), "§4Invalid item with no registry name!");
					return;
				}
				Commons.addTooltip(event.getToolTip(), "§8" + registryNameItem);
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// (item duration can't be directly understood => out)
		
		// durability
		if (WarpDriveConfig.CLIENT_TOOLTIP_DURABILITY.isEnabled(isSneaking, isCreativeMode)) {
			try {
				if (event.getItemStack().isItemStackDamageable()) {
					Commons.addTooltip(event.getToolTip(), String.format("Durability: %d / %d",
					                                                     event.getItemStack().getMaxDamage() - event.getItemStack().getItemDamage(),
					                                                     event.getItemStack().getMaxDamage()));
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
		
		// armor stats
		if (WarpDriveConfig.CLIENT_TOOLTIP_ARMOR.isEnabled(isSneaking, isCreativeMode)) {
			try {
				if (item instanceof ItemArmor) {
					Commons.addTooltip(event.getToolTip(), String.format("Armor points: %d",
					                                                     ((ItemArmor) item).damageReduceAmount));
					final ArmorMaterial armorMaterial = ((ItemArmor) item).getArmorMaterial();
					Commons.addTooltip(event.getToolTip(), String.format("Enchantability: %d",
					                                                     armorMaterial.getEnchantability()));
					
					if (WarpDriveConfig.CLIENT_TOOLTIP_REPAIR_WITH.isEnabled(isSneaking, isCreativeMode)) {
						final ItemStack itemStackRepair = armorMaterial.getRepairItemStack();
						if (!itemStackRepair.isEmpty()) {
							Commons.addTooltip(event.getToolTip(), String.format("Repair with %s",
							                                                     itemStackRepair.getTranslationKey()));
						}
					}
				}
			} catch (final Exception exception) {
				// no operation
			}
		}
	}
	
	@SubscribeEvent
	public void onClientTick(final ClientTickEvent event) {
		if (event.side != Side.CLIENT || event.phase != Phase.END) {
			return;
		}
		
		WarpDrive.cloaks.onClientTick();
	}
}
