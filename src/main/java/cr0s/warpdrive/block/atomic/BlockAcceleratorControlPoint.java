package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockAcceleratorControlPoint extends BlockAbstractAccelerator implements ITileEntityProvider {
	
	public BlockAcceleratorControlPoint(final String registryName, final EnumTier enumTier, final boolean isSubBlock) {
		super(registryName, enumTier);
		
		if (isSubBlock) {
			return;
		}
		
		setUnlocalizedName("warpdrive.atomic.accelerator_control_point");
		BlockAbstractContainer.registerTileEntity(TileEntityAcceleratorControlPoint.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityAcceleratorControlPoint(enumTier);
	}
}
