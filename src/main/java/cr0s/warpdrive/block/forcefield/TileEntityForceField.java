package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.TileEntityAbstractBase;
import cr0s.warpdrive.data.ForceFieldSetup;
import cr0s.warpdrive.data.VectorI;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityForceField extends TileEntityAbstractBase {
	private VectorI vProjector;

	// cache parameters used for rendering, force projector check for others
	private int cache_beamFrequency;
	private Block cache_blockCamouflage;
	private int cache_metadataCamouflage;
	private int gracePeriod_calls = 1;
	
	@Override
	public boolean canUpdate() {
		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (tag.hasKey("projector")) {
			vProjector = VectorI.readFromNBT(tag.getCompoundTag("projector"));
			cache_beamFrequency = tag.getInteger("beamFrequency");
			if (tag.hasKey("projector")) {
				try {
					cache_blockCamouflage = Block.getBlockFromName(tag.getString("camouflageBlock"));
					cache_metadataCamouflage = tag.getByte("camouflageMeta");
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			} else {
				cache_blockCamouflage = null;
				cache_metadataCamouflage = 0;
			}
		} else {
			vProjector = null;
			cache_beamFrequency = -1;
			cache_blockCamouflage = null;
			cache_metadataCamouflage = 0;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		if (vProjector != null) {
			tagCompound.setTag("projector", vProjector.writeToNBT(new NBTTagCompound()));
			tagCompound.setInteger("beamFrequency", cache_beamFrequency);
			if (cache_blockCamouflage != null) {
				tagCompound.setString("camouflageBlock", Block.blockRegistry.getNameForObject(cache_blockCamouflage));
				tagCompound.setByte("camouflageMeta", (byte)cache_metadataCamouflage);
			}
		}
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
		// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); // TODO is it needed?
	}
	
	public void setProjector(final VectorI vectorI) {
		vProjector = vectorI;
		ForceFieldSetup forceFieldSetup = getForceFieldSetup();
		if (forceFieldSetup != null) {
			cache_beamFrequency = forceFieldSetup.beamFrequency;
			cache_blockCamouflage = forceFieldSetup.getCamouflageBlock();
			cache_metadataCamouflage = forceFieldSetup.getCamouflageMetadata();
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public TileEntityForceFieldProjector getProjector() {
		if (vProjector != null) {
			TileEntity tileEntity = vProjector.getTileEntity(worldObj);
			if (tileEntity instanceof TileEntityForceFieldProjector) {
				TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector) tileEntity;
				if (worldObj.isRemote || tileEntityForceFieldProjector.isPartOfForceField(new VectorI(this))) {
					return tileEntityForceFieldProjector;
				}
			}
		}
		
		if (!worldObj.isRemote) {
			gracePeriod_calls--;
			if (gracePeriod_calls < 0) {
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		return null;
	}
	
	public ForceFieldSetup getForceFieldSetup() {
		TileEntityForceFieldProjector tileEntityForceFieldProjector = getProjector();
		if (tileEntityForceFieldProjector == null) {
			return null;
		}
		return tileEntityForceFieldProjector.getForceFieldSetup();
	}
}
