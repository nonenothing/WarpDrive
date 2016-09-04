package cr0s.warpdrive.block.detection;

import java.util.Random;

import cr0s.warpdrive.block.BlockAbstractBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockCloakingCoil extends BlockAbstractBase {
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	public static final PropertyBool OUTER = PropertyBool.create("outer");
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	public BlockCloakingCoil(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(3.5F);
		setUnlocalizedName("warpdrive.detection.CloakingCoil");
		
		setDefaultState(getDefaultState().withProperty(ACTIVE, false).withProperty(OUTER, false).withProperty(FACING, EnumFacing.UP));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ACTIVE, OUTER, FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata) {
		boolean isActive = (metadata & 7) != 0;
		boolean isOuter = (metadata & 7) > 1;
		return getDefaultState()
				.withProperty(ACTIVE, isActive)
				.withProperty(OUTER, isOuter)
				.withProperty(FACING, isOuter ? EnumFacing.getFront(metadata & 7 - 1) : EnumFacing.UP);
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState) {
		if (!blockState.getValue(ACTIVE)) {
			return 0;
		}
		if (!blockState.getValue(OUTER)) {
			return 1;
		}
		return 2 + blockState.getValue(FACING).ordinal();
	}
	
	public static void setBlockState(@Nonnull World world, @Nonnull final BlockPos blockPos, final boolean isActive, final boolean isOuter, final EnumFacing enumFacing) {
		IBlockState blockStateActual = world.getBlockState(blockPos);
		IBlockState blockStateNew = blockStateActual.withProperty(ACTIVE, isActive).withProperty(OUTER, isOuter);
		if (enumFacing != null) {
			blockStateNew = blockStateNew.withProperty(FACING, enumFacing);
		}
		if (blockStateActual.getBlock().getMetaFromState(blockStateActual) != blockStateActual.getBlock().getMetaFromState(blockStateNew)) {
			world.setBlockState(blockPos, blockStateNew);
		}
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}
