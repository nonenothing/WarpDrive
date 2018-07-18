package cr0s.warpdrive.block;

import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public abstract class BlockAbstractRotatingContainer extends BlockAbstractContainer {
	
	protected BlockAbstractRotatingContainer(final String registryName, final EnumTier enumTier, final Material material) {
		super(registryName, enumTier, material);
		
		setDefaultState(blockState.getBaseState()
		                .withProperty(BlockProperties.FACING, EnumFacing.NORTH));
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
