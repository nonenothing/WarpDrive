package cr0s.warpdrive.block.hull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class ItemBlockHullSlab extends ItemBlockHull {
	
	private final Block blockSlab;
	
	public ItemBlockHullSlab(final Block blockSlab) {
		super(blockSlab);
		this.blockSlab = blockSlab;
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemstack) {
		return getUnlocalizedName();
	}
	
	@Override
	public boolean onItemUse(final ItemStack itemStack, final EntityPlayer entityPlayer, final World world,
	                         final int x, final int y, final int z, final int side,
	                         final float hitX, final float hitY, final float hitZ) {
		if (itemStack.stackSize == 0) {
			return false;
		}
		
		// check if clicked block can be interacted with
		final ForgeDirection facing = ForgeDirection.getOrientation(side);
		final int metadataItem = itemStack.getItemDamage();
		final Block blockWorld = world.getBlock(x, y, z);
		final int metadataWorld = world.getBlockMetadata(x, y, z);
		final int typeWorld = getType(metadataWorld);
		final int typeItem = getType(metadataItem);
		
		if ( blockWorld == blockSlab
		   && metadataItem < 12
		   && metadataWorld < 12
		   && typeWorld == typeItem ) {
			if (!entityPlayer.canPlayerEdit(x, y, z, side, itemStack)) {
				return false;
			}
			
			// try to merge slabs when right-clicking directly the inner face
			if (metadataWorld - typeWorld == facing.getOpposite().ordinal()) {
				final AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
				if (world.checkNoEntityCollision(boundingBox)) {
					if (typeWorld == 0) {// plain
						world.setBlock(x, y, z, blockSlab, 12, 3);
					} else {
						world.setBlock(x, y, z, blockSlab, 13 + side / 2, 3);
					}
					world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
							blockSlab.stepSound.func_150496_b(),
							(blockSlab.stepSound.getVolume() + 1.0F) / 2.0F,
							blockSlab.stepSound.getPitch() * 0.8F);
					itemStack.stackSize--;
				}
				
				return true;
			}
			
		} else {
			// check is closer block can be interacted with
			final int xSide = x + facing.offsetX;
			final int ySide = y + facing.offsetY;
			final int zSide = z + facing.offsetZ;
			final Block blockSide = world.getBlock(xSide, ySide, zSide);
			final int metadataSide = world.getBlockMetadata(xSide, ySide, zSide);
			final int typeSide = getType(metadataSide);
			
			if ( blockSide == blockSlab
			  && metadataItem < 12
			  && metadataSide < 12
			  && typeSide == typeItem ) {
				if (!entityPlayer.canPlayerEdit(xSide, ySide, zSide, side, itemStack)) {
					return false;
				}
				
				// try to place ignoring the existing block
				final int metadataPlaced = blockSlab.onBlockPlaced(world, xSide, ySide, zSide, side, hitX, hitY, hitZ, metadataItem);
				final int sidePlaced = ForgeDirection.getOrientation(metadataPlaced - typeSide).getOpposite().ordinal();
				
				// try to merge slabs when right-clicking on a side block
				if (sidePlaced == metadataSide - typeSide) {
					final AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(xSide, ySide, zSide, xSide + 1.0D, ySide + 1.0D, zSide + 1.0D);
					if (world.checkNoEntityCollision(boundingBox)) {
						if (typeSide == 0) {// plain
							world.setBlock(xSide, ySide, zSide, blockSlab, 12, 3);
						} else {
							world.setBlock(xSide, ySide, zSide, blockSlab, 13 + sidePlaced / 2, 3);
						}
						world.playSoundEffect(xSide + 0.5D, ySide + 0.5D, zSide + 0.5D,
								blockSlab.stepSound.func_150496_b(),
								(blockSlab.stepSound.getVolume() + 1.0F) / 2.0F,
								blockSlab.stepSound.getPitch() * 0.8F);
						itemStack.stackSize--;
					}
					
					return true;
				}
				
			}
		}
		
		return super.onItemUse(itemStack, entityPlayer, world, x, y, z, side, hitX, hitY, hitZ);
	}
	
	private int getType(final int metadata) {
		return metadata <= 5 ? 0 : 6;
	}
	
	// Should return true if item can be used
	@Override
	public boolean func_150936_a(final World world, final int x, final int y, final int z, final int side, final EntityPlayer entityPlayer, final ItemStack itemStack) {
		// check if clicked block can be interacted with
		final int metadataItem = itemStack.getItemDamage();
		final Block blockWorld = world.getBlock(x, y, z);
		final int metadataWorld = world.getBlockMetadata(x, y, z);
		final int typeWorld = getType(metadataWorld);
		final int typeItem = getType(metadataItem);
		if (blockWorld == blockSlab && metadataItem < 12 && metadataWorld < 12 && typeWorld == typeItem) {
			return true;
		}
		
		// check the block on our side
		final ForgeDirection direction = ForgeDirection.getOrientation(side);
		final int xSide = x + direction.offsetX;
		final int ySide = y + direction.offsetY;
		final int zSide = z + direction.offsetZ;
		final Block blockSide = world.getBlock(xSide, ySide, zSide);
		final int metadataSide = world.getBlockMetadata(xSide, ySide, zSide);
		final int typeSide = getType(metadataSide);
		if (blockSide == blockSlab && typeSide == typeItem) {
			return true;
		}
		
		// default behavior
		return super.func_150936_a(world, x, y, z, side, entityPlayer, itemStack);
	}
}
