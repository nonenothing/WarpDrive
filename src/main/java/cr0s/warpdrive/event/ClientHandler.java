package cr0s.warpdrive.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cr0s.warpdrive.WarpDrive;

/**
 *
 * @author LemADEC
 */
public class ClientHandler {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltipEvent(ItemTooltipEvent event) {
		if (event.getEntityPlayer() == null || !event.getEntityPlayer().capabilities.isCreativeMode) {
			return;
		}
		if (WarpDrive.isDev) {// disabled in production
			Block block = Block.getBlockFromItem(event.getItemStack().getItem());
			if (block != Blocks.AIR) {
				try {
					ResourceLocation resourceLocation = Block.REGISTRY.getNameForObject(block);
					if (resourceLocation != null) {
						event.getToolTip().add("" + resourceLocation + "");
					}
				} catch(Exception exception) {
					// no operation
				}
				
				try {
					IBlockState blockState = block.getStateFromMeta(event.getItemStack().getItemDamage());
					String harvestTool = block.getHarvestTool(blockState);
					event.getToolTip().add("Harvest with " + harvestTool + " (" + block.getHarvestLevel(blockState) + ")");
				} catch(Exception exception) {
					// no operation
				}
				
				try {
					IBlockState blockState = block.getStateFromMeta(event.getItemStack().getItemDamage());
					event.getToolTip().add("Light opacity is " + block.getLightOpacity(blockState));
				} catch(Exception exception) {
					// no operation
				}
				
				try {
					event.getToolTip().add("Hardness is " + (float)WarpDrive.fieldBlockHardness.get(block));
				} catch(Exception exception) {
					// no operation
				}
				event.getToolTip().add("Explosion resistance is " + block.getExplosionResistance(null));
			}
		}
	}
}
