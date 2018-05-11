package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
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
		setUnlocalizedName("warpdrive.breathing.air");
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(final IBlockState state) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullyOpaque(final IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isAir(final IBlockState state, final IBlockAccess blockAccess, final BlockPos pos) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final World world, @Nonnull final BlockPos blockPos) {
		return null;
	}
	
	@Override
	public boolean isReplaceable(final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos) {
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean canPlaceBlockAt(final World world, @Nonnull final BlockPos blockPos) {
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean canCollideCheck(final IBlockState blockState, final boolean hitIfLiquid) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(@Nonnull final Item item, final CreativeTabs creativeTab, final List<ItemStack> list) {
		// hide in NEI
		for (int i = 0; i < 16; i++) {
			Commons.hideItemStack(new ItemStack(item, 1, i));
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(final IBlockState state) {
		return EnumPushReaction.DESTROY;
	}
	
	@Nullable
	@Override
	public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
		return null;
	}
	
	@Override
	public int quantityDropped(final Random random) {
		return 0;
	}
	
	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos, final EnumFacing facing) {
		if (WarpDriveConfig.BREATHING_AIR_BLOCK_DEBUG) {
			return facing == EnumFacing.DOWN || facing == EnumFacing.UP;
		}
		
		final BlockPos blockPosSide = blockPos.offset(facing);
		final Block blockSide = blockAccess.getBlockState(blockPosSide).getBlock();
		if (blockSide instanceof BlockAbstractAir) {
			return false;
		}
		
		return blockAccess.isAirBlock(blockPosSide);
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
}