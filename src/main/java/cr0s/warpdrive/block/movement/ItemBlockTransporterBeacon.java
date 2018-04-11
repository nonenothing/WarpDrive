package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IItemTransporterBeacon;
import cr0s.warpdrive.api.computer.ITransporterCore;
import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemBlockTransporterBeacon extends ItemBlockAbstractBase implements IItemTransporterBeacon {
	
	public ItemBlockTransporterBeacon(final Block block) {
		super(block);
		setMaxStackSize(1);
		setMaxDamage(100);
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
		final int maxDamage = itemStack.getMaxDamage();
		itemStack.setItemDamage(maxDamage - maxDamage * energy / WarpDriveConfig.TRANSPORTER_BEACON_MAX_ENERGY_STORED);
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
		if ( isHeld
		  && entity instanceof EntityPlayer ) {
			final EntityPlayer entityPlayer = (EntityPlayer) entity;
			final ItemStack itemStackCheck = entityPlayer.inventory.getStackInSlot(indexSlot);
			if (itemStackCheck != itemStack) {
				WarpDrive.logger.error(String.format("Invalid item selection: possible dup tentative from %s",
				                                     entityPlayer));
				return;
			}
			
			// consume energy
			final int energy = getEnergy(itemStack) - WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK;
			if (energy >= 0) {
				final ItemStack itemStackNew = setEnergy(itemStack, energy);
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
		final Block block = world.getBlock(x, y, z);
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		
		if (!(tileEntity instanceof ITransporterCore)) {
			return super.onItemUse(itemStack, entityPlayer, world, x, y, z, side, hitX, hitY, hitZ);
		}
		if (!entityPlayer.canPlayerEdit(x, y, z, side, itemStack)) {
			return false;
		}
		
		if (entityPlayer.isSneaking()) {// update transporter signature
			ItemStack itemStackNew = setTransporterName(itemStack, ((ITransporterCore) tileEntity).getStarMapName());
			itemStackNew = setTransporterSignature(itemStackNew, ((ITransporterCore) tileEntity).getUUID());
			// @TODO feedback to player
			world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
			                      block.stepSound.func_150496_b(),
			                      (block.stepSound.getVolume() + 1.0F) / 2.0F,
					              block.stepSound.getPitch() * 0.8F);
			
		} else {// apply signature to transporter
			final UUID uuid = getTransporterSignature(itemStack);
			if (uuid != null) {
				((ITransporterCore) tileEntity).remoteLocation(new Object[] { uuid });
				// @TODO feedback to player
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
				                      block.stepSound.func_150496_b(),
				                      (block.stepSound.getVolume() + 1.0F) / 2.0F,
				                      block.stepSound.getPitch() * 0.8F);
			}
		}
		
		return true;
	}
}
