package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.api.IBlockUpdateDetector;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.common.util.ForgeDirection;

@Optional.InterfaceList({
	@Optional.Interface(iface = "defense.api.IEMPBlock", modid = "DefenseTech"),
	@Optional.Interface(iface = "resonant.api.explosion.IEMPBlock", modid = "icbmclassic")
})
public abstract class BlockAbstractContainer extends BlockContainer implements IBlockBase, defense.api.IEMPBlock, resonant.api.explosion.IEMPBlock {
	
	protected boolean isRotating = false;
	protected boolean hasSubBlocks = false;
	private static boolean isInvalidEMPreported = false;
	
	protected BlockAbstractContainer(Material material) {
		super(material);
		setHardness(5.0F);
		setResistance(6.0F * 5 / 3);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof IBlockUpdateDetector) {
			((IBlockUpdateDetector) tileEntity).onBlockUpdateDetected();
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
		if (isRotating) {
			final int metadata = Commons.getFacingFromEntity(entityLiving);
			if (metadata >= 0 && metadata <= 15) {
				world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
			}
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (itemStack.hasTagCompound()) {
			final NBTTagCompound tagCompound = (NBTTagCompound) itemStack.getTagCompound().copy();
			tagCompound.setInteger("x", x);
			tagCompound.setInteger("y", y);
			tagCompound.setInteger("z", z);
			tileEntity.readFromNBT(tagCompound);
			world.markBlockForUpdate(x, y, z);
		}
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
	}
	
	@Override
	protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack itemStack) {
		if (itemStack.getItem() == Item.getItemFromBlock(this)) {
			itemStack.setItemDamage(getDamageValue(world, x, y, z));
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity == null) {
				WarpDrive.logger.error("Missing tile entity for " + this + " at " + world + " " + x + " " + y + " " + z);
			} else if (tileEntity instanceof TileEntityAbstractBase) {
				final NBTTagCompound tagCompound = new NBTTagCompound();
				((TileEntityAbstractBase) tileEntity).writeItemDropNBT(tagCompound);
				itemStack.setTagCompound(tagCompound);
			}
		}
		world.setBlockToAir(x, y, z);
		super.dropBlockAsItem(world, x, y, z, itemStack);
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer entityPlayer) {
		ItemStack itemStack = super.getPickBlock(target, world, x, y, z, entityPlayer);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		final NBTTagCompound tagCompound = new NBTTagCompound();
		if (tileEntity instanceof TileEntityAbstractBase) {
			((TileEntityAbstractBase) tileEntity).writeItemDropNBT(tagCompound);
			itemStack.setTagCompound(tagCompound);
		}
		return itemStack;
	}
	
	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		if (isRotating) {
			world.setBlockMetadataWithNotify(x, y, z, axis.ordinal(), 3);
			return true;
		}
		return false;
	}
	
	@Override
	public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof IBlockUpdateDetector) {
			((IBlockUpdateDetector) tileEntity).onBlockUpdateDetected();
		}
	}
	
	@Override
	@Optional.Method(modid = "DefenseTech")
	public void onEMP(World world, int x, int y, int z, defense.api.IExplosion explosiveEMP) {
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info(String.format("EMP received @ %s (%d %d %d) from %s with energy %d and radius %.1f",
			                                    world.provider.getDimensionName(), x, y, z,
			                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
		}
		// EMP tower = 3k Energy, 60 radius
		// EMP explosive = 3k Energy, 50 radius
		if (explosiveEMP.getRadius() == 60.0F) {// compensate tower stacking effect
			onEMP(world, x, y, z, 0.02F);
		} else if (explosiveEMP.getRadius() == 50.0F) {
			onEMP(world, x, y, z, 0.70F);
		} else {
			if (!isInvalidEMPreported) {
				isInvalidEMPreported = true;
				WarpDrive.logger.warn(String.format("EMP received @ %s (%d %d %d) from %s with energy %d and unsupported radius %.1f",
				                                    world.provider.getDimensionName(), x, y, z,
				                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
				Commons.dumpAllThreads();
			}
			onEMP(world, x, y, z, 0.02F);
		}
	}
	
	@Override
	@Optional.Method(modid = "icbmclassic")
	public void onEMP(World world, int x, int y, int z, resonant.api.explosion.IExplosion explosiveEMP) {
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info(String.format("EMP received @ %s (%d %d %d) from %s with energy %d and radius %.1f",
			                                    world.provider.getDimensionName(), x, y, z,
			                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
		}
		// EMP tower = 3k Energy, 60 radius
		// EMP explosive = 3k Energy, 50 radius
		if (explosiveEMP.getRadius() == 60.0F) {// compensate tower stacking effect
			onEMP(world, x, y, z, 0.02F);
		} else if (explosiveEMP.getRadius() == 50.0F) {
			onEMP(world, x, y, z, 0.70F);
		} else {
			if (!isInvalidEMPreported) {
				isInvalidEMPreported = true;
				WarpDrive.logger.warn(String.format("EMP received @ %s (%d %d %d) from %s with energy %d and unsupported radius %.1f",
				                                    world.provider.getDimensionName(), x, y, z,
				                                    explosiveEMP, explosiveEMP.getEnergy(), explosiveEMP.getRadius()));
				Commons.dumpAllThreads();
			}
			onEMP(world, x, y, z, 0.02F);
		}
	}
	
	public void onEMP(World world, final int x, final int y, final int z, final float efficiency) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityAbstractEnergy) {
			TileEntityAbstractEnergy tileEntityAbstractEnergy = (TileEntityAbstractEnergy) tileEntity;
			if (tileEntityAbstractEnergy.energy_getMaxStorage() > 0) {
				tileEntityAbstractEnergy.energy_consume(Math.round(tileEntityAbstractEnergy.energy_getEnergyStored() * efficiency), false);
			}
		}
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 1;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity) {
		switch (getTier(itemStack)) {
			case 0:	return EnumRarity.epic;
			case 1:	return EnumRarity.common;
			case 2:	return EnumRarity.uncommon;
			case 3:	return EnumRarity.rare;
			default: return rarity;
		}
	}
	
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		// get context
		final TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityAbstractBase)) {
			return false;
		}
		final TileEntityAbstractBase tileEntityAbstractBase = (TileEntityAbstractBase) tileEntity;
		final ItemStack itemStackHeld = entityPlayer.getHeldItem();
		
		EnumComponentType enumComponentType = null;
		if ( itemStackHeld != null
		  && itemStackHeld.getItem() instanceof ItemComponent ) {
			enumComponentType = EnumComponentType.get(itemStackHeld.getItemDamage());
		}
		
		// sneaking with an empty hand or an upgrade item in hand to dismount current upgrade
		if (entityPlayer.isSneaking()) {
			// using an upgrade item or an empty hand means dismount upgrade
			if ( tileEntityAbstractBase.isUpgradeable()
			  && ( itemStackHeld == null
			    || enumComponentType != null ) ) {
				// find a valid upgrade to dismount
				if ( itemStackHeld == null
				  || !tileEntityAbstractBase.hasUpgrade(enumComponentType) ) {
					enumComponentType = (EnumComponentType) tileEntityAbstractBase.getFirstUpgradeOfType(EnumComponentType.class, null);
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
				
				tileEntityAbstractBase.dismountUpgrade(enumComponentType);
				// upgrade dismounted
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.dismounted", enumComponentType.name()));
				return false;
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			Commons.addChatMessage(entityPlayer, tileEntityAbstractBase.getStatus());
			return true;
			
		} else if ( tileEntityAbstractBase.isUpgradeable()
		         && enumComponentType != null ) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityAbstractBase.getUpgradeMaxCount(enumComponentType) <= 0) {
				// invalid upgrade type
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.invalidUpgrade"));
				return true;
			}
			if (!tileEntityAbstractBase.canUpgrade(enumComponentType)) {
				// too many upgrades
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.tooManyUpgrades",
				                                                                             tileEntityAbstractBase.getUpgradeMaxCount(enumComponentType)));
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
			tileEntityAbstractBase.mountUpgrade(enumComponentType);
			// upgrade mounted
			Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.upgrade.result.mounted", enumComponentType.name()));
		}
		
		return false;
	}
}
