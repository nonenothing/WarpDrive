package cr0s.warpdrive.data;

import cr0s.warpdrive.config.WarpDriveConfig;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class MovingEntity {
	
	public static final MovingEntity INVALID = new MovingEntity(null);
	
	private final WeakReference<Entity> weakEntity;
	public final Vector3 v3OriginalPosition;
	
	public MovingEntity(final Entity entity) {
		if (entity == null) {
			weakEntity = new WeakReference<>(null);
			v3OriginalPosition = new Vector3(0, -100, 0);
		} else {
			weakEntity = new WeakReference<>(entity);
			v3OriginalPosition = new Vector3(entity);
		}
	}
	
	public Entity getEntity() {
		return weakEntity.get();
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
		if (entity == null) {
			return Double.MAX_VALUE;
		}
		return v3OriginalPosition.distanceTo_square(entity);
	}
	
	
}
