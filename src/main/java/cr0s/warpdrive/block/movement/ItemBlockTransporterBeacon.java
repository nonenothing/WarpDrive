package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IItemTransporterBeacon;
import cr0s.warpdrive.api.computer.ITransporterCore;
import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumTransporterBeaconState;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemBlockTransporterBeacon extends ItemBlockAbstractBase implements IItemTransporterBeacon {
	
	public ItemBlockTransporterBeacon(final Block block) {
		super(block);
		setMaxStackSize(1);
		setMaxDamage(100 * 8);
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemstack) {
		return getUnlocalizedName();
	}
	
	private static String getTransporterName(final ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemBlockTransporterBeacon)) {
			return "";
		}
		if (!itemStack.hasTagCompound()) {
			return "";
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		final String name = tagCompound.getString("name");
		final UUID uuid = new UUID(tagCompound.getLong("uuidMost"), tagCompound.getLong("uuidLeast"));
		if (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0) {
			return "";
		}
		return name;
	}
	
	private static ItemStack setTransporterName(final ItemStack itemStack, final String name) {
		if (!(itemStack.getItem() instanceof ItemBlockTransporterBeacon)) {
			return itemStack;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		if ( name == null
		  || name.isEmpty() ) {
			tagCompound.removeTag("name");
		} else {
			tagCompound.setString("name", name);
		}
		itemStack.setTagCompound(tagCompound);
		return itemStack;
	}
	
	private static UUID getTransporterSignature(final ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemBlockTransporterBeacon)) {
			return null;
		}
		if (!itemStack.hasTagCompound()) {
			return null;
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		final UUID uuid = new UUID(tagCompound.getLong("uuidMost"), tagCompound.getLong("uuidLeast"));
		if (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0) {
			return null;
		}
		return uuid;
	}
	
	private static ItemStack setTransporterSignature(final ItemStack itemStack, final UUID uuid) {
		if (!(itemStack.getItem() instanceof ItemBlockTransporterBeacon)) {
			return itemStack;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		if (uuid == null) {
			tagCompound.removeTag("uuidMost");
			tagCompound.removeTag("uuidLeast");
		} else {
			tagCompound.setLong("uuidMost", uuid.getMostSignificantBits());
			tagCompound.setLong("uuidLeast", uuid.getLeastSignificantBits());
		}
		itemStack.setTagCompound(tagCompound);
		return itemStack;
	}
	
	private static int getEnergy(final ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemBlockTransporterBeacon)) {
			return 0;
		}
		if (!itemStack.hasTagCompound()) {
			return 0;
		}
		final NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound.hasKey(TileEntityAbstractEnergy.ENERGY_TAG)) {
			return tagCompound.getInteger(TileEntityAbstractEnergy.ENERGY_TAG);
		}
		return 0;
	}
	
	private static ItemStack setEnergy(final ItemStack itemStack, final int energy) {
		if (!(itemStack.getItem() instanceof ItemBlockTransporterBeacon)) {
			return itemStack;
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		tagCompound.setInteger(TileEntityAbstractEnergy.ENERGY_TAG, energy);
		itemStack.setTagCompound(tagCompound);
		return itemStack;
	}
	
	private static ItemStack updateDamage(final ItemStack itemStack, final int energy, final boolean isActive) {
		final int maxDamage = itemStack.getMaxDamage();
		final int metadataEnergy = maxDamage - maxDamage * energy / WarpDriveConfig.TRANSPORTER_BEACON_MAX_ENERGY_STORED;
		final EnumTransporterBeaconState enumTransporterBeaconState = isActive ? EnumTransporterBeaconState.PACKED_ACTIVE : EnumTransporterBeaconState.PACKED_INACTIVE;
		itemStack.setItemDamage((metadataEnergy & ~0x7) + enumTransporterBeaconState.getMetadata());
		return itemStack;
	}
	
	// ITransporterBeacon overrides
	@Override
	public boolean isActive(final ItemStack itemStack) {
		return getEnergy(itemStack) > WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK;
	}
	
	// Item overrides
	@Override
	public void onUpdate(final ItemStack itemStack, final World world, final Entity entity, final int indexSlot, final boolean isHeld) {
		if (entity instanceof EntityPlayer) {
			final EntityPlayer entityPlayer = (EntityPlayer) entity;
			final ItemStack itemStackCheck = entityPlayer.inventory.getStackInSlot(indexSlot);
			if (itemStackCheck != itemStack) {
				WarpDrive.logger.error(String.format("Invalid item selection: possible dup tentative from %s",
				                                     entityPlayer));
				return;
			}
			
			// consume energy
			final int energy =  isHeld ? getEnergy(itemStack) - WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK : -1;
			if (energy >= 0) {
				ItemStack itemStackNew;
				itemStackNew = setEnergy(itemStack, energy);
				itemStackNew = updateDamage(itemStackNew, energy, true);
				((EntityPlayer) entity).inventory.setInventorySlotContents(indexSlot, itemStackNew);
				
			} else if (itemStack.getItemDamage() != EnumTransporterBeaconState.PACKED_INACTIVE.getMetadata()) {
				final ItemStack itemStackNew = updateDamage(itemStack, energy, false);
				((EntityPlayer) entity).inventory.setInventorySlotContents(indexSlot, itemStackNew);
			}
		}
		super.onUpdate(itemStack, world, entity, indexSlot, isHeld);
	}
	
	@Override
	public boolean onItemUse(final ItemStack itemStack, final EntityPlayer entityPlayer, final World world,
	                         final int x, final int y, final int z, final int side,
	                         final float hitX, final float hitY, final float hitZ) {
		if (itemStack.stackSize == 0) {
			return false;
		}
		
		// check if clicked block can be interacted with
		// final Block block = world.getBlock(x, y, z);
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		
		if (!(tileEntity instanceof ITransporterCore)) {
			return super.onItemUse(itemStack, entityPlayer, world, x, y, z, side, hitX, hitY, hitZ);
		}
		if (!entityPlayer.canPlayerEdit(x, y, z, side, itemStack)) {
			return false;
		}
		
		if (entityPlayer.isSneaking()) {// update transporter signature
			ItemStack itemStackNew = setTransporterName(itemStack, ((ITransporterCore) tileEntity).getStarMapName());
			setTransporterSignature(itemStackNew, ((ITransporterCore) tileEntity).getUUID());
			world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
			                      "mob.zombie.unfect",
			                      1.0F,
			                      world.rand.nextFloat() * 0.2F + 1.8F);
			
		} else {// apply signature to transporter
			final UUID uuid = getTransporterSignature(itemStack);
			if (uuid != null) {
				((ITransporterCore) tileEntity).remoteLocation(new Object[] { uuid });
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
				                      "mob.zombie.infect",
				                      1.0F,
				                      world.rand.nextFloat() * 0.2F + 1.2F);
			}
		}
		
		return true;
	}
}
