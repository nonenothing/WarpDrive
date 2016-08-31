package cr0s.warpdrive.block.forcefield;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockForceFieldRelay extends BlockAbstractForceField {
	
	public BlockForceFieldRelay(final String registryName, final byte tier) {
		super(registryName, tier, Material.IRON);
		setUnlocalizedName("warpdrive.forcefield.relay" + tier);
		GameRegistry.registerTileEntity(TileEntityForceFieldRelay.class, WarpDrive.PREFIX + registryName);
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
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityForceFieldRelay)) {
			return false;
		}
		TileEntityForceFieldRelay tileEntityForceFieldRelay = (TileEntityForceFieldRelay) tileEntity;
		
		// sneaking with an empty hand or an upgrade item in hand to dismount current upgrade
		if (entityPlayer.isSneaking()) {
			if (tileEntityForceFieldRelay.getUpgrade() != EnumForceFieldUpgrade.NONE) {
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the upgrade item
					ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(tileEntityForceFieldRelay.getUpgrade(), 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityForceFieldRelay.setUpgrade(EnumForceFieldUpgrade.NONE);
				// upgrade dismounted
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.dismounted"));
				
			} else {
				// no more upgrades to dismount
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.noUpgradeToDismount"));
				return true;
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand to show status
			WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldRelay.getStatus());
			return true;
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {
			// validate type
			if (EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage()).maxCountOnRelay <= 0) {
				// invalid upgrade type
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.invalidRelayUpgrade"));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.notEnoughUpgrades"));
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
			tileEntityForceFieldRelay.setUpgrade(EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage()));
			// upgrade mounted
			WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.mounted"));
		}
		
		return false;
	}

	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityForceFieldRelay();
	}
}
