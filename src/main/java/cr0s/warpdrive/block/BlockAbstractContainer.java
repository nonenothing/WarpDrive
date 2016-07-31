package cr0s.warpdrive.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.Optional;
import cr0s.warpdrive.config.WarpDriveConfig;
import defense.api.IEMPBlock;
import defense.api.IExplosion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockUpdateDetector;

@Optional.InterfaceList({
    @Optional.Interface(iface = "defense.api.IEMPBlock", modid = "DefenseTech")
})
public abstract class BlockAbstractContainer extends BlockContainer implements IEMPBlock {
	protected boolean isRotating = false;
	
	protected BlockAbstractContainer(Material material) {
		super(material);
		setHardness(5.0F);
		setResistance(6.0F * 5 / 3);
		setSoundType(SoundType.METAL);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof IBlockUpdateDetector) {
			((IBlockUpdateDetector) tileEntity).updatedNeighbours();
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, pos, state, entityLiving, itemStack);
		if (isRotating) {
			if (entityLiving != null) {
				int metadata;
				if (entityLiving.rotationPitch > 65) {
					metadata = 1;
				} else if (entityLiving.rotationPitch < -65) {
					metadata = 0;
				} else {
					int direction = Math.round(entityLiving.rotationYaw / 90.0F) & 3;
					switch (direction) {
						case 0:
							metadata = 2;
							break;
						case 1:
							metadata = 5;
							break;
						case 2:
							metadata = 3;
							break;
						case 3:
							metadata = 4;
							break;
						default:
							metadata = 2;
							break;
					}
				}
				world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
			}
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (itemStack.hasTagCompound()) {
			NBTTagCompound nbtTagCompound = (NBTTagCompound)itemStack.getTagCompound().copy();
			nbtTagCompound.setInteger("x", x);
			nbtTagCompound.setInteger("y", y);
			nbtTagCompound.setInteger("z", z);
			tileEntity.readFromNBT(nbtTagCompound);
			world.markBlockForUpdate(x, y, z);
		}
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
	}
	
	@Override
	protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack itemStack) {
		itemStack.setItemDamage(getDamageValue(world, x, y, z));
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null) {
			WarpDrive.logger.error("Missing tile entity for " + this + " at " + world + " " + x + " " + y + " " + z);
		} else {
			NBTTagCompound nbtTagCompound = new NBTTagCompound();
			tileEntity.writeToNBT(nbtTagCompound);
			nbtTagCompound.removeTag("x");
			nbtTagCompound.removeTag("y");
			nbtTagCompound.removeTag("z");
			itemStack.setTagCompound(nbtTagCompound);
		}
		world.setBlockToAir(x, y, z);
		super.dropBlockAsItem(world, x, y, z, itemStack);
	}
	
	public ItemStack getPickBlock(RayTraceResult target, World world, int x, int y, int z, EntityPlayer entityPlayer) {
		ItemStack itemStack = super.getPickBlock(target, world, x, y, z, entityPlayer);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		tileEntity.writeToNBT(nbtTagCompound);
		nbtTagCompound.removeTag("x");
		nbtTagCompound.removeTag("y");
		nbtTagCompound.removeTag("z");
		itemStack.setTagCompound(nbtTagCompound);
		return itemStack;
	}
	
	@Override
	public boolean rotateBlock(World world, int x, int y, int z, EnumFacing axis) {
		world.setBlockMetadataWithNotify(x, y, z, axis.ordinal(), 3);
		return true;
	}
	
	// FIXME untested
	/*
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		boolean hasResponse = false;
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof IUpgradable) {
			IUpgradable upgradable = (IUpgradable) tileEntity;
			ItemStack itemStack = entityPlayer.inventory.getCurrentItem();
			if (itemStack != null) {
				Item i = itemStack.getItem();
				if (i instanceof ItemWarpUpgrade) {
					if (upgradable.takeUpgrade(EnumUpgradeTypes.values()[itemStack.getItemDamage()], false)) {
						if (!entityPlayer.capabilities.isCreativeMode)
							entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
						entityPlayer.addChatMessage("Upgrade accepted");
					} else {
						entityPlayer.addChatMessage("Upgrade declined");
					}
					hasResponse = true;
				}
			}
		}
		
		return hasResponse;
	}
	/**/
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof IBlockUpdateDetector) {
			((IBlockUpdateDetector) tileEntity).updatedNeighbours();
		}
	}
	
	@Optional.Method(modid = "DefenseTech")
	public void onEMP(World world, int x, int y, int z, IExplosion explosiveEMP) {
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info("EMP received at " + x + " " + y + " " + z + " from " + explosiveEMP + " with energy " + explosiveEMP.getEnergy() + " and radius " + explosiveEMP.getRadius());
		}
		// EMP tower = 3k Energy, 60 radius
		// EMP explosive = 3k Energy, 50 radius
		onEMP(world, x, y, z, explosiveEMP.getRadius() / 100.0F);
	}
	
	public void onEMP(World world, final int x, final int y, final int z, final float efficiency) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityAbstractEnergy) {
			TileEntityAbstractEnergy tileEntityAbstractEnergy = (TileEntityAbstractEnergy) tileEntity;
			if (tileEntityAbstractEnergy.getMaxEnergyStored() > 0) {
				tileEntityAbstractEnergy.consumeEnergy(Math.round(tileEntityAbstractEnergy.getEnergyStored() * efficiency), false);
			}
		}
	}
}
