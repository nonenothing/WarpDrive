package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockChunkLoader extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockChunkLoader() {
		super(Material.iron);
		setBlockName("warpdrive.machines.ChunkLoader");
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityChunkLoader();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[3];
		icons[0] = iconRegister.registerIcon("warpdrive:chunk_loader-offline");
		icons[1] = iconRegister.registerIcon("warpdrive:chunk_loader-out_of_power");
		icons[2] = iconRegister.registerIcon("warpdrive:chunk_loader-active");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		if (metadata < icons.length) {
			return icons[metadata];
		}
		
		return icons[0];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int damage) {
		return icons[2];
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityChunkLoader)) {
			return false;
		}
		final TileEntityChunkLoader tileEntityChunkLoader = (TileEntityChunkLoader) tileEntity;
		final ItemStack itemStackHeld = entityPlayer.getHeldItem();
		
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
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.noUpgradeToDismount"));
					return true;
				}
				
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the current upgrade item
					final ItemStack itemStackDrop = ItemComponent.getItemStackNoCache(enumComponentType, 1);
					final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.delayBeforeCanPickup = 0;
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityChunkLoader.dismountUpgrade(enumComponentType);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.dismounted", enumComponentType.name()));
				return false;
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			Commons.addChatMessage(entityPlayer, tileEntityChunkLoader.getStatus());
			return true;
			
		} else if (enumComponentType != null) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityChunkLoader.getUpgradeMaxCount(enumComponentType) <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.invalidUpgrade"));
				return true;
			}
			if (!tileEntityChunkLoader.canUpgrade(enumComponentType)) {
				// too many upgrades
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.tooManyUpgrades",
				                                                                             tileEntityChunkLoader.getUpgradeMaxCount(enumComponentType)));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.notEnoughUpgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= 1;
			}
			
			// mount the new upgrade item
			tileEntityChunkLoader.mountUpgrade(enumComponentType);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.mounted", enumComponentType.name()));
		}
		
		return false;
	}
}
