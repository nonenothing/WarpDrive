package cr0s.warpdrive.block.decoration;

import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockAbstractLamp extends BlockAbstractBase {
	
	BlockAbstractLamp(final String registryName, final EnumTier enumTier, final String unlocalizedName) {
		super(registryName, enumTier, Material.ROCK);
		
		setHardness(WarpDriveConfig.HULL_HARDNESS[0]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[0] * 5 / 3);
		setSoundType(SoundType.METAL);
		setTranslationKey(unlocalizedName);
		setDefaultState(getDefaultState()
				                .withProperty(BlockProperties.ACTIVE, false)
				                .withProperty(BlockProperties.FACING, EnumFacing.DOWN)
		               );
		
		setLightLevel(14.0F / 15.0F);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.ACTIVE, BlockProperties.FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				       .withProperty(BlockProperties.ACTIVE, (metadata & 0x8) != 0)
				       .withProperty(BlockProperties.FACING, EnumFacing.byIndex(metadata & 0x7));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return (blockState.getValue(BlockProperties.ACTIVE) ? 0x8 : 0x0)
		     | (blockState.getValue(BlockProperties.FACING).getIndex());
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos, final EnumFacing side) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isBlockNormalCube(final IBlockState blockState) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(final IBlockState blockState) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(final IBlockState blockState) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos) {
		return NULL_AABB;
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withRotation(@Nonnull final IBlockState blockState, final Rotation rot) {
		return blockState.withProperty(BlockProperties.FACING, rot.rotate(blockState.getValue(BlockProperties.FACING)));
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withMirror(@Nonnull final IBlockState blockState, final Mirror mirrorIn) {
		return blockState.withRotation(mirrorIn.toRotation(blockState.getValue(BlockProperties.FACING)));
	}
}