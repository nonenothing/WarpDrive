package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockAcceleratorController extends BlockAbstractContainer {
	
	public BlockAcceleratorController(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setUnlocalizedName("warpdrive.atomic.accelerator_controller");
		
		registerTileEntity(TileEntityAcceleratorController.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityAcceleratorController(enumTier);
	}
}
