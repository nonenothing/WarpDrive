package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockCamera extends BlockAbstractContainer {
	
	public BlockCamera(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.detection.camera");
		registerTileEntity(TileEntityCamera.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityCamera();
	}
}