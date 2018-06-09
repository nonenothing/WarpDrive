package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.data.BlockProperties;

import javax.annotation.Nonnull;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

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
}