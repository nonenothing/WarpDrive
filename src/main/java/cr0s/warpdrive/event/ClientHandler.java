package cr0s.warpdrive.event;

import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cr0s.warpdrive.WarpDrive;

public class ClientHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltipEvent(ItemTooltipEvent event) {
		if (event.entityPlayer == null || !event.entityPlayer.capabilities.isCreativeMode) {
			return;
		}
		if (Dictionary.ITEMS_BREATHING_HELMET.contains(event.itemStack.getItem()) && WarpDriveConfig.isIndustrialCraft2Loaded) {
			WarpDrive.addTooltip(event.toolTip, StatCollector.translateToLocalFormatted("warpdrive.tooltip.itemTag.breathingHelmet"));
		}
		if (Dictionary.ITEMS_FLYINSPACE.contains(event.itemStack.getItem())) {
			WarpDrive.addTooltip(event.toolTip, StatCollector.translateToLocalFormatted("warpdrive.tooltip.itemTag.flyInSpace"));
		}
		if (Dictionary.ITEMS_NOFALLDAMAGE.contains(event.itemStack.getItem())) {
			WarpDrive.addTooltip(event.toolTip, StatCollector.translateToLocalFormatted("warpdrive.tooltip.itemTag.noFallDamage"));
		}
		if (WarpDrive.isDev && event.entityPlayer.capabilities.isCreativeMode) {// disabled in production
			Block block = Block.getBlockFromItem(event.itemStack.getItem());
			if (block != Blocks.air) {
				try {
					String uniqueName = Block.blockRegistry.getNameForObject(block);
					if (uniqueName != null) {
						WarpDrive.addTooltip(event.toolTip, "" + uniqueName + "");
					}
				} catch(Exception exception) {
					// no operation
				}
				
				try {
					String harvestTool = block.getHarvestTool(event.itemStack.getItemDamage());
					if (harvestTool != null) {
						WarpDrive.addTooltip(event.toolTip, "Harvest with " + harvestTool + " (" + block.getHarvestLevel(event.itemStack.getItemDamage()) + ")");
					}
				} catch(Exception exception) {
					// no operation
				}
				
				WarpDrive.addTooltip(event.toolTip, "Light opacity is " + block.getLightOpacity());
				
				try {
					WarpDrive.addTooltip(event.toolTip, "Hardness is " + (float)WarpDrive.fieldBlockHardness.get(block));
				} catch(Exception exception) {
					// no operation
				}
				WarpDrive.addTooltip(event.toolTip, "Explosion resistance is " + block.getExplosionResistance(null));
			}
		}
	}
}
