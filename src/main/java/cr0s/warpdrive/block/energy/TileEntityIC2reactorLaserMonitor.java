package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.block.TileEntityAbstractLaser;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.item.ItemIC2reactorLaserFocus;
import cr0s.warpdrive.network.PacketHandler;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.fluids.BlockFluidBase;

public class TileEntityIC2reactorLaserMonitor extends TileEntityAbstractLaser {
	
	// persistent properties
	private int ticks = WarpDriveConfig.IC2_REACTOR_COOLING_INTERVAL_TICKS;
	
	// computed properties
	public EnumFacing facing = null;
	private boolean isValid = false;
	
	public TileEntityIC2reactorLaserMonitor() {
		super();
		laserMedium_maxCount = 1;
		peripheralName = "warpdriveIC2reactorLaserCooler";
	}
	
	// returns IReactor tile entities
	@Optional.Method(modid = "IC2")
	private IReactor findReactor() {
		for(final EnumFacing facing : EnumFacing.values()) {
			final TileEntity tileEntity = worldObj.getTileEntity(
				xCoord + 2 * facing.getFrontOffsetX(),
				yCoord + 2 * facing.getFrontOffsetY(),
				zCoord + 2 * facing.getFrontOffsetZ());
			if (tileEntity == null) {
				continue;
			}
			
			IReactor output = null;
			if (tileEntity instanceof IReactor) {
				output = (IReactor) tileEntity;
				
			} else if (tileEntity instanceof IReactorChamber) {
				final IReactor reactor = ((IReactorChamber) tileEntity).getReactor();
				if (reactor == null) {
					continue;
				}
				
				// ignore if we're right next to the reactor
				// ignore if we're not aligned with the reactor
				final ChunkCoordinates coords = reactor.getPosition();
				if ( coords.posX != xCoord + 3 * facing.getFrontOffsetX()
				  || coords.posY != yCoord + 3 * facing.getFrontOffsetY()
				  || coords.posZ != zCoord + 3 * facing.getFrontOffsetZ() ) {
					continue;
				}
				
				output = reactor;
			}
			
			// if reactor or chamber was found, check the space in between
			if (output != null) {
				final Block block = worldObj.getBlock(
					xCoord + facing.getFrontOffsetX(),
					yCoord + facing.getFrontOffsetY(),
					zCoord + facing.getFrontOffsetZ());
				final boolean isAir = block.isAir(worldObj,
				    xCoord + facing.getFrontOffsetX(),
				    yCoord + facing.getFrontOffsetY(),
				    zCoord + facing.getFrontOffsetZ()); 
				isValid = ( isAir
				         || block instanceof BlockFluidBase
				         || block instanceof IReactorChamber
				         || !block.getMaterial().isOpaque());
				this.facing = facing; 
				return output;
			}
		}
		isValid = false;
		this.facing = null;
		return null;
	}
	
	@Optional.Method(modid = "IC2")
	private boolean coolReactor(final IReactor reactor) {
		for(int x = 0; x < 9; x++) {
			for(int y = 0; y < 6; y++) {
				final ItemStack itemStack = reactor.getItemAt(x, y);
				if ( itemStack != null
				  && itemStack.getItem() instanceof ItemIC2reactorLaserFocus ) {
					final int heatInLaserFocus = itemStack.getItemDamage();
					final int heatEnergyCap = (int) Math.floor(Math.min(laserMedium_getEnergyStored() / WarpDriveConfig.IC2_REACTOR_ENERGY_PER_HEAT, heatInLaserFocus));
					final int heatToTransfer = Math.min(heatEnergyCap, WarpDriveConfig.IC2_REACTOR_COOLING_PER_INTERVAL); 
					if (heatToTransfer > 0) {
						if (laserMedium_consumeExactly((int) Math.ceil(heatToTransfer * WarpDriveConfig.IC2_REACTOR_ENERGY_PER_HEAT), false)) {
							ItemIC2reactorLaserFocus.addHeat(itemStack, -heatToTransfer);
							return true;
						}
					}
					return false;
				}
			}
		}
		return false;
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		ticks--;
		if (ticks <= 0)  {
			ticks = WarpDriveConfig.IC2_REACTOR_COOLING_INTERVAL_TICKS;
			final IReactor reactor = findReactor();
			setMetadata();
			if (reactor == null) {
				return;
			}
			
			if (coolReactor(reactor)) {
				final Vector3 vMonitor = new Vector3(this).translate(0.5);
				PacketHandler.sendBeamPacket(worldObj,
				                             vMonitor,
				                             new Vector3(reactor.getPosition()).translate(0.5D),
				                             0.0f, 0.8f, 1.0f, 20, 0, 20);
			}
		}
	}
	
	private void setMetadata() {
		int metadata = (facing != null ? facing.ordinal() : 6);
		if ( isValid
		  && cache_laserMedium_energyStored >= WarpDriveConfig.IC2_REACTOR_ENERGY_PER_HEAT) {
			metadata |= 8;
		}
		if (getBlockMetadata() != metadata) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 3);
		}
	}
	
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("ticks", ticks);
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		ticks = tagCompound.getInteger("ticks");
	}
	
	@Override
	public Packet getDescriptionPacket() {
		final NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(final NetworkManager networkManager, final S35PacketUpdateTileEntity packet) {
		final NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public String getStatus() {
		if (worldObj == null) {
			return super.getStatus();
		}
		
		final IReactor reactor = findReactor();
		if (reactor != null) {
			return super.getStatus() 
			       + StatCollector.translateToLocalFormatted("warpdrive.IC2reactorLaserMonitor.multipleReactors",
						1);
		} else {
			return super.getStatus() 
			       + StatCollector.translateToLocalFormatted("warpdrive.IC2reactorLaserMonitor.noReactor");
		}
	}
}
