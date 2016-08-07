package cr0s.warpdrive.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	public BlockChunkLoader() {
		super(Material.IRON);
		setRegistryName("warpdrive.machines.ChunkLoader");
		GameRegistry.register(this);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityChunkLoader();
	}
}
