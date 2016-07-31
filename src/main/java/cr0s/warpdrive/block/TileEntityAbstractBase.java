package cr0s.warpdrive.block;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockUpdateDetector;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.VectorI;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class TileEntityAbstractBase extends TileEntity implements IBlockUpdateDetector, ITickable {
	private boolean isFirstTick = true;
	
	@Override
	public void update() {
		if (isFirstTick) {
			isFirstTick = false;
			onFirstUpdateTick();
		}
	}
	
	protected void onFirstUpdateTick() {
		// No operation
	}
	
	@Override
	public void updatedNeighbours() {
	}
	
	protected boolean isOnPlanet() {
		return worldObj.provider.getDimension() == 0;
	}
	
	protected void updateMetadata(int metadata) {
		if (getBlockMetadata() != metadata) {
			worldObj.setBlockState(pos, blockType.getStateFromMeta(metadata), 2);
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		/* TODO 1.10
		if (worldObj != null) {
			worldObj.markBlockForUpdate(pos);
		}
		/**/
	}
	
	// Inventory management methods
	
	public static ItemStack copyWithSize(ItemStack itemStack, int newSize) {
		ItemStack ret = itemStack.copy();
		ret.stackSize = newSize;
		return ret;
	}
	
	public static Collection<IInventory> getConnectedInventories(TileEntity tileEntityConnection) {
		Collection<IInventory> result = new ArrayList<>(6);
		
		for(EnumFacing side : EnumFacing.VALUES) {
			TileEntity tileEntity = tileEntityConnection.getWorld().getTileEntity(
				tileEntityConnection.getPos().offset(side));
			if (tileEntity != null && (tileEntity instanceof IInventory)) {
				result.add((IInventory) tileEntity);
				
				if (tileEntity instanceof TileEntityChest) {
					TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
					tileEntityChest.checkForAdjacentChests();
					if (tileEntityChest.adjacentChestXNeg != null) {
						result.add(tileEntityChest.adjacentChestXNeg);
					} else if (tileEntityChest.adjacentChestXPos != null) {
						result.add(tileEntityChest.adjacentChestXPos);
					} else if (tileEntityChest.adjacentChestZNeg != null) {
						result.add(tileEntityChest.adjacentChestZNeg);
					} else if (tileEntityChest.adjacentChestZPos != null) {
						result.add(tileEntityChest.adjacentChestZPos);
					}
				}
			}
		}
		return result;
	}
	
	protected boolean addToConnectedInventories(final ItemStack itemStack) {
		List<ItemStack> itemStacks = new ArrayList<>(1);
		itemStacks.add(itemStack);
		return addToInventories(itemStacks, getConnectedInventories(this));
	}
	
	protected boolean addToConnectedInventories(final List<ItemStack> itemStacks) {
		return addToInventories(itemStacks, getConnectedInventories(this));
	}
	
	protected boolean addToInventories(final List<ItemStack> itemStacks, final Collection<IInventory> inventories) {
		boolean overflow = false;
		if (itemStacks != null) {
			for (ItemStack itemStack : itemStacks) {
				int qtyLeft = itemStack.stackSize;
				ItemStack itemStackLeft = itemStack.copy();
				for (IInventory inventory : inventories) {
					qtyLeft = addToInventory(itemStack, inventory);
					if (qtyLeft > 0) {
						itemStackLeft.stackSize = qtyLeft;
					} else {
						break;
					}
				}
				if (qtyLeft > 0) {
					if (WarpDriveConfig.LOGGING_COLLECTION) {
						WarpDrive.logger.info(this + " Overflow detected");
					}
					overflow = true;
					int transfer;
					while (qtyLeft > 0) {
						transfer = Math.min(qtyLeft, itemStackLeft.getMaxStackSize());
						ItemStack itemStackDrop = copyWithSize(itemStackLeft, transfer);
						EntityItem entityItem = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, itemStackDrop);
						worldObj.spawnEntityInWorld(entityItem);
						qtyLeft -= transfer;
					}
				}
			}
		}
		return overflow;
	}
	
	private static int addToInventory(final ItemStack itemStackSource, IInventory inventory) {
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
	
	
	// searching methods
	
	public static final EnumFacing[] UP_DIRECTIONS = { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
	public static Set<VectorI> getConnectedBlocks(World world, VectorI start, EnumFacing[] directions, HashSet<Block> whitelist, int maxRange, VectorI... ignore) {
		return getConnectedBlocks(world, Arrays.asList(start), directions, whitelist, maxRange, ignore);
	}
	public static Set<VectorI> getConnectedBlocks(World world, Collection<VectorI> start, EnumFacing[] directions, HashSet<Block> whitelist, int maxRange, VectorI... ignore) {
		Set<VectorI> toIgnore = new HashSet<>();
		if (ignore != null) {
			toIgnore.addAll(Arrays.asList(ignore));
		}
		
		Set<VectorI> toIterate = new HashSet<>();
		toIterate.addAll(start);
		
		Set<VectorI> toIterateNext;
		
		Set<VectorI> iterated = new HashSet<>();
		
		int range = 0;
		while(!toIterate.isEmpty() && range < maxRange) {
			toIterateNext = new HashSet<>();
			for (VectorI current : toIterate) {
				if (whitelist.contains(current.getBlockState_noChunkLoading(world))) {
					iterated.add(current);
				}
				
				for(EnumFacing direction : directions) {
					VectorI next = current.clone(direction);
					if (!iterated.contains(next) && !toIgnore.contains(next) && !toIterate.contains(next) && !toIterateNext.contains(next)) {
						if (whitelist.contains(next.getBlockState_noChunkLoading(world))) {
							toIterateNext.add(next);
						}
					}
				}
			}
			toIterate = toIterateNext;
			range++;
		}
		
		return iterated;
	}
	
	
	// data manipulation methods
	
	protected static int toInt(double d) {
		return (int) Math.round(d);
	}
	
	protected static int toInt(Object object) {
		return toInt(toDouble(object));
	}
	
	protected static double toDouble(Object object) {
		return Double.parseDouble(object.toString());
	}
	
	protected static float toFloat(Object object) {
		return Float.parseFloat(object.toString());
	}
	
	protected static boolean toBool(Object object) {
		if (object == null) {
			 return false;
		}
		if (object instanceof Boolean) {
			 return ((Boolean) object);
		}
		String string = object.toString();
		return string.equals("true") || string.equals("1.0") || string.equals("1") || string.equals("y") || string.equals("yes");
	}
	
	protected static int clamp(final int min, final int max, final int value) {
		return Math.min(max, Math.max(value, min));
	}
	
	protected static float clamp(final float min, final float max, final float value) {
		return Math.min(max, Math.max(value, min));
	}
	
	protected static double clamp(final double min, final double max, final double value) {
		return Math.min(max, Math.max(value, min));
	}
	
	
	// area protection
	protected boolean isBlockBreakCanceled(EntityPlayer entityPlayer, World world, BlockPos blockPosEvent) {
		return WarpDrive.proxy.isBlockBreakCanceled(entityPlayer, pos, world, blockPosEvent);
	}
	
	protected boolean isBlockPlaceCanceled(EntityPlayer entityPlayer, World world, BlockPos blockPosEvent, IBlockState blockState) {
		return WarpDrive.proxy.isBlockPlaceCanceled(entityPlayer, pos, world, blockPosEvent, blockState);
	}
	
	// saved properties
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (tag.hasKey("upgrades")) {
			NBTTagCompound nbtTagCompoundUpgrades = tag.getCompoundTag("upgrades");
			Set<String> keys = nbtTagCompoundUpgrades.getKeySet();
			for (String key : keys) {
				Object object = getUpgradeFromString(key);
				int value = nbtTagCompoundUpgrades.getByte(key);
				if (object == null) {
					WarpDrive.logger.error("Found an unknown upgrade named '" + key + "' in " + this);
					object = key;
				}
				installedUpgrades.put(object, value);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		if (!installedUpgrades.isEmpty()) {
			NBTTagCompound nbtTagCompoundUpgrades = new NBTTagCompound();
			for (Entry<Object, Integer> entry : installedUpgrades.entrySet()) {
				String key = getUpgradeAsString(entry.getKey());
				nbtTagCompoundUpgrades.setByte(key, (byte)(int)entry.getValue());
			}
			tagCompound.setTag("upgrades", nbtTagCompoundUpgrades);
		}
		return tagCompound;
	}
	
	// upgrade system
	private final HashMap<Object, Integer> installedUpgrades = new HashMap<>(10);
	private final HashMap<Object, Integer> maxUpgrades = new HashMap<>(10);
	public boolean hasUpgrade(final Object upgrade) {
		return getUpgradeCount(upgrade) > 0;
	}
	
	private String getUpgradeAsString(Object object) {
		if (object instanceof Item) {
			return Item.REGISTRY.getNameForObject((Item)object).toString();
		} else if (object instanceof Block) {
			return Block.REGISTRY.getNameForObject((Block)object).toString();
		} else if (object instanceof ItemStack) {
			return Item.REGISTRY.getNameForObject(((ItemStack) object).getItem()) + ":" + ((ItemStack) object).getItemDamage();
		} else {
			return object.toString();
		}
	}
	
	private Object getUpgradeFromString(String name) {
		for (Object object : maxUpgrades.keySet()) {
			if (getUpgradeAsString(object).equals(name)) {
				return object;
			}
		}
		return null;
	}
	
	public Object getFirstUpgradeOfType(final Class clazz, final Object defaultValue) {
		for (Object object : installedUpgrades.keySet()) {
			if (clazz != null && clazz.isInstance(object)) {
				return object;
			}
		}
		return defaultValue;
	}
	
	public Map<Object, Integer> getUpgradesOfType(final Class clazz) {
		if (clazz == null) {
			return installedUpgrades;
		}
		Map<Object, Integer> mapResult = new HashMap<>(installedUpgrades.size());
		for (Entry<Object, Integer> entry : installedUpgrades.entrySet()) {
			if (clazz.isInstance(entry.getKey())) {
				mapResult.put(entry.getKey(), entry.getValue());
			}
		}
		return mapResult;
	}
	
	public int getUpgradeCount(final Object upgrade) {
		Integer value = installedUpgrades.get(upgrade);
		return value == null ? 0 : value;
	}
	
	public int getUpgradeMaxCount(final Object upgrade) {
		Integer value = maxUpgrades.get(upgrade);
		return value == null ? 0 : value;
	}
	
	protected String getUpgradesAsString() {
		String message = "";
		for (Entry<Object, Integer> entry : installedUpgrades.entrySet()) {
			if (!message.isEmpty()) {
				message += ", ";
			}
			Object key = entry.getKey();
			String keyName = key.toString();
			if (key instanceof Item) {
				keyName = ((Item) key).getUnlocalizedName();
			} else if (key instanceof Block) {
				keyName = ((Block) key).getUnlocalizedName();
			}
			if (entry.getValue() == 1) {
				message += keyName;
			} else {
				message += entry.getValue() + " x " + keyName;
			}
		}
		return message;
	}
	
	protected void setUpgradeMaxCount(final Object upgrade, final int value) {
		maxUpgrades.put(upgrade, value);
	}
	
	public boolean canUpgrade(final Object upgrade) {
		return getUpgradeMaxCount(upgrade) >= getUpgradeCount(upgrade) + 1;
	}
	
	public boolean mountUpgrade(final Object upgrade) {
		if (canUpgrade(upgrade)) {
			installedUpgrades.put(upgrade, getUpgradeCount(upgrade) + 1);
			markDirty();
			return true;
		}
		return false;
	}
	
	public boolean dismountUpgrade(final Object upgrade) {
		int count = getUpgradeCount(upgrade);
		if (count > 1) {
			installedUpgrades.put(upgrade, count - 1);
			markDirty();
			return true;
			
		} else if (count > 0) {
			installedUpgrades.remove(upgrade);
			markDirty();
			return true;
		}
		return false;
	}
}
