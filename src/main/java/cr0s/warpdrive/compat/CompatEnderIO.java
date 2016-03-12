package cr0s.warpdrive.compat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.config.WarpDriveConfig;

public class CompatEnderIO implements IBlockTransformer {
	
	private static Class<?> classTileEntityEIO;
	
	public static void register() {
		try {
			classTileEntityEIO = Class.forName("crazypants.enderio.TileEntityEio");
			WarpDriveConfig.registerBlockTransformer("EnderIO", new CompatEnderIO());
		} catch(ClassNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		return classTileEntityEIO.isInstance(tileEntity);
	}
	
	@Override
	public boolean isJumpReady(TileEntity tileEntity) {
		return true;
	}
	
	@Override
	public NBTBase saveExternals(final TileEntity tileEntity) {
		// nothing to do
		return null;
	}
	
	@Override
	public void remove(TileEntity tileEntity) {
		// nothing to do
	}
	
	private static final short[] mrot = {  0,  1,  5,  4,  2,  3,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	private static final byte NBTTagByteArrayId = 7; // new NBTTagByteArray(null).getId();
	private static final byte NBTTagCompoundId = 10; // new NBTTagCompound().getId();
	private static final byte NBTTagIntArrayId = 11; // new NBTTagIntArray(null).getId();
    private static final Map<String, String> rotSideNames;
    static {
        Map<String, String> map = new HashMap();
        map.put("EAST", "SOUTH");
        map.put("SOUTH", "WEST");
        map.put("WEST", "NORTH");
        map.put("NORTH", "EAST");
        rotSideNames = Collections.unmodifiableMap(map);
    }
	private byte[] rotate_byteArray(final byte rotationSteps, final byte[] data) {
		byte[] newData = data.clone();
		for (int index = 0; index < data.length; index++) {
			switch (rotationSteps) {
			case 1:
				newData[mrot[index]] = data[index];
				break;
			case 2:
				newData[mrot[mrot[index]]] = data[index];
				break;
			case 3:
				newData[mrot[mrot[mrot[index]]]] = data[index];
				break;
			default:
				break;
			}
		}
		return newData;
	}
	private NBTTagCompound rotate_conduit(final byte rotationSteps, NBTTagCompound conduit) {
		NBTTagCompound newConduit = new NBTTagCompound();
		Set<String> keys = conduit.func_150296_c();
		for (String key : keys) {
			NBTBase base = conduit.getTag(key);
			switch(base.getId()) {
			case NBTTagIntArrayId:	// "connections", "externalConnections"
				int[] data = conduit.getIntArray(key);
				int[] newData = data.clone();
				for (int index = 0; index < data.length; index++) {
					switch (rotationSteps) {
					case 1:
						newData[index] = mrot[data[index]];
						break;
					case 2:
						newData[index] = mrot[mrot[data[index]]];
						break;
					case 3:
						newData[index] = mrot[mrot[mrot[data[index]]]];
						break;
					default:
						break;
					}
				}
				newConduit.setIntArray(key, newData);
				break;
				
			case NBTTagByteArrayId:	// "conModes", "signalColors", "forcedConnections", "signalStrengths"
				newConduit.setByteArray(key, rotate_byteArray(rotationSteps, conduit.getByteArray(key)));
				break;
				
			default:
				String[] parts = key.split("\\.");
				if (parts.length != 2 || !rotSideNames.containsKey(parts[1])) {
					newConduit.setTag(key, base);
				} else {
					switch (rotationSteps) {
					case 1:
						newConduit.setTag(parts[0] + "." + rotSideNames.get(parts[1]), base);
						break;
					case 2:
						newConduit.setTag(parts[0] + "." + rotSideNames.get(rotSideNames.get(parts[1])), base);
						break;
					case 3:
						newConduit.setTag(parts[0] + "." + rotSideNames.get(rotSideNames.get(rotSideNames.get(parts[1]))), base);
						break;
					default:
						newConduit.setTag(key, base);
						break;
					}
				}
				break;
			}
		}
		return newConduit;
	}
	
	@Override
	public int rotate(final Block block, final int metadata, NBTTagCompound nbtTileEntity, final byte rotationSteps, final float rotationYaw) {
		if (rotationSteps == 0) {
			return metadata;
		}
		
		if (nbtTileEntity.hasKey("facing")) {
			short facing = nbtTileEntity.getShort("facing");
			switch (rotationSteps) {
			case 1:
				nbtTileEntity.setShort("facing", mrot[facing]);
				return metadata;
			case 2:
				nbtTileEntity.setShort("facing", mrot[mrot[facing]]);
				return metadata;
			case 3:
				nbtTileEntity.setShort("facing", mrot[mrot[mrot[facing]]]);
				return metadata;
			default:
				return metadata;
			}
		}
		if (nbtTileEntity.hasKey("conduits")) {
			NBTTagList conduits = nbtTileEntity.getTagList("conduits", NBTTagCompoundId);
			NBTTagList newConduits = new NBTTagList(); 
			for (int index = 0; index < conduits.tagCount(); index++) {
				NBTTagCompound conduitTypeAndContent = conduits.getCompoundTagAt(index);
				NBTTagCompound newConduitTypeAndContent = new NBTTagCompound();
				newConduitTypeAndContent.setString("conduitType", conduitTypeAndContent.getString("conduitType"));
				newConduitTypeAndContent.setTag("conduit", rotate_conduit(rotationSteps, conduitTypeAndContent.getCompoundTag("conduit")));
				newConduits.appendTag(newConduitTypeAndContent);
			}
			nbtTileEntity.setTag("conduits", newConduits);
		}
		return metadata;
	}
	
	@Override
	public void restoreExternals(TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		// nothing to do
	}
}
