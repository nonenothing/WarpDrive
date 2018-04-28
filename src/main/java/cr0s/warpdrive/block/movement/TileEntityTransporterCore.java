package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IItemTransporterBeacon;
import cr0s.warpdrive.api.IStarMapRegistryTileEntity;
import cr0s.warpdrive.api.computer.ITransporterBeacon;
import cr0s.warpdrive.api.computer.ITransporterCore;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.block.forcefield.BlockForceField;
import cr0s.warpdrive.block.forcefield.TileEntityForceField;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.data.EnumTransporterState;
import cr0s.warpdrive.data.ForceFieldRegistry;
import cr0s.warpdrive.data.ForceFieldSetup;
import cr0s.warpdrive.data.GlobalPosition;
import cr0s.warpdrive.data.MovingEntity;
import cr0s.warpdrive.data.StarMapRegistry;
import cr0s.warpdrive.data.EnumStarMapEntryType;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityTransporterCore extends TileEntityAbstractEnergy implements ITransporterCore, IBeamFrequency, IStarMapRegistryTileEntity {
	
	// persistent properties
	private UUID uuid = null;
	private ArrayList<VectorI> vLocalScanners = null;
	private int beamFrequency = -1;
	private String transporterName = "";
	private boolean isEnabled = true;
	private boolean isLockRequested = false;
	private boolean isEnergizeRequested = false;
	private Object remoteLocationRequested = null;  // can be VectorI() coordinates, or transporter UUID, or player name
	private double energyFactor = 1.0D;
	private double lockStrengthActual = 0.0D;
	private int tickCooldown = 0;
	private EnumTransporterState transporterState = EnumTransporterState.DISABLED;
	
	// computed properties
	private ArrayList<VectorI> vLocalContainments = null;
	private AxisAlignedBB aabbLocalScanners = null;
	private boolean isBlockUpdated = false;
	private int tickUpdateRegistry = 0;
	private int tickUpdateParameters = 0;
	private boolean isConnected = false;
	private GlobalPosition globalPositionBeacon = null;
	private double energyCostForAcquiring = 0.0D;
	private double energyCostForEnergizing = 0.0D;
	private double lockStrengthOptimal = -1.0D;
	private double lockStrengthSpeed = 0.0D;
	private boolean isJammed = false;
	private String reasonJammed = "";
	private GlobalPosition globalPositionLocal = null;
	private GlobalPosition globalPositionRemote = null;
	private ArrayList<VectorI> vRemoteScanners = null;
	private HashMap<Integer, MovingEntity> movingEntitiesLocal = new HashMap<>(8);
	private HashMap<Integer, MovingEntity> movingEntitiesRemote = new HashMap<>(8);
	private int tickEnergizing = 0;
	
	public TileEntityTransporterCore() {
		super();
		
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
		
		peripheralName = "warpdriveTransporterCore";
		addMethods(new String[] {
			"beamFrequency",
			"transporterName",
			"enable",
			"state",
			"remoteLocation",
			"lock",
			"energyFactor",
			"getLockStrength",
			"getEnergyRequired",
			"energize"
		});
		CC_scripts = Collections.singletonList("startup");
		
		setUpgradeMaxCount(EnumComponentType.ENDER_CRYSTAL, WarpDriveConfig.TRANSPORTER_RANGE_UPGRADE_MAX_QUANTITY);
		setUpgradeMaxCount(EnumComponentType.CAPACITIVE_CRYSTAL, WarpDriveConfig.TRANSPORTER_ENERGY_STORED_UPGRADE_MAX_QUANTITY);
		setUpgradeMaxCount(EnumComponentType.EMERALD_CRYSTAL, WarpDriveConfig.TRANSPORTER_LOCKING_UPGRADE_MAX_QUANTITY);
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		tickUpdateParameters = 0;
		globalPositionLocal = new GlobalPosition(this);
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
					reasonJammed = "Insufficient energy for operation";
					transporterState = EnumTransporterState.IDLE;
					tickCooldown = Math.max(tickCooldown, WarpDriveConfig.TRANSPORTER_JAMMED_COOLDOWN_TICKS);
				}
			} else {
				isPowered = true;
			}
			
			
			// lock strength is capped at optimal, increasing when powered, decaying otherwise
			// final double lockStrengthPrevious = lockStrengthActual;
			if ( isPowered
			  && ( transporterState == EnumTransporterState.ACQUIRING
			    || transporterState == EnumTransporterState.ENERGIZING ) ) {
				// a slight overshoot is added to force convergence
				final double overshoot = 0.01D;
				lockStrengthActual = Math.min(lockStrengthOptimal,
				                              lockStrengthActual + lockStrengthSpeed * (lockStrengthOptimal - lockStrengthActual + overshoot));
			} else {
				lockStrengthActual = Math.max(0.0D, lockStrengthActual * WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_FACTOR_PER_TICK);
			}
			// WarpDrive.logger.info(String.format("Transporter strength %.5f -> %.5f", lockStrengthPrevious, lockStrengthActual));
		}
		
		// periodically update starmap registry & scanners location
		if (isBlockUpdated) {
			tickUpdateRegistry = Math.min(10, tickUpdateRegistry);
		}
		tickUpdateRegistry--;
		if (tickUpdateRegistry <= 0) {
			tickUpdateRegistry = 20 * WarpDriveConfig.STARMAP_REGISTRY_UPDATE_INTERVAL_SECONDS;
			isBlockUpdated = false;
			
			updateScanners();
			
			if (uuid == null || (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0)) {
				uuid = UUID.randomUUID();
			}
			// recover registration, shouldn't be needed, in theory...
			WarpDrive.starMap.updateInRegistry(this);
		}
		
		// state feedback
		updateMetadata(!isConnected ? 0 : !isEnabled ? 1 : !isPowered ? 2 : 3);
		if (isConnected && isEnabled) {
			if (isLockRequested && isJammed) {
				PacketHandler.sendSpawnParticlePacket(worldObj, "jammed", (byte) 5, new Vector3(this).translate(0.5F),
						new Vector3(0.0D, 0.0D, 0.0D),
						1.0F, 1.0F, 1.0F,
						1.0F, 1.0F, 1.0F,
						32);
			}
			if ( lockStrengthActual > 0.01F
			  || (transporterState == EnumTransporterState.ENERGIZING && tickEnergizing > 0)
			  || tickCooldown > 0 ) {
				PacketHandler.sendTransporterEffectPacket(worldObj, globalPositionLocal, globalPositionRemote, lockStrengthActual,
				                                          movingEntitiesLocal.values(), movingEntitiesRemote.values(),
				                                          tickEnergizing, tickCooldown, 64);
			}
		}
		
		// periodically update parameters from main thread
		tickUpdateParameters--;
		if (tickUpdateParameters <= 0) {
			tickUpdateParameters = WarpDriveConfig.TRANSPORTER_SETUP_UPDATE_PARAMETERS_TICKS;
			updateParameters();
		}
		
		// execute state transitions
		switch (transporterState) {
		case DISABLED:
			isLockRequested = false;
			isEnergizeRequested = false;
			lockStrengthActual = 0.0D;
			if (isConnected && isEnabled) {
				transporterState = EnumTransporterState.IDLE;
			}
			break;
		
		case IDLE:
			if ( isLockRequested
			  && tickCooldown == 0 ) {
				// force parameters validation for next tick
				tickUpdateParameters = 0;
				transporterState = EnumTransporterState.ACQUIRING;
			}
			break;
		
		case ACQUIRING:
			if (!isLockRequested) {
				transporterState = EnumTransporterState.IDLE;
				
			} else if (isJammed) {// (jammed while acquiring)
				tickCooldown += WarpDriveConfig.TRANSPORTER_JAMMED_COOLDOWN_TICKS;
				transporterState = EnumTransporterState.IDLE;
				
			} else if (tickCooldown == 0) {
				if ( globalPositionBeacon != null
				  && !isEnergizeRequested
				  && lockStrengthActual >= 0.85D ) {// automatic energizing triggered by beacon
					isEnergizeRequested = true;
					
				} else if (isEnergizeRequested) {
					// force parameters validation for next tick
					tickUpdateParameters = 0;
					
					tickEnergizing = WarpDriveConfig.TRANSPORTER_ENERGIZING_CHARGING_TICKS;
					transporterState = EnumTransporterState.ENERGIZING;
				}
			}
			break;
		
		case ENERGIZING:
			if (!isLockRequested) {
				transporterState = EnumTransporterState.IDLE;
				
			} else if (!isEnergizeRequested) {
				transporterState = EnumTransporterState.ACQUIRING;
				
			} else if (isJammed) {// (jammed while energizing)
				tickCooldown += WarpDriveConfig.TRANSPORTER_JAMMED_COOLDOWN_TICKS;
				transporterState = EnumTransporterState.IDLE;
				
			} else if (tickCooldown <= 0) {// (not cooling down)
				state_energizing();
			}
			break;
		
		default:
			transporterState = EnumTransporterState.DISABLED;
			break;
		}
	}
	
	@Override
	public void invalidate() {
		if (!worldObj.isRemote) {
			rebootTransporter();
			WarpDrive.starMap.removeFromRegistry(this);
		}
		super.invalidate();
	}
	
	private void rebootTransporter() {
		// switch connected scanners to 'offline'
		if (vLocalScanners != null) {
			for (final VectorI vScanner : vLocalScanners) {
				final Block block = worldObj.getBlock(vScanner.x, vScanner.y, vScanner.z);
				if (block == WarpDrive.blockTransporterScanner) {
					worldObj.setBlockMetadataWithNotify(vScanner.x, vScanner.y, vScanner.z, 0, 2);
				}
			}
		}
	}
	
	private void state_energizing() {
		// get entities
		final EntityValues entityValues = updateEntitiesToEnergize();
		
		// post event on first tick
		if (tickEnergizing == WarpDriveConfig.TRANSPORTER_ENERGIZING_CHARGING_TICKS) {
			sendEvent("transporterEnergizing", entityValues.count);
		}
		
		// cancel if not entity was found
		if (entityValues.count == 0) {
			// cancel transfer, cooldown, don't loose strength
			isEnergizeRequested = false;
			tickCooldown += WarpDriveConfig.TRANSPORTER_ENERGIZING_COOLDOWN_TICKS;
			transporterState = EnumTransporterState.ACQUIRING;
			return;
		}
		
		// warm-up
		if (tickEnergizing > 0) {
			tickEnergizing--;
			return;
		}
		
		// transfer at final tick
		if ( vRemoteScanners == null
		  || vRemoteScanners.isEmpty() ) {
			energizeEntities(lockStrengthActual, movingEntitiesLocal, worldObj, globalPositionRemote.getVectorI());
		} else {
			energizeEntities(lockStrengthActual, movingEntitiesLocal, worldObj, vRemoteScanners);
		}
		energizeEntities(lockStrengthActual, movingEntitiesRemote, worldObj, vLocalScanners);
		
		// clear entities, cancel transfer, cooldown, loose a bit of strength
		isEnergizeRequested = false;
		tickUpdateParameters = 0;
		tickCooldown += WarpDriveConfig.TRANSPORTER_ENERGIZING_COOLDOWN_TICKS;
		lockStrengthActual = Math.max(0.0D, lockStrengthActual - WarpDriveConfig.TRANSPORTER_ENERGIZING_LOCKING_LOST);
		transporterState = EnumTransporterState.ACQUIRING;
		
		// inform beacon provider
		if (globalPositionBeacon != null) {
			final World world = globalPositionBeacon.getWorldServerIfLoaded();
			if (world == null) {
				WarpDrive.logger.warn("Unable to disable TransporterBeacon %s: world isn't loaded",
				                      globalPositionBeacon);
			} else {
				final TileEntity tileEntity = world.getTileEntity(globalPositionBeacon.x, globalPositionBeacon.y, globalPositionBeacon.z);
				if (tileEntity instanceof ITransporterBeacon) {
					((ITransporterBeacon) tileEntity).energizeDone();
				} else {
					WarpDrive.logger.warn("Unable to disable TransporterBeacon %s: unsupported tile entity %s",
					                      globalPositionBeacon, tileEntity);
				}
			}
		}
	}
	
	private void energizeEntities(final double lockStrength, final HashMap<Integer, MovingEntity> movingEntities, final World world, final VectorI vPosition) {
		for (final Entry<Integer, MovingEntity> entryEntity : movingEntities.entrySet()) {
			final MovingEntity movingEntity = entryEntity.getValue();
			energizeEntity(lockStrength, movingEntity, world, vPosition);
		}
	}
	private void energizeEntities(final double lockStrength, final HashMap<Integer, MovingEntity> movingEntities, final World world, final ArrayList<VectorI> vScanners) {
		for (final Entry<Integer, MovingEntity> entryEntity : movingEntities.entrySet()) {
			final int indexEntity = entryEntity.getKey();
			final MovingEntity movingEntity = entryEntity.getValue();
			final VectorI vScanner = vScanners.get(indexEntity);
			energizeEntity(lockStrength, movingEntity, world, vScanner);
		}
	}
	private void energizeEntity(final double lockStrength, final MovingEntity movingEntity, final World world, final VectorI vPosition) {
		// check entity is valid
		if (movingEntity == MovingEntity.INVALID) {
			return;
		}
		if ( movingEntity == null
		  || vPosition == null ) {// corrupted data
			WarpDrive.logger.warn(String.format("Invalid entity %s for position %s, skipping transportation...",
			                                    movingEntity, vPosition));
			return;
		}
		final Entity entity = movingEntity.getEntity();
		if (entity == null) {// (bad state)
			WarpDrive.logger.warn("Entity went missing, skipping transportation...");
			return;
		}
		
		// compute friendly name
		final String nameEntity = entity.getCommandSenderName();
		
		// check lock strength
		if ( lockStrength < 1.0D
		  && world.rand.nextDouble() > lockStrength ) {
			if (WarpDriveConfig.LOGGING_TRANSPORTER) {
				WarpDrive.logger.info(String.format("Insufficient lock strength %.3f to transport %s",
				                                    lockStrength, entity));
			}
			applyTeleportationDamages(false, entity, lockStrength);
			
			// post event on transfer done
			sendEvent("transporterFailure", nameEntity);
			return;
		}
		
		// teleport
		final Vector3 v3Target = new Vector3(
				vPosition.x + 0.5D,
				vPosition.y + 0.99D,
				vPosition.z + 0.5D);
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info(String.format("Transporting entity %s to %s",
			                                    entity, v3Target));
		}
		Commons.moveEntity(entity, world, v3Target);
		applyTeleportationDamages(false, entity, lockStrength);
		
		// post event on transfer done
		sendEvent("transporterSuccess", nameEntity);
	}
	
	@Override
	public String getStatusHeader() {
		if ( globalPositionLocal == null
		  || globalPositionRemote == null ) {
			return super.getStatusHeader();
		}
		return super.getStatusHeader()
		       + "\n" + StatCollector.translateToLocalFormatted("warpdrive.transporter.status",
		                                                        globalPositionLocal.x, globalPositionLocal.y, globalPositionLocal.z,
		                                                        globalPositionRemote.x, globalPositionRemote.y, globalPositionRemote.z);
	}
	
	@Override
	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	@Override
	public void setBeamFrequency(final int beamFrequency) {
		if (this.beamFrequency != beamFrequency && (beamFrequency <= BEAM_FREQUENCY_MAX) && (beamFrequency > BEAM_FREQUENCY_MIN)) {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Beam frequency set from " + this.beamFrequency + " to " + beamFrequency);
			}
			if (hasWorldObj()) {
				ForceFieldRegistry.removeFromRegistry(this);
			}
			this.beamFrequency = beamFrequency;
			tickUpdateParameters = 0;
		}
		markDirty();
	}
	
	// IStarMapRegistryTileEntity overrides
	@Override
	public EnumStarMapEntryType getStarMapType() {
		return EnumStarMapEntryType.TRANSPORTER;
	}
	
	@Override
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public AxisAlignedBB getStarMapArea() {
		return AxisAlignedBB.getBoundingBox(
			Math.min(xCoord - WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS     , aabbLocalScanners == null ? xCoord : aabbLocalScanners.minX),
			Math.min(yCoord - WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_Y_BELOW_BLOCKS, aabbLocalScanners == null ? yCoord : aabbLocalScanners.minY),
			Math.min(zCoord - WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS     , aabbLocalScanners == null ? zCoord : aabbLocalScanners.minZ),
			Math.max(xCoord + WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS     , aabbLocalScanners == null ? xCoord : aabbLocalScanners.maxX),
			Math.max(yCoord + WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_Y_ABOVE_BLOCKS, aabbLocalScanners == null ? yCoord : aabbLocalScanners.maxY),
			Math.max(zCoord + WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS     , aabbLocalScanners == null ? zCoord : aabbLocalScanners.maxZ) );
	}
	
	@Override
	public int getMass() {
		return vLocalScanners == null ? 0 : vLocalScanners.size();
	}
	
	@Override
	public double getIsolationRate() {
		return 0.0D;
	}
	
	@Override
	public String getStarMapName() {
		return transporterName;
	}
	
	@Override
	public void onBlockUpdatedInArea(final VectorI vector, final Block block, final int metadata) {
		// skip in case of explosion, etc.
		if (isBlockUpdated) {
			return;
		}
		
		// check for significant change
		if ( block instanceof BlockTransporterScanner
		  || block instanceof BlockTransporterContainment) {
			isBlockUpdated = true;
			return;
		}
		if ( aabbLocalScanners != null
		  && vector.x >= aabbLocalScanners.minX
		  && vector.y >= aabbLocalScanners.minY
		  && vector.z >= aabbLocalScanners.minZ
		  && vector.x <  aabbLocalScanners.maxX
		  && vector.y <  aabbLocalScanners.maxY
		  && vector.z <  aabbLocalScanners.maxZ ) {
			isBlockUpdated = true;
		}
		
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info("onBlockUpdatedInArea block " + block);
		}
	}
	
	private void updateScanners() {
		// scan the whole area for scanners
		final int xMin = xCoord - WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS;
		final int xMax = xCoord + WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS;
		final int yMin = yCoord - WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_Y_BELOW_BLOCKS;
		final int yMax = yCoord + WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_Y_ABOVE_BLOCKS;
		final int zMin = zCoord - WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS;
		final int zMax = zCoord + WarpDriveConfig.TRANSPORTER_SETUP_SCANNER_RANGE_XZ_BLOCKS;
		
		final ArrayList<VectorI> vScanners = new ArrayList<>(16);
		final HashSet<VectorI> vContainments = new HashSet<>(64);
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				if (y < 0 || y > 254) {
					continue;
				}
				
				for (int z = zMin; z <= zMax; z++) {
					final Block block = worldObj.getBlock(x, y, z);
					if (block instanceof BlockTransporterScanner) {
						
						// only accept valid ones, spawn particles on others
						final VectorI vScanner = new VectorI(x, y, z);
						final Collection<VectorI> vValidContainments = ((BlockTransporterScanner) block).getValidContainment(worldObj, vScanner);
						if (vValidContainments == null || vValidContainments.isEmpty()) {
							worldObj.setBlockMetadataWithNotify(x, y, z, 0, 2);
							PacketHandler.sendSpawnParticlePacket(worldObj, "jammed", (byte) 5, new Vector3(vScanner.x + 0.5D, vScanner.y + 1.5D, vScanner.z + 0.5D),
									new Vector3(0.0D, 0.0D, 0.0D),
									1.0F, 1.0F, 1.0F,
									1.0F, 1.0F, 1.0F,
									32);
						} else {
							vScanners.add(vScanner);
							vContainments.addAll(vValidContainments);
							worldObj.setBlockMetadataWithNotify(x, y, z, 1, 2);
						}
					}
				}
			}
		}
		setLocalScanners(vScanners, vContainments);
	}
	
	private void setLocalScanners(final ArrayList<VectorI> vScanners, final Collection<VectorI> vContainments) {
		// no scanner defined => force null
		if (vScanners == null || vScanners.isEmpty()) {
			vLocalScanners = null;
			vLocalContainments = null;
			aabbLocalScanners = null;
			return;
		}
		
		// cache block update detection area
		final VectorI vMin = vScanners.get(0).clone();
		final VectorI vMax = vScanners.get(0).clone();
		for (final VectorI vScanner : vScanners) {
			vMin.x = Math.min(vMin.x, vScanner.x - 1);
			vMin.y = Math.min(vMin.y, vScanner.y    );
			vMin.z = Math.min(vMin.z, vScanner.z - 1);
			vMax.x = Math.max(vMax.x, vScanner.x + 1);
			vMax.y = Math.max(vMax.y, vScanner.y + 2);
			vMax.z = Math.max(vMax.z, vScanner.z + 1);
		}
		
		// save values
		vLocalScanners = vScanners;
		vLocalContainments = new ArrayList<>(vContainments);
		aabbLocalScanners = AxisAlignedBB.getBoundingBox(
				vMin.x, vMin.y, vMin.z,
				vMax.x + 1.0D, vMax.y + 1.0D, vMax.z + 1.0D);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<VectorI> getContainments() {
		return vLocalContainments;
	}
	
	private static class FocusValues {
		ArrayList<VectorI> vScanners;
		int countRangeUpgrades;
		double strength;
		double speed;
	}
	
	private static class EntityValues {
		int count;
		long mass;
	}
	
	private void updateParameters() {
		isJammed = false;
		reasonJammed = "";
		
		// reset entities to grab
		if (transporterState != EnumTransporterState.ENERGIZING) {
			movingEntitiesLocal.clear();
			movingEntitiesRemote.clear();
		}
		
		// check connection
		if (!isConnected) {
			isJammed = true;
			reasonJammed = "Beam frequency not set";
			return;
		}
		
		// compute local universal coordinates
		final CelestialObject celestialObjectLocal = CelestialObjectManager.get(worldObj, xCoord, zCoord);
		final Vector3 v3Local_universal = StarMapRegistry.getUniversalCoordinates(celestialObjectLocal, globalPositionLocal.x, globalPositionLocal.y, globalPositionLocal.z);
		
		// check beacon obsolescence
		if (globalPositionBeacon != null) {
			final WorldServer worldBeacon = globalPositionBeacon.getWorldServerIfLoaded();
			if (worldBeacon == null) {
				globalPositionBeacon = null;
				isLockRequested = false;
				isEnergizeRequested = false;
			} else {
				final TileEntity tileEntity = worldBeacon.getTileEntity(globalPositionBeacon.x, globalPositionBeacon.y, globalPositionBeacon.z);
				if ( !(tileEntity instanceof ITransporterBeacon)
				  || !((ITransporterBeacon) tileEntity).isActive() ) {
					globalPositionBeacon = null;
					isLockRequested = false;
					isEnergizeRequested = false;
				}
			}
		}
		
		// compute remote universal coordinates
		GlobalPosition globalPositionRemoteNew = null;
		if (globalPositionBeacon != null) {
			globalPositionRemoteNew = globalPositionBeacon;
			
		} else if (remoteLocationRequested instanceof VectorI) {
			final VectorI vRequest = (VectorI) remoteLocationRequested;
			if (vRequest.y < 0) {
				final CelestialObject celestialObjectChild = CelestialObjectManager.getClosestChild(worldObj, xCoord, zCoord);
				if (celestialObjectChild == null) {
					reasonJammed = "Not in orbit of a planet";
				} else {
					vRequest.translate(celestialObjectChild.getEntryOffset());
					globalPositionRemoteNew = new GlobalPosition(celestialObjectChild.dimensionId, vRequest.x, (vRequest.y + 1024) % 256, vRequest.z);
				}
			} else if (vRequest.y > 256) {
				vRequest.translateBack(celestialObjectLocal.getEntryOffset());
				globalPositionRemoteNew = new GlobalPosition(celestialObjectLocal.parent.dimensionId, vRequest.x, vRequest.y % 256, vRequest.z);
				
			} else {
				globalPositionRemoteNew = new GlobalPosition(worldObj.provider.dimensionId, vRequest.x, vRequest.y, vRequest.z);
			}
			
		} else if (remoteLocationRequested instanceof UUID) {
			globalPositionRemoteNew = WarpDrive.starMap.getByUUID(EnumStarMapEntryType.TRANSPORTER, (UUID) remoteLocationRequested);
			if (globalPositionRemoteNew == null) {
				reasonJammed = "Unknown transporter signature";
			}
			
		} else if (remoteLocationRequested instanceof String) {
			final EntityPlayerMP entityPlayer = Commons.getOnlinePlayerByName((String) remoteLocationRequested);
			if (entityPlayer == null) {
				reasonJammed = "No player by that name";
			} else {
				final ItemStack itemStackHeld = entityPlayer.getHeldItem();
				if ( itemStackHeld == null
				  || itemStackHeld.stackSize <= 0
				  || !(itemStackHeld.getItem() instanceof IItemTransporterBeacon) ) {
				    reasonJammed = "No transporter beacon in player hand";
				} else if (!((IItemTransporterBeacon) itemStackHeld.getItem()).isActive(itemStackHeld)) {
					reasonJammed = "Player beacon is out of power";
				} else {
					globalPositionRemoteNew = new GlobalPosition(entityPlayer);
				}
			}
		} else {
			reasonJammed = "No remote location defined";
		}
		
		if ( globalPositionRemoteNew == null
		  || !globalPositionRemoteNew.equals(globalPositionRemote) ) {
			globalPositionRemote = globalPositionRemoteNew;
			lockStrengthActual = 0;
			if (transporterState == EnumTransporterState.ENERGIZING) {
				transporterState = EnumTransporterState.ACQUIRING;
			}
			isJammed = (globalPositionRemoteNew == null);
			if (isJammed) {
				return;
			}
		}
		
		// validate target dimension
		final CelestialObject celestialObjectRemote = globalPositionRemote.getCelestialObject(worldObj.isRemote);
		final Vector3 v3Remote_universal = globalPositionRemote.getUniversalCoordinates(worldObj.isRemote);
		
		if (celestialObjectRemote == null) {
			isJammed = true;
			reasonJammed = "Unknown remote celestial object";
			return;
		}
		
		// validate cross dimension transport rules
		if (celestialObjectLocal != celestialObjectRemote) {
			if ( celestialObjectLocal.isHyperspace()
			  || celestialObjectRemote.isHyperspace() ) {
				isJammed = true;
				reasonJammed = "Blocked by warp field barrier";
				return;
			}
			
			if (celestialObjectRemote.isVirtual()) {
				isJammed = true;
				reasonJammed = "Unable to reach virtual planet";
				return;
			}
		}
		
		// get remote world
		final WorldServer worldRemote = Commons.getOrCreateWorldServer(celestialObjectRemote.dimensionId);
		if (worldRemote == null) {
			WarpDrive.logger.error(String.format("Unable to initialize dimension %d for %s",
			                                     celestialObjectRemote.dimensionId,
			                                     this));
			isJammed = true;
			reasonJammed = String.format("Unable to initialize dimension %d", celestialObjectRemote.dimensionId);
			return;
		}
		
		// compute range
		final double rangeActualSquared = v3Local_universal.clone().subtract(v3Remote_universal).getMagnitudeSquared();
		final int rangeActual = (int) Math.ceil(Math.sqrt(rangeActualSquared));
		
		// compute focalization bonuses
		final FocusValues focusValuesLocal  = getFocusValueAtCoordinates(worldObj, globalPositionLocal.getVectorI());
		final FocusValues focusValuesRemote = getFocusValueAtCoordinates(worldRemote, globalPositionRemote.getVectorI());
		final double focusBoost = Commons.interpolate(
				1.0D,
				0.0D,
				WarpDriveConfig.TRANSPORTER_ENERGIZING_MAX_ENERGY_FACTOR,
				WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_BONUS_AT_MAX_ENERGY_FACTOR,
				energyFactor);
		lockStrengthOptimal = (focusValuesLocal.strength + focusValuesRemote.strength) / 2.0D + focusBoost;
		lockStrengthSpeed   = (focusValuesLocal.speed + focusValuesRemote.speed) / 2.0D
		                    / WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_OPTIMAL_TICKS;
		
		final int rangeMax = WarpDriveConfig.TRANSPORTER_RANGE_BASE_BLOCKS
		                   + WarpDriveConfig.TRANSPORTER_RANGE_UPGRADE_BLOCKS * focusValuesLocal.countRangeUpgrades
		                   + WarpDriveConfig.TRANSPORTER_RANGE_UPGRADE_BLOCKS * focusValuesRemote.countRangeUpgrades;
		
		// retrieve remote scanner positions
		vRemoteScanners = focusValuesRemote.vScanners;
		
		// update entities in range
		final EntityValues entityValues = updateEntitiesToEnergize();
		
		// compute energy cost from range
		energyCostForAcquiring = Math.max(0, WarpDriveConfig.TRANSPORTER_LOCKING_ENERGY_FACTORS[0]
		                                   + WarpDriveConfig.TRANSPORTER_LOCKING_ENERGY_FACTORS[1]
		                                     * vLocalScanners.size()
		                                     * ( Math.log(1.0D + WarpDriveConfig.TRANSPORTER_LOCKING_ENERGY_FACTORS[2] * rangeActual)
		                                       + Math.pow(WarpDriveConfig.TRANSPORTER_LOCKING_ENERGY_FACTORS[3] + rangeActual,
		                                                  WarpDriveConfig.TRANSPORTER_LOCKING_ENERGY_FACTORS[4]) ) );
		
		energyCostForEnergizing = Math.max(0, WarpDriveConfig.TRANSPORTER_ENERGIZING_ENERGY_FACTORS[0]
		                                    + WarpDriveConfig.TRANSPORTER_ENERGIZING_ENERGY_FACTORS[1]
		                                      * entityValues.mass
		                                      * ( Math.log(1.0D + WarpDriveConfig.TRANSPORTER_ENERGIZING_ENERGY_FACTORS[2] * rangeActual)
		                                        + Math.pow(WarpDriveConfig.TRANSPORTER_ENERGIZING_ENERGY_FACTORS[3] + rangeActual,
		                                                   WarpDriveConfig.TRANSPORTER_ENERGIZING_ENERGY_FACTORS[4]) ) );
		
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info(String.format("Transporter parameters at (%d %d %d) are range (actual %d max %d) lockStrength (actual %.5f optimal %.5f speed %.5f)",
			                                    xCoord, yCoord, zCoord,
			                                    rangeActual, rangeMax,
			                                    lockStrengthActual, lockStrengthOptimal, lockStrengthSpeed));
		}
		
		// check minimum range
		if (rangeActual < 16) {
			isJammed = true;
			reasonJammed = "Remote location is too close";
			return;
		}
		
		//  check maximum range
		if (rangeActual > rangeMax) {
			isJammed = true;
			reasonJammed = String.format("Out of range: %d > %d", rangeActual, rangeMax);
			return;
		}
		
		// validate shields along trajectory
		if (worldObj == worldRemote) {// same world
			isJammed |= isJammedTrajectory(worldObj, globalPositionLocal.getVectorI(), globalPositionRemote.getVectorI(), beamFrequency);
		} else if (v3Local_universal.y > v3Remote_universal.y) {// remote is below us
			isJammed |= isJammedTrajectory(worldObj,
			                               globalPositionLocal.getVectorI(),
			                               new VectorI(globalPositionLocal.x, -1, globalPositionLocal.z),
			                               beamFrequency);
			isJammed |= isJammedTrajectory(worldRemote,
			                               new VectorI(globalPositionRemote.x, 256, globalPositionRemote.z),
			                               globalPositionRemote.getVectorI(),
			                               beamFrequency);
		} else {// remote is above us
			isJammed |= isJammedTrajectory(worldObj,
			                               globalPositionLocal.getVectorI(),
			                               new VectorI(globalPositionLocal.x, 256, globalPositionLocal.z),
			                               beamFrequency);
			isJammed |= isJammedTrajectory(worldRemote,
			                               new VectorI(globalPositionRemote.x, -1, globalPositionRemote.z),
			                               globalPositionRemote.getVectorI(),
			                               beamFrequency);
		}
		if (isJammed) {
			reasonJammed = "Blocked by force field or unbreakable block";
		}
	}
	
	boolean updateBeacon(final TileEntity tileEntity, final UUID uuid) {
		if (tileEntity == null || uuid == null) {
			WarpDrive.logger.error(String.format("%s Invalid parameters in beacon call to transporter as %s, %s",
			                                     this, tileEntity, uuid));
			return false;
		}
		
		if (!this.uuid.equals(uuid)) {
			if (!isJammed && WarpDriveConfig.LOGGING_TRANSPORTER) {
				WarpDrive.logger.info(String.format("%s Conflicting beacon requests received %s is not %s",
				                                    this, tileEntity, uuid));
			}
			isJammed = true;
			reasonJammed = "Conflicting beacon requests received";
			tickCooldown = Math.max(tickCooldown, WarpDriveConfig.TRANSPORTER_JAMMED_COOLDOWN_TICKS);
			return false;
		}
		
		if ( globalPositionBeacon == null
		  || !globalPositionBeacon.equals(tileEntity) ) {
			globalPositionBeacon = new GlobalPosition(tileEntity);
			energyFactor = Math.max(4.0D, energyFactor);    // ensure minimum energy factor for beacon activation
			isJammed = true;
			reasonJammed = "Beacon request received";
			lockStrengthActual = 0;
			if (transporterState == EnumTransporterState.ENERGIZING) {
				transporterState = EnumTransporterState.ACQUIRING;
			}
		}
		
		return true;
	}
	
	private static FocusValues getFocusValueAtCoordinates(final World world, final VectorI vLocation) {
		// scan the area
		int countBeacons = 0;
		int countTransporters = 0;
		int sumRangeUpgrades = 0;
		int sumFocusUpgrades = 0;
		
		final int xMin = vLocation.x - WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int xMax = vLocation.x + WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int yMin = vLocation.y - WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int yMax = vLocation.y + WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int zMin = vLocation.z - WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		final int zMax = vLocation.z + WarpDriveConfig.TRANSPORTER_FOCUS_SEARCH_RADIUS_BLOCKS;
		
		ArrayList<VectorI> vScanners = null;
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				if (y < 0 || y > 254) {
					continue;
				}
				
				for (int z = zMin; z <= zMax; z++) {
					final Block block = world.getBlock(x, y, z);
					if (block instanceof BlockTransporterBeacon) {
						// count active beacons
						final boolean isActive = world.getBlockMetadata(x, y, z) == 0;
						if (isActive) {
							countBeacons++;
						}
						
					} else if (block instanceof BlockTransporterCore) {
						// count active transporters
						final TileEntity tileEntity = world.getTileEntity(x, y, z);
						if ( tileEntity instanceof TileEntityTransporterCore
						  && ((TileEntityTransporterCore) tileEntity).isEnabled
						  && ((TileEntityTransporterCore) tileEntity).isConnected ) {
							countTransporters++;
							// remember scanners coordinates
							vScanners = ((TileEntityTransporterCore) tileEntity).vLocalScanners;
							// remember upgrades
							sumRangeUpgrades += ((TileEntityTransporterCore) tileEntity).getUpgradeCount(EnumComponentType.ENDER_CRYSTAL);
							sumFocusUpgrades += ((TileEntityTransporterCore) tileEntity).getUpgradeCount(EnumComponentType.EMERALD_CRYSTAL);
						}
					}
				}
			}
		}
		
		// compute values
		final FocusValues result = new FocusValues();
		if (countTransporters == 1) {
			result.vScanners = vScanners;
			result.countRangeUpgrades = sumRangeUpgrades;
			result.speed = WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_AT_TRANSPORTER + sumFocusUpgrades * WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_UPGRADE;
			result.strength = WarpDriveConfig.TRANSPORTER_LOCKING_STRENGTH_AT_TRANSPORTER + sumFocusUpgrades * WarpDriveConfig.TRANSPORTER_LOCKING_SPEED_UPGRADE;
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
			                                    vLocation, result.countRangeUpgrades, result.speed, result.strength));
		}
		
		return result;
	}
	
	private static boolean isJammedTrajectory(final World world, final VectorI vSource, final VectorI vDestination, final int beamFrequency) {
		final VectorI vPath = vDestination.clone().translateBack(vSource);
		final int length = (int) Math.ceil(3 * Math.sqrt(vPath.getMagnitudeSquared()));
		final Vector3 v3Delta = new Vector3(vPath.x / (double) length, vPath.y / (double) length, vPath.z / (double) length);
		
		// scan along given trajectory
		final Vector3 v3Current = new Vector3(vSource.x, vSource.y, vSource.z).translate(0.5D);
		final VectorI vCurrent = vSource.clone();
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
			if (isJammedCoordinate(world, vCurrent, beamFrequency)) return true;
			
			// remember this coordinates
			vPrevious.x = vCurrent.x;
			vPrevious.y = vCurrent.y;
			vPrevious.z = vCurrent.z;
		}
		return false;
	}
	private static boolean isJammedCoordinate(final World world, final VectorI vCurrent, final int beamFrequency) {
		// check block blacklist for blinking
		final Block block = vCurrent.getBlock(world);
		if (Dictionary.BLOCKS_NOBLINK.contains(block)) {
			if (WarpDriveConfig.LOGGING_TRANSPORTER) {
				WarpDrive.logger.info(String.format("Transporter beam jammed by blacklisted block %s", block));
			}
			return true;
		}
		
		// allow passing through force fields with same beam frequency
		if (block instanceof BlockForceField) {
			final TileEntity tileEntity = vCurrent.getTileEntity(world);
			if (tileEntity instanceof TileEntityForceField) {
				final ForceFieldSetup forceFieldSetup = ((TileEntityForceField) tileEntity).getForceFieldSetup();
				if (forceFieldSetup == null) {
					// projector not loaded yet, consider it jammed by default
					WarpDrive.logger.warn(String.format("Transporter beam jammed by non-loaded force field projector at %s", tileEntity));
					return true;
				}
				if (forceFieldSetup.beamFrequency != beamFrequency) {
					// jammed by invalid beam frequency
					if (WarpDriveConfig.LOGGING_TRANSPORTER) {
						WarpDrive.logger.info(String.format("Transporter beam jammed by invalid frequency against %s", tileEntity));
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
			return (int) Math.ceil(energyCostForAcquiring * energyFactor);
			
		case ENERGIZING:
			return (int) Math.ceil(energyCostForEnergizing * energyFactor / WarpDriveConfig.TRANSPORTER_ENERGIZING_CHARGING_TICKS);
			
		default:
			return 0;
		}
	}
	
	private static void applyTeleportationDamages(final boolean isPreTeleportation, final Entity entity, final double strength) {
		// skip invulnerable
		if ( entity.isDead
		  || entity.isEntityInvulnerable() ) {
			return;
		}
		
		// add bonus if transport was successful
		final double strengthToUse = isPreTeleportation ? strength : Math.random() * WarpDriveConfig.TRANSPORTER_ENERGIZING_SUCCESS_LOCK_BONUS + strength;
		
		final double strengthSafe = 0.95D;
		final double strengthMaxDamage = 0.65D;
		final double strengthNoDamage = 0.10D;
		if (strengthToUse > strengthSafe) {
			return;
		}
		final double damageNormalized = (strengthSafe - strengthToUse) / (strengthSafe - strengthMaxDamage);
		final double damageMax = isPreTeleportation ? WarpDriveConfig.TRANSPORTER_ENERGIZING_FAILURE_MAX_DAMAGE : WarpDriveConfig.TRANSPORTER_ENERGIZING_SUCCESS_MAX_DAMAGE;
		// final double damageAmount = Commons.clamp(1.0D, 1000.0D, Math.pow(10.0D, 10.0D * damageNormalized));
		final double damageAmount = Commons.clamp(1.0D, 1000.0D, damageMax * damageNormalized);
		
		if (WarpDriveConfig.LOGGING_TRANSPORTER) {
			WarpDrive.logger.info(String.format("Applying teleportation damages %s transport with %.3f strength, %.2f damage towards %s",
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
			if (entity.worldObj.isAirBlock(vPosition.x, vPosition.y, vPosition.z)) {
				entity.worldObj.setBlock(vPosition.x, vPosition.y, vPosition.z, Blocks.flowing_lava, 6, 2);
			}
		}
	}
	
	private EntityValues updateEntitiesToEnergize() {
		final int countScanners = Math.min(vLocalScanners.size(), vRemoteScanners != null ? vRemoteScanners.size() : vLocalScanners.size());
		
		final EntityValues entityValues = new EntityValues();
		
		// return default values unless we're acquiring or energizing
		if ( transporterState != EnumTransporterState.ENERGIZING
		  && transporterState != EnumTransporterState.ACQUIRING ) {
			entityValues.count = countScanners;
			entityValues.mass = 8000 * countScanners;
			return entityValues;
		}
		
		// collect all candidates at local location
		final EntityValues entityValuesLocal = updateEntitiesOnScanners(worldObj, vLocalScanners, countScanners, movingEntitiesLocal);
		
		// collect all candidates at remote location
		final World worldRemote = Commons.getOrCreateWorldServer(globalPositionRemote.dimensionId);
		final EntityValues entityValuesRemote;
		if (vRemoteScanners != null) {
			entityValuesRemote = updateEntitiesOnScanners(worldRemote, vRemoteScanners, countScanners, movingEntitiesRemote);
		} else {
			entityValuesRemote = updateEntitiesInArea(worldRemote, globalPositionRemote, countScanners, movingEntitiesRemote);
		}
		entityValues.count = entityValuesLocal.count + entityValuesRemote.count;
		entityValues.mass  = entityValuesLocal.mass  + entityValuesRemote.mass;
		return entityValues;
	}
	
	private static EntityValues updateEntitiesOnScanners(final World world, final ArrayList<VectorI> vScanners, final int countScanners,
	                                                     final HashMap<Integer, MovingEntity> movingEntities) {
		final double tolerance2 = WarpDriveConfig.TRANSPORTER_ENERGIZING_ENTITY_MOVEMENT_TOLERANCE_BLOCKS
		                        * WarpDriveConfig.TRANSPORTER_ENERGIZING_ENTITY_MOVEMENT_TOLERANCE_BLOCKS;
		// remember entities allocated so we don't double grab them
		final HashSet<Entity> entitiesOnScanners = new HashSet<>(countScanners);
		final EntityValues entityValues = new EntityValues();
		
		// allocate an entity to each scanner
		for (int index = 0; index < countScanners; index++) {
			MovingEntity movingEntity = movingEntities.get(index);
			// skip marked spots
			if (movingEntity == MovingEntity.INVALID) {
				continue;
			}
			
			// validate existing entity
			if (movingEntity != null) {
				final Entity entity = movingEntity.getEntity();
				if ( entity == null
				  || entity.isDead ) {// no longer valid => search a new one, no energy lost
					movingEntity = null;
					
				} else {
					final double distance2 = movingEntity.getDistanceMoved_square();
					if (distance2 > tolerance2) {// entity moved too much => damage existing one, loose this slot
						final double strength = Math.sqrt(distance2) / Math.sqrt(tolerance2) / 2.0D;
						applyTeleportationDamages(true, entity, strength);
						movingEntity = MovingEntity.INVALID;
					}
				}
			}
			
			// grab another entity
			if ( movingEntity == null) {
				final Entity entityOnScanner = getCandidateEntityOnScanner(world, vScanners.get(index), entitiesOnScanners);
				if (entityOnScanner != null) {
					movingEntity = new MovingEntity(entityOnScanner);
					entitiesOnScanners.add(entityOnScanner);
				}
			}
			
			// save updated entity
			if (movingEntity == null) {// no candidate => mark the spot so we don't grab new ones while energizing
				movingEntities.put(index, MovingEntity.INVALID);
			} else {
				movingEntities.put(index, movingEntity);
				entityValues.count++;
				entityValues.mass += movingEntity.getMass();
			}
		}
		
		return entityValues;
	}
	
	private static EntityValues updateEntitiesInArea(final World world, final GlobalPosition globalPosition, final int countScanners,
	                                                 final HashMap<Integer, MovingEntity> movingEntities) {
		final double tolerance2 = WarpDriveConfig.TRANSPORTER_ENERGIZING_ENTITY_MOVEMENT_TOLERANCE_BLOCKS
		                        * WarpDriveConfig.TRANSPORTER_ENERGIZING_ENTITY_MOVEMENT_TOLERANCE_BLOCKS;
		final LinkedHashSet<Entity> entities = getCandidateEntitiesInArea(world, globalPosition);
		final EntityValues entityValues = new EntityValues();
		
		// allocate an entity to each scanner
		for (int index = 0; index < countScanners; index++) {
			MovingEntity movingEntity = movingEntities.get(index);
			// skip marked spots
			if (movingEntity == MovingEntity.INVALID) {
				continue;
			}
			
			// validate existing entity
			if (movingEntity != null) {
				final Entity entity = movingEntity.getEntity();
				if ( entity == null
				  || entity.isDead ) {// no longer valid => search a new one, no energy lost
					movingEntity = null;
					
				} else if (entities.contains(entity)) {// still in the list
					final double distance2 = movingEntity.getDistanceMoved_square();
					if (distance2 > tolerance2) {// entity moved too much => damage existing one, loose this slot
						final double strength = Math.sqrt(distance2) / Math.sqrt(tolerance2) / 2.0D;
						applyTeleportationDamages(true, entity, strength);
						movingEntity = MovingEntity.INVALID;
					} else {// still valid => remove it from candidates to avoid double grab
						entities.remove(entity);
					}
				} else {// exited the area or shifted in order => invalidate the spot
					movingEntity = MovingEntity.INVALID;
				}
			}
			
			// grab another entity
			if ( movingEntity == null
			  && !entities.isEmpty() ) {
				Iterator<Entity> entityIterable = entities.iterator();
				final Entity entity = entityIterable.next();
				if (entity != null) {
					movingEntity = new MovingEntity(entity);
					entityIterable.remove();
				}
			}
			
			// save updated entity
			if (movingEntity == null) {// no candidate => mark the spot so we don't grab new ones while energizing
				movingEntities.put(index, MovingEntity.INVALID);
			} else {
				movingEntities.put(index, movingEntity);
				entityValues.count++;
				entityValues.mass += movingEntity.getMass();
			}
		}
		
		return entityValues;
	}
	
	private static Entity getCandidateEntityOnScanner(final World world, final VectorI vScanner, final HashSet<Entity> entitiesOnScanners) {
		final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
				vScanner.x - 0.05D,
				vScanner.y - 1.00D,
				vScanner.z - 0.05D,
				vScanner.x + 1.05D,
				vScanner.y + 2.00D,
				vScanner.z + 1.05D);
		
		final List entities = world.getEntitiesWithinAABBExcludingEntity(null, aabb);
		Entity entityReturn = null;
		int countEntities = 0;
		for (final Object object : entities) {
			if (!(object instanceof Entity)) {
				continue;
			}
			
			final Entity entity = (Entity) object;
			
			// (particle effects are client side only, no need to filter them out)
			
			// skip blacklisted ids
			final String entityId = EntityList.getEntityString(entity);
			if (Dictionary.ENTITIES_LEFTBEHIND.contains(entityId)) {
				if (WarpDriveConfig.LOGGING_TRANSPORTER) {
					WarpDrive.logger.info(String.format("Entity is not valid for transportation (id %s) %s",
					entityId, entity));
				}
				continue;
			}
			
			// skip already attached entities
			if (entitiesOnScanners.contains(entity)) {
				continue;
			}
			
			// keep it
			entityReturn = entity;
			countEntities++;
		}
		
		// only accept a single entity in the area
		if (countEntities > 1) {
			PacketHandler.sendSpawnParticlePacket(world, "jammed", (byte) 5, new Vector3(vScanner.x + 0.5D, vScanner.y + 1.5D, vScanner.z + 0.5D),
					new Vector3(0.0D, 0.0D, 0.0D),
					1.0F, 1.0F, 1.0F,
					1.0F, 1.0F, 1.0F,
					32);
			return null;
		}
		return entityReturn;
	}
	
	private static LinkedHashSet<Entity> getCandidateEntitiesInArea(final World world, final GlobalPosition globalPosition) {
		final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
			globalPosition.x - WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS,
			globalPosition.y - 1.0D,
			globalPosition.z - WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS,
			globalPosition.x + WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS + 1.0D,
			globalPosition.y + 2.0D,
			globalPosition.z + WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS + 1.0D);
		
		final List entities = world.getEntitiesWithinAABBExcludingEntity(null, aabb);
		final LinkedHashSet<Entity> entitiesReturn = new LinkedHashSet<>(entities.size());
		for (final Object object : entities) {
			if (!(object instanceof Entity)) {
				continue;
			}
			
			final Entity entity = (Entity) object;
			
			// (particle effects are client side only, no need to filter them out)
			
			// skip blacklisted ids
			final String entityId = EntityList.getEntityString(entity);
			if (Dictionary.ENTITIES_LEFTBEHIND.contains(entityId)) {
				if (WarpDriveConfig.LOGGING_TRANSPORTER) {
					WarpDrive.logger.info(String.format("Entity is not valid for transportation (id %s) %s", 
					                                    entityId, entity));
				}
				continue;
			}
			
			// keep it
			entitiesReturn.add(entity);
		}
		
		return entitiesReturn;
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
		
		if (uuid != null) {
			tagCompound.setLong("uuidMost", uuid.getMostSignificantBits());
			tagCompound.setLong("uuidLeast", uuid.getLeastSignificantBits());
		}
		
		if ( vLocalScanners != null
		  && vLocalContainments != null ) {
			final NBTTagList tagListScanners = new NBTTagList();
			for (final VectorI vScanner : vLocalScanners) {
				final NBTTagCompound tagCompoundScanner = vScanner.writeToNBT(new NBTTagCompound());
				tagListScanners.appendTag(tagCompoundScanner);
			}
			tagCompound.setTag("scanners", tagListScanners);
			
			final NBTTagList tagListContainments = new NBTTagList();
			for (final VectorI vContainment : vLocalContainments) {
				final NBTTagCompound tagCompoundContainment = vContainment.writeToNBT(new NBTTagCompound());
				tagListContainments.appendTag(tagCompoundContainment);
			}
			tagCompound.setTag("containments", tagListContainments);
		}
		
		tagCompound.setInteger(IBeamFrequency.BEAM_FREQUENCY_TAG, beamFrequency);
		tagCompound.setString("name", transporterName);
		tagCompound.setBoolean("isEnabled", isEnabled);
		tagCompound.setBoolean("isLockRequested", isLockRequested);
		tagCompound.setBoolean("isEnergizeRequested", isEnergizeRequested);
		
		NBTTagCompound tagRemoteLocation = new NBTTagCompound();
		if (remoteLocationRequested instanceof UUID) {
			tagRemoteLocation.setLong("uuidMost", ((UUID) remoteLocationRequested).getMostSignificantBits());
			tagRemoteLocation.setLong("uuidLeast", ((UUID) remoteLocationRequested).getLeastSignificantBits());
		} else if (remoteLocationRequested instanceof VectorI) {
			tagRemoteLocation = ((VectorI) remoteLocationRequested).writeToNBT(tagRemoteLocation);
		} else if (remoteLocationRequested instanceof String) {
			tagRemoteLocation.setString("playerName", (String) remoteLocationRequested);
		}
		tagCompound.setTag("remoteLocation", tagRemoteLocation);
		
		tagCompound.setDouble("energyFactor", energyFactor);
		tagCompound.setDouble("lockStrengthActual", lockStrengthActual);
		tagCompound.setInteger("tickCooldown", tickCooldown);
		
		tagCompound.setString("state", transporterState.toString());
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		uuid = new UUID(tagCompound.getLong("uuidMost"), tagCompound.getLong("uuidLeast"));
		if (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0) {
			uuid = UUID.randomUUID();
		}
		
		if ( tagCompound.hasKey("scanners", Constants.NBT.TAG_LIST)
		  && tagCompound.hasKey("containments", Constants.NBT.TAG_LIST)) {
			final NBTTagList tagListScanners = (NBTTagList) tagCompound.getTag("scanners");
			final ArrayList<VectorI> vScanners = new ArrayList<>(tagListScanners.tagCount());
			for (int indexScanner = 0; indexScanner < tagListScanners.tagCount(); indexScanner++) {
				final VectorI vScanner = VectorI.createFromNBT(tagListScanners.getCompoundTagAt(indexScanner));
				vScanners.add(vScanner);
			}
			
			final NBTTagList tagListContainments = (NBTTagList) tagCompound.getTag("containments");
			final ArrayList<VectorI> vContainments = new ArrayList<>(tagListContainments.tagCount());
			for (int indexContainment = 0; indexContainment < tagListContainments.tagCount(); indexContainment++) {
				final VectorI vContainment = VectorI.createFromNBT(tagListContainments.getCompoundTagAt(indexContainment));
				vContainments.add(vContainment);
			}
			setLocalScanners(vScanners, vContainments);
		}
		
		beamFrequency = tagCompound.getInteger(IBeamFrequency.BEAM_FREQUENCY_TAG);
		transporterName = tagCompound.getString("name");
		isEnabled = tagCompound.getBoolean("isEnabled");
		isLockRequested = tagCompound.getBoolean("isLockRequested");
		isEnergizeRequested = tagCompound.getBoolean("isEnergizeRequested");
		
		final NBTBase tagRemoteLocation = tagCompound.getTag("remoteLocation");
		if (tagRemoteLocation instanceof NBTTagCompound) {
			final NBTTagCompound tagCompoundRemoteLocation = (NBTTagCompound) tagRemoteLocation;
			if (tagCompoundRemoteLocation.hasKey("uuidMost")) {
				remoteLocationRequested = new UUID(
						tagCompoundRemoteLocation.getLong("uuidMost"),
						tagCompoundRemoteLocation.getLong("uuidLeast"));
				
			} else if (tagCompoundRemoteLocation.hasKey("x")) {
				remoteLocationRequested = new VectorI();
				((VectorI) remoteLocationRequested).readFromNBT(tagCompoundRemoteLocation);
				
			} else if (tagCompoundRemoteLocation.hasKey("playerName")) {
				remoteLocationRequested = tagCompoundRemoteLocation.getString("playerName");
			}
		}
		
		energyFactor = Commons.clamp(1, WarpDriveConfig.TRANSPORTER_ENERGIZING_MAX_ENERGY_FACTOR, tagCompound.getDouble("energyFactor"));
		lockStrengthActual = tagCompound.getDouble("lockStrengthActual");
		tickCooldown = tagCompound.getInteger("tickCooldown");
		
		try {
			transporterState = EnumTransporterState.valueOf(tagCompound.getString("state"));
		} catch (IllegalArgumentException exception) {
			transporterState = EnumTransporterState.DISABLED;
		}
	}
	
	@Override
	public Packet getDescriptionPacket() {
		final NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		tagCompound.removeTag("uuidMost");
		tagCompound.removeTag("uuidLeast");
		tagCompound.removeTag(IBeamFrequency.BEAM_FREQUENCY_TAG);
		tagCompound.removeTag("name");
		
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, tagCompound);
	}
	
	@Override
	public void onDataPacket(final NetworkManager networkManager, final S35PacketUpdateTileEntity packet) {
		final NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
	}
	
	// Common OC/CC methods
	@Override
	public String[] transporterName(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final String transporterNameNew = arguments[0].toString();
			if (!transporterName.equals(transporterNameNew)) {
				transporterName = transporterNameNew;
				uuid = UUID.randomUUID();
			}
		}
		return new String[] { transporterName, uuid == null ? null : uuid.toString() };
	}
	
	@Override
	public Boolean[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			isEnabled = Commons.toBool(arguments[0]);
			markDirty();
		}
		return new Boolean[] { isEnabled };
	}
	
	@Override
	public Object[] state() {
		final int energy = energy_getEnergyStored();
		final String status = getStatusHeaderInPureText();
		final String state = isJammed ? reasonJammed : tickCooldown > 0 ? String.format("Cooling down %d s", Math.round(tickCooldown / 20)) : transporterState.getName();
		return new Object[] { status, state, isConnected, isEnabled, isJammed, energy, lockStrengthActual };
	}
	
	@Override
	public Object[] remoteLocation(final Object[] arguments) {
		if (arguments.length == 3) {
			if (remoteLocationRequested instanceof VectorI) {// already using direct coordinates
				final VectorI vNew = computer_getVectorI((VectorI) remoteLocationRequested, arguments);
				if (!vNew.equals(remoteLocationRequested)) {
					remoteLocationRequested = vNew;
					tickUpdateParameters = 0;
				}
				
			} else {
				final VectorI vNew = computer_getVectorI(null, arguments);
				if (vNew != null) {
					remoteLocationRequested = vNew;
					tickUpdateParameters = 0;
				}
			}
		} else if (arguments.length == 1 && arguments[0] != null) {
			// is it a UUID?
			final UUID uuidNew = computer_getUUID(null, arguments);
			if (uuidNew != null) {
				if (remoteLocationRequested instanceof UUID) {
					if (!uuidNew.equals(remoteLocationRequested)) {// replacing existing UUID
						remoteLocationRequested = uuidNew;
						tickUpdateParameters = 0;
					}
				} else {
					remoteLocationRequested = uuidNew;
					tickUpdateParameters = 0;
				}
			} else {// new player name
				final String playerNameNew = (String) arguments[0];
				if ( playerNameNew != null
				  && !playerNameNew.equals(remoteLocationRequested) ) {
					remoteLocationRequested = playerNameNew;
					tickUpdateParameters = 0;
				}
			}
		}
		
		// return (updated) value
		if (remoteLocationRequested instanceof VectorI) {
			final VectorI vRemoteLocation = (VectorI) remoteLocationRequested;
			return new Integer[] { vRemoteLocation.x, vRemoteLocation.y, vRemoteLocation.z };
		}
		return new Object[] { remoteLocationRequested == null ? null : remoteLocationRequested.toString() };
	}
	
	@Override
	public Boolean[] lock(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			isLockRequested = Commons.toBool(arguments[0]);
			markDirty();
		}
		return new Boolean[] { isLockRequested };
	}
	
	@Override
	public Double[] energyFactor(final Object[] arguments) {
		try {
			if (arguments.length >= 1) {
				energyFactor = Commons.clamp(1, WarpDriveConfig.TRANSPORTER_ENERGIZING_MAX_ENERGY_FACTOR, Commons.toDouble(arguments[0]));
			}
		} catch (NumberFormatException exception) {
			// ignore
		}
		
		return new Double[] { energyFactor };
	}
	
	@Override
	public Double[] getLockStrength() {
		return new Double[] { lockStrengthActual };
	}
	
	@Override
	public Integer[] getEnergyRequired() {
		return new Integer[] { getEnergyRequired(EnumTransporterState.ACQUIRING), getEnergyRequired(EnumTransporterState.ENERGIZING) };
	}
	
	@Override
	public Boolean[] energize(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
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
	public Object[] transporterName(Context context, Arguments arguments) {
		return transporterName(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) {
		return enable(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] state(Context context, Arguments arguments) {
		return state();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] remoteLocation(Context context, Arguments arguments) {
		return remoteLocation(argumentsOCtoCC(arguments));
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
			if (arguments.length == 1 && arguments[0] != null) {
				setBeamFrequency(Commons.toInt(arguments[0]));
			}
			return new Integer[] { beamFrequency };
			
		case "transporterName":
			return transporterName(arguments);
		
		case "enable":
			return enable(arguments);
		
		case "state":
			return state();
		
		case "remoteLocation":
			return remoteLocation(arguments);
		
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
		return String.format("%s \'%s\' Beam %d @ %s (%d %d %d)",
		                     getClass().getSimpleName(),
		                     transporterName,
		                     beamFrequency,
		                     worldObj == null ? "~NULL~" : worldObj.provider.getDimensionName(),
		                     xCoord, yCoord, zCoord);
	}
}
