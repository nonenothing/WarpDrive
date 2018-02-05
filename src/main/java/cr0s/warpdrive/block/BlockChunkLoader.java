package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	public BlockChunkLoader(final String registryName) {
		super(registryName, Material.IRON);
		setUnlocalizedName("warpdrive.machines.ChunkLoader");
		GameRegistry.registerTileEntity(TileEntityChunkLoader.class, WarpDrive.PREFIX + registryName);
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityChunkLoader();
	}
	
	@Override
	public boolean onBlockActivated(final World world, final BlockPos blockPos, final IBlockState state, final EntityPlayer entityPlayer,
	                                final EnumHand hand, @Nullable final ItemStack itemStackHeld,
	                                final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		final TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityChunkLoader)) {
			return false;
		}
		final TileEntityChunkLoader tileEntityChunkLoader = (TileEntityChunkLoader) tileEntity;
		
		EnumComponentType enumComponentType = null;
		if ( itemStackHeld != null
		  && itemStackHeld.getItem() instanceof ItemComponent ) {
			enumComponentType = EnumComponentType.get(itemStackHeld.getItemDamage());
		}
		
		// sneaking with an empty hand or an upgrade item in hand to dismount current upgrade
		if (entityPlayer.isSneaking()) {
			// using an upgrade item or an empty hand means dismount upgrade
			if ( itemStackHeld == null
			  || enumComponentType != null ) {
				// find a valid upgrade to dismount
				if ( itemStackHeld == null
				  || !tileEntityChunkLoader.hasUpgrade(enumComponentType) ) {
					enumComponentType = (EnumComponentType) tileEntityChunkLoader.getFirstUpgradeOfType(EnumComponentType.class, null);
				}
				
				if (enumComponentType == null) {
					// no more upgrades to dismount
					Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.noUpgradeToDismount"));
					return true;
				}
				
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the current upgrade item
					final ItemStack itemStackDrop = ItemComponent.getItemStackNoCache(enumComponentType, 1);
					final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityChunkLoader.dismountUpgrade(enumComponentType);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.dismounted", enumComponentType.name()));
				return false;
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			Commons.addChatMessage(entityPlayer, tileEntityChunkLoader.getStatus());
			return true;
			
		} else if (enumComponentType != null) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityChunkLoader.getUpgradeMaxCount(enumComponentType) <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.invalidUpgrade"));
				return true;
			}
			if (!tileEntityChunkLoader.canUpgrade(enumComponentType)) {
				// too many upgrades
				Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.tooManyUpgrades",
				                                                                  tileEntityChunkLoader.getUpgradeMaxCount(enumComponentType)));
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
			}
			
			// mount the new upgrade item
			tileEntityChunkLoader.mountUpgrade(enumComponentType);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.upgrade.result.mounted", enumComponentType.name()));
		}
		
		return false;
	}
}
