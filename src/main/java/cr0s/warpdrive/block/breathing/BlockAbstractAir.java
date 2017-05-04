package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockAbstractAir extends BlockAbstractBase {
	
	public static final PropertyInteger CONCENTRATION = PropertyInteger.create("concentration", 0, 15);
	
	BlockAbstractAir(final String registryName) {
		super(registryName, Material.FIRE);
		setHardness(0.0F);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setUnlocalizedName("warpdrive.breathing.air");
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
	public boolean isAir(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull World world, @Nonnull BlockPos blockPos) {
		return null;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess blockAccess, @Nonnull BlockPos blockPos) {
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, @Nonnull BlockPos blockPos) {
		return true;
	}
	
	@Override
	public boolean canCollideCheck(IBlockState blockState, boolean hitIfLiquid) {
		return false;
	}
	
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
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos blockPos, EnumFacing facing) {
		if (WarpDriveConfig.BREATHING_AIR_BLOCK_DEBUG) {
			return facing == EnumFacing.DOWN || facing == EnumFacing.UP;
		}
		
		BlockPos blockPosSide = blockPos.offset(facing);
		Block sideBlock = blockAccess.getBlockState(blockPosSide).getBlock();
		return !(sideBlock instanceof BlockAbstractAir) && blockAccess.isAirBlock(blockPosSide);
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
}