package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IForceFieldUpgrade;
import cr0s.warpdrive.api.IForceFieldUpgradeEffector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;

public enum EnumForceFieldUpgrade implements IForceFieldUpgrade, IForceFieldUpgradeEffector {
	//            name         projector  relay value  cap     scan   place startup  scan  place  entity  comment
	NONE         ("none"         , false, false,    0,      0,  0.0F,  0.0F,   0.0F, 0.0F,  0.0F,   0.0F, ""),
	BREAK        ("break"        , false, true , 1000,  25000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is hardness level"),
	CAMOUFLAGE   ("camouflage"   , false, true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"), 
	COOL         ("cool"         , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is heat units"),
	FUSION       ("fusion"       , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"),
	INVERT       ("invert"       , false, true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"),
	MUTE         ("mute"         , true , false, 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"),
	PUMP         ("pump"         , false, true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is TO BE DEFINED"),
	RANGE        ("range"        , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is bonus blocks"),
	ROTATION     ("rotation"     , true , false, 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"),
	SHOCK        ("shock"        , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is damage points"),
	SPEED        ("speed"        , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is bonus ratio"),
	STABILIZE    ("stabilize"    , false, true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"),
	THICKNESS    ("thickness"    , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is bonus ratio"),
	TRANSLATION  ("translation"  , true , false, 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is boolean"),
	WARM         ("warm"         , true , true , 1000,   1000, 10.0F, 10.0F, 100.0F, 1.0F, 10.0F, 100.0F, "value is heat units"),
	;
	
	public final String unlocalizedName;
	public final boolean allowOnProjector;
	public final boolean allowOnRelay;
	private final int upgradeValue;
	private final int upgradeValueMax;
	private final float maxScanSpeed;
	private final float maxPlaceSpeed;
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
	
	EnumForceFieldUpgrade(final String unlocalizedName, final boolean allowOnProjector, final boolean allowOnRelay,
	                      final int upgradeValue, final int upgradeValueMax,
	                      final float maxScanSpeed, final float maxPlaceSpeed,
	                      final float startupEnergyCost, final float scanEnergyCost, final float placeEnergyCost, final float entityEffectEnergyCost,
	                      final String comment) {
		this.unlocalizedName = unlocalizedName;
		this.allowOnProjector = allowOnProjector;
		this.allowOnRelay = allowOnRelay;
		this.upgradeValue = upgradeValue;
		this.upgradeValueMax = upgradeValueMax;
		this.maxScanSpeed = maxScanSpeed;
		this.maxPlaceSpeed = maxPlaceSpeed;
		this.startupEnergyCost = startupEnergyCost;
		this.scanEnergyCost = scanEnergyCost;
		this.placeEnergyCost = placeEnergyCost;
		this.entityEffectEnergyCost = entityEffectEnergyCost;
		assert(!comment.isEmpty());
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
	public int getUpgradeValue() {
		return upgradeValue;
	}
	
	@Override
	public float getScaledValue(final float ratio, final int upgradeValue) {
		return ratio * Math.min(upgradeValueMax, upgradeValue) / 1000.0F;
	}
	
	@Override
	public float getMaxScanSpeed(final float scaledValue) {
		return maxScanSpeed * scaledValue;
	}
	
	@Override
	public float getMaxPlaceSpeed(final float scaledValue) {
		return maxPlaceSpeed * scaledValue;
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
		
		// TODO add some particle effects
		switch(this) {
		case COOL:
			if (scaledValue >= 0 || !(entity instanceof EntityLivingBase)) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageCold, Math.abs(scaledValue));
			return 10;
		
		case SHOCK:
			if (scaledValue <= 0 || !(entity instanceof EntityLivingBase)) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageShock, Math.abs(scaledValue));
			return 10;
		
		case WARM:
			if (scaledValue <= 0 || !(entity instanceof EntityLivingBase)) {
				return 0;
			}
			entity.attackEntityFrom(WarpDrive.damageWarm, Math.abs(scaledValue));
			return 10;
		
		default:
			return 0;
		}
	}
}
