package cr0s.warpdrive.api;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IForceFieldUpgradeEffector {
	// Apply scaling to the sum of all upgrades for this upgrade category
	// Use this to cap the upgrade or apply non-linear scaling.
	float getScaledValue(final float ratio, final int upgradeValue);
	
	// Maximum speed of this upgrade in blocks per projector update
	// Typical values: 0.3 to 30 blocks per upgrade
	// Return 0 if the upgrade has no effect
	float getMaxScanSpeed(final float scaledValue);
	float getMaxPlaceSpeed(final float scaledValue);
	
	// Energy impacts of this upgrade
	float getStartupEnergyCost(final float scaledValue);
	float getScanEnergyCost(final float scaledValue);
	float getPlaceEnergyCost(final float scaledValue);
	float getEntityEffectEnergyCost(final float scaledValue);
	
	// Entity impact of this upgrade
	// Return 
	int onEntityEffect(final float scaledValue, World world, final int x, final int y, final int z, Entity entity);
}
