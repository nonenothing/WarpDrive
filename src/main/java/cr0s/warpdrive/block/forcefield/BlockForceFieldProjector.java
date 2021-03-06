package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.data.EnumForceFieldShape;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.item.ItemForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockForceFieldProjector extends BlockAbstractForceField {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockForceFieldProjector(final byte tier) {
		super(tier, Material.iron);
		isRotating = true;
		setBlockName("warpdrive.forcefield.projector" + tier);
		setBlockTextureName("warpdrive:forcefield/projector");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[11];
		icons[ 0] = iconRegister.registerIcon("warpdrive:forcefield/projector-side_not_connected");
		icons[ 1] = iconRegister.registerIcon("warpdrive:forcefield/projector-side_connected_not_powered");
		icons[ 2] = iconRegister.registerIcon("warpdrive:forcefield/projector-side_connected_powered");
		icons[ 3] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_none");
		icons[ 4] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_sphere");
		icons[ 5] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_cylinder_h");
		icons[ 6] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_cylinder_v");
		icons[ 7] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_cube");
		icons[ 8] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_plane");
		icons[ 9] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_tube");
		icons[10] = iconRegister.registerIcon("warpdrive:forcefield/projector-shape_tunnel");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityForceFieldProjector)) {
			return icons[0];
		}
		
		if (side == (metadata & 7) || (((TileEntityForceFieldProjector) tileEntity).isDoubleSided && ForgeDirection.OPPOSITES[side] == (metadata & 7))) {
			return icons[3 + ((TileEntityForceFieldProjector) tileEntity).getShape().ordinal()];
		}
		if ( !((TileEntityForceFieldProjector) tileEntity).isConnected
		  || !((TileEntityForceFieldProjector) tileEntity).isEnabled ) {
			return icons[0];
		}
		if (!((TileEntityForceFieldProjector) tileEntity).isPowered) {
			return icons[1];
		}
		
		return icons[2];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return side == 3 ? icons[4] : icons[2];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityForceFieldProjector();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(final Item item, final CreativeTabs creativeTab, final List list) {
		for (int i = 0; i < 2; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@Override
	public int getDamageValue(final World world, final int x, final int y, final int z) {
		super.getDamageValue(world, x, y, z);
		final TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(x, y, z);
		return tileEntityForceFieldProjector.isDoubleSided ? 1 : 0;
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entityLiving, final ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
		final TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(x, y, z);
		if (!itemStack.hasTagCompound()) {
			tileEntityForceFieldProjector.isDoubleSided = (itemStack.getItemDamage() == 1);
		}
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityForceFieldProjector)) {
			return false;
		}
		final TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector) tileEntity;
		final ItemStack itemStackHeld = entityPlayer.getHeldItem();
		final int metadata = world.getBlockMetadata(x, y, z);
		
		EnumForceFieldUpgrade enumForceFieldUpgrade = EnumForceFieldUpgrade.NONE;
		if (itemStackHeld != null && itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {
			enumForceFieldUpgrade = EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage());
		}
		
		// sneaking with an empty hand or an upgrade/shape item in hand to dismount current upgrade/shape
		if (entityPlayer.isSneaking()) {
			// using an upgrade item or no shape defined means dismount upgrade, otherwise dismount shape
			if ( (itemStackHeld != null && itemStackHeld.getItem() instanceof ItemForceFieldUpgrade)
			  || (tileEntityForceFieldProjector.getShape() == EnumForceFieldShape.NONE)
			  || (side != (metadata & 7) && (!tileEntityForceFieldProjector.isDoubleSided || ForgeDirection.OPPOSITES[side] != (metadata & 7))) ) {
				// find a valid upgrade to dismount
				if (!tileEntityForceFieldProjector.hasUpgrade(enumForceFieldUpgrade)) {
					enumForceFieldUpgrade = (EnumForceFieldUpgrade)tileEntityForceFieldProjector.getFirstUpgradeOfType(EnumForceFieldUpgrade.class, EnumForceFieldUpgrade.NONE);
				}
				
				if (enumForceFieldUpgrade == EnumForceFieldUpgrade.NONE) {
					// no more upgrades to dismount
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.no_upgrade_to_dismount"));
					return true;
				}
				
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the current upgrade item
					final ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(enumForceFieldUpgrade, 1);
					final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.delayBeforeCanPickup = 0;
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityForceFieldProjector.dismountUpgrade(enumForceFieldUpgrade);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.dismounted", enumForceFieldUpgrade.name()));
				return false;
				
			} else {// default to dismount shape
				if (tileEntityForceFieldProjector.getShape() != EnumForceFieldShape.NONE) {
					if (side == (metadata & 7) || (tileEntityForceFieldProjector.isDoubleSided && ForgeDirection.OPPOSITES[side] == (metadata & 7))) {
						if (!entityPlayer.capabilities.isCreativeMode) {
							// dismount the shape item(s)
							final ItemStack itemStackDrop = ItemForceFieldShape.getItemStackNoCache(tileEntityForceFieldProjector.getShape(), tileEntityForceFieldProjector.isDoubleSided ? 2 : 1);
							final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
							entityItem.delayBeforeCanPickup = 0;
							world.spawnEntityInWorld(entityItem);
						}
						
						tileEntityForceFieldProjector.setShape(EnumForceFieldShape.NONE);
						// shape dismounted
						Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.shape_dismounted"));
					} else {
						// wrong side
						Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.wrong_shape_side"));
						return true;
					}
				} else {
					// no shape to dismount
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.no_shape_to_dismount"));
					return true;
				}
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			Commons.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
			return true;
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldShape) {// no sneaking and shape in hand => mounting a shape
			if (side == (metadata & 7) || (((TileEntityForceFieldProjector) tileEntity).isDoubleSided && ForgeDirection.OPPOSITES[side] == (metadata & 7))) {
				if (!entityPlayer.capabilities.isCreativeMode) {
					// validate quantity
					if (itemStackHeld.stackSize < (tileEntityForceFieldProjector.isDoubleSided ? 2 : 1)) {
						// not enough shape items
						Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted(
							tileEntityForceFieldProjector.isDoubleSided ?
								"warpdrive.upgrade.result.not_enough_shapes.double" : "warpdrive.upgrade.result.not_enough_shapes.single"));
						return true;
					}
					
					// update player inventory
					itemStackHeld.stackSize -= tileEntityForceFieldProjector.isDoubleSided ? 2 : 1;
					
					// dismount the current shape item(s)
					if (tileEntityForceFieldProjector.getShape() != EnumForceFieldShape.NONE) {
						final ItemStack itemStackDrop = ItemForceFieldShape.getItemStackNoCache(tileEntityForceFieldProjector.getShape(), tileEntityForceFieldProjector.isDoubleSided ? 2 : 1);
						final EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
						entityItem.delayBeforeCanPickup = 0;
						world.spawnEntityInWorld(entityItem);
					}
				}
				
				// mount the new shape item(s)
				tileEntityForceFieldProjector.setShape(EnumForceFieldShape.get(itemStackHeld.getItemDamage()));
				// shape mounted
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.shape_mounted"));
				
			} else {
				// wrong side
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.wrong_shape_side"));
				return true;
			}
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityForceFieldProjector.getUpgradeMaxCount(enumForceFieldUpgrade) <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.invalid_upgrade_for_projector"));
				return true;
			}
			if (!tileEntityForceFieldProjector.canUpgrade(enumForceFieldUpgrade)) {
				// too many upgrades
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.too_many_upgrades",
					tileEntityForceFieldProjector.getUpgradeMaxCount(enumForceFieldUpgrade)));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.not_enough_upgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= 1;
			}
			
			// mount the new upgrade item
			tileEntityForceFieldProjector.mountUpgrade(enumForceFieldUpgrade);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.mounted", enumForceFieldUpgrade));
		}
		
		return false;
	}
}
