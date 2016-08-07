package cr0s.warpdrive.block.passive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGas extends Block {

	public BlockGas() {
		super(Material.FIRE);
		setHardness(0.0F);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setRegistryName("warpdrive.passive.Gas");
		GameRegistry.register(this);
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull World world, @Nonnull BlockPos blockPos) {
		return null;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, @Nonnull BlockPos blockPos) {
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, @Nonnull BlockPos blockPos) {
		return true;
	}
	
	@Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		return false;
	}
	
	// 0 "warpdrive:passive/gasBlockBlue"
	// 1 "warpdrive:passive/gasBlockRed"
	// 2 "warpdrive:passive/gasBlockGreen"
	// 3 "warpdrive:passive/gasBlockYellow"
	// 4 "warpdrive:passive/gasBlockDark"
	// 5 "warpdrive:passive/gasBlockDarkness"
	// 6 "warpdrive:passive/gasBlockWhite"
	// 7 "warpdrive:passive/gasBlockMilk"
	// 8 "warpdrive:passive/gasBlockOrange"
	// 9 "warpdrive:passive/gasBlockSyren"
	// 10 "warpdrive:passive/gasBlockGray"
	// 11 "warpdrive:passive/gasBlockViolet"

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Nullable
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos blockPos, EnumFacing side) {
		IBlockState blockStateSide = blockAccess.getBlockState(blockPos);
		if (blockStateSide.getBlock().isAssociatedBlock(this)) {
			return false;
		}
		return blockAccess.isAirBlock(blockPos);
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos blockPos, IBlockState blockState) {
		// Gas blocks allow only in space
		if (world.provider.getDimension() != WarpDriveConfig.G_SPACE_DIMENSION_ID) {
			world.setBlockToAir(blockPos);
		}
	}
}