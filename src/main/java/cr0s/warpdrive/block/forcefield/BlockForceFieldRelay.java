package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.data.EnumTier;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockForceFieldRelay extends BlockAbstractForceField {
	
	public static final PropertyEnum<EnumForceFieldUpgrade> UPGRADE = PropertyEnum.create("upgrade", EnumForceFieldUpgrade.class);
	
	public BlockForceFieldRelay(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier, Material.IRON);
		
		setUnlocalizedName("warpdrive.force_field.relay." + enumTier.getName());
		
		setDefaultState(getDefaultState().withProperty(UPGRADE, EnumForceFieldUpgrade.NONE));
		registerTileEntity(TileEntityForceFieldRelay.class, new ResourceLocation(WarpDrive.MODID, registryName));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, UPGRADE);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(final int metadata) {
		return this.getDefaultState();
	}
	
	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull final IBlockState blockState, final IBlockAccess world, final BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityForceFieldRelay) {
			return blockState.withProperty(UPGRADE, ((TileEntityForceFieldRelay) tileEntity).getUpgrade());
		} else {
			return blockState;
		}
	}
	
	@Nullable
	@Override
	public ItemBlock createItemBlock() {
		return new ItemBlockForceFieldRelay(this);
	}
	
	@Override
	public int damageDropped(final IBlockState blockState) {
		return blockState.getBlock().getMetaFromState(blockState);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityForceFieldRelay(enumTier);
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
		if (!(tileEntity instanceof TileEntityForceFieldRelay)) {
			return false;
		}
		final TileEntityForceFieldRelay tileEntityForceFieldRelay = (TileEntityForceFieldRelay) tileEntity;
		
		// sneaking with an empty hand or an upgrade item in hand to dismount current upgrade
		if (entityPlayer.isSneaking()) {
			final EnumForceFieldUpgrade enumForceFieldUpgrade = tileEntityForceFieldRelay.getUpgrade();
			if (enumForceFieldUpgrade != EnumForceFieldUpgrade.NONE) {
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the upgrade item
					final ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(enumForceFieldUpgrade, 1);
					final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntity(entityItem);
				}
				
				tileEntityForceFieldRelay.setUpgrade(EnumForceFieldUpgrade.NONE);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.dismounted", enumForceFieldUpgrade.name()));
				
			} else {
				// no more upgrades to dismount
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.no_upgrade_to_dismount"));
				return true;
			}
			
		} else if (itemStackHeld.isEmpty()) {// no sneaking and no item in hand to show status
			Commons.addChatMessage(entityPlayer, tileEntityForceFieldRelay.getStatus());
			return true;
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {
			// validate type
			if (EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage()).maxCountOnRelay <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.invalid_upgrade_for_relay"));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.getCount() < 1) {
					// not enough upgrade items
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.not_enough_upgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.shrink(1);
				
				// dismount the current upgrade item
				if (tileEntityForceFieldRelay.getUpgrade() != EnumForceFieldUpgrade.NONE) {
					final ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(tileEntityForceFieldRelay.getUpgrade(), 1);
					final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntity(entityItem);
				}
			}
			
			// mount the new upgrade item
			final EnumForceFieldUpgrade enumForceFieldUpgrade = EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage());
			tileEntityForceFieldRelay.setUpgrade(enumForceFieldUpgrade);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.mounted", enumForceFieldUpgrade.name()));
		}
		
		return false;
	}
}
