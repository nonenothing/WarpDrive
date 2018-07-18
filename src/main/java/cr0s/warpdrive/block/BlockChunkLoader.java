package cr0s.warpdrive.block;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	public BlockChunkLoader(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setUnlocalizedName("warpdrive.machines.chunk_loader" + enumTier.getIndex());
		registerTileEntity(TileEntityChunkLoader.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityChunkLoader(enumTier);
	}
}
