package cr0s.warpdrive.block;

import java.util.ArrayList;
import java.util.List;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockUpdateDetector;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileEntityAbstractBase extends TileEntity implements IBlockUpdateDetector {
	
	@Override
	public void updatedNeighbours() {
	}
	
	protected boolean isOnEarth() {
		return worldObj.provider.dimensionId == 0;
	}
	
	protected void updateMetadata(int metadata) {
		if (getBlockMetadata() != metadata) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 2);
		}
	}
	
	// Inventory management methods
	
	public static ItemStack copyWithSize(ItemStack itemStack, int newSize) {
		ItemStack ret = itemStack.copy();
		ret.stackSize = newSize;
		return ret;
	}
	
	protected IInventory getFirstConnectedInventory() {
		TileEntity result = null;
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			result = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
			if (result != null && (result instanceof IInventory)) {
				return (IInventory) result;
			}
		}
		return null;
	}
	
	protected boolean addToConnectedInventory(ItemStack itemStack) {
		List<ItemStack> stacks = new ArrayList<ItemStack>(1);
		stacks.add(itemStack);
		return addToConnectedInventory(stacks);
	}
	
	protected boolean addToConnectedInventory(List<ItemStack> stacks) {
		boolean overflow = false;
		if (stacks != null) {
			int qtyLeft = 0;
			for (ItemStack stack : stacks) {
				qtyLeft = addToInventory(getFirstConnectedInventory(), stack);
				if (qtyLeft > 0) {
					if (WarpDriveConfig.LOGGING_COLLECTION) {
						WarpDrive.logger.info(this + " Overflow detected");
					}
					overflow = true;
					int transfer;
					while (qtyLeft > 0) {
						transfer = Math.min(qtyLeft, stack.getMaxStackSize());
						ItemStack dropItemStack = copyWithSize(stack, transfer);
						EntityItem itemEnt = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 1.0D, zCoord + 0.5D, dropItemStack);
						worldObj.spawnEntityInWorld(itemEnt);
						qtyLeft -= transfer;
					}
				}
			}
		}
		return overflow;
	}
	
	private int addToInventory(IInventory inventory, ItemStack itemStackSource) {
		if (itemStackSource == null) {
			return 0;
		}
		
		int qtyLeft = itemStackSource.stackSize;
		int transfer;
		
		if (inventory != null) {
			// fill existing stacks first
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				if (!inventory.isItemValidForSlot(i, itemStackSource)) {
					continue;
				}
				
				ItemStack itemStack = inventory.getStackInSlot(i);
				if (itemStack == null || !itemStack.isItemEqual(itemStackSource)) {
					continue;
				}
				
				transfer = Math.min(qtyLeft, itemStack.getMaxStackSize() - itemStack.stackSize);
				itemStack.stackSize += transfer;
				qtyLeft -= transfer;
				if (qtyLeft <= 0) {
					return 0;
				}
			}
			
			// put remaining in empty slot
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				if (!inventory.isItemValidForSlot(i, itemStackSource)) {
					continue;
				}
				
				ItemStack itemStack = inventory.getStackInSlot(i);
				if (itemStack != null) {
					continue;
				}
				
				transfer = Math.min(qtyLeft, itemStackSource.getMaxStackSize());
				ItemStack dest = copyWithSize(itemStackSource, transfer);
				inventory.setInventorySlotContents(i, dest);
				qtyLeft -= transfer;
				
				if (qtyLeft <= 0) {
					return 0;
				}
			}
		}
		
		return qtyLeft;
	}
	
	protected static int toInt(double d) {
		return (int) Math.round(d);
	}
	
	protected static int toInt(Object o) {
		return toInt(toDouble(o));
	}
	
	protected static double toDouble(Object o) {
		return Double.parseDouble(o.toString());
	}

	protected static float toFloat(Object o) {
		return Float.parseFloat(o.toString());
	}
	
	protected static boolean toBool(Object o) {
		if (o == null) {
			 return false;
		}
		if (o instanceof Boolean) {
			 return ((Boolean) o);
		}
		if (o.toString() == "true" || o.toString() == "1.0" || o.toString() == "1" || o.toString() == "y" || o.toString() == "yes") {
			return true;
		}
		return false;
	}
	
	protected static int clamp(final int min, final int max, final int value) {
		return Math.min(max, Math.max(value, min));
	}
	
	protected static double clamp(final double min, final double max, final double value) {
		return Math.min(max, Math.max(value, min));
	}
}
