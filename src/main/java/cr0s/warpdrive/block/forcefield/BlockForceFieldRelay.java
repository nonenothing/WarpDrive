package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockForceFieldRelay extends BlockAbstractForceField {
	
	public static final PropertyEnum<EnumForceFieldUpgrade> UPGRADE = PropertyEnum.create("upgrade", EnumForceFieldUpgrade.class);
	
	public BlockForceFieldRelay(final String registryName, final byte tier) {
		super(registryName, tier, Material.IRON);
		setUnlocalizedName("warpdrive.forcefield.relay" + tier);
		
		setDefaultState(getDefaultState().withProperty(UPGRADE, EnumForceFieldUpgrade.NONE));
		GameRegistry.registerTileEntity(TileEntityForceFieldRelay.class, WarpDrive.PREFIX + registryName);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, UPGRADE);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState blockState, IBlockAccess world, BlockPos pos) {
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
	public int damageDropped(IBlockState blockState) {
		return blockState.getBlock().getMetaFromState(blockState);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityForceFieldRelay)) {
			return false;
		}
		TileEntityForceFieldRelay tileEntityForceFieldRelay = (TileEntityForceFieldRelay) tileEntity;
		
		// sneaking with an empty hand or an upgrade item in hand to dismount current upgrade
		if (entityPlayer.isSneaking()) {
			EnumForceFieldUpgrade enumForceFieldUpgrade = tileEntityForceFieldRelay.getUpgrade();
			if (enumForceFieldUpgrade != EnumForceFieldUpgrade.NONE) {
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the upgrade item
					ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(enumForceFieldUpgrade, 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityForceFieldRelay.setUpgrade(EnumForceFieldUpgrade.NONE);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.dismounted", enumForceFieldUpgrade.name()));
				
			} else {
				// no more upgrades to dismount
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.noUpgradeToDismount"));
				return true;
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand to show status
			Commons.addChatMessage(entityPlayer, tileEntityForceFieldRelay.getStatus());
			return true;
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {
			// validate type
			if (EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage()).maxCountOnRelay <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.invalidRelayUpgrade"));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.notEnoughUpgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= 1;
				
				// dismount the current upgrade item
				if (tileEntityForceFieldRelay.getUpgrade() != EnumForceFieldUpgrade.NONE) {
					ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(tileEntityForceFieldRelay.getUpgrade(), 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntityInWorld(entityItem);
				}
			}
			
			// mount the new upgrade item
			EnumForceFieldUpgrade enumForceFieldUpgrade = EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage());
			tileEntityForceFieldRelay.setUpgrade(enumForceFieldUpgrade);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.mounted", enumForceFieldUpgrade.name()));
		}
		
		return false;
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityForceFieldRelay();
	}
}
