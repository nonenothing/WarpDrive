package cr0s.warpdrive.block.forcefield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockForceFieldRelay extends BlockAbstractForceField {
	@SideOnly(Side.CLIENT)
	private final IIcon[] icons;
	
	public BlockForceFieldRelay(final byte tier) {
		super(tier, Material.iron);
		isRotating = false;
		setBlockName("warpdrive.forcefield.relay" + tier);
		setBlockTextureName("warpdrive:forcefield/relay");
		
		icons = new IIcon[EnumForceFieldUpgrade.length + 1];
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		for (EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			if (enumForceFieldUpgrade.allowOnRelay) {
				icons[enumForceFieldUpgrade.ordinal()] = iconRegister.registerIcon("warpdrive:forcefield/relay" + "_" + enumForceFieldUpgrade.unlocalizedName);
			} else {
				icons[enumForceFieldUpgrade.ordinal()] = iconRegister.registerIcon("warpdrive:forcefield/relay" + "_" + EnumForceFieldUpgrade.NONE.unlocalizedName);
			}
		}
		icons[EnumForceFieldUpgrade.length] = iconRegister.registerIcon("warpdrive:forcefield/relay_top");
	}
	
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null || !(tileEntity instanceof TileEntityForceFieldRelay)) {
			return icons[0];
		}
		
		if (side == 0 || side == 1) {
			return icons[EnumForceFieldUpgrade.length];
		}
		return icons[((TileEntityForceFieldRelay)tileEntity).getUpgrade().ordinal()];
	}
	
	@Override
	public IIcon getIcon(int side, int damage) {
		if (side == 0 || side == 1) {
			return icons[EnumForceFieldUpgrade.length];
		}
		if (damage >= 0 && damage < EnumForceFieldUpgrade.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public int damageDropped(int damage) {
		return damage;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityForceFieldRelay)) {
			return false;
		}
		TileEntityForceFieldRelay tileEntityForceFieldRelay = (TileEntityForceFieldRelay) tileEntity;
		ItemStack itemStackHeld = entityPlayer.getHeldItem();
		
		if (itemStackHeld == null) {
			if (entityPlayer.isSneaking()) {
				if (tileEntityForceFieldRelay.getUpgrade() != EnumForceFieldUpgrade.NONE) {
					// dismount the upgrade item(s)
					ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(tileEntityForceFieldRelay.getUpgrade(), 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.delayBeforeCanPickup = 0;
					world.spawnEntityInWorld(entityItem);
					tileEntityForceFieldRelay.setUpgrade(EnumForceFieldUpgrade.NONE);
				} else {
					// no upgrade to dismount TODO
					WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldRelay.getStatus());
					return true;
				}
			} else {
				WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldRelay.getStatus());
				return true;
			}
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {
			// validate quantity
			if (itemStackHeld.stackSize < 1) {
				// not enough upgrade items TODO
				WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldRelay.getStatus());
				return true;
			}
			
			// update player inventory
			itemStackHeld.stackSize -= 1;
			
			// dismount the shape item(s)
			if (tileEntityForceFieldRelay.getUpgrade() != EnumForceFieldUpgrade.NONE) {
				ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(tileEntityForceFieldRelay.getUpgrade(), 1);
				EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
				entityItem.delayBeforeCanPickup = 0;
				world.spawnEntityInWorld(entityItem);
			}
			
			// mount the new upgrade item(s)
			tileEntityForceFieldRelay.setUpgrade(EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage()));
		}
		
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityForceFieldRelay();
	}
	
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer entityPlayer) {
		return null; // FIXME
	}
}
