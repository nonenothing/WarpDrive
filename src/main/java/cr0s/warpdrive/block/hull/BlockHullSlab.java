package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.client.ClientProxy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.Vector3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHullSlab extends BlockSlab implements IBlockBase, IDamageReceiver {
	
	// Metadata values are
	// 0-5 for plain slabs orientations
	// 6-11 for tiled slabs orientations
	// 12 for plain double slab
	// 13-15 for tiled double slabs
	
	protected static final EnumVariant[] VARIANT_FROM_METADATA = {
		EnumVariant.PLAIN_HORIZONTAL,
		EnumVariant.PLAIN_HORIZONTAL,
		EnumVariant.PLAIN_VERTICAL,
		EnumVariant.PLAIN_VERTICAL,
		EnumVariant.PLAIN_VERTICAL,
		EnumVariant.PLAIN_VERTICAL,
		EnumVariant.TILED_HORIZONTAL,
		EnumVariant.TILED_HORIZONTAL,
		EnumVariant.TILED_VERTICAL,
		EnumVariant.TILED_VERTICAL,
		EnumVariant.TILED_VERTICAL,
		EnumVariant.TILED_VERTICAL,
		EnumVariant.PLAIN_FULL,
		EnumVariant.TILED_FULL_X,
		EnumVariant.TILED_FULL_Y,
		EnumVariant.TILED_FULL_Z
	};
	
	protected static final AxisAlignedBB AABB_HALF_BOTTOM = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 1.00D, 0.50D, 1.00D);
	protected static final AxisAlignedBB AABB_HALF_TOP    = new AxisAlignedBB(0.00D, 0.50D, 0.00D, 1.00D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_HALF_NORTH  = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 1.00D, 1.00D, 0.50D);
	protected static final AxisAlignedBB AABB_HALF_SOUTH  = new AxisAlignedBB(0.00D, 0.00D, 0.50D, 1.00D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_HALF_EAST   = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 0.50D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_HALF_WEST   = new AxisAlignedBB(0.50D, 0.00D, 0.00D, 1.00D, 1.00D, 1.00D);
	protected static final AxisAlignedBB AABB_FULL        = FULL_BLOCK_AABB;
	
	public static final PropertyEnum<EnumVariant> VARIANT = PropertyEnum.create("variant", EnumVariant.class);
	
	@Deprecated() // Dirty hack for rendering vertical slabs
	private IBlockState blockStateForRender;
	
	final byte tier;
	private final IBlockState blockStateHull;
	
	public BlockHullSlab(final String registryName, final IBlockState blockStateHull, final byte tier) {
		super(Material.ROCK);
		this.tier = tier;
		this.blockStateHull = blockStateHull;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setUnlocalizedName("warpdrive.hull" + tier + ".slab." + EnumDyeColor.byMetadata(blockStateHull.getBlock().getMetaFromState(blockStateHull)).getName());
		setRegistryName(registryName);
		WarpDrive.register(this, new ItemBlockHullSlab(this));
		
		setDefaultState(getDefaultState()
		                .withProperty(BlockProperties.FACING, EnumFacing.DOWN)
		                .withProperty(VARIANT, EnumVariant.PLAIN_HORIZONTAL));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockProperties.FACING, VARIANT);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
		       .withProperty(BlockProperties.FACING, metadata < 12 ? EnumFacing.getFront(metadata % 6) : EnumFacing.DOWN)
		       .withProperty(VARIANT, VARIANT_FROM_METADATA[metadata]);
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState) {
		switch (blockState.getValue(VARIANT)) {
		default:
		case PLAIN_HORIZONTAL: return blockState.getValue(BlockProperties.FACING).getIndex();
		case PLAIN_VERTICAL  : return blockState.getValue(BlockProperties.FACING).getIndex();
		case TILED_HORIZONTAL: return 6 + blockState.getValue(BlockProperties.FACING).getIndex();
		case TILED_VERTICAL  : return 6 + blockState.getValue(BlockProperties.FACING).getIndex();
		case PLAIN_FULL      : return 12;
		case TILED_FULL_X    : return 13;
		case TILED_FULL_Y    : return 14;
		case TILED_FULL_Z    : return 15;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(@Nonnull Item item, CreativeTabs creativeTab, List<ItemStack> list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 2));
		list.add(new ItemStack(item, 1, 6));
		list.add(new ItemStack(item, 1, 8));
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState blockState) {
		blockStateForRender = blockState;
		return super.getRenderType(blockState);
	}
	
	@Override
	public int damageDropped(final IBlockState blockState) {
		final int metadata = getMetaFromState(blockState);
		return metadata <= 1 ? 0    // plain horizontal
		     : metadata <= 5 ? 2    // plain vertical
		     : metadata <= 7 ? 6    // tiled horizontal
		     : metadata <= 11 ? 8   // tiled vertical
		     : metadata;            // others
	}
	
	// ItemSlab abstract methods
	@Nonnull
	@Override
	public String getUnlocalizedName(int metadata) {
		return getUnlocalizedName();
	}
	
	@Override
	public boolean isDouble() {
		return false;
	}
	
	@Nonnull
	@Override
	public IProperty<?> getVariantProperty() {
		return VARIANT;
	}
	
	@Nonnull
	@Override
	public Comparable<?> getTypeForItem(@Nonnull ItemStack itemStack) {
		return VARIANT_FROM_METADATA[itemStack.getItemDamage()];
	}
	
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos blockPos) {
		return getBlockBoundsFromState(blockState);
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final World world, @Nonnull final BlockPos blockPos) {
		return getBlockBoundsFromState(blockState);
	}
	
	private AxisAlignedBB getBlockBoundsFromState(final IBlockState blockState) {
		final int metadata = blockState == null ? 0 : getMetaFromState(blockState);
		if (metadata >= 12) {
			return AABB_FULL;
			
		} else {
			switch (metadata % 6) {
			case 0: return AABB_HALF_TOP;
			case 1: return AABB_HALF_BOTTOM;
			case 2: return AABB_HALF_SOUTH;
			case 3: return AABB_HALF_NORTH;
			case 4: return AABB_HALF_WEST;
			case 5: return AABB_HALF_EAST;
			default: return AABB_FULL;
			}
		}
	}
	
	@Override
	public boolean isFullyOpaque(final IBlockState state) {
		return ((BlockSlab) state.getBlock()).isDouble();
	}
	
	@Nonnull
	@Override
	public IBlockState onBlockPlaced(final World world, final BlockPos blockPos, final EnumFacing facing,
	                                 final float hitX, final float hitY, final float hitZ, final int metadata,
	                                 final EntityLivingBase entityLivingBase) {
		final IBlockState blockState = getStateFromMeta(metadata);
		
		// full block?
		if (isDouble() || metadata >= 12) {
			return blockState;
		}
		
		// horizontal slab?
		if (metadata == 0 || metadata == 6) {
			// reuse vanilla logic
			final EnumFacing blockFacing = (facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5F) ? EnumFacing.DOWN : EnumFacing.UP);
			return blockState.withProperty(BlockProperties.FACING, blockFacing);
		}
		// vertical slab?
		if (metadata == 2 || metadata == 8) {
			if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
				return blockState.withProperty(BlockProperties.FACING, facing);
			}
			// is X the furthest away from center?
			if (Math.abs(hitX - 0.5F) > Math.abs(hitZ - 0.5F)) {
				// west (4) vs east (5)
				final EnumFacing blockFacing = hitX > 0.5F ? EnumFacing.EAST : EnumFacing.WEST;
				return blockState.withProperty(BlockProperties.FACING, blockFacing);
			}
			// north (2) vs south (3)
			final EnumFacing blockFacing = hitZ > 0.5F ? EnumFacing.SOUTH : EnumFacing.NORTH;
			return blockState.withProperty(BlockProperties.FACING, blockFacing);
		}
		return getStateById(metadata);
	}
	
	@Override
	public int quantityDropped(Random random) {
		return isDouble() ? 2 : 1;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos, final EnumFacing side) {
		if (isDouble()) {
			return super.shouldSideBeRendered(blockState, blockAccess, blockPos, side);
		} else if (side != EnumFacing.DOWN && side != EnumFacing.UP && !super.shouldSideBeRendered(blockState, blockAccess, blockPos, side)) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return isDouble();
	}
	
	@Override
	public boolean doesSideBlockRendering(final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos blockPos, final EnumFacing side) {
		if (blockState.isOpaqueCube()) {
			return true;
		}
		
		final EnumFacing enumFacing = blockState.getValue(BlockProperties.FACING);
		return enumFacing == side;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public MapColor getMapColor(IBlockState blockState) {
		return blockStateHull.getMapColor();
	}
	
	@Override
	public byte getTier(ItemStack itemStack) {
		return tier;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		switch (getTier(itemStack)) {
		case 0:	return EnumRarity.EPIC;
		case 1:	return EnumRarity.COMMON;
		case 2:	return EnumRarity.UNCOMMON;
		case 3:	return EnumRarity.RARE;
		default: return rarity;
		}
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockHull(this);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		Item item = Item.getItemFromBlock(this);
		ClientProxy.modelInitialisation(item);
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos blockPos,
	                              DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		// TODO: adjust hardness to damage type/color
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(IBlockState blockState, World world, BlockPos blockPos,
	                       DamageSource damageSource, int damageParameter, Vector3 damageDirection, int damageLevel) {
		if (damageLevel <= 0) {
			return 0;
		}
		if (tier == 1) {
			world.setBlockToAir(blockPos);
		} else {
			world.setBlockState(blockPos, WarpDrive.blockHulls_slab[tier - 2][blockStateHull.getBlock().getMetaFromState(blockStateHull)]
			                              .getDefaultState()
			                              .withProperty(BlockProperties.FACING, blockState.getValue(BlockProperties.FACING))
			                              .withProperty(VARIANT, blockState.getValue(VARIANT)), 2);
		}
		return 0;
	}
	
	public enum EnumVariant implements IStringSerializable {
		PLAIN_HORIZONTAL("plain_horizontal", false),
		PLAIN_VERTICAL("plain_vertical", false),
		TILED_HORIZONTAL("tiled_horizontal", false),
		TILED_VERTICAL("tiled_vertical", false),
		PLAIN_FULL("plain_full", true),
		TILED_FULL_X("tiled_full_x", true),
		TILED_FULL_Y("tiled_full_y", true),
		TILED_FULL_Z("tiled_full_z", true);
		
		private final String name;
		private final boolean isDouble;
		
		EnumVariant(final String name, final boolean isDouble) {
			this.name = name;
			this.isDouble = isDouble;
		}
		
		@Nonnull
		@Override
		public String getName()
		{
			return name;
		}
		
		public boolean getIsDouble()
		{
			return isDouble;
		}
	}
}
