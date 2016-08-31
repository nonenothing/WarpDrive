package cr0s.warpdrive.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class BlockAbstractRotatingContainer extends BlockAbstractContainer {
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	protected BlockAbstractRotatingContainer(final String registryName, final Material material) {
		super(registryName, material);
		
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.getFront(metadata & 7));
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState) {
		return blockState.getValue(FACING).getIndex();
	}
	
	@Nonnull
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entityLiving) {
		EnumFacing enumFacing = getFacingFromEntity(pos, entityLiving);
		return this.getDefaultState().withProperty(FACING, enumFacing);
	}
	
	public static EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
		return EnumFacing.getFacingFromVector(
				(float) (entity.posX - clickedBlock.getX()),
				(float) (entity.posY - clickedBlock.getY()),
				(float) (entity.posZ - clickedBlock.getZ()));
	}
}
