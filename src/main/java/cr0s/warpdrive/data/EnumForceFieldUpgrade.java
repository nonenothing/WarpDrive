package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IForceFieldUpgrade;
import cr0s.warpdrive.api.IForceFieldUpgradeEffector;
import cr0s.warpdrive.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumForceFieldUpgrade implements IForceFieldUpgrade, IForceFieldUpgradeEffector {
	//            Upgrade         - Compatibility -  ----- Value -----  -- Scan speed --  -- Place speed --  --------- Energy costs ---------  comment
	//            name            projector   relay    incr.       cap  minimum  maximum   minimum  maximum  startup   scan   place    entity  
	NONE         ("none"         ,        0,      0,    0.0F,     0.0F,  0.000F,  0.000F,   0.000F,  0.000F,    0.0F, 0.000F, 0.000F,    0.0F, ""),
	ATTRACTION   ("attraction"   ,        0,      1,    1.0F,     4.0F,  0.000F,  0.000F,   0.000F,  0.000F,   50.0F, 0.150F, 0.000F,  200.0F, "value is acceleration"),
	BREAKING     ("breaking"     ,        0,      1,    1.0F,    25.0F,  0.400F,  0.500F,   0.020F,  0.150F,  700.0F, 0.080F, 4.000F,    0.0F, "value is hardness level"),
	CAMOUFLAGE   ("camouflage"   ,        0,      1,    1.0F,     3.0F,  0.600F,  0.850F,   0.700F,  0.950F, 1000.0F, 3.000F, 7.000F,    0.0F, "value is boolean"),
	COOLING      ("cooling"      ,        3,      1,   30.0F,   300.0F,  0.000F,  0.000F,   0.900F,  0.900F,  150.0F, 0.060F, 1.500F, 1000.0F, "value is heat units"),
	FUSION       ("fusion"       ,        1,      1,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F, 1000.0F, 0.150F, 0.150F,    0.0F, "value is boolean"),
	HEATING      ("heating"      ,        3,      1,  100.0F, 10000.0F,  0.000F,  0.000F,   0.900F,  0.900F,  150.0F, 0.300F, 3.000F,  500.0F, "value is heat units"),
	INVERSION    ("inversion"    ,        1,      0,    1.0F,     1.0F,  1.250F,  1.250F,   0.000F,  0.000F, 1500.0F, 0.150F, 0.150F,  100.0F, "value is boolean"),
	ITEM_PORT    ("itemPort"     ,        0,      1,    1.0F,    10.0F,  0.000F,  0.000F,   0.950F,  0.900F,   50.0F, 0.120F, 0.500F,  800.0F, "value is boolean"),
	PUMPING      ("pumping"      ,        0,      1, 1000.0F, 50000.0F,  0.800F,  1.000F,   0.400F,  1.000F,  800.0F, 0.500F, 2.250F,    0.0F, "value is viscosity"),
	RANGE        ("range"        ,        4,      1,    8.0F,    56.0F,  1.150F,  0.800F,   1.150F,  0.800F,   10.0F, 0.300F, 0.750F,  400.0F, "value is bonus blocks"),
	REPULSION    ("repulsion"    ,        0,      1,    1.0F,     4.0F,  0.000F,  0.000F,   0.000F,  0.000F,   50.0F, 0.150F, 0.000F,  200.0F, "value is acceleration"),
	ROTATION     ("rotation"     ,        1,      0,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,  100.0F, 0.000F, 0.000F,    0.0F, "value is boolean"),
	SHOCK        ("shock"        ,        3,      1,    1.0F,    10.0F,  0.800F,  0.800F,   0.800F,  0.800F,  300.0F, 0.600F, 4.000F, 3000.0F, "value is damage points"),
	SILENCER     ("silencer"     ,        1,      0,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,    0.0F, 0.120F, 0.620F,    0.0F, "value is boolean"),
	SPEED        ("speed"        ,        4,      1,    1.0F,    20.0F,  1.250F,  6.000F,   1.200F,  5.000F,  200.0F, 0.400F, 1.700F,  500.0F, "value is not used (just a counter)"),
	STABILIZATION("stabilization",        0,      1,    1.0F,     6.0F,  0.450F,  0.550F,   0.050F,  0.150F,  400.0F, 1.520F, 4.300F,    0.0F, "value is boolean"),
	THICKNESS    ("thickness"    ,        5,      1,    0.2F,     1.0F,  0.800F,  1.600F,   0.000F,  0.000F,  100.0F, 0.700F, 2.400F,  100.0F, "value is bonus ratio"),
	TRANSLATION  ("translation"  ,        1,      0,    1.0F,     1.0F,  0.000F,  0.000F,   0.000F,  0.000F,  100.0F, 0.000F, 0.000F,    0.0F, "value is boolean"),
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
		
		// entity classification
		int entityLevel = 0;
		if (!entity.isDead) {
			if (entity instanceof EntityFireworkRocket) {
				entityLevel = 0;
			} else if (entity instanceof EntityPlayer) {
				entityLevel = 4;
			} else if (entity instanceof EntityMob
				           || entity instanceof EntityGolem
				           || entity instanceof EntityFireball
				           || entity instanceof EntityTNTPrimed
				           || entity instanceof EntityThrowable
				           || entity instanceof EntityMinecart) {
				entityLevel = 3;
			} else if (entity instanceof EntityLivingBase
				           || entity instanceof EntityXPOrb
				           || entity instanceof EntityBoat) {
				entityLevel = 2;
			} else if (entity instanceof EntityItem
				           || entity instanceof EntityArrow
				           || entity instanceof EntityFallingBlock) {
				entityLevel = 1;
			}
		}
		
		double maxSpeed = scaledValue / (entityLevel / 4.0F) * ForceFieldSetup.FORCEFIELD_ACCELERATION_FACTOR;
		Vector3 motion = direction.clone().scale(maxSpeed); // new Vector3(entity.motionX, entity.motionY, entity.motionZ);
		// apply damages and particle effects
		switch(this) {
		case ATTRACTION:
			if (scaledValue <= 0.1F || entityLevel > scaledValue) {
				return 0;
			}
			// WarpDrive.logger.info("scaledValue " + scaledValue + " maxSpeed " + maxSpeed + " direction " + direction + " motion " + motion);
			motion.invert();
			entity.fallDistance = 0.0F;
			entity.addVelocity(motion.x, motion.y, motion.z);
			
			PacketHandler.sendBeamPacket(world, origin, origin.clone().translate(motion),
				0.2F, 0.4F, 0.7F, 10, 0, 50);
			return 10;
		
		case REPULSION:
			if (scaledValue <= 0.1F || entityLevel > scaledValue) {
				return 0;
			}
			WarpDrive.logger.info("scaledValue " + scaledValue + " maxSpeed " + maxSpeed + " direction " + direction + " motion " + motion);
			entity.fallDistance = 0.0F;
			entity.addVelocity(motion.x, motion.y, motion.z);
			
			PacketHandler.sendBeamPacket(world, origin, origin.clone().translate(motion),
				0.2F, 0.4F, 0.7F, 10, 0, 50);
			return 10;
		
		case COOLING:
			if (scaledValue >= 295.0F || !(entity instanceof EntityLivingBase) || entityLevel <= 0) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageCold, (300 - scaledValue) / 10);
			
			direction.scale(0.20D);
			PacketHandler.sendSpawnParticlePacket(world, "snowshovel", origin, direction,
				0.20F + 0.10F * world.rand.nextFloat(), 0.25F + 0.25F * world.rand.nextFloat(), 0.60F + 0.30F * world.rand.nextFloat(),
				0.0F, 0.0F, 0.0F, 32);
			return 10;
		
		case HEATING:
			if (scaledValue <= 305.0F || !(entity instanceof EntityLivingBase) || entityLevel <= 0) {
				return 0;
			}
			if (!entity.isImmuneToFire()) {
				entity.setFire(1);
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
		
		case SHOCK:
			if (scaledValue <= 0 || !(entity instanceof EntityLivingBase) || entityLevel <= 0) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageShock, Math.abs(scaledValue));
			
			direction.scale(0.15D);
			PacketHandler.sendSpawnParticlePacket(world, "fireworksSpark", origin, direction,
				0.20F + 0.30F * world.rand.nextFloat(), 0.50F + 0.15F * world.rand.nextFloat(), 0.75F + 0.25F * world.rand.nextFloat(),
				0.10F + 0.20F * world.rand.nextFloat(), 0.10F + 0.30F * world.rand.nextFloat(), 0.20F + 0.10F * world.rand.nextFloat(),
				32);
			return 10;
		
		default:
			return 0;
		}
	}
}
