package cr0s.warpdrive.block.decoration;


import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.EnumGasColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGas extends BlockAbstractBase {
	
	public static final PropertyEnum<EnumGasColor> COLOR = PropertyEnum.create("color", EnumGasColor.class);
	
	public BlockGas(final String registryName) {
		super(registryName, Material.FIRE);
		setHardness(0.0F);
		setUnlocalizedName("warpdrive.decoration.gas");
		
		setDefaultState(getDefaultState().withProperty(COLOR, EnumGasColor.RED));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, COLOR);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				.withProperty(COLOR, EnumGasColor.get(metadata));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(COLOR).ordinal();
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockGas(this);
	}
	
	@Override
	public void getSubBlocks(@Nonnull final Item item, final CreativeTabs creativeTab, final List<ItemStack> list) {
		for (final EnumGasColor enumGasColor : EnumGasColor.values()) {
			list.add(new ItemStack(item, 1, enumGasColor.ordinal()));
		}
	}
	
	@Override
	public int damageDropped(final IBlockState blockState) {
		return blockState.getBlock().getMetaFromState(blockState);
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
	public boolean isAir(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final World world, @Nonnull final BlockPos blockPos) {
		return null;
	}
	
	@Override
	public boolean isReplaceable(final IBlockAccess worldIn, @Nonnull final BlockPos blockPos) {
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(final World worldIn, @Nonnull final BlockPos blockPos) {
		return true;
	}
	
	@Override
	public boolean canCollideCheck(final IBlockState state, final boolean hitIfLiquid) {
		return false;
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
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos, final EnumFacing facing) {
		final BlockPos blockPosSide = blockPos.offset(facing);
		final IBlockState blockStateSide = blockAccess.getBlockState(blockPosSide);
		if (blockStateSide.getBlock().isAssociatedBlock(this)) {
			return false;
		}
		return blockAccess.isAirBlock(blockPosSide);
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
	
	@Override
	public void onBlockAdded(final World world, final BlockPos blockPos, final IBlockState blockState) {
		// Gas blocks are only allowed in space
		if (CelestialObjectManager.hasAtmosphere(world, blockPos.getX(), blockPos.getZ())) {
			world.setBlockToAir(blockPos);
		}
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		return EnumRarity.COMMON;
	}
}