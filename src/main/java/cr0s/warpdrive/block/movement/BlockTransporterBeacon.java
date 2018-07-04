package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.client.ClientProxy;
import cr0s.warpdrive.data.EnumTransporterBeaconState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTransporterBeacon extends BlockAbstractContainer {
	
	private static final double BOUNDING_RADIUS = 3.0D / 32.0D;
	private static final double BOUNDING_HEIGHT = 21.0D / 32.0D;
	private static final AxisAlignedBB AABB_BEACON = new AxisAlignedBB(0.5D - BOUNDING_RADIUS, 0.0D, 0.5D - BOUNDING_RADIUS,
	                                                                   0.5D + BOUNDING_RADIUS, BOUNDING_HEIGHT, 0.5D + BOUNDING_RADIUS);
	
	public static final PropertyEnum<EnumTransporterBeaconState> VARIANT = PropertyEnum.create("variant", EnumTransporterBeaconState.class);
	
	public BlockTransporterBeacon(final String registryName) {
		super(registryName, Material.IRON);
		setHardness(0.5F);
		setUnlocalizedName("warpdrive.movement.transporter_beacon");
		
		setDefaultState(getDefaultState()
				                .withProperty(VARIANT, EnumTransporterBeaconState.PACKED_INACTIVE)
		               );
		registerTileEntity(TileEntityTransporterBeacon.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockTransporterBeacon(this);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return getDefaultState()
				       .withProperty(VARIANT, EnumTransporterBeaconState.get(metadata & 0x3));
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(VARIANT).getMetadata();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void modelInitialisation() {
		final Item item = Item.getItemFromBlock(this);
		ClientProxy.modelInitialisation(item);
		
		// Bind our TESR to our tile entity
		// @TODO ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransporterBeacon.class, new TileEntityForceFieldProjectorRenderer());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(final IBlockState blockState) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(@Nonnull final IBlockState blockState, final IBlockAccess blockAccess, final BlockPos blockPos) {
		return AABB_BEACON;
	}
	
	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos) {
		return null;
	}
	
	@Override
	public int getLightValue(@Nonnull final IBlockState blockState, final IBlockAccess blockAccess, @Nonnull final BlockPos blockPos) {
		final EnumTransporterBeaconState enumTransporterBeaconState = blockState.getValue(VARIANT);
		return enumTransporterBeaconState == EnumTransporterBeaconState.PACKED_ACTIVE
		    || enumTransporterBeaconState == EnumTransporterBeaconState.DEPLOYED_ACTIVE ? 6 : 0;
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityTransporterBeacon();
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState blockState,
	                                final EntityPlayer entityPlayer, final EnumHand enumHand,
	                                final EnumFacing enumFacing, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (enumHand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		// get context
		final ItemStack itemStackHeld = entityPlayer.getHeldItem(enumHand);
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityTransporterBeacon)) {
			return false;
		}
		final TileEntityTransporterBeacon tileEntityTransporterBeacon = (TileEntityTransporterBeacon) tileEntity;
		
		// sneaking with an empty hand
		if ( itemStackHeld.isEmpty()
		  && entityPlayer.isSneaking() ) {
			final boolean isEnabledOld = tileEntityTransporterBeacon.enable(new Object[] { })[0];
			final boolean isEnabledNew = tileEntityTransporterBeacon.enable(new Object[] { !isEnabledOld })[0];
			if (isEnabledOld != isEnabledNew) {
				if (isEnabledNew) {
					Commons.addChatMessage(entityPlayer, Commons.getChatPrefix(this)
					                                            .appendSibling(new TextComponentTranslation("warpdrive.is_enabled.set.enabled")));
				} else {
					Commons.addChatMessage(entityPlayer, Commons.getChatPrefix(this)
					                                            .appendSibling(new TextComponentTranslation("warpdrive.is_enabled.set.disabled")));
				}
			}
			return true;
		}
		
		return super.onBlockActivated(world, blockPos, blockState, entityPlayer, enumHand, enumFacing, hitX, hitY, hitZ);
	}
}
