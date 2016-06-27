package cr0s.warpdrive.data;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IForceFieldUpgrade;
import cr0s.warpdrive.api.IForceFieldShape;
import cr0s.warpdrive.api.IForceFieldUpgradeEffector;
import cr0s.warpdrive.block.forcefield.TileEntityForceFieldProjector;
import cr0s.warpdrive.config.WarpDriveConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ForceFieldSetup extends GlobalPosition {
	private static final float FORCEFIELD_BASE_SCAN_SPEED_BLOCKS_PER_SECOND = 500;
	private static final float FORCEFIELD_BASE_PLACE_SPEED_BLOCKS_PER_SECOND = 100;
	private static final float FORCEFIELD_MAX_SCAN_SPEED_BLOCKS_PER_SECOND = 50000;
	private static final float FORCEFIELD_MAX_PLACE_SPEED_BLOCKS_PER_SECOND = 20000;
	private static final float FORCEFIELD_UPGRADE_BOOST_PER_PROJECTOR_TIER = 0.50F;
	public static final float FORCEFIELD_UPGRADE_BOOST_PER_RELAY_TIER = 0.25F;
	
	public final int beamFrequency;
	public final byte tier;
	public final Set<TileEntityForceFieldProjector> projectors = new HashSet<>();
	private Block blockCamouflage;
	private int metadataCamouflage;
	private int colorMultiplierCamouflage;
	private int lightCamouflage;
	private static final List<Integer> ALLOWED_RENDER_TYPES = Arrays.asList(
		0, 1, 2, 3, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 41);
	private final HashMap<IForceFieldUpgradeEffector, Float> upgrades = new HashMap<>(EnumForceFieldUpgrade.length);
	
	public float scanSpeed;
	public float placeSpeed;
	public int startupEnergyCost;
	public int scanEnergyCost;
	public int placeEnergyCost;
	private int entityEnergyCost;
	
	public float breaking_maxHardness;
	public float temperatureLevel;
	public boolean hasStabilize;
	public boolean hasFusion;
	public boolean isInverted;
	public float thickness;
	public float pumping_maxViscosity;
	
	// Projector provided properties
	public float rotationYaw;
	public float rotationPitch;
	public float rotationRoll;
	public VectorI vTranslation = new VectorI(0, 0, 0);
	public VectorI vMin = new VectorI(-8, -8, -8);
	public VectorI vMax = new VectorI(8, 8, 8);
	public IForceFieldShape shapeProvider;
	public boolean isDoubleSided = true;
	
	public ForceFieldSetup(final int dimensionId, final int x, final int y, final int z, final byte tier, final int beamFrequency) {
		super(dimensionId, x, y, z);
		this.tier = tier;
		this.beamFrequency = beamFrequency;
		refresh();
		if (WarpDriveConfig.LOGGING_FORCEFIELD) {
			WarpDrive.logger.info("Force field projector energy costs:" 
				                      + " startup " + startupEnergyCost
				                      + " / " + Math.round(startupEnergyCost + placeEnergyCost * placeSpeed * TileEntityForceFieldProjector.PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F)
				                      + " scan " + scanEnergyCost
				                      + " place " + placeEnergyCost
				                      + " entity " + entityEnergyCost
				                      + " speeds: scan " + scanSpeed
				                      + " place " + placeSpeed);
		}
	}
	
	public boolean isAccessGranted(EntityPlayer entityPlayer, EnumPermissionNode enumPermissionNode) {
		return false; // TODO
	}
	
	private static boolean isValidCamouflage(final Block block) {
		return block != null && block != Blocks.air && ALLOWED_RENDER_TYPES.contains(block.getRenderType());
	}
	
	public Block getCamouflageBlock() {
		if (isValidCamouflage(blockCamouflage)) {
			return blockCamouflage;
		}
		return null;
	}
	
	public int getCamouflageMetadata() {
		if (isValidCamouflage(blockCamouflage)) {
			return metadataCamouflage;
		}
		return 0;
	}
	
	public int getCamouflageColorMultiplier() {
		if (isValidCamouflage(blockCamouflage)) {
			return colorMultiplierCamouflage;
		}
		return 0;
	}
	
	public int getCamouflageLight() {
		if (isValidCamouflage(blockCamouflage)) {
			return lightCamouflage;
		}
		return 0;
	}
	
	private float getScaledUpgrade(final IForceFieldUpgradeEffector effector) {
		Float scaledValue = upgrades.get(effector);
		return scaledValue == null ? 0.0F : scaledValue;
	}
	
	private void refresh() {
		Set<TileEntity> tileEntities = ForceFieldRegistry.getTileEntities(beamFrequency);
		HashMap<IForceFieldUpgradeEffector, Float> upgradeValues = new HashMap<>(EnumForceFieldUpgrade.length);
		
		for (TileEntity tileEntity : tileEntities) {
			// only consider same dimension
			if (tileEntity.getWorldObj().provider.dimensionId != dimensionId) {
				continue;
			}
			// projectors
			if (tileEntity instanceof TileEntityForceFieldProjector) {
				TileEntityForceFieldProjector projector = (TileEntityForceFieldProjector) tileEntity;
				if (tileEntity.xCoord == x && tileEntity.yCoord == y && tileEntity.zCoord == z) {
					shapeProvider = projector.getShapeProvider();
					isDoubleSided = projector.isDoubleSided;
					vTranslation = new VectorI(projector);
					rotationYaw = projector.getRotationYaw();
					rotationPitch = projector.getRotationPitch();
					rotationRoll = projector.getRotationRoll();
					// TODO vMin = projector.vMin;
					// TODO vMax = projector.vMax;
					for (Entry<Object, Integer> entry : projector.getUpgradesOfType(null).entrySet()) {
						if (entry.getKey() instanceof IForceFieldUpgrade) {
							IForceFieldUpgradeEffector upgradeEffector = ((IForceFieldUpgrade)entry.getKey()).getUpgradeEffector();
							if (upgradeEffector != null) {
								Float currentValue = upgradeValues.get(upgradeEffector);
								if (currentValue == null) {
									currentValue = 0.0F;
								}
								float addedValue = ((IForceFieldUpgrade)entry.getKey()).getUpgradeValue() * entry.getValue();
								addedValue *= 1 + (tier - 1) * FORCEFIELD_UPGRADE_BOOST_PER_PROJECTOR_TIER;
								upgradeValues.put(upgradeEffector, currentValue + addedValue);
							}
						}
					}
					
				} else {
					if ((((TileEntityForceFieldProjector) tileEntity).isEnabled)
						&& (((TileEntityForceFieldProjector) tileEntity).isCalculated())
						&& (((TileEntityForceFieldProjector) tileEntity).isValid())) {
						projectors.add((TileEntityForceFieldProjector) tileEntity);
					}
				}
			}
			// upgrade blocks (namely, relays)
			if (tileEntity instanceof IForceFieldUpgrade) {
				IForceFieldUpgradeEffector upgradeEffector = ((IForceFieldUpgrade)tileEntity).getUpgradeEffector();
				if (upgradeEffector != null) {
					Float currentValue = upgradeValues.get(upgradeEffector);
					if (currentValue == null) {
						currentValue = 0.0F;
					}
					upgradeValues.put(upgradeEffector, currentValue + ((IForceFieldUpgrade)tileEntity).getUpgradeValue());
					
					// camouflage identification
					if (upgradeEffector == EnumForceFieldUpgrade.CAMOUFLAGE) {
						Block blockCandidate = tileEntity.getWorldObj().getBlock(tileEntity.xCoord, tileEntity.yCoord + 1, tileEntity.zCoord);
						if (isValidCamouflage(blockCandidate)) {
							blockCamouflage = blockCandidate;
							metadataCamouflage = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord + 1, tileEntity.zCoord);
							colorMultiplierCamouflage = 0x808080; // blockCandidate.colorMultiplier(tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord + 1, tileEntity.zCoord);
							lightCamouflage = blockCandidate.getLightValue(tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord + 1, tileEntity.zCoord);
						}
					}
				}
			}
		}
		
		// set default coefficients, depending on projector
		scanSpeed = FORCEFIELD_BASE_SCAN_SPEED_BLOCKS_PER_SECOND * (isDoubleSided ? 2.1F : 1.0F);
		placeSpeed = FORCEFIELD_BASE_PLACE_SPEED_BLOCKS_PER_SECOND * (isDoubleSided ? 2.1F : 1.0F);
		float startupEnergyCost = 60.0F + 20.0F * tier;
		float scanEnergyCost = 1.0F + 1.0F * tier;
		float placeEnergyCost = 3.0F + 3.0F * tier;
		float entityEnergyCost = 2.0F;
		if (isDoubleSided) {
			scanSpeed *= 2.1F;
			placeSpeed *= 2.1F;
			startupEnergyCost += 20.0F * tier;
			scanEnergyCost *= 0.9F;
			placeEnergyCost *= 0.9F;
			entityEnergyCost *= 0.9F;
		}
		
		// apply scaling
		float speedRatio;
		for (Map.Entry<IForceFieldUpgradeEffector, Float> entry : upgradeValues.entrySet()) {
			float scaledValue = entry.getKey().getScaledValue(1.0F, entry.getValue());
			if (scaledValue != 0.0F) {
				upgrades.put(entry.getKey(), scaledValue);
				
				speedRatio = entry.getKey().getScanSpeedFactor(scaledValue);
				if (speedRatio > 0.0F) {
					scanSpeed *= speedRatio;
				}
				speedRatio = entry.getKey().getPlaceSpeedFactor(scaledValue);
				if (speedRatio > 0.0F) {
					placeSpeed *= speedRatio;
				}
				
				startupEnergyCost += entry.getKey().getStartupEnergyCost(scaledValue);
				scanEnergyCost += entry.getKey().getScanEnergyCost(scaledValue);
				placeEnergyCost += entry.getKey().getPlaceEnergyCost(scaledValue);
				entityEnergyCost +=  entry.getKey().getEntityEffectEnergyCost(scaledValue);
			}
		}
		// finalize coefficients
		scanSpeed = Math.min(FORCEFIELD_MAX_SCAN_SPEED_BLOCKS_PER_SECOND, scanSpeed);
		placeSpeed = Math.min(FORCEFIELD_MAX_PLACE_SPEED_BLOCKS_PER_SECOND, placeSpeed);
		this.startupEnergyCost = Math.round(startupEnergyCost);
		this.scanEnergyCost = Math.round(scanEnergyCost);
		this.placeEnergyCost = Math.round(placeEnergyCost);
		this.entityEnergyCost = Math.round(entityEnergyCost);
		
		
		// fusion, inversion and stabilize just needs to be defined
		hasFusion = getScaledUpgrade(EnumForceFieldUpgrade.FUSION) > 0.0F;
		isInverted = getScaledUpgrade(EnumForceFieldUpgrade.INVERSION) > 0.0F;
		hasStabilize = getScaledUpgrade(EnumForceFieldUpgrade.STABILIZATION) > 0.0F;
		
		// temperature is a compound of cooling and heating
		temperatureLevel = getScaledUpgrade(EnumForceFieldUpgrade.HEATING) - getScaledUpgrade(EnumForceFieldUpgrade.COOLING);
		
		// disintegration, pump and thickness is the actual value
		breaking_maxHardness = getScaledUpgrade(EnumForceFieldUpgrade.BREAKING);
		pumping_maxViscosity = getScaledUpgrade(EnumForceFieldUpgrade.PUMPING);
		thickness = 1.0F + getScaledUpgrade(EnumForceFieldUpgrade.THICKNESS);
	}
	
	@Override
	public String toString() {
		return String.format("%s @ DIM%d (%d %d %d) (%d %d %d) -> (%d %d %d)",
			getClass().getSimpleName(), dimensionId,
			x, y, z,
			vMin.x, vMin.y, vMin.z,
			vMax.x, vMax.y, vMax.z);
	}
	
	public int onEntityEffect(World world, final int x, final int y, final int z, Entity entity) {
		int countdown = 0;
		for (Map.Entry<IForceFieldUpgradeEffector, Float> entry : upgrades.entrySet()) {
			countdown += entry.getKey().onEntityEffect(entry.getValue(), world, x, y, z, entity);
		}
		return countdown;
	}
	
	public int applyDamage(World world, final DamageSource damageSource, final int damageLevel) {
		assert(damageSource != null);
		TileEntity tileEntity = world.getTileEntity(this.x, this.y, this.z);
		if (tileEntity instanceof TileEntityForceFieldProjector) {
			float scaledDamage = damageLevel * entityEnergyCost / 20000.0F;
			int energyCost = (int)Math.floor(scaledDamage) + (world.rand.nextFloat() <= scaledDamage - Math.floor(scaledDamage) ? 1 : 0);
			((TileEntityForceFieldProjector)tileEntity).consumeEnergy(energyCost, false);
			return 0;
		}
		return damageLevel;
	}
}
