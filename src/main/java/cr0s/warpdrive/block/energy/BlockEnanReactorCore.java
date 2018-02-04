package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumReactorFace;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockEnanReactorCore extends BlockAbstractContainer {
	
	public BlockEnanReactorCore(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.energy.EnanReactorCore");
		GameRegistry.registerTileEntity(TileEntityEnanReactorCore.class, WarpDrive.PREFIX + registryName);
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityEnanReactorCore();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void breakBlock(World world, @Nonnull BlockPos blockPos, @Nonnull IBlockState blockState) {
		super.breakBlock(world, blockPos, blockState);
		
		for (final EnumReactorFace reactorFace : EnumReactorFace.values()) {
			if (reactorFace.indexStability < 0) {
				continue;
			}
			
			final TileEntity tileEntity = world.getTileEntity(blockPos);
			if (tileEntity instanceof TileEntityEnanReactorLaser) {
				if (((TileEntityEnanReactorLaser) tileEntity).getReactorFace() == reactorFace) {
					((TileEntityEnanReactorLaser) tileEntity).setReactorFace(EnumReactorFace.UNKNOWN, null);
				}
			}
		}
	}
	
	@Override

	public byte getTier(final ItemStack itemStack) {
		return 3;
	}
}