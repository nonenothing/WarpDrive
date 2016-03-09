package cr0s.warpdrive.compat;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import am2.api.power.IPowerNode;
import am2.power.PowerNodeRegistry;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.config.WarpDriveConfig;

public class CompatArsMagica2 implements IBlockTransformer {
	
	public static void register() {
		WarpDriveConfig.registerBlockTransformer("arsmagica2", new CompatArsMagica2());
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		return tileEntity instanceof IPowerNode;
	}
	
	@Override
	public boolean isJumpReady(final TileEntity tileEntity) {
		return true;
	}
	
	@Override
	@Optional.Method(modid = "arsmagica2")
	public NBTBase saveExternals(final TileEntity tileEntity) {
		if (tileEntity instanceof IPowerNode) {
			NBTBase nbtArsMagica2 = PowerNodeRegistry.For(tileEntity.getWorldObj()).getDataCompoundForNode((IPowerNode) tileEntity);
			return nbtArsMagica2;
		}
		return null;
	}
	
	@Override
	@Optional.Method(modid = "arsmagica2")
	public void remove(TileEntity tileEntity) {
		PowerNodeRegistry.For(tileEntity.getWorldObj()).removePowerNode((IPowerNode) tileEntity);
	}
	
	@Override
	public int rotate(final Block block, final int metadata, NBTTagCompound nbtTileEntity, final byte rotationSteps, final float rotationYaw) {
		return metadata;
	}
	
	@Override
	@Optional.Method(modid = "arsmagica2")
	public void restoreExternals(TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		NBTTagCompound nbtTagCompound = (NBTTagCompound) nbtBase;
		
		// powerAmounts
		// (no changes)
		
		// powerPathList
		NBTTagList powerPathList = nbtTagCompound.getTagList("powerPathList", Constants.NBT.TAG_COMPOUND);
		if (powerPathList != null) {
			for (int powerPathIndex = 0; powerPathIndex < powerPathList.tagCount(); powerPathIndex++) {
				NBTTagCompound powerPathEntry = (NBTTagCompound) powerPathList.removeTag(0);
				
				// powerPathList[powerPathIndex].powerType
				// (no change)
				
				// powerPathList[powerPathIndex].nodePaths
				NBTTagList nodePaths = powerPathEntry.getTagList("nodePaths", Constants.NBT.TAG_LIST);
				if (nodePaths != null) {
					for (int nodePathIndex = 0; nodePathIndex < nodePaths.tagCount(); nodePathIndex++) {
						// we can't directly access it, hence removing then adding back later on
						NBTTagList nodeList = (NBTTagList) nodePaths.removeTag(0);
						if (nodeList != null) {
							for (int nodeIndex = 0; nodeIndex < nodeList.tagCount(); nodeIndex++) {
								NBTTagCompound node = (NBTTagCompound) nodeList.removeTag(0);
								// read coordinates
								Vec3 target = transformation.apply(node.getFloat("Vec3_x"), node.getFloat("Vec3_y"), node.getFloat("Vec3_z"));
								node.setFloat("Vec3_x", (float)target.xCoord);
								node.setFloat("Vec3_y", (float)target.yCoord);
								node.setFloat("Vec3_z", (float)target.zCoord);
								//tack the node on to the power path
								nodeList.appendTag(node);
							}
							nodePaths.appendTag(nodeList);
						}
					}
					powerPathEntry.setTag("nodePaths", nodePaths);
				}
				powerPathList.appendTag(powerPathEntry);
			}
			nbtTagCompound.setTag("powerPathList", powerPathList);
		}
		
		World targetWorld = transformation.getTargetWorld();
		ChunkCoordinates target = transformation.apply(tileEntity);
		PowerNodeRegistry.For(targetWorld).setDataCompoundForNode((IPowerNode) targetWorld.getTileEntity(target.posX, target.posY, target.posZ), nbtTagCompound);
	}
}
