package cr0s.warpdrive.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cr0s.warpdrive.EntityJump;
import cr0s.warpdrive.LocalProfiler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;

public class JumpShip {
	public World worldObj;
	public int coreX;
	public int coreY;
	public int coreZ;
	public int dx;
	public int dz;
	public int maxX;
	public int maxZ;
	public int maxY;
	public int minX;
	public int minZ;
	public int minY;
	public JumpBlock[] jumpBlocks;
	public TileEntityShipCore shipCore;
	public List<MovingEntity> entitiesOnShip;
	
	public JumpShip() {
	}

	public void messageToAllPlayersOnShip(EntityJump entityJump, String msg) {
		if (entitiesOnShip == null) {
			shipCore.messageToAllPlayersOnShip(msg);
		} else {
			WarpDrive.logger.info(entityJump + " messageToAllPlayersOnShip: " + msg);
			for (MovingEntity me : entitiesOnShip) {
				if (me.entity instanceof EntityPlayer) {
					WarpDrive.addChatMessage((EntityPlayer) me.entity, "["
							+ ((shipCore != null && shipCore.shipName.length() > 0) ? shipCore.shipName : "WarpCore") + "] " + msg);
				}
			}
		}
	}

	public String saveEntities(EntityJump entityJump) {
		String result = null;
		entitiesOnShip = new ArrayList<MovingEntity>();
		
		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		
		List<Entity> list = entityJump.worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		
		for (Entity entity : list) {
			if (entity == null || (entity instanceof EntityJump)) {
				continue;
			}
			
			String id = EntityList.getEntityString(entity);
			if (Dictionary.ENTITIES_ANCHOR.contains(id)) {
				result = "Anchor entity " + id + " detected at " + Math.floor(entity.posX) + ", " + Math.floor(entity.posY) + ", " + Math.floor(entity.posZ) + ", aborting jump...";
				// we need to continue so players are added so they can see the message...
				continue;
			}
			if (Dictionary.ENTITIES_LEFTBEHIND.contains(id)) {
				if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
					WarpDrive.logger.info("Leaving entity " + id + " behind: " + entity);
				}
				continue;
			}
			if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
				if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
					WarpDrive.logger.info("Adding entity " + id + ": " + entity);
				}
			} 
			MovingEntity movingEntity = new MovingEntity(entity);
			entitiesOnShip.add(movingEntity);
		}
		return result;
	}

	public void setMinMaxes(EntityJump entityJump, int minXV, int maxXV, int minYV, int maxYV, int minZV, int maxZV) {
		minX = minXV;
		maxX = maxXV;
		minY = minYV;
		maxY = maxYV;
		minZ = minZV;
		maxZ = maxZV;
	}
	
	@Override
	public String toString() {
		return String.format("%s/%d \'%s\' @ \'%s\' (%d %d %d)",
			getClass().getSimpleName(), hashCode(),
			shipCore == null ? "~NULL~" : (shipCore.uuid + ":" + shipCore.shipName),
			"-", // worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			Double.valueOf(coreX), Double.valueOf(coreY), Double.valueOf(coreZ));
	}
	
	public int getRealShipVolume_checkBedrock(EntityJump entityJump, StringBuilder reason) {
		LocalProfiler.start("EntityJump.getRealShipVolume_checkBedrock");
		int shipVolume = 0;
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					Block block = entityJump.worldObj.getBlock(x, y, z);
					
					// Skipping vanilla air & ignored blocks
					if (block == Blocks.air || Dictionary.BLOCKS_LEFTBEHIND.contains(block)) {
						continue;
					}
					
					shipVolume++;
					
					if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
						WarpDrive.logger.info("Block(" + x + ", " + y + ", " + z + ") is " + block.getUnlocalizedName() + "@" + entityJump.worldObj.getBlockMetadata(x, y, z));
					}
					
					// Stop on non-movable blocks
					if (Dictionary.BLOCKS_ANCHOR.contains(block)) {
						reason.append(block.getUnlocalizedName() + " detected onboard at " + x + ", " + y + ", " + z + ". Aborting.");
						LocalProfiler.stop();
						return -1;
					}
				}
			}
		}
		
		// Abort jump if blocks with TE are connecting to the ship (avoid crash when splitting multi-blocks)
		for (int x = minX - 1; x <= maxX + 1; x++) {
			boolean xBorder = (x == minX - 1) || (x == maxX + 1);
			for (int z = minZ - 1; z <= maxZ + 1; z++) {
				boolean zBorder = (z == minZ - 1) || (z == maxZ + 1);
				for (int y = minY - 1; y <= maxY + 1; y++) {
					boolean yBorder = (y == minY - 1) || (y == maxY + 1);
					if ((y < 0) || (y > 255)) {
						continue;
					}
					if (!(xBorder || yBorder || zBorder)) {
						continue;
					}
					
					Block block = worldObj.getBlock(x, y, z);
					
					// Skipping any air block & ignored blocks
					if (worldObj.isAirBlock(x, y, z) || Dictionary.BLOCKS_LEFTBEHIND.contains(block)) {
						continue;
					}
					
					// Skipping non-movable blocks
					if (Dictionary.BLOCKS_ANCHOR.contains(block)) {
						continue;
					}
					
					// Skipping blocks without tile entities
					TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
					if (tileEntity == null) {
						continue;
					}
					
					reason.append("Ship snagged by " + block.getLocalizedName() + " at " + x + ", " + y + ", " + z + ". Damage report pending...");
					worldObj.createExplosion((Entity) null, x, y, z, Math.min(4F * 30, 4F * (shipVolume / 50)), false);
					LocalProfiler.stop();
					return -1;
				}
			}
		}
		
		LocalProfiler.stop();
		return shipVolume;
	}
}