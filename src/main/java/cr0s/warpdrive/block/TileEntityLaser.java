package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.block.forcefield.BlockForceField;
import cr0s.warpdrive.block.forcefield.TileEntityForceField;
import cr0s.warpdrive.block.weapon.BlockLaserCamera;
import cr0s.warpdrive.block.weapon.TileEntityLaserCamera;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.ForceFieldSetup;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.common.Optional;

public class TileEntityLaser extends TileEntityAbstractLaser implements IBeamFrequency {
	
	private int legacyVideoChannel = -1;
	private boolean legacyCheck = !(this instanceof TileEntityLaserCamera);
	
	private float yaw, pitch; // laser direction
	
	protected int beamFrequency = -1;
	private float r, g, b; // beam color (corresponds to frequency)
	
	private boolean isEmitting = false;
	
	private int delayTicks = 0;
	private int energyFromOtherBeams = 0;
	
	private enum ScanResultType {
		IDLE("IDLE"), BLOCK("BLOCK"), NONE("NONE");
		
		public final String name;
		
		ScanResultType(final String name) {
			this.name = name;
		}
	}
	private ScanResultType scanResult_type = ScanResultType.IDLE;
	private VectorI scanResult_position = null;
	private String scanResult_blockUnlocalizedName;
	private int scanResult_blockMetadata = 0;
	private float scanResult_blockResistance = -2;
	
	public TileEntityLaser() {
		super();
		
		peripheralName = "warpdriveLaser";
		addMethods(new String[] {
			"emitBeam",
			"beamFrequency",
			"getScanResult"
		});
		laserMedium_maxCount = WarpDriveConfig.LASER_CANNON_MAX_MEDIUMS_COUNT;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		// Legacy tile entity
		if (legacyCheck) {
			if (worldObj.getBlock(xCoord, yCoord, zCoord) instanceof BlockLaserCamera) {
				try {
					WarpDrive.logger.info("Self-upgrading legacy tile entity " + this);
					final NBTTagCompound nbtOld = new NBTTagCompound();
					writeToNBT(nbtOld);
					final TileEntityLaserCamera newTileEntity = new TileEntityLaserCamera(); // id has changed, we can't directly call createAndLoadEntity
					newTileEntity.readFromNBT(nbtOld);
					newTileEntity.setWorldObj(worldObj);
					newTileEntity.validate();
					invalidate();
					worldObj.removeTileEntity(xCoord, yCoord, zCoord);
					worldObj.setTileEntity(xCoord, yCoord, zCoord, newTileEntity);
					newTileEntity.setVideoChannel(legacyVideoChannel);
				} catch (final Exception exception) {
					exception.printStackTrace();
				}
			}
			legacyCheck = false;
		}
		
		// Frequency is not set
		if (beamFrequency <= 0 || beamFrequency > IBeamFrequency.BEAM_FREQUENCY_MAX) {
			return;
		}
		
		delayTicks++;
		if ( isEmitting
		  && ( (beamFrequency != BEAM_FREQUENCY_SCANNING && delayTicks > WarpDriveConfig.LASER_CANNON_EMIT_FIRE_DELAY_TICKS)
		    || (beamFrequency == BEAM_FREQUENCY_SCANNING && delayTicks > WarpDriveConfig.LASER_CANNON_EMIT_SCAN_DELAY_TICKS))) {
			delayTicks = 0;
			isEmitting = false;
			final int beamEnergy = Math.min(
					laserMedium_consumeUpTo(Integer.MAX_VALUE, false) + MathHelper.floor_double(energyFromOtherBeams * WarpDriveConfig.LASER_CANNON_BOOSTER_BEAM_ENERGY_EFFICIENCY),
					WarpDriveConfig.LASER_CANNON_MAX_LASER_ENERGY);
			emitBeam(beamEnergy);
			energyFromOtherBeams = 0;
			sendEvent("laserSend", beamFrequency, beamEnergy);
		}
	}
	
	public void initiateBeamEmission(final float parYaw, final float parPitch) {
		yaw = parYaw;
		pitch = parPitch;
		delayTicks = 0;
		isEmitting = true;
	}
	
	private void addBeamEnergy(final int amount) {
		if (isEmitting) {
			energyFromOtherBeams += amount;
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(String.format("%s Added boosting energy %d for a total accumulation of %d",
				                                    this, amount, energyFromOtherBeams));
			}
		} else {
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.warn(String.format("%s Ignored boosting energy %d",
				                                    this, amount));
			}
		}
	}
	
	// loosely based on World.rayTraceBlocks/func_147447_a
	// - replaced byte b0 with EnumFacing
	// - inverted 2nd flag
	// - added force field pass through based on beamFrequency
	// - increased max range from 200 to laser limit
	// - code cleanup
	public static MovingObjectPosition rayTraceBlocks(final World world, final Vec3 vSource, final Vec3 vTarget, final int beamFrequency,
	                                                  final boolean checkLiquids, final boolean checkAir, final boolean doReturnMissed) {
		// validate parameters
		if (Double.isNaN(vSource.xCoord) || Double.isNaN(vSource.yCoord) || Double.isNaN(vSource.zCoord)) {
			return null;
		}
		
		if (Double.isNaN(vTarget.xCoord) || Double.isNaN(vTarget.yCoord) || Double.isNaN(vTarget.zCoord)) {
			return null;
		}
		
		// check collision at source
		final int xSource = MathHelper.floor_double(vSource.xCoord);
		final int ySource = MathHelper.floor_double(vSource.yCoord);
		final int zSource = MathHelper.floor_double(vSource.zCoord);
		final Block blockSource = world.getBlock(xSource, ySource, zSource);
		final int metadataSource = world.getBlockMetadata(xSource, ySource, zSource);
		
		if ( (checkAir || blockSource.getCollisionBoundingBoxFromPool(world, xSource, ySource, zSource) != null)
		  && blockSource.canCollideCheck(metadataSource, checkLiquids)) {
			final MovingObjectPosition movingObjectPosition = blockSource.collisionRayTrace(world, xSource, ySource, zSource, vSource, vTarget);
			
			if (movingObjectPosition != null) {
				return movingObjectPosition;
			}
		}
		
		// loop positions along trajectory
		final int xTarget = MathHelper.floor_double(vTarget.xCoord);
		final int yTarget = MathHelper.floor_double(vTarget.yCoord);
		final int zTarget = MathHelper.floor_double(vTarget.zCoord);
		
		final Vec3 vCurrent = Vec3.createVectorHelper(vSource.xCoord, vSource.yCoord, vSource.zCoord);
		int xCurrent = xSource;
		int yCurrent = ySource;
		int zCurrent = zSource;
		MovingObjectPosition movingObjectPositionMissed = null;
		
		int countLoop = WarpDriveConfig.LASER_CANNON_RANGE_MAX * 2;
		while (countLoop-- >= 0) {
			// sanity check
			if (Double.isNaN(vCurrent.xCoord) || Double.isNaN(vCurrent.yCoord) || Double.isNaN(vCurrent.zCoord)) {
				WarpDrive.logger.error(String.format("Critical error while raytracing blocks from %s to %s in %s",
				                                     vSource, vTarget, world.provider.getDimensionName()));
				return null;
			}
			
			// check arrival
			if (xCurrent == xTarget && yCurrent == yTarget && zCurrent == zTarget) {
				return doReturnMissed ? movingObjectPositionMissed : null;
			}
			
			// propose 1 block step along each axis
			boolean hasOffsetX = true;
			boolean hasOffsetY = true;
			boolean hasOffsetZ = true;
			double xProposed = 999.0D;
			double yProposed = 999.0D;
			double zProposed = 999.0D;
			
			if (xTarget > xCurrent) {
				xProposed = xCurrent + 1.0D;
			} else if (xTarget < xCurrent) {
				xProposed = xCurrent + 0.0D;
			} else {
				hasOffsetX = false;
			}
			
			if (yTarget > yCurrent) {
				yProposed = yCurrent + 1.0D;
			} else if (yTarget < yCurrent) {
				yProposed = yCurrent + 0.0D;
			} else {
				hasOffsetY = false;
			}
			
			if (zTarget > zCurrent) {
				zProposed = zCurrent + 1.0D;
			} else if (zTarget < zCurrent) {
				zProposed = zCurrent + 0.0D;
			} else {
				hasOffsetZ = false;
			}
			
			// compute normalized movement
			double xDeltaNormalized = 999.0D;
			double yDeltaNormalized = 999.0D;
			double zDeltaNormalized = 999.0D;
			final double xDeltaToTarget = vTarget.xCoord - vCurrent.xCoord;
			final double yDeltaToTarget = vTarget.yCoord - vCurrent.yCoord;
			final double zDeltaToTarget = vTarget.zCoord - vCurrent.zCoord;
			
			if (hasOffsetX) {
				xDeltaNormalized = (xProposed - vCurrent.xCoord) / xDeltaToTarget;
			}
			
			if (hasOffsetY) {
				yDeltaNormalized = (yProposed - vCurrent.yCoord) / yDeltaToTarget;
			}
			
			if (hasOffsetZ) {
				zDeltaNormalized = (zProposed - vCurrent.zCoord) / zDeltaToTarget;
			}
			
			// move along shortest axis
			final EnumFacing facing;
			if (xDeltaNormalized < yDeltaNormalized && xDeltaNormalized < zDeltaNormalized) {
				if (xTarget > xCurrent) {
					facing = EnumFacing.WEST;
				} else {
					facing = EnumFacing.EAST;
				}
				
				vCurrent.xCoord = xProposed;
				vCurrent.yCoord += yDeltaToTarget * xDeltaNormalized;
				vCurrent.zCoord += zDeltaToTarget * xDeltaNormalized;
			} else if (yDeltaNormalized < zDeltaNormalized) {
				if (yTarget > yCurrent) {
					facing = EnumFacing.UP;
				} else {
					facing = EnumFacing.DOWN;
				}
				
				vCurrent.xCoord += xDeltaToTarget * yDeltaNormalized;
				vCurrent.yCoord = yProposed;
				vCurrent.zCoord += zDeltaToTarget * yDeltaNormalized;
			} else {
				if (zTarget > zCurrent) {
					facing = EnumFacing.SOUTH;
				} else {
					facing = EnumFacing.NORTH;
				}
				
				vCurrent.xCoord += xDeltaToTarget * zDeltaNormalized;
				vCurrent.yCoord += yDeltaToTarget * zDeltaNormalized;
				vCurrent.zCoord = zProposed;
			}
			
			// round to block position
			xCurrent = MathHelper.floor_double(vCurrent.xCoord);
			if (facing == EnumFacing.EAST) {
				xCurrent--;
			}
			
			yCurrent = MathHelper.floor_double(vCurrent.yCoord);
			if (facing == EnumFacing.DOWN) {
				yCurrent--;
			}
			
			zCurrent = MathHelper.floor_double(vCurrent.zCoord);
			if (facing == EnumFacing.NORTH) {
				zCurrent--;
			}
			
			// get current block
			final Block blockCurrent = world.getBlock(xCurrent, yCurrent, zCurrent);
			final int metadataCurrent = world.getBlockMetadata(xCurrent, yCurrent, zCurrent);
			
			// allow passing through force fields with same beam frequency
			if (blockCurrent instanceof BlockForceField) {
				final TileEntity tileEntity = world.getTileEntity(xCurrent, yCurrent, zCurrent);
				if (tileEntity instanceof TileEntityForceField) {
					final ForceFieldSetup forceFieldSetup = ((TileEntityForceField) tileEntity).getForceFieldSetup();
					if (forceFieldSetup == null) {
						// projector not loaded yet, consider it jammed by default
						WarpDrive.logger.warn(String.format("Laser beam stopped by non-loaded force field projector at %s", tileEntity));
					} else {
						if (forceFieldSetup.beamFrequency == beamFrequency) {// pass-through force field
							if (WarpDriveConfig.LOGGING_WEAPON) {
								WarpDrive.logger.info(String.format("Laser beam passing through force field %s", tileEntity));
							}
							continue;
						}
					}
				}
			}
			
			if (checkAir || blockCurrent.getCollisionBoundingBoxFromPool(world, xCurrent, yCurrent, zCurrent) != null) {
				if (blockCurrent.canCollideCheck(metadataCurrent, checkLiquids)) {
					final MovingObjectPosition movingObjectPosition = blockCurrent.collisionRayTrace(world, xCurrent, yCurrent, zCurrent, vCurrent, vTarget);
					if (movingObjectPosition != null) {
						return movingObjectPosition;
					}
				} else {
					movingObjectPositionMissed = new MovingObjectPosition(xCurrent, yCurrent, zCurrent, facing.ordinal(), vCurrent, false);
				}
			}
		}
		
		return doReturnMissed ? movingObjectPositionMissed : null;
	}
	
	private void emitBeam(final int beamEnergy) {
		int energy = beamEnergy;
		
		final int beamLengthBlocks = Commons.clamp(0, WarpDriveConfig.LASER_CANNON_RANGE_MAX, energy / 200);
		
		if (energy == 0 || beamFrequency > 65000 || beamFrequency <= 0) {
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(this + " Beam canceled (energy " + energy + " over " + beamLengthBlocks + " blocks, beamFrequency " + beamFrequency + ")");
			}
			return;
		}
		
		final float yawZ = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		final float yawX = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		final float pitchHorizontal = -MathHelper.cos(-pitch * 0.017453292F);
		final float pitchVertical = MathHelper.sin(-pitch * 0.017453292F);
		final float directionX = yawX * pitchHorizontal;
		final float directionZ = yawZ * pitchHorizontal;
		final Vector3 vDirection = new Vector3(directionX, pitchVertical, directionZ);
		final Vector3 vSource = new Vector3(this).translate(0.5D).translate(vDirection);
		final Vector3 vReachPoint = vSource.clone().translateFactor(vDirection, beamLengthBlocks);
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info(this + " Energy " + energy + " over " + beamLengthBlocks + " blocks"
					+ ", Orientation " + yaw + " " + pitch
					+ ", Direction " + vDirection
					+ ", From " + vSource + " to " + vReachPoint);
		}
		
		playSoundCorrespondsEnergy(energy);
		
		// This is a scanning beam, do not deal damage to block nor entity
		if (beamFrequency == BEAM_FREQUENCY_SCANNING) {
			final MovingObjectPosition mopResult = rayTraceBlocks(worldObj, vSource.toVec3(), vReachPoint.toVec3(), beamFrequency,
			                                                      false, true, false);
			
			scanResult_blockUnlocalizedName = null;
			scanResult_blockMetadata = 0;
			scanResult_blockResistance = -2;
			if (mopResult != null) {
				scanResult_type = ScanResultType.BLOCK;
				scanResult_position = new VectorI(mopResult.blockX, mopResult.blockY, mopResult.blockZ);
				final Block block = worldObj.getBlock(scanResult_position.x, scanResult_position.y, scanResult_position.z);
				if (block != null) {
					scanResult_blockUnlocalizedName = block.getUnlocalizedName();
					scanResult_blockMetadata = worldObj.getBlockMetadata(scanResult_position.x, scanResult_position.y, scanResult_position.z);
					scanResult_blockResistance = block.getExplosionResistance(null);
				}
				PacketHandler.sendBeamPacket(worldObj, vSource, new Vector3(mopResult.hitVec), r, g, b, 50, energy, 200);
			} else {
				scanResult_type = ScanResultType.NONE;
				scanResult_position = new VectorI(vReachPoint.intX(), vReachPoint.intY(), vReachPoint.intZ());
				PacketHandler.sendBeamPacket(worldObj, vSource, vReachPoint, r, g, b, 50, energy, 200);
			}
			
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info("Scan result type " + scanResult_type.name
					+ " at " + scanResult_position.x + " " + scanResult_position.y + " " + scanResult_position.z
					+ " block " + scanResult_blockUnlocalizedName + " " + scanResult_blockMetadata + " resistance " + scanResult_blockResistance);
			}
			
			sendEvent("laserScanning",
					scanResult_type.name, scanResult_position.x, scanResult_position.y, scanResult_position.z,
					scanResult_blockUnlocalizedName, scanResult_blockMetadata, scanResult_blockResistance);
			return;
		}
		
		// get colliding entities
		final TreeMap<Double, MovingObjectPosition> entityHits = raytraceEntities(vSource.clone(), vDirection.clone(), beamLengthBlocks);
		
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info("Entity hits are (" + ((entityHits == null) ? 0 : entityHits.size()) + ") " + entityHits);
		}
		
		Vector3 vHitPoint = vReachPoint.clone();
		double distanceTravelled = 0.0D; // distance traveled from beam sender to previous hit if there were any
		for (int passedBlocks = 0; passedBlocks < beamLengthBlocks; passedBlocks++) {
			// Get next block hit
			final MovingObjectPosition blockHit = rayTraceBlocks(worldObj, vSource.toVec3(), vReachPoint.toVec3(), beamFrequency,
			                                                     false, true, false);
			double blockHitDistance = beamLengthBlocks + 0.1D;
			if (blockHit != null) {
				blockHitDistance = blockHit.hitVec.distanceTo(vSource.toVec3());
			}
			
			// Apply effect to entities
			if (entityHits != null) {
				for (final Entry<Double, MovingObjectPosition> entityHitEntry : entityHits.entrySet()) {
					final double entityHitDistance = entityHitEntry.getKey();
					// ignore entities behind walls
					if (entityHitDistance >= blockHitDistance) {
						break;
					}
					
					// only hits entities with health or whitelisted
					final MovingObjectPosition mopEntity = entityHitEntry.getValue();
					if (mopEntity == null) {
						continue;
					}
					EntityLivingBase entity = null;
					if (mopEntity.entityHit instanceof EntityLivingBase) {
						entity = (EntityLivingBase) mopEntity.entityHit;
						if (WarpDriveConfig.LOGGING_WEAPON) {
							WarpDrive.logger.info("Entity is a valid target (living) " + entity);
						}
					} else {
						final String entityId = EntityList.getEntityString(mopEntity.entityHit);
						if (!Dictionary.ENTITIES_NONLIVINGTARGET.contains(entityId)) {
							if (WarpDriveConfig.LOGGING_WEAPON) {
								WarpDrive.logger.info("Entity is an invalid target (non-living " + entityId + ") " + mopEntity.entityHit);
							}
							// remove entity from hit list
							entityHits.put(entityHitDistance, null);
							continue;
						}
						if (WarpDriveConfig.LOGGING_WEAPON) {
							WarpDrive.logger.info("Entity is a valid target (non-living " + entityId + ") " + mopEntity.entityHit);
						}
					}
					
					// Consume energy
					energy *= getTransmittance(entityHitDistance - distanceTravelled);
					energy -= WarpDriveConfig.LASER_CANNON_ENTITY_HIT_ENERGY;
					distanceTravelled = entityHitDistance;
					vHitPoint = new Vector3(mopEntity.hitVec);
					if (energy <= 0) {
						break;
					}
					
					// apply effects
					mopEntity.entityHit.setFire(WarpDriveConfig.LASER_CANNON_ENTITY_HIT_SET_ON_FIRE_SECONDS);
					if (entity != null) {
						final float damage = (float) Commons.clamp(0.0D, WarpDriveConfig.LASER_CANNON_ENTITY_HIT_MAX_DAMAGE,
								WarpDriveConfig.LASER_CANNON_ENTITY_HIT_BASE_DAMAGE + energy / WarpDriveConfig.LASER_CANNON_ENTITY_HIT_ENERGY_PER_DAMAGE);
						entity.attackEntityFrom(DamageSource.inFire, damage);
					} else {
						mopEntity.entityHit.setDead();
					}
					
					if (energy > WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_ENERGY_THRESHOLD) {
						final float strength = (float) Commons.clamp(0.0D, WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_MAX_STRENGTH,
							  WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_BASE_STRENGTH + energy / WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_ENERGY_PER_STRENGTH); 
						worldObj.newExplosion(null, mopEntity.entityHit.posX, mopEntity.entityHit.posY, mopEntity.entityHit.posZ, strength, true, true);
					}
					
					// remove entity from hit list
					entityHits.put(entityHitDistance, null);
				}
				if (energy <= 0) {
					break;
				}
			}
			
			// Laser went too far or no block hit
			if (blockHitDistance >= beamLengthBlocks || blockHit == null) {
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("No more blocks to hit or too far: blockHitDistance is " + blockHitDistance + ", blockHit is " + blockHit);
				}
				vHitPoint = vReachPoint;
				break;
			}
			
			final Block block = worldObj.getBlock(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
			// int blockMeta = worldObj.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
			// get hardness and blast resistance
			float hardness = -2.0F;
			if (WarpDrive.fieldBlockHardness != null) {
				// WarpDrive.fieldBlockHardness.setAccessible(true);
				try {
					hardness = (float) WarpDrive.fieldBlockHardness.get(block);
				} catch (final IllegalArgumentException | IllegalAccessException exception) {
					exception.printStackTrace();
					WarpDrive.logger.error("Unable to access block hardness value of " + block);
				}
			}
			if (block instanceof IDamageReceiver) {
				hardness = ((IDamageReceiver) block).getBlockHardness(worldObj, blockHit.blockX, blockHit.blockY, blockHit.blockZ,
					WarpDrive.damageLaser, beamFrequency, vDirection, energy);
			}				
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(String.format("Block collision found at (%d %d %d) with block %s of hardness %.2f",
				                                    blockHit.blockX, blockHit.blockY, blockHit.blockZ,
				                                    block.getUnlocalizedName(), hardness));
			}
			
			// check area protection
			if (isBlockBreakCanceled(null, worldObj, blockHit.blockX, blockHit.blockY, blockHit.blockZ)) {
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("Laser weapon cancelled at (" + blockHit.blockX + " " + blockHit.blockY + " " + blockHit.blockZ + ")");
				}
				vHitPoint = new Vector3(blockHit.hitVec);
				break;
			}
			
			// Boost a laser if it uses same beam frequency
			if (block.isAssociatedBlock(WarpDrive.blockLaser) || block.isAssociatedBlock(WarpDrive.blockLaserCamera)) {
				final TileEntityLaser tileEntityLaser = (TileEntityLaser) worldObj.getTileEntity(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
				if (tileEntityLaser != null && tileEntityLaser.getBeamFrequency() == beamFrequency) {
					tileEntityLaser.addBeamEnergy(energy);
					vHitPoint = new Vector3(blockHit.hitVec);
					break;
				}
			}
			
			// explode on unbreakable blocks
			if (hardness < 0.0F) {
				final float strength = (float) Commons.clamp(0.0D, WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_MAX_STRENGTH,
					WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_BASE_STRENGTH + energy / WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_ENERGY_PER_STRENGTH);
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("Explosion triggered with strength " + strength);
				}
				worldObj.newExplosion(null, blockHit.blockX, blockHit.blockY, blockHit.blockZ, strength, true, true);
				vHitPoint = new Vector3(blockHit.hitVec);
				break;
			}
			
			// Compute parameters
			final int energyCost = Commons.clamp(WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ENERGY_MIN, WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ENERGY_MAX,
					Math.round(hardness * WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ENERGY_PER_BLOCK_HARDNESS));
			final double absorptionChance = Commons.clamp(0.0D, WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ABSORPTION_MAX,
					hardness * WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ABSORPTION_PER_BLOCK_HARDNESS);
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(String.format("Block energy cost is %d with %.1f %% of absorption",
				                                    energyCost, absorptionChance * 100.0D));
			}
			
			// apply environmental absorption
			energy *= getTransmittance(blockHitDistance - distanceTravelled);
			
			do {
				// Consume energy
				energy -= energyCost;
				distanceTravelled = blockHitDistance;
				vHitPoint = new Vector3(blockHit.hitVec);
				if (energy <= 0) {
					if (WarpDriveConfig.LOGGING_WEAPON) {
						WarpDrive.logger.info("Beam died out of energy");
					}
					break;
				}
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info(String.format("Beam energy down to %d", energy));
				}
				
				// apply chance of absorption
				if (worldObj.rand.nextDouble() > absorptionChance) {
					break;
				}
			} while (true);
			if (energy <= 0) {
				break;
			}
			
			// add 'explode' effect with the beam color
			// worldObj.newExplosion(null, blockHit.blockX, blockHit.blockY, blockHit.blockZ, 4, true, true);
			final Vector3 origin = new Vector3(
				blockHit.blockX -0.3D * vDirection.x + worldObj.rand.nextFloat() - worldObj.rand.nextFloat(),
				blockHit.blockY -0.3D * vDirection.y + worldObj.rand.nextFloat() - worldObj.rand.nextFloat(),
				blockHit.blockZ -0.3D * vDirection.z + worldObj.rand.nextFloat() - worldObj.rand.nextFloat());
			final Vector3 direction = new Vector3(
				-0.2D * vDirection.x + 0.05 * (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()),
				-0.2D * vDirection.y + 0.05 * (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()),
				-0.2D * vDirection.z + 0.05 * (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()));
			PacketHandler.sendSpawnParticlePacket(worldObj, "explode", (byte) 5, origin, direction, r, g, b, r, g, b, 96);
			
			// apply custom damages
			if (block instanceof IDamageReceiver) {
				energy = ((IDamageReceiver) block).applyDamage(worldObj, blockHit.blockX, blockHit.blockY, blockHit.blockZ,
				                                               WarpDrive.damageLaser, beamFrequency, vDirection, energy);
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("IDamageReceiver damage applied, remaining energy is " + energy);
				}
				if (energy <= 0) {
					break;
				}
			}
			
			if (hardness >= WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_HARDNESS_THRESHOLD) {
				final float strength = (float) Commons.clamp(0.0D, WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_MAX_STRENGTH,
						WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_BASE_STRENGTH + energy / WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_ENERGY_PER_STRENGTH); 
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("Explosion triggered with strength " + strength);
				}
				worldObj.newExplosion(null, blockHit.blockX, blockHit.blockY, blockHit.blockZ, strength, true, true);
				worldObj.setBlock(blockHit.blockX, blockHit.blockY, blockHit.blockZ, (worldObj.rand.nextBoolean()) ? Blocks.fire : Blocks.air);
			} else {
				worldObj.setBlockToAir(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
			}
		}
		
		PacketHandler.sendBeamPacket(worldObj, new Vector3(this).translate(0.5D).translate(vDirection.scale(0.5D)), vHitPoint, r, g, b, 50, energy,
				beamLengthBlocks);
	}
	
	private double getTransmittance(final double distance) {
		if (distance <= 0) {
			return 1.0D;
		}
		final double attenuation;
		if (CelestialObjectManager.hasAtmosphere(worldObj, xCoord, zCoord)) {
			attenuation = WarpDriveConfig.LASER_CANNON_ENERGY_ATTENUATION_PER_AIR_BLOCK;
		} else {
			attenuation = WarpDriveConfig.LASER_CANNON_ENERGY_ATTENUATION_PER_VOID_BLOCK;
		}
		final double transmittance = Math.exp(- attenuation * distance);
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info("Transmittance over " + distance + " blocks is " + transmittance);
		}
		return transmittance;
	}
	
	private TreeMap<Double, MovingObjectPosition> raytraceEntities(final Vector3 vSource, final Vector3 vDirection, final double reachDistance) {
		final double raytraceTolerance = 2.0D;
		
		// Pre-computation
		final Vec3 vec3Source = vSource.toVec3();
		final Vec3 vec3Target = Vec3.createVectorHelper(
				vec3Source.xCoord + vDirection.x * reachDistance,
				vec3Source.yCoord + vDirection.y * reachDistance,
				vec3Source.zCoord + vDirection.z * reachDistance);
		
		// Get all possible entities
		final AxisAlignedBB boxToScan = AxisAlignedBB.getBoundingBox(
				Math.min(xCoord - raytraceTolerance, vec3Target.xCoord - raytraceTolerance),
				Math.min(yCoord - raytraceTolerance, vec3Target.yCoord - raytraceTolerance),
				Math.min(zCoord - raytraceTolerance, vec3Target.zCoord - raytraceTolerance),
				Math.max(xCoord + raytraceTolerance, vec3Target.xCoord + raytraceTolerance),
				Math.max(yCoord + raytraceTolerance, vec3Target.yCoord + raytraceTolerance),
				Math.max(zCoord + raytraceTolerance, vec3Target.zCoord + raytraceTolerance));
		@SuppressWarnings("unchecked")
		final List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(null, boxToScan);
		
		if (entities == null || entities.isEmpty()) {
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info("No entity on trajectory (box)");
			}
			return null;
		}
		
		// Pick the closest one on trajectory
		final HashMap<Double, MovingObjectPosition> entityHits = new HashMap<>(entities.size());
		for (final Entity entity : entities) {
			if (entity != null && entity.canBeCollidedWith() && entity.boundingBox != null) {
				final double border = entity.getCollisionBorderSize();
				final AxisAlignedBB aabbEntity = entity.boundingBox.expand(border, border, border);
				final MovingObjectPosition hitMOP = aabbEntity.calculateIntercept(vec3Source, vec3Target);
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("Checking " + entity + " boundingBox " + entity.boundingBox + " border " + border + " aabbEntity " + aabbEntity + " hitMOP " + hitMOP);
				}
				if (hitMOP != null) {
					final MovingObjectPosition mopEntity = new MovingObjectPosition(entity);
					mopEntity.hitVec = hitMOP.hitVec;
					double distance = vec3Source.distanceTo(hitMOP.hitVec);
					if (entityHits.containsKey(distance)) {
						distance += worldObj.rand.nextDouble() / 10.0D;
					}
					entityHits.put(distance, mopEntity);
				}
			}
		}
		
		if (entityHits.isEmpty()) {
			return null;
		}
		
		return new TreeMap<>(entityHits);
	}
	
	@Override
	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	@Override
	public void setBeamFrequency(final int parBeamFrequency) {
		if (beamFrequency != parBeamFrequency && (parBeamFrequency <= BEAM_FREQUENCY_MAX) && (parBeamFrequency > BEAM_FREQUENCY_MIN)) {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Beam frequency set from " + beamFrequency + " to " + parBeamFrequency);
			}
			beamFrequency = parBeamFrequency;
		}
		final Vector3 vRGB = IBeamFrequency.getBeamColor(beamFrequency);
		r = (float) vRGB.x;
		g = (float) vRGB.y;
		b = (float) vRGB.z;
	}
	
	private void playSoundCorrespondsEnergy(final int energy) {
		if (energy <= 500000) {
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
		} else if (energy > 500000 && energy <= 1000000) {
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:midlaser", 4F, 1F);
		} else if (energy > 1000000) {
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
		}
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		setBeamFrequency(tagCompound.getInteger(BEAM_FREQUENCY_TAG));
		legacyVideoChannel = tagCompound.getInteger("cameraFrequency") + tagCompound.getInteger(IVideoChannel.VIDEO_CHANNEL_TAG);
	}
	
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger(BEAM_FREQUENCY_TAG, beamFrequency);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] emitBeam(final Context context, final Arguments arguments) {
		return emitBeam(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] beamFrequency(final Context context, final Arguments arguments) {
		if (arguments.count() == 1) {
			setBeamFrequency(arguments.checkInteger(0));
		}
		return new Integer[] { beamFrequency };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getScanResult(final Context context, final Arguments arguments) {
		return getScanResult();
	}
	
	private Object[] emitBeam(final Object[] arguments) {
		try {
			final float newYaw, newPitch;
			if (arguments.length == 2) {
				newYaw = Commons.toFloat(arguments[0]);
				newPitch = Commons.toFloat(arguments[1]);
				initiateBeamEmission(newYaw, newPitch);
			} else if (arguments.length == 3) {
				final float deltaX = -Commons.toFloat(arguments[0]);
				final float deltaY = -Commons.toFloat(arguments[1]);
				final float deltaZ = Commons.toFloat(arguments[2]);
				final double horizontalDistance = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
				//noinspection SuspiciousNameCombination
				newYaw = (float) (Math.atan2(deltaX, deltaZ) * 180.0D / Math.PI);
				newPitch = (float) (Math.atan2(deltaY, horizontalDistance) * 180.0D / Math.PI);
				initiateBeamEmission(newYaw, newPitch);
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
			return new Object[] { false };
		}
		return new Object[] { true };
	}
	
	private Object[] getScanResult() {
		if (scanResult_type != ScanResultType.IDLE) {
			try {
				final Object[] info = { scanResult_type.name,
						scanResult_position.x, scanResult_position.y, scanResult_position.z,
						scanResult_blockUnlocalizedName, scanResult_blockMetadata, scanResult_blockResistance };
				scanResult_type = ScanResultType.IDLE;
				scanResult_position = null;
				scanResult_blockUnlocalizedName = null;
				scanResult_blockMetadata = 0;
				scanResult_blockResistance = -2;
				return info;
			} catch (final Exception exception) {
				exception.printStackTrace();
				return new Object[] { COMPUTER_ERROR_TAG, 0, 0, 0, null, 0, -3 };
			}
		} else {
			return new Object[] { scanResult_type.name, 0, 0, 0, null, 0, -1 };
		}
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method, final Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "emitBeam":  // emitBeam(yaw, pitch) or emitBeam(deltaX, deltaY, deltaZ)
			return emitBeam(arguments);
			
		case "position":
			return new Integer[] { xCoord, yCoord, zCoord };
			
		case "beamFrequency":
			if (arguments.length == 1 && arguments[0] != null) {
				setBeamFrequency(Commons.toInt(arguments[0]));
			}
			return new Integer[] { beamFrequency };
			
		case "getScanResult":
			return getScanResult();
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