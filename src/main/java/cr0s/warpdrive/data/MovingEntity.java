package cr0s.warpdrive.data;

import cr0s.warpdrive.config.WarpDriveConfig;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class MovingEntity {
	
	public static final MovingEntity INVALID = new MovingEntity(null);
	
	private final WeakReference<Entity> weakEntity;
	private final WeakReference<World> weakWorld;
	private final int entityId;
	public final Vector3 v3OriginalPosition;
	
	public MovingEntity(final Entity entity) {
		if (entity == null) {
			weakEntity = new WeakReference<>(null);
			weakWorld = new WeakReference<>(null);
			entityId = -1;
			v3OriginalPosition = new Vector3(0, -100, 0);
		} else {
			weakEntity = new WeakReference<>(entity);
			weakWorld = new WeakReference<>(entity.worldObj);
			entityId = entity.getEntityId();
			v3OriginalPosition = new Vector3(entity);
		}
	}
	
	public Entity getEntity() {
		if (entityId < 0) {
			return null;
		}
		
		final Entity entity = weakEntity.get();
		if (entity != null) {
			return entity;
		}
		
		// try to recover
		final World world = weakWorld.get();
		if (world == null) {
			return null;
		}
		return world.getEntityByID(entityId);
	}
	
	public boolean isUnlimited() {
		final Entity entity = getEntity();
		if (!(entity instanceof EntityPlayer)) {
			return false;
		}
		
		final String playerName = ((EntityPlayer) entity).getDisplayName();
		for (final String unlimitedName : WarpDriveConfig.SHIP_VOLUME_UNLIMITED_PLAYERNAMES) {
			if (unlimitedName.equals(playerName)) {
				return true;
			}
		}
		
		return false;
	}
	
	public double getDistanceMoved_square() {
		final Entity entity = getEntity();
		if (entity == null) {// dead or disconnected
			return Double.MAX_VALUE;
		}
		if (entity.worldObj != weakWorld.get()) {// moved to another dimension
			return Double.MAX_VALUE;
		}
		return v3OriginalPosition.distanceTo_square(entity);
	}
	
	public int getMass() {
		final Entity entity = getEntity();
		if (entity == null) {
			return 0;
		}
		
		final NBTTagCompound tagCompound = new NBTTagCompound();
		entity.writeToNBT(tagCompound);
		return tagCompound.toString().length();
	}
}
