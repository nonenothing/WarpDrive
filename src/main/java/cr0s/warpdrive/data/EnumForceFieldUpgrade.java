package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IForceFieldUpgrade;
import cr0s.warpdrive.api.IForceFieldUpgradeEffector;
import cr0s.warpdrive.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumForceFieldUpgrade implements IForceFieldUpgrade, IForceFieldUpgradeEffector {
	//            Upgrade         - Compatibility -  ----- Value -----  -- Scan speed --  -- Place speed --  ------- Energy costs -------  comment
	//            name            projector   relay    incr.       cap  minimum  maximum   minimum  maximum  startup   scan   place  entity  
	NONE         ("none"         ,        0,      0,    0.0F,     0.0F,  0.000F,  0.000F,   0.000F,  0.000F,    0.0F, 0.000F, 0.000F,   0.0F, ""),
	BREAKING     ("breaking"     ,        0,      1,    1.0F,    25.0F,  0.400F,  0.500F,   0.010F,  0.100F,   70.0F, 0.020F, 1.000F,   0.0F, "value is hardness level"),
	CAMOUFLAGE   ("camouflage"   ,        0,      1,    1.0F,     3.0F,  0.600F,  0.850F,   0.700F,  0.950F,  100.0F, 0.100F, 0.300F,   0.0F, "value is boolean"), 
	COOLING      ("cooling"      ,        3,      1,   30.0F,   300.0F,  0.000F,  0.000F,   0.900F,  0.900F,   15.0F, 0.020F, 0.500F,  10.0F, "value is heat units"),
	FUSION       ("fusion"       ,        1,      1,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,  100.0F, 0.050F, 0.050F,   0.0F, "value is boolean"),
	HEATING      ("heating"      ,        3,      1,  100.0F, 10000.0F,  0.000F,  0.000F,   0.900F,  0.900F,   15.0F, 0.100F, 1.000F,   5.0F, "value is heat units"),
	INVERSION    ("inversion"    ,        1,      0,    1.0F,     1.0F,  0.250F,  0.250F,   0.000F,  0.000F,  150.0F, 0.050F, 0.050F,   1.0F, "value is boolean"),
	PUMPING      ("pumping"      ,        0,      1, 1000.0F, 50000.0F,  0.800F,  1.000F,   0.400F,  1.000F,   80.0F, 0.010F, 0.500F,   0.0F, "value is viscosity"),
	RANGE        ("range"        ,        4,      1,    8.0F,    56.0F,  1.100F,  0.800F,   1.100F,  0.800F,    1.0F, 0.100F, 0.250F,   4.0F, "value is bonus blocks"),
	ROTATION     ("rotation"     ,        1,      0,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,   10.0F, 0.000F, 0.000F,   0.0F, "value is boolean"),
	SHOCK        ("shock"        ,        3,      1,    1.0F,    10.0F,  0.800F,  0.800F,   0.800F,  0.800F,   30.0F, 0.100F, 2.000F,  30.0F, "value is damage points"),
	SILENCER     ("silencer"     ,        1,      0,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,    0.0F, 0.010F, 0.020F,   0.0F, "value is boolean"),
	SPEED        ("speed"        ,        4,      1,    1.0F,    20.0F,  1.250F,  6.000F,   1.200F,  5.000F,   20.0F, 0.100F, 1.000F,   5.0F, "value is not used (just a counter)"),
	STABILIZATION("stabilization",        0,      1,    1.0F,     6.0F,  0.450F,  0.550F,   0.050F,  0.150F,   40.0F, 0.100F, 1.000F,   0.0F, "value is boolean"),
	THICKNESS    ("thickness"    ,        5,      1,    0.2F,     1.0F,  0.800F,  1.600F,   0.000F,  0.000F,   10.0F, 0.100F, 1.000F,   1.0F, "value is bonus ratio"),
	TRANSLATION  ("translation"  ,        1,      0,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,   10.0F, 0.000F, 0.000F,   0.0F, "value is boolean"),
	;
	
	public final String unlocalizedName;
	public final int maxCountOnProjector;
	public final int maxCountOnRelay;
	private final float upgradeValue;
	private final float upgradeValueMax;
	private final float scanSpeedOffset;
	private final float scanSpeedSlope;
	private final float placeSpeedOffset;
	private final float placeSpeedSlope;
	private final float startupEnergyCost;
	private final float scanEnergyCost;
	private final float placeEnergyCost;
	private final float entityEffectEnergyCost;
	
	// cached values
	public static final int length;
	private static final HashMap<Integer, EnumForceFieldUpgrade> ID_MAP = new HashMap<>();
	
	static {
		length = EnumForceFieldUpgrade.values().length;
		for (EnumForceFieldUpgrade forceFieldShapeType : values()) {
			ID_MAP.put(forceFieldShapeType.ordinal(), forceFieldShapeType);
		}
	}
	
	EnumForceFieldUpgrade(final String unlocalizedName, final int allowOnProjector, final int maxCountOnRelay,
	                      final float upgradeValue, final float upgradeValueMax,
	                      final float scanSpeedMinimum, final float scanSpeedMaximum, final float placeSpeedMinimum, final float placeSpeedMaximum,
	                      final float startupEnergyCost, final float scanEnergyCost, final float placeEnergyCost, final float entityEffectEnergyCost,
	                      final String comment) {
		this.unlocalizedName = unlocalizedName;
		this.maxCountOnProjector = allowOnProjector;
		this.maxCountOnRelay = maxCountOnRelay;
		
		this.upgradeValue = upgradeValue;
		this.upgradeValueMax = upgradeValueMax;
		
		this.scanSpeedSlope = (upgradeValueMax == upgradeValue) ? 0.0F : (scanSpeedMaximum - scanSpeedMinimum) / (upgradeValueMax - upgradeValue);
		this.scanSpeedOffset = scanSpeedMinimum - scanSpeedSlope * upgradeValue;
		this.placeSpeedSlope = (upgradeValueMax == upgradeValue) ? 0.0F : (placeSpeedMaximum - placeSpeedMinimum) / (upgradeValueMax - upgradeValue);
		this.placeSpeedOffset = placeSpeedMinimum - placeSpeedSlope * upgradeValue;
		
		this.startupEnergyCost = startupEnergyCost / (upgradeValue != 0.0F ? upgradeValue : 1.0F);
		this.scanEnergyCost = scanEnergyCost / (upgradeValue != 0.0F ? upgradeValue : 1.0F);
		this.placeEnergyCost = placeEnergyCost / (upgradeValue != 0.0F ? upgradeValue : 1.0F);
		this.entityEffectEnergyCost = entityEffectEnergyCost / (upgradeValue != 0.0F ? upgradeValue : 1.0F);
		assert(!comment.isEmpty());
	}
	
	@Override
	public String toString() {
		return name();
	}
	
	@Nonnull
	public static EnumForceFieldUpgrade get(final int damage) {
		EnumForceFieldUpgrade enumForceFieldUpgrade = ID_MAP.get(damage);
		return enumForceFieldUpgrade == null ? EnumForceFieldUpgrade.NONE : enumForceFieldUpgrade;
	}
	
	@Override
	public IForceFieldUpgradeEffector getUpgradeEffector() {
		return this;
	}
	
	@Override
	public float getUpgradeValue() {
		return upgradeValue;
	}
	
	@Override
	public float getScaledValue(final float ratio, final float upgradeValue) {
		return ratio * Math.min(upgradeValueMax, upgradeValue);
	}
	
	@Override
	public float getScanSpeedFactor(final float scaledValue) {
		return scanSpeedOffset + scanSpeedSlope * scaledValue;
	}
	
	@Override
	public float getPlaceSpeedFactor(final float scaledValue) {
		return placeSpeedOffset + placeSpeedSlope * scaledValue;
	}
	
	@Override
	public float getStartupEnergyCost(final float scaledValue) {
		return startupEnergyCost * scaledValue;
	}
	
	@Override
	public float getScanEnergyCost(final float scaledValue) {
		return scanEnergyCost * scaledValue;
	}
	
	@Override
	public float getPlaceEnergyCost(final float scaledValue) {
		return placeEnergyCost * scaledValue;
	}
	
	@Override
	public float getEntityEffectEnergyCost(final float scaledValue) { return entityEffectEnergyCost * scaledValue; }
	
	
	@Override
	public int onEntityEffect(final float scaledValue, World world, final int x, final int y, final int z, Entity entity) {
		if (scaledValue == 0.0F) {
			return 0;
		}
		
		// common particle effects properties
		Vector3 origin = new Vector3(x + 0.5D, y + 0.5D, z + 0.5D);
		Vector3 direction = new Vector3(entity).subtract(origin).normalize();
		origin.translateFactor(direction, 0.6D);
		
		// apply damages and particle effects
		switch(this) {
		case COOLING:
			if (scaledValue >= 295 || !(entity instanceof EntityLivingBase)) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageCold, (300 - scaledValue) / 10);
			
			direction.scale(0.20D);
			PacketHandler.sendSpawnParticlePacket(world, "snowshovel", origin, direction,
				0.20F + 0.10F * world.rand.nextFloat(), 0.25F + 0.25F * world.rand.nextFloat(), 0.60F + 0.30F * world.rand.nextFloat(),
				0.0F, 0.0F, 0.0F, 32);
			return 10;
		
		case SHOCK:
			if (scaledValue <= 0 || !(entity instanceof EntityLivingBase)) {
				return 0;
			}
			// entity.attackEntityFrom(WarpDrive.damageShock, Math.abs(scaledValue));
			
			direction.scale(0.15D);
			PacketHandler.sendSpawnParticlePacket(world, "fireworksSpark", origin, direction,
				0.20F + 0.30F * world.rand.nextFloat(), 0.50F + 0.15F * world.rand.nextFloat(), 0.75F + 0.25F * world.rand.nextFloat(),
				0.10F + 0.20F * world.rand.nextFloat(), 0.10F + 0.30F * world.rand.nextFloat(), 0.20F + 0.10F * world.rand.nextFloat(),
				32);
			return 10;
		
		case HEATING:
			if (scaledValue <= 305 || !(entity instanceof EntityLivingBase)) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageWarm, (scaledValue - 300) / 100);
			
			direction.scale(0.20D);
			PacketHandler.sendSpawnParticlePacket(world, "snowshovel", origin, direction,
				0.90F + 0.10F * world.rand.nextFloat(), 0.35F + 0.25F * world.rand.nextFloat(), 0.30F + 0.15F * world.rand.nextFloat(),
				0.0F, 0.0F, 0.0F, 32);
			/*
			direction.scale(0.10D);
			PacketHandler.sendSpawnParticlePacket(world, "flame", origin, direction,
				0.85F, 0.75F, 0.75F,
				0.0F, 0.0F, 0.0F, 32);
			/**/
			return 10;
		
		default:
			return 0;
		}
	}
}
