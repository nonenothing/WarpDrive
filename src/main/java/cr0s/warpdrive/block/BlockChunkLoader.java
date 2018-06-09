package cr0s.warpdrive.block;

import cr0s.warpdrive.WarpDrive;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	public BlockChunkLoader(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.machines.chunk_loader");
		registerTileEntity(TileEntityChunkLoader.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityChunkLoader();
	}
}
