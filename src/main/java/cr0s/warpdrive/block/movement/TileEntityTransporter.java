package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.block.forcefield.BlockForceField;
import cr0s.warpdrive.block.forcefield.TileEntityForceField;
import cr0s.warpdrive.block.passive.BlockTransportBeacon;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.data.EnumTransporterState;
import cr0s.warpdrive.data.ForceFieldSetup;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityTransporter extends TileEntityAbstractEnergy implements IBeamFrequency {
	
	// persistent properties
	private int beamFrequency = -1;
	private boolean isEnabled = true;
	private boolean isLockRequested = false;
	private boolean isEnergizeRequested = false;
	private VectorI vSource_relative = new VectorI();
	private VectorI vDestination_relative = new VectorI();
	private double energyFactor = 1.0D;
	private double lockStrengthActual = 0.0D;
	private int tickCooldown = 0;
	private EnumTransporterState transporterState = EnumTransporterState.DISABLED;
	
	// computed properties
	private boolean isConnected = false;
	private int energyCostForTransfer = 0;
	private double lockStrengthOptimal = -1.0D;
	private double lockStrengthSpeed = 0.0D;
	private boolean isJammed = false;
	private VectorI vSource_absolute = new VectorI();
	private VectorI vDestination_absolute = new VectorI();
	private WeakReference<Entity> weakEntity = null;
	private Vector3 v3EntityPosition = null;
	private int tickEnergizing = 0;
	
	public TileEntityTransporter() {
		super();
		
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
		
		peripheralName = "warpdriveTransporter";
		addMethods(new String[] {
			"beamFrequency",
			"enable",
			"source",
			"destination",
			"lock",
			"energyFactor",
			"getLockStrength",
			"getEnergyRequired",
			"energize",
			"upgrades"
		});
		
		setUpgradeMaxCount(EnumComponentType.ENDER_CRYSTAL, WarpDriveConfig.TRANSPORTER_RANGE_UPGRADE_MAX_QUANTITY);
		setUpgradeMaxCount(EnumComponentType.CAPACITIVE_CRYSTAL, WarpDriveConfig.TRANSPORTER_ENERGY_STORED_UPGRADE_MAX_QUANTITY);
		setUpgradeMaxCount(EnumComponentType.EMERALD_CRYSTAL, WarpDriveConfig.TRANSPORTER_LOCKING_UPGRADE_MAX_QUANTITY);
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		updateParameters();
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		// frequency status
		isConnected = beamFrequency > 0 && beamFrequency <= IBeamFrequency.BEAM_FREQUENCY_MAX;
		
		// always cooldown
		if (tickCooldown > 0) {
			tickCooldown--;
		} else {
			tickCooldown = 0;
		}
		
		// consume power and apply general lock strength increase and decay
		boolean isPowered;
		if (!isEnabled) {
			transporterState = EnumTransporterState.DISABLED;
			isPowered = false;
			// (lock strength is cleared in state machine)
		} else {
			// energy consumption
			final int energyRequired = getEnergyRequired(transporterState);
			if (energyRequired > 0) {
				isPowered = energy_consume(energyRequired, false);
				if (!isPowered) {
					transporterState = EnumTransporterState.IDLE;
				}
			} else {
				isPowered = true;
			}
			
			// lock strength always decays
			lockStrengthActual = Math.max(0.0D, lockStrengthActual * WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_FACTOR_PER_TICK);
			
			// lock strength is capped at optimal, increasing when powered
			// a slight overshoot is added to force convergence
			if ( isPowered
			  && ( transporterState == EnumTransporterState.ACQUIRING
			    || transporterState == EnumTransporterState.ENERGIZING ) ) {
				final double overshoot = 0.01D;
				lockStrengthActual = Math.min(lockStrengthOptimal,
				                              lockStrengthActual + lockStrengthSpeed * (lockStrengthOptimal - lockStrengthActual + overshoot));
			}
		}
		
		// state feedback
		final boolean isActive = isEnabled && isConnected && isPowered;
		updateMetadata(isActive ? 1 : 0);
		if (isActive && isLockRequested && isJammed) {
			PacketHandler.sendSpawnParticlePacket(worldObj, "jammed", (byte) 5, new Vector3(this).translate(0.5F),
			                                      new Vector3(0.0D, 0.0D, 0.0D),
			                                      1.0F, 1.0F, 1.0F,
			                                      1.0F, 1.0F, 1.0F,
			                                      32);
		}
		
		// execute state transitions
		switch (transporterState) {
		case DISABLED:
			isLockRequested = false;
			isEnergizeRequested = false;
			lockStrengthActual = 0.0D;
			if (isActive) {
				transporterState = EnumTransporterState.IDLE;
			}
			break;
		
		case IDLE:
			if (isLockRequested) {
				// initial validation before starting acquisition
				updateParameters();
				
				if (!isJammed) {
					transporterState = EnumTransporterState.ACQUIRING;
				} else {
					tickCooldown = WarpDriveConfig.TRANSPORTER_JAMMED_COOLDOWN_TICKS;
				}
			}
			break;
		
		case ACQUIRING:
			if (!isLockRequested) {
				transporterState = EnumTransporterState.IDLE;
				
			} else if (isEnergizeRequested) {
				// final validation in case environment has changed
				updateParameters();
				
				if (isJammed) {
					tickCooldown += WarpDriveConfig.TRANSPORTER_JAMMED_COOLDOWN_TICKS;
					transporterState = EnumTransporterState.IDLE;
				} else {
					// reset entity to grab
					weakEntity = null;
					v3EntityPosition = null;
					
					// consume energy
					final int energyRequired = getEnergyRequired(transporterState);
					if (energy_consume(energyRequired, false)) {
						tickEnergizing = WarpDriveConfig.TRANSPORTER_TRANSFER_WARMUP_TICKS;
						transporterState = EnumTransporterState.ENERGIZING;
					}
				}
			}
			break;
		
		case ENERGIZING:
			// get entity
			updateEntityToTransfer();
			if (weakEntity == null) {
				transporterState = EnumTransporterState.ACQUIRING;
				isEnergizeRequested = false;
				tickCooldown = WarpDriveConfig.TRANSPORTER_TRANSFER_COOLDOWN_TICKS;
				lockStrengthActual = Math.max(0.0D, lockStrengthActual - WarpDriveConfig.TRANSPORTER_TRANSFER_LOCKING_LOST);
				break;
			}
			final Entity entity = weakEntity.get();
			if (entity == null) {
				// bad state
				WarpDrive.logger.info(String.format("%s Entity went missing, retrying next tick...",
				                                    this));
				break;
			}
			
			// warm-up
			if (tickEnergizing > 0) {
				tickEnergizing--;
				break;
			}
			
			// check lock strength
			if ( lockStrengthActual < 1.0D
			  && worldObj.rand.nextDouble() > lockStrengthActual ) {
				if (WarpDriveConfig.LOGGING_TRANSPORTER) {
					WarpDrive.logger.info(String.format("%s Insufficient lock strength %.3f", this, lockStrengthActual));
				}
				applyTeleportationDamages(false, entity, lockStrengthActual);
				tickCooldown = WarpDriveConfig.TRANSPORTER_TRANSFER_COOLDOWN_TICKS;
				break;
			}
			
			// teleport
			final Vector3 v3Target = new Vector3(
				vDestination_absolute.x + entity.posX - vSource_absolute.x,
				vDestination_absolute.y + entity.posY - vSource_absolute.y,
				vDestination_absolute.z + entity.posZ - vSource_absolute.z);
			if (WarpDriveConfig.LOGGING_TRANSPORTER) {
				WarpDrive.logger.info(String.format("%s Teleporting entity %s to %s",
				                                    this, entity, v3Target));
			}
			Commons.moveEntity(entity, worldObj, v3Target);
			applyTeleportationDamages(false, entity, lockStrengthActual);
			tickCooldown = WarpDriveConfig.TRANSPORTER_TRANSFER_COOLDOWN_TICKS;
			lockStrengthActual = Math.max(0.0D, lockStrengthActual - WarpDriveConfig.TRANSPORTER_TRANSFER_LOCKING_LOST);
			break;
		
		default:
			transporterState = EnumTransporterState.DISABLED;
			break;
		}
		
		// client effects
		if ( lockStrengthActual > 0.0F
		  || tickEnergizing > 0
		  || tickCooldown > 0 ) {
			final Entity entity = weakEntity == null ? null : weakEntity.get();
			PacketHandler.sendTransporterEffectPacket(worldObj, vSource_absolute, vDestination_absolute, lockStrengthActual,
			                                          entity, v3EntityPosition,
			                                          tickEnergizing, tickCooldown, 64);
		}
	}
	
	@Override
	public String getStatusHeader() {
		return super.getStatusHeader()
		       + "\n" + StatCollector.translateToLocalFormatted("warpdrive.transporter.status",
		                                                        vSource_absolute.x, vSource_absolute.y, vSource_absolute.z,
		                                                        vDestination_absolute.x, vDestination_absolute.y, vDestination_absolute.z);
	}
	
	@Override
	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	@Override
	public void setBeamFrequency(final int beamFrequency) {
		this.beamFrequency = beamFrequency;
	}
	
	private class FocusValues {
		public int countRangeUpgrades;
		public double strength;
		public double speed;
	}
	
	private void updateParameters() {
		isJammed = false;
		
		// check connection
		if (!isConnected) {
			isJammed = true;
			return;
		}
		
		// check minimum range
		if (vSource_relative.subtract(vDestination_relative).getMagnitudeSquared() < 2) {
			isJammed = true;
			return;
		}	
		
		// compute absolute coordinates
		vSource_absolute = new VectorI(this).translate(EnumFacing.UP).translate(vSource_relative);
		vDestination_absolute = new VectorI(this).translate(EnumFacing.UP).translate(vDestination_relative);
		
		// compute range as max distance between transporter, source and destination
		final int rangeSource2 = vSource_relative.getMagnitudeSquared();
		final int rangeDestination2 = vDestination_relative.getMagnitudeSquared();
		final int rangeDelta2 = vSource_relative.subtract(vDestination_relative).getMagnitudeSquared();
		final int rangeActual = (int) Math.ceil(Math.sqrt(Math.max(rangeSource2, Math.max(rangeDestination2, rangeDelta2))));
		
		// compute energy cost from range
		energyCostForTransfer = (int) Math.ceil(Math.max(0, Commons.interpolate(
				0,
				WarpDriveConfig.TRANSPORTER_TRANSFER_ENERGY_AT_MIN_RANGE,
				WarpDriveConfig.TRANSPORTER_RANGE_BASE_BLOCKS,
				WarpDriveConfig.TRANSPORTER_TRANSFER_ENERGY_AT_MAX_RANGE, 
		        rangeActual)));
		
		// compute focalization bonuses
		final FocusValues focusValuesSource      = getFocusValueAtCoordinates(vSource_absolute);
		final FocusValues focusValuesDestination = getFocusValueAtCoordinates(vDestination_absolute);
		final double focusBoost = energyFactor 
		                        * WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_BONUS_AT_MAX_ENERGY_FACTOR 
		                        / WarpDriveConfig.TRANSPORTER_TRANSFER_ENERGY_FACTOR_MAX;
		lockStrengthOptimal = (focusValuesSource.strength + focusValuesDestination.strength) / 2.0D + focusBoost;
		lockStrengthSpeed   = (focusValuesSource.speed + focusValuesDestination.speed) / 2.0D
		                    / WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_OPTIMAL_TICKS;
		
		final int rangeMax = WarpDriveConfig.TRANSPORTER_RANGE_BASE_BLOCKS
		                   + WarpDriveConfig.TRANSPORTER_RANGE_UPGRADE_BLOCKS * focusValuesSource.countRangeUpgrades
		                   + WarpDriveConfig.TRANSPORTER_RANGE_UPGRADE_BLOCKS * focusValuesDestination.countRangeUpgrades;
		
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info(String.format("Transporter parameters at (%d %d %d) are range (actual %d max %d) lockStrength (optimal %.3f speed %.3f)",
			                                    xCoord, yCoord, zCoord,
			                                    rangeActual, rangeMax,
			                                    lockStrengthOptimal, lockStrengthSpeed));
		}
		
		if (rangeActual > rangeMax) {
			isJammed = true;
			return;
		}
		
		isJammed |= isJammedTrajectory(vSource_absolute);
		isJammed |= isJammedTrajectory(vDestination_absolute);
	}
	
	private FocusValues getFocusValueAtCoordinates(final VectorI vAbsolute) {
		// scan the area
		int countBeacons = 0;
		int countTransporters = 0;
		int sumRangeUpgrades = 0;
		int sumFocusUpgrades = 0;
		
		final int xMin = vAbsolute.x - WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int xMax = vAbsolute.x + WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int yMin = vAbsolute.y - WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int yMax = vAbsolute.y + WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int zMin = vAbsolute.z - WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int zMax = vAbsolute.z + WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				if (y < 0 || y > 254) {
					continue;
				}
				
				for (int z = zMin; z <= zMax; z++) {
					final Block block = worldObj.getBlock(x, y, z);
					if (block instanceof BlockTransportBeacon) {
						// count active beacons
						final boolean isActive = worldObj.getBlockMetadata(x, y, z) == 0;
						if (isActive) {
							countBeacons++;
						}
						
					} else if (block instanceof BlockTransporter) {
						// count active transporters
						final TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
						if (tileEntity instanceof TileEntityTransporter) {
							countTransporters++;
							// remember upgrades
							sumRangeUpgrades += ((TileEntityTransporter) tileEntity).getUpgradeCount(EnumComponentType.ENDER_CRYSTAL);
							sumFocusUpgrades += ((TileEntityTransporter) tileEntity).getUpgradeCount(EnumComponentType.EMERALD_CRYSTAL);
						}
					}
				}
			}
		}
		
		// compute values
		final FocusValues result = new FocusValues();
		if (countTransporters > 0) {
			final int countFocusUpgrades = sumFocusUpgrades / countTransporters;
			result.countRangeUpgrades = sumRangeUpgrades / countTransporters;
			result.speed = WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_AT_TRANSPORTER + countFocusUpgrades * WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_UPGRADE;
			result.strength = WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_AT_TRANSPORTER + countFocusUpgrades * WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_UPGRADE;
		} else if (countBeacons > 0) {
			result.countRangeUpgrades = 0;
			result.speed = WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_AT_FOCUS;
			result.strength = WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_AT_FOCUS;
		} else {
			result.countRangeUpgrades = 0;
			result.speed = WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_IN_WILDERNESS;
			result.strength = WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_IN_WILDERNESS;
		}
		
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info(String.format("Transporter getFocusValueAtCoordinates %s gives range %d speed %.3f strength %.3f",
			                                    vAbsolute, result.countRangeUpgrades, result.speed, result.strength));
		}
		
		return result;
	}
	
	private boolean isJammedTrajectory(final VectorI vAbsolute) {
		final VectorI vPath = vAbsolute.clone().translateBack(new VectorI(this));
		final int length = (int) Math.ceil(3 * Math.sqrt(vPath.getMagnitudeSquared()));
		final Vector3 v3Delta = new Vector3(vPath.x / (double) length, vPath.y / (double) length, vPath.z / (double) length);
		
		// scan along given trajectory
		final Vector3 v3Current = new Vector3(this).translate(0.5D);
		final VectorI vCurrent = new VectorI(this);
		final VectorI vPrevious = vCurrent.clone();
		for (int step = 0; step < length; step++) {
			v3Current.translate(v3Delta);
			vCurrent.x = (int) Math.round(v3Current.x);
			vCurrent.y = (int) Math.round(v3Current.y);
			vCurrent.z = (int) Math.round(v3Current.z);
			// skip repeating coordinates
			if (vCurrent.equals(vPrevious)) {
				continue;
			}
			if (isJammedCoordinate(vCurrent)) return true;
			
			// remember this coordinates
			vPrevious.x = vCurrent.x;
			vPrevious.y = vCurrent.y;
			vPrevious.z = vCurrent.z;
		}
		return false;
	}
	private boolean isJammedCoordinate(final VectorI vCurrent) {
		// check block blacklist for blinking
		final Block block = vCurrent.getBlock(worldObj);
		if (Dictionary.BLOCKS_NOBLINK.contains(block)) {
			return true;
		}
		
		// allow passing through force fields with same beam frequency
		if (block instanceof BlockForceField) {
			final TileEntity tileEntity = vCurrent.getTileEntity(worldObj);
			if (tileEntity instanceof TileEntityForceField) {
				final ForceFieldSetup forceFieldSetup = ((TileEntityForceField) tileEntity).getForceFieldSetup();
				if (forceFieldSetup == null) {
					// projector not loaded yet, consider it jammed by default
					WarpDrive.logger.warn(String.format("%s projector not loaded at %s", this, tileEntity));
					return true;
				}
				if (forceFieldSetup.beamFrequency != beamFrequency) {
					// jammed by invalid beam frequency
					if (WarpDriveConfig.LOGGING_TRANSPORTER) {
						WarpDrive.logger.info(String.format("%s signal jammed by %s", this, tileEntity));
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private int getEnergyRequired(final EnumTransporterState transporterState) {
		switch (transporterState) {
		case DISABLED:
			return 0;
			
		case IDLE:
			return 0;
			
		case ACQUIRING:
			return (int) Math.ceil(WarpDriveConfig.TRANSPORTER_ACQUIRING_ENERGY_FACTOR * energyCostForTransfer * energyFactor);
			
		case ENERGIZING:
			return (int) Math.ceil(energyCostForTransfer * energyFactor);
			
		default:
			return 0;
		}
	}
	
	private void applyTeleportationDamages(final boolean isPreTeleportation, final Entity entity, final double strength) {
		// skip invulnerable
		if ( entity.isDead
		  || entity.isEntityInvulnerable() ) {
			return;
		}
		
		// add bonus if transport was successful
		final double strengthToUse = isPreTeleportation ? strength : Math.random() * WarpDriveConfig.TRANSPORTER_TRANSFER_SUCCESS_LOCK_BONUS + strength;
		
		final double strengthSafe = 0.95D;
		final double strengthMaxDamage = 0.65D;
		final double strengthNoDamage = 0.10D;
		if (strengthToUse > strengthSafe) {
			return;
		}
		final double damageNormalized = (strengthSafe - strengthToUse) / (strengthSafe - strengthMaxDamage);
		final double damageMax = isPreTeleportation ? WarpDriveConfig.TRANSPORTER_TRANSFER_FAILURE_MAX_DAMAGE : WarpDriveConfig.TRANSPORTER_TRANSFER_SUCCESS_MAX_DAMAGE ;
		// final double damageAmount = Commons.clamp(1.0D, 1000.0D, Math.pow(10.0D, 10.0D * damageNormalized));
		final double damageAmount = Commons.clamp(1.0D, 1000.0D, damageMax * damageNormalized);
		
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info(String.format("%s Applying teleportation damages %s transport with %.3f strength, %.2f damage towards %s",
			                                    this,
			                                    isPreTeleportation ? "pre" : "post",
			                                    strengthToUse,
			                                    damageAmount,
			                                    entity));
		}
		
		if (entity instanceof EntityLivingBase) {
			entity.attackEntityFrom(WarpDrive.damageTeleportation, (float) damageAmount);
			final boolean isCreative = (entity instanceof EntityPlayer) && ((EntityPlayer) entity).capabilities.isCreativeMode;
			if (!isCreative) {
				if (isPreTeleportation) {
					// add 1s nausea
					((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.confusion.id, 20, 0, true));
				} else {
					// add 5s poison
					((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.poison.id, 5 * 20, 0, true));
				}
			}
		} else if (strengthToUse > strengthNoDamage) {
			// add lava blade at location
			final VectorI vPosition = new VectorI(entity);
			if (worldObj.isAirBlock(vPosition.x, vPosition.y, vPosition.z)) {
				worldObj.setBlock(vPosition.x, vPosition.y, vPosition.z, Blocks.flowing_lava, 6, 2);
			}
		}
	}
	
	private void updateEntityToTransfer() {
		// validate existing entity
		if (weakEntity != null) {
			final Entity entity = weakEntity.get();
			if ( entity == null
			  || entity.isDead ) {
				// no longer valid => search a new one, no energy lost
				weakEntity = null;
				v3EntityPosition = null;
				
			} else {
				final double tolerance2 = WarpDriveConfig.TRANSPORTER_ENTITY_MOVEMENT_TOLERANCE_BLOCKS
				                        * WarpDriveConfig.TRANSPORTER_ENTITY_MOVEMENT_TOLERANCE_BLOCKS;
				final double distance2 = v3EntityPosition.distanceTo_square(entity); 
				if (distance2 > tolerance2) {
					// entity moved too much => damage existing one, grab another, lose energy
					final double strength = Math.sqrt(distance2) / Math.sqrt(tolerance2) / 2.0D; 
					applyTeleportationDamages(true, entity, strength);
					weakEntity = null;
					v3EntityPosition = null;
					transporterState = EnumTransporterState.ACQUIRING;
				}
			}
		}
		
		// grab another entity on first tick or if bad things happened
		if (weakEntity == null) {
			final Entity entityClosest = getClosestEntityAtSource();
			if (entityClosest == null) {
				return;
			}
			weakEntity = new WeakReference<>(entityClosest);
			v3EntityPosition = new Vector3(entityClosest);
		}
	}
	
	private Entity getClosestEntityAtSource() {
		final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
			vSource_absolute.x - WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS,
			vSource_absolute.y - 1.0D,
			vSource_absolute.z - WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS,
			vSource_absolute.x + WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS + 1.0D,
			vSource_absolute.y + 2.0D,
			vSource_absolute.z + WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS + 1.0D);
		
		Entity entityClosest = null;
		double rangeClosest2 = Integer.MAX_VALUE;
		
		final List entities = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
		for (final Object object : entities) {
			if (!(object instanceof Entity)) {
				continue;
			}
			
			final Entity entity = (Entity) object;
			
			// skip particle effects
			if (entity instanceof EntityFX) {
				continue;
			}
			
			// skip blacklisted ids
			final String entityId = EntityList.getEntityString(entity);
			if (Dictionary.ENTITIES_LEFTBEHIND.contains(entityId)) {
				if (WarpDriveConfig.LOGGING_TRANSPORTER) {
					WarpDrive.logger.info(String.format("Entity is not valid for transportation (id %s) %s", 
					                                    entityId, entity));
				}
				continue;
			}
			
			// keep closest one
			final double range2 = vSource_absolute.distance2To(entity);
			if (range2 < rangeClosest2) {
				rangeClosest2 = range2;
				entityClosest = (Entity) object;
			}
		}
		
		return entityClosest;
	}
	
	@Override
	public int energy_getMaxStorage() {
		final int energyUpgrades = getUpgradeCount(EnumComponentType.CAPACITIVE_CRYSTAL);
		return WarpDriveConfig.TRANSPORTER_MAX_ENERGY_STORED + energyUpgrades * WarpDriveConfig.TRANSPORTER_ENERGY_STORED_UPGRADE_BONUS;
	}
	
	@Override
	public boolean energy_canInput(final ForgeDirection from) {
		return from != ForgeDirection.UP;
	}
	
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		
		tagCompound.setInteger(IBeamFrequency.BEAM_FREQUENCY_TAG, beamFrequency);
		tagCompound.setBoolean("isEnabled", isEnabled);
		tagCompound.setBoolean("isLockRequested", isLockRequested);
		tagCompound.setBoolean("isEnergizeRequested", isEnergizeRequested);
		
		tagCompound.setTag("source", vSource_relative.writeToNBT(new NBTTagCompound()));
		tagCompound.setTag("destination", vDestination_relative.writeToNBT(new NBTTagCompound()));
		tagCompound.setDouble("energyFactor", energyFactor);
		tagCompound.setDouble("lockStrengthActual", lockStrengthActual);
		tagCompound.setInteger("tickCooldown", tickCooldown);
		
		tagCompound.setString("state", transporterState.toString());
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		beamFrequency = tagCompound.getInteger(IBeamFrequency.BEAM_FREQUENCY_TAG);
		isEnabled = tagCompound.getBoolean("isEnabled");
		isLockRequested = tagCompound.getBoolean("isLockRequested");
		isEnergizeRequested = tagCompound.getBoolean("isEnergizeRequested");
		
		final NBTBase tagSource = tagCompound.getTag("source");
		if (tagSource instanceof NBTTagCompound) {
			vSource_relative.readFromNBT((NBTTagCompound) tagSource);
		}
		
		final NBTBase tagDestination = tagCompound.getTag("destination");
		if (tagDestination instanceof NBTTagCompound) {
			vDestination_relative.readFromNBT((NBTTagCompound) tagDestination);
		}
		
		energyFactor = tagCompound.getDouble("energyFactor");
		lockStrengthActual = tagCompound.getDouble("lockStrengthActual");
		tickCooldown = tagCompound.getInteger("tickCooldown");
		
		try {
			transporterState = EnumTransporterState.valueOf(tagCompound.getString("state"));
		} catch (IllegalArgumentException exception) {
			transporterState = EnumTransporterState.DISABLED;
		}
	}
	
	// Common OC/CC methods
	public Boolean[] enable(Object[] arguments) {
		if (arguments.length == 1) {
			isEnabled = Commons.toBool(arguments[0]);
			markDirty();
		}
		return new Boolean[] { isEnabled };
	}
	
	public Integer[] source(final Object[] arguments) {
		final VectorI vNew = getVectorI(vSource_relative, arguments);
		if (!vNew.equals(vSource_relative)) {
			isLockRequested = false;
			isEnergizeRequested = false;
			vSource_relative = vNew;
		}
		return new Integer[] { vSource_relative.x, vSource_relative.y, vSource_relative.z };
	}
	
	public Integer[] destination(final Object[] arguments) {
		final VectorI vNew = getVectorI(vDestination_relative, arguments);
		if (!vNew.equals(vDestination_relative)) {
			isLockRequested = false;
			isEnergizeRequested = false;
			vDestination_relative = vNew;
		}
		return new Integer[] { vDestination_relative.x, vDestination_relative.y, vDestination_relative.z };
	}
	
	private VectorI getVectorI(final VectorI vDefault, final Object[] arguments) {
		final VectorI vResult = vDefault.clone();
		
		try {
			if (arguments.length == 3) {
				vResult.x = Commons.toInt(arguments[0]);
				vResult.y = Commons.toInt(arguments[1]);
				vResult.z = Commons.toInt(arguments[2]);
			} else if (arguments.length == 1) {
				vResult.x = 0;
				vResult.y = 0;
				vResult.z = 0;
			}
		} catch (NumberFormatException e) {
			// ignore
		}
		return vResult;
	}
	
	private Boolean[] lock(final Object[] arguments) {
		if (arguments.length == 1) {
			isLockRequested = Commons.toBool(arguments[0]);
			markDirty();
		}
		return new Boolean[] { isLockRequested };
	}
	
	private Double[] energyFactor(final Object[] arguments) {
		try {
			if (arguments.length >= 1) {
				energyFactor = Commons.clamp(1, WarpDriveConfig.TRANSPORTER_TRANSFER_ENERGY_FACTOR_MAX, Commons.toDouble(arguments[0]));
			}
		} catch (NumberFormatException exception) {
			// ignore
		}
		
		return new Double[] { energyFactor };
	}
	
	private Double[] getLockStrength() {
		return new Double[] { lockStrengthActual };
	}
	
	private Integer[] getEnergyRequired() {
		return new Integer[] { getEnergyRequired(EnumTransporterState.ENERGIZING) };
	}
	
	private Boolean[] energize(final Object[] arguments) {
		if (arguments.length == 1) {
			isEnergizeRequested = Commons.toBool(arguments[0]);
			markDirty();
		}
		return new Boolean[] { isEnergizeRequested };
	}
	
	// OpenComputers callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] beamFrequency(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setBeamFrequency(arguments.checkInteger(0));
		}
		return new Integer[] { beamFrequency };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) {
		return enable(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] source(Context context, Arguments arguments) {
		return source(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] destination(Context context, Arguments arguments) {
		return destination(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] lock(Context context, Arguments arguments) {
		return lock(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] energyFactor(Context context, Arguments arguments) {
		return energyFactor(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getLockStrength(Context context, Arguments arguments) {
		return getLockStrength();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyRequired(Context context, Arguments arguments) {
		return getEnergyRequired();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] energize(Context context, Arguments arguments) {
		return energize(argumentsOCtoCC(arguments));
	}
	
	// ComputerCraft IPeripheral methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "beamFrequency":
			if (arguments.length == 1) {
				setBeamFrequency(Commons.toInt(arguments[0]));
			}
			return new Integer[] { beamFrequency };
		
		case "enable":
			return enable(arguments);
		
		case "source":
			return source(arguments);
		
		case "destination":
			return destination(arguments);
		
		case "lock":
			return lock(arguments);
		
		case "energyFactor":
			return energyFactor(arguments);
		
		case "getLockStrength":
			return getLockStrength();
		
		case "getEnergyRequired":
			return getEnergyRequired();
		
		case "energize":
			return energize(arguments);
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public String toString() {
		return String.format("%s Beam \'%d\' @ %s (%d %d %d)",
		                     getClass().getSimpleName(),
		                     beamFrequency,
		                     worldObj == null ? "~NULL~" : worldObj.provider.getDimensionName(),
		                     xCoord, yCoord, zCoord);
	}
}
