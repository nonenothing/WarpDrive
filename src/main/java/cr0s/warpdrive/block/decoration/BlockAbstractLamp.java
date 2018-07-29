package cr0s.warpdrive.block.decoration;

import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.EnumTier;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
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
		setDefaultState(blockState.getBaseState().withProperty(BlockProperties.FACING, EnumFacing.NORTH));
		
		setLightLevel(14.0F / 15.0F);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this,
				new IProperty[] { BlockProperties.FACING },
				new IUnlistedProperty[] {  });
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				.withProperty(BlockProperties.FACING, EnumFacing.byIndex(metadata & 7));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(BlockProperties.FACING).getIndex();
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
	public boolean isOpaqueCube(final IBlockState state) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(final IBlockState state) {
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
	public IBlockState withRotation(@Nonnull final IBlockState state, final Rotation rot) {
		return state.withProperty(BlockProperties.FACING, rot.rotate(state.getValue(BlockProperties.FACING)));
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withMirror(@Nonnull final IBlockState state, final Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(BlockProperties.FACING)));
	}
}