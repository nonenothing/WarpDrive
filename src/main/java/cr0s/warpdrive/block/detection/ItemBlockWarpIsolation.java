package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemBlockWarpIsolation extends ItemBlockAbstractBase {
	
	public ItemBlockWarpIsolation(final Block block) {
		super(block);
		setMaxDamage(0);
	}
	
	// Item overrides
	
	@Override
	public void addInformation(@Nonnull final ItemStack itemStack, @Nullable World world,
	                           @Nonnull final List<String> list, @Nullable final ITooltipFlag advancedItemTooltips) {
		super.addInformation(itemStack, world, list, advancedItemTooltips);
		
		Commons.addTooltip(list, new TextComponentTranslation(getUnlocalizedName(itemStack) + ".formatted_tooltip",
		                                                      WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS,
		                                                      Math.round(WarpDriveConfig.RADAR_MIN_ISOLATION_EFFECT * 100.0D),
		                                                      WarpDriveConfig.RADAR_MAX_ISOLATION_BLOCKS,
		                                                      Math.round(WarpDriveConfig.RADAR_MAX_ISOLATION_EFFECT * 100.0D),
		                                                      WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1).getFormattedText());
	}
}
