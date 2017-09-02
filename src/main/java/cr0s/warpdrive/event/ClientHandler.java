package cr0s.warpdrive.event;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltipEvent(ItemTooltipEvent event) {
		if (event.getEntityPlayer() == null) {
			return;
		}
		if (Dictionary.ITEMS_BREATHING_HELMET.contains(event.getItemStack().getItem()) && WarpDriveConfig.isIndustrialCraft2Loaded) {
			Commons.addTooltip(event.getToolTip(), new TextComponentTranslation("warpdrive.tooltip.itemTag.breathingHelmet").getFormattedText());
		}
		if (Dictionary.ITEMS_FLYINSPACE.contains(event.getItemStack().getItem())) {
			Commons.addTooltip(event.getToolTip(), new TextComponentTranslation("warpdrive.tooltip.itemTag.flyInSpace").getFormattedText());
		}
		if (Dictionary.ITEMS_NOFALLDAMAGE.contains(event.getItemStack().getItem())) {
			Commons.addTooltip(event.getToolTip(), new TextComponentTranslation("warpdrive.tooltip.itemTag.noFallDamage").getFormattedText());
		}
		if (WarpDrive.isDev && event.getEntityPlayer().capabilities.isCreativeMode) {// disabled in production
			Block block = Block.getBlockFromItem(event.getItemStack().getItem());
			if (block != Blocks.AIR && block != null) {
				try {
					ResourceLocation resourceLocation = Block.REGISTRY.getNameForObject(block);
					Commons.addTooltip(event.getToolTip(), "" + resourceLocation + "");
				} catch(Exception exception) {
					// no operation
				}
				
				try {
					IBlockState blockState = block.getStateFromMeta(event.getItemStack().getItemDamage());
					String harvestTool = block.getHarvestTool(blockState);
					Commons.addTooltip(event.getToolTip(), "Harvest with " + harvestTool + " (" + block.getHarvestLevel(blockState) + ")");
				} catch(Exception | AssertionError exception) {
					// no operation
				}
				
				try {
					IBlockState blockState = block.getStateFromMeta(event.getItemStack().getItemDamage());
					Commons.addTooltip(event.getToolTip(), "Light opacity is " + block.getLightOpacity(blockState));
				} catch(Exception | AssertionError exception) {
					// no operation
				}
				
				try {
					Commons.addTooltip(event.getToolTip(), "Hardness is " + (float) WarpDrive.fieldBlockHardness.get(block));
				} catch(Exception exception) {
					// no operation
				}
				try {
					Commons.addTooltip(event.getToolTip(), "Explosion resistance is " + block.getExplosionResistance(null));
				} catch(Exception exception) {
					// no operation
				}
				Commons.addTooltip(event.getToolTip(), "Explosion resistance is " + block.getExplosionResistance(null));
				
			} else {
				try {
					String uniqueName = event.getItemStack().getItem().getRegistryName().toString();
					Commons.addTooltip(event.getToolTip(), "" + uniqueName + "");
				} catch(Exception exception) {
					// no operation
				}
			}
		}
	}
}
