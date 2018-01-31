package cr0s.warpdrive.data;

import cr0s.warpdrive.config.WarpDriveConfig;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class MovingEntity {
    
    private final WeakReference<Entity> weakEntity;
    public final double originalX;
    public final double originalY;
    public final double originalZ;
    
    public MovingEntity(final Entity entity) {
        weakEntity = new WeakReference<>(entity);
        originalX = entity.posX;
        originalY = entity.posY;
        originalZ = entity.posZ;
    }
    
    public Entity getEntity() {
        return weakEntity.get();
    }
    
    public boolean isUnlimited() {
        final Entity entity = getEntity();
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        
        final String playerName = entity.getName();
        for (final String unlimitedName : WarpDriveConfig.SHIP_VOLUME_UNLIMITED_PLAYERNAMES) {
            if (unlimitedName.equals(playerName)) {
                return true;
            }
        }
        
        return false;
    }
}
