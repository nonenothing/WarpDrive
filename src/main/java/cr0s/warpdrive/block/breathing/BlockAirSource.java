package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.event.ChunkHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAirSource extends BlockAbstractAir {
	
	public BlockAirSource(final String registryName) {
		super(registryName);
		
		setDefaultState(getDefaultState().withProperty(BlockProperties.FACING, EnumFacing.DOWN));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
		       .withProperty(BlockProperties.FACING, EnumFacing.getFront(metadata & 7));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.FACING).getIndex();
	}
	
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final World world, @Nonnull final BlockPos blockPos) {
		if (!world.isRemote) {
			final StateAir stateAir = ChunkHandler.getStateAir(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if ( stateAir != null
			  && (!stateAir.isAirSource() || stateAir.concentration == 0) ) {
				world.setBlockToAir(blockPos);
			}
		}
		return super.getCollisionBoundingBox(blockState, world, blockPos);
	}
}