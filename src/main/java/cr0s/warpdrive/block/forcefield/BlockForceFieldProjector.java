package cr0s.warpdrive.block.forcefield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldShape;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class BlockForceFieldProjector extends BlockAbstractForceField {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockForceFieldProjector(final byte tier) {
		super(tier, Material.iron);
		isRotating = true;
		setBlockName("warpdrive.forcefield.projector" + tier);
		setBlockTextureName("warpdrive:forcefield/projector");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[11];
		icons[ 0] = iconRegister.registerIcon("warpdrive:forcefield/projectorSide_notConnected");
		icons[ 1] = iconRegister.registerIcon("warpdrive:forcefield/projectorSide_connectedNotPowered");
		icons[ 2] = iconRegister.registerIcon("warpdrive:forcefield/projectorSide_connectedPowered");
		icons[ 3] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_none");
		icons[ 4] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_sphere");
		icons[ 5] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_cylinder_h");
		icons[ 6] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_cylinder_v");
		icons[ 7] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_cube");
		icons[ 8] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_plane");
		icons[ 9] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_tube");
		icons[10] = iconRegister.registerIcon("warpdrive:forcefield/projectorShape_tunnel");
	}
	
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		int metadata = world.getBlockMetadata(x, y, z);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null || !(tileEntity instanceof TileEntityForceFieldProjector)) {
			return icons[0];
		}
		
		if (side == (metadata & 7) || (((TileEntityForceFieldProjector)tileEntity).isDoubleSided && ForgeDirection.OPPOSITES[side] == (metadata & 7))) {
			return icons[3 + ((TileEntityForceFieldProjector)tileEntity).getShape().ordinal()];
		} else if (((TileEntityForceFieldProjector)tileEntity).isConnected) {
			if (((TileEntityForceFieldProjector)tileEntity).isPowered) {
				return icons[2];
			} else {
				return icons[1];
			}
		}
		
		return icons[0];
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return side == 3 ? icons[4] : icons[2];
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(x, y, z);
		tileEntityForceFieldProjector.isDoubleSided = (itemStack.getItemDamage() == 1);
		if (itemStack.hasTagCompound()) {
			tileEntityForceFieldProjector.readFromNBT(itemStack.getTagCompound());
		}
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
	}
	
	@Override
	protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack itemStack) {
		// TODO: TE is already removed, need another method
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(x, y, z);
		if (tileEntityForceFieldProjector == null) {
			WarpDrive.logger.error("Missing tile entity for " + this + " at " + world + " " + x + " " + y + " " + z);
		} else {
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			tileEntityForceFieldProjector.writeToNBT(nbtTagCompound);
			itemStack.setTagCompound(nbtTagCompound);
		}
		world.setBlockToAir(x, y, z);
		super.dropBlockAsItem(world, x, y, z, itemStack);
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(x, y, z);
		super.breakBlock(world, x, y, z, block, metadata);
	}
	
	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return super.getItemDropped(p_149650_1_, p_149650_2_, p_149650_3_);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityForceFieldProjector)) {
			return false;
		}
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector) tileEntity;
		ItemStack itemStackHeld = entityPlayer.getHeldItem();
		int metadata = world.getBlockMetadata(x, y, z);
		
		if (itemStackHeld == null) {
			if (entityPlayer.isSneaking()) {
				if (tileEntityForceFieldProjector.getShape() != EnumForceFieldShape.NONE) {
					if (side == (metadata & 7) || (tileEntityForceFieldProjector.isDoubleSided && ForgeDirection.OPPOSITES[side] == (metadata & 7))) {
						// dismount the shape item(s)
						ItemStack itemStackDrop = ItemForceFieldShape.getItemStackNoCache(tileEntityForceFieldProjector.getShape(), tileEntityForceFieldProjector.isDoubleSided ? 2 : 1);
						EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
						entityItem.delayBeforeCanPickup = 0;
						world.spawnEntityInWorld(entityItem);
						
						tileEntityForceFieldProjector.setShape(EnumForceFieldShape.NONE);
					} else {
						// wrong side TODO
						WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
						return true;
					}
				} else {
					// no shape to dismount TODO
					WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
					return true;
				}
			} else {
				WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
				return true;
			}
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldShape) {
			if (side == (metadata & 7) || (((TileEntityForceFieldProjector) tileEntity).isDoubleSided && ForgeDirection.OPPOSITES[side] == (metadata & 7))) {
				// validate quantity
				if (itemStackHeld.stackSize < (tileEntityForceFieldProjector.isDoubleSided ? 2 : 1)) {
					// not enough shape items TODO
					WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= tileEntityForceFieldProjector.isDoubleSided ? 2 : 1;
				
				// dismount the shape item(s)
				if (tileEntityForceFieldProjector.getShape() != EnumForceFieldShape.NONE) {
					ItemStack itemStackDrop = ItemForceFieldShape.getItemStackNoCache(tileEntityForceFieldProjector.getShape(), tileEntityForceFieldProjector.isDoubleSided ? 2 : 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.delayBeforeCanPickup = 0;
					world.spawnEntityInWorld(entityItem);
				}
				
				// mount the new shape item(s)
				tileEntityForceFieldProjector.setShape(EnumForceFieldShape.get(itemStackHeld.getItemDamage()));
			} else {
				// wrong side TODO
				WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityForceFieldProjector();
	}
	
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer entityPlayer) {
		ItemStack itemStack = super.getPickBlock(target, world, x, y, z, entityPlayer);
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(x, y, z);
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		tileEntityForceFieldProjector.writeToNBT(nbtTagCompound);
		itemStack.setTagCompound(nbtTagCompound);
		return itemStack;
	}
}
