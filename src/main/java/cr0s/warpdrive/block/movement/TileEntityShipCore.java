package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IStarMapRegistryTileEntity;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.ShipMovementCosts;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.EnumShipControllerCommand;
import cr0s.warpdrive.data.EnumShipCoreState;
import cr0s.warpdrive.data.EnumShipMovementType;
import cr0s.warpdrive.data.Jumpgate;
import cr0s.warpdrive.data.StarMapRegistryItem.EnumStarMapEntryType;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.event.JumpSequencer;
import cr0s.warpdrive.world.SpaceTeleporter;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityShipCore extends TileEntityAbstractEnergy implements IStarMapRegistryTileEntity {
	
	private static final int LOG_INTERVAL_TICKS = 20 * 60;
	
	// persistent properties
	public ForgeDirection facing;
	public UUID uuid = null;
	public String shipName = "default";
	public double isolationRate = 0.0D;
	private int cooldownTime = 0;
	protected int jumpCount = 0;
	
	// computed properties
	public int maxX, maxY, maxZ;
	public int minX, minY, minZ;
	
	private EnumShipCoreState stateCurrent = EnumShipCoreState.IDLE;
	private EnumShipControllerCommand commandCurrent = EnumShipControllerCommand.IDLE;
	
	private long timeLastShipScanDone = -1;
	public int shipMass;
	public int shipVolume;
	private EnumShipMovementType shipMovementType;
	private ShipMovementCosts shipMovementCosts;
	
	private long distanceSquared = 0; 
	private int warmupTime = 0;
	private boolean isMotionSicknessApplied = false;
	private boolean isSoundPlayed = false;
	private boolean isCooldownReported = false;
	protected int randomWarmupAddition_ticks = 0;
	
	private int registryUpdateTicks = 0;
	private int bootTicks = 20;
	private int logTicks = 120;
	
	private int isolationBlocksCount = 0;
	private int isolationUpdateTicks = 0;
	
	
	public TileEntityShipController controller;
	
	
	public TileEntityShipCore() {
		super();
		peripheralName = "warpdriveShipCore";
		// methodsArray = Arrays.asList("", "");;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		// Always cooldown
		if (cooldownTime > 0) {
			cooldownTime--;
			warmupTime = 0;
			if (cooldownTime == 0 && controller != null) {
				controller.cooldownDone();
			}
		}
		
		// Enforce priority states
		if (cooldownTime > 0) {
			if (stateCurrent != EnumShipCoreState.COOLING_DOWN) {
				stateCurrent = EnumShipCoreState.COOLING_DOWN;
				isCooldownReported = false;
			}
		} else if (controller == null) {
			stateCurrent = EnumShipCoreState.DISCONNECTED;
		} else {
			if (stateCurrent == EnumShipCoreState.DISCONNECTED) {
				stateCurrent = EnumShipCoreState.IDLE;
			}
			if (!controller.isEnabled) {
				stateCurrent = EnumShipCoreState.IDLE;
			}
			if (timeLastShipScanDone <= 0L) {
				stateCurrent = EnumShipCoreState.SCANNING;
			}
		}
		
		// Clear properties
		if ( stateCurrent == EnumShipCoreState.COOLING_DOWN
		  || stateCurrent == EnumShipCoreState.DISCONNECTED
		  || stateCurrent == EnumShipCoreState.IDLE ) {
			warmupTime = 0;
			isMotionSicknessApplied = false;
			isSoundPlayed = false;
		}
			
		// Refresh rendering
		if (getBlockMetadata() != stateCurrent.getMetadata()) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, stateCurrent.getMetadata(), 1 + 2);
		}
				
		// accelerate update ticks during boot
		if (bootTicks > 0) {
			bootTicks--;
			if (controller == null) {
				registryUpdateTicks = 1;
			}
		}
		
		// periodically update starmap registry
		registryUpdateTicks--;
		if (registryUpdateTicks <= 0) {
			registryUpdateTicks = 20 * WarpDriveConfig.STARMAP_REGISTRY_UPDATE_INTERVAL_SECONDS;
			if (uuid == null || (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0)) {
				uuid = UUID.randomUUID();
			}
			// recover registration, shouldn't be needed, in theory...
			WarpDrive.starMap.updateInRegistry(this);
			
			controller = findControllerBlock();
		}
		
		// periodically log the ship state
		logTicks--;
		if (logTicks <= 0) {
			logTicks = LOG_INTERVAL_TICKS;
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " controller is " + controller + ", warmupTime " + warmupTime + ", stateCurrent " + stateCurrent + ", jumpFlag "
						+ (controller == null ? "NA" : controller.isEnabled) + ", cooldownTime " + cooldownTime);
			}
		}
		
		// periodically check isolation blocks
		isolationUpdateTicks--;
		if (isolationUpdateTicks <= 0) {
			isolationUpdateTicks = WarpDriveConfig.SHIP_CORE_ISOLATION_UPDATE_INTERVAL_SECONDS * 20;
			updateIsolationState();
		}
		
		if (controller == null) {
			return;
		}
		
		final StringBuilder reason = new StringBuilder();
				
		final EnumShipControllerCommand commandController = controller.getCommand();
		
		switch (stateCurrent) {
		case DISCONNECTED:
			// empty state, will move directly to IDLE upon next tick
			break;
			
		case IDLE:
			if ( controller.isEnabled
			  && commandController != EnumShipControllerCommand.IDLE
			  && commandController != EnumShipControllerCommand.MAINTENANCE ) {
				commandCurrent = commandController;
				stateCurrent = EnumShipCoreState.ONLINE;
			}
			break;
			
		case COOLING_DOWN:
			// Report cooldown time when command is requested
			if ( controller.isEnabled
			  && commandController != EnumShipControllerCommand.IDLE
			  && commandController != EnumShipControllerCommand.MAINTENANCE ) {
				if (cooldownTime % 20 == 0) {
					int seconds = cooldownTime / 20;
					if (!isCooldownReported || (seconds < 5) || ((seconds < 30) && (seconds % 5 == 0)) || (seconds % 10 == 0)) {
						isCooldownReported = true;
						messageToAllPlayersOnShip("Warp core is cooling down... " + seconds + "s to go...");
					}
				}
			}
			if (cooldownTime <= 0) {
				stateCurrent = EnumShipCoreState.IDLE;
			}
			break;
		
		case SCANNING:
			stateCurrent = EnumShipCoreState.IDLE;
			timeLastShipScanDone = worldObj.getTotalWorldTime();
			if (!validateShipSpatialParameters(reason)) {// @TODO progressive scan
				if (controller.isEnabled) {
					controller.commandDone(false, reason.toString());
				}
			}
			break;
		
		case ONLINE:
			// (disabling will switch back to IDLE and clear variables)
			
			switch (commandCurrent) {
			case SUMMON:
				if (controller.getTargetName() == null || controller.getTargetName().isEmpty()) {
					summonPlayers();
				} else {
					summonSinglePlayer(controller.getTargetName());
				}
				controller.commandDone(true, "Teleportation done");
				break;
			
			case MANUAL:
			case HYPERDRIVE:
			case GATE:
				// initiating jump
				
				// compute random ticks to warmup so it's harder to 'dup' items
				randomWarmupAddition_ticks = worldObj.rand.nextInt(WarpDriveConfig.SHIP_WARMUP_RANDOM_TICKS);
				// compute distance
				distanceSquared = controller.getMovement().getMagnitudeSquared();
				// rescan ship mass/volume if it's too old
				if (timeLastShipScanDone + WarpDriveConfig.SHIP_VOLUME_SCAN_AGE_TOLERANCE_SECONDS < worldObj.getTotalWorldTime()) {
					stateCurrent = EnumShipCoreState.SCANNING;
					break;
				}
				
				messageToAllPlayersOnShip("Running pre-jump checklist...");
				
				// update ship spatial parameters
				if (!validateShipSpatialParameters(reason)) {
					controller.commandDone(false, reason.toString());
					return;
				}
				
				// update movement parameters
				if (!validateShipMovementParameters(reason)) {
					controller.commandDone(false, reason.toString());
					return;
				}
				
				stateCurrent = EnumShipCoreState.WARMING_UP;
				warmupTime = 0;
				break;
			
			default:
				WarpDrive.logger.error(String.format("%s Invalid controller command %s for current state %s", this, commandController, stateCurrent));
				break;
			}
			break;
			
		case WARMING_UP:
			// Compute actual warm-up time
			final int targetWarmup_ticks = shipMovementCosts.warmup_seconds * 20 + randomWarmupAddition_ticks;
			
			// Apply motion sickness as applicable
			if (shipMovementCosts.sickness_seconds > 0) {
				final int motionSicknessThreshold_ticks = targetWarmup_ticks - shipMovementCosts.sickness_seconds * 20 + randomWarmupAddition_ticks / 4; 
				if (!isMotionSicknessApplied && motionSicknessThreshold_ticks <= warmupTime) {
					if (WarpDriveConfig.LOGGING_JUMP) {
						WarpDrive.logger.info(this + " Giving warp sickness to on-board players");
					}
					makePlayersOnShipDrunk(targetWarmup_ticks + WarpDriveConfig.SHIP_WARMUP_RANDOM_TICKS);
					isMotionSicknessApplied = true;
				}
			}
			
			// Select best sound file and adjust offset
			final int soundThreshold;
			final String soundFile;
			if (targetWarmup_ticks < 10 * 20) {
				soundThreshold = targetWarmup_ticks - 4 * 20 + randomWarmupAddition_ticks;
				soundFile = "warpdrive:warp_4s";
			} else if (targetWarmup_ticks > 29 * 20) {
				soundThreshold = targetWarmup_ticks - 30 * 20 + randomWarmupAddition_ticks;
				soundFile = "warpdrive:warp_30s";
			} else {
				soundThreshold = targetWarmup_ticks - 10 * 20 + randomWarmupAddition_ticks;
				soundFile = "warpdrive:warp_10s";
			}
			
			if (!isSoundPlayed && (soundThreshold > warmupTime)) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info(this + " Playing sound effect '" + soundFile + "' soundThreshold " + soundThreshold + " warmupTime " + warmupTime);
				}
				worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, soundFile, 4F, 1F);
				isSoundPlayed = true;
			}
			
			// Awaiting warm-up time
			if (warmupTime < targetWarmup_ticks) {
				warmupTime++;
				break;
			}
			
			warmupTime = 0;
			isMotionSicknessApplied = false;
			isSoundPlayed = false;
			
			if (!validateShipSpatialParameters(reason)) {
				controller.commandDone(false, reason.toString());
				return;
			}
			
			if (WarpDrive.starMap.isWarpCoreIntersectsWithOthers(this)) {
				controller.commandDone(false, "Warp field intersects with other ship's field. Disable the other core to jump.");
				return;
			}
			
			if (WarpDrive.cloaks.isCloaked(worldObj.provider.dimensionId, xCoord, yCoord, zCoord)) {
				controller.commandDone(false, "Core is inside a cloaking field. Aborting. Disable cloaking field to jump!");
				return;
			}
			
			doJump();
			cooldownTime = Math.max(1, shipMovementCosts.cooldown_seconds * 20);
			controller.commandDone(true, "Ok");
			jumpCount++;
			stateCurrent = EnumShipCoreState.COOLING_DOWN;
			isCooldownReported = false;
			break;
			
		default:
			break;
		}
	}
	
	public void messageToAllPlayersOnShip(final String message) {
		final AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		final List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		
		WarpDrive.logger.info(this + " messageToAllPlayersOnShip: " + message);
		for (Object object : list) {
			if (!(object instanceof EntityPlayer)) {
				continue;
			}
			
			Commons.addChatMessage((EntityPlayer) object, "[" + (!shipName.isEmpty() ? shipName : "ShipCore") + "] " + message);
		}
	}
	
	private void updateIsolationState() {
		// Search block in cube around core
		int xMax, yMax, zMax;
		int xMin, yMin, zMin;
		xMin = xCoord - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		xMax = xCoord + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		
		zMin = zCoord - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		zMax = zCoord + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		
		// scan 1 block higher to encourage putting isolation block on both
		// ground and ceiling
		yMin = Math.max(0, yCoord - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1);
		yMax = Math.min(255, yCoord + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1);
		
		int newCount = 0;
		
		// Search for warp isolation blocks
		for (int y = yMin; y <= yMax; y++) {
			for (int x = xMin; x <= xMax; x++) {
				for (int z = zMin; z <= zMax; z++) {
					if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockWarpIsolation)) {
						newCount++;
					}
				}
			}
		}
		isolationBlocksCount = newCount;
		double legacy_isolationRate = isolationRate;
		if (isolationBlocksCount >= WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS) {
			isolationRate = Math.min(1.0, WarpDriveConfig.RADAR_MIN_ISOLATION_EFFECT
					+ (isolationBlocksCount - WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS) // bonus blocks
					* (WarpDriveConfig.RADAR_MAX_ISOLATION_EFFECT - WarpDriveConfig.RADAR_MIN_ISOLATION_EFFECT)
					/ (WarpDriveConfig.RADAR_MAX_ISOLATION_BLOCKS - WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS));
		} else {
			isolationRate = 0.0D;
		}
		if (WarpDriveConfig.LOGGING_RADAR && (WarpDrive.isDev || legacy_isolationRate != isolationRate)) {
			WarpDrive.logger.info(String.format("%s Isolation updated to %d (%.1f%%)",
			                                    this, isolationBlocksCount , isolationRate * 100.0));
		}
	}
	
	private void makePlayersOnShipDrunk(final int tickDuration) {
		final AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		final List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		
		for (Object object : list) {
			if (object == null || !(object instanceof EntityPlayer)) {
				continue;
			}
			
			// Set "drunk" effect
			((EntityPlayer) object).addPotionEffect(new PotionEffect(Potion.confusion.id, tickDuration, 0, true));
		}
	}
	
	private void summonPlayers() {
		final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		
		for (int i = 0; i < controller.players.size(); i++) {
			final String playerName = controller.players.get(i);
			final EntityPlayerMP entityPlayerMP = MinecraftServer.getServer().getConfigurationManager().func_152612_a(playerName);
			
			if ( entityPlayerMP != null
			  && isOutsideBB(aabb, MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ)) ) {
				summonPlayer(entityPlayerMP);
			}
		}
	}
	
	private void summonSinglePlayer(final String nickname) {
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		
		for (int i = 0; i < controller.players.size(); i++) {
			final String playerName = controller.players.get(i);
			final EntityPlayerMP entityPlayerMP = MinecraftServer.getServer().getConfigurationManager().func_152612_a(playerName);
			
			if ( entityPlayerMP != null && playerName.equals(nickname)
			  && isOutsideBB(aabb, MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ)) ) {
				summonPlayer(entityPlayerMP);
				return;
			}
		}
	}
	
	public boolean summonOwnerOnDeploy(final EntityPlayerMP entityPlayerMP) {
		if (entityPlayerMP == null) {
			WarpDrive.logger.warn(this + " No player given to summonOwnerOnDeploy()");
			return false;
		}
		final StringBuilder reason = new StringBuilder();
		if (!validateShipSpatialParameters(reason)) {
			Commons.addChatMessage(entityPlayerMP, "[" + (!shipName.isEmpty() ? shipName : "ShipCore") + "] §c" + reason.toString());
			return false;
		}
		
		controller = findControllerBlock();
		if (controller != null) {
			controller.players.clear();
			controller.players.add(entityPlayerMP.getCommandSenderName());
		} else {
			WarpDrive.logger.warn(this + " Failed to find controller block");
			return false;
		}
		
		final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		if (isOutsideBB(aabb, MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ))) {
			summonPlayer(entityPlayerMP);
		}
		return true;
	}
	
	private static final VectorI[] SUMMON_OFFSETS = { new VectorI(2, 0, 0), new VectorI(-1, 0, 0),
		new VectorI(2, 0, 1), new VectorI(2, 0, -1), new VectorI(-1, 0, 1), new VectorI(-1, 0, -1),
		new VectorI(1, 0, 1), new VectorI(1, 0, -1), new VectorI( 0, 0, 1), new VectorI( 0, 0, -1) };
	private void summonPlayer(final EntityPlayerMP entityPlayer) {
		// validate distance
		double distance = Math.sqrt(new VectorI(entityPlayer).distance2To(this));
		if (entityPlayer.worldObj != this.worldObj) {
			distance += 256;
			if (!WarpDriveConfig.SHIP_SUMMON_ACROSS_DIMENSIONS) {
				messageToAllPlayersOnShip("§c" + String.format("%1$s is in a different dimension, too far away to be summoned", entityPlayer.getDisplayName()));
				return;
			}
		}
		if (WarpDriveConfig.SHIP_SUMMON_MAX_RANGE >= 0 && distance > WarpDriveConfig.SHIP_SUMMON_MAX_RANGE) {
			messageToAllPlayersOnShip("§c" + String.format("%1$s is too far away to be summoned (max. is %2$d m)", entityPlayer.getDisplayName(), WarpDriveConfig.SHIP_SUMMON_MAX_RANGE));
			Commons.addChatMessage(entityPlayer, "§c" + String.format("You are to far away to be summoned aboard '%1$s' (max. is %2$d m)", shipName, WarpDriveConfig.SHIP_SUMMON_MAX_RANGE));
			return;
		}
		
		// find a free spot
		for (VectorI vOffset : SUMMON_OFFSETS) {
			VectorI vPosition = new VectorI(
				xCoord + facing.offsetX * vOffset.x + facing.offsetZ * vOffset.z,
			    yCoord,
			    zCoord + facing.offsetZ * vOffset.x + facing.offsetX * vOffset.z);
			if (worldObj.isAirBlock(vPosition.x, vPosition.y, vPosition.z)) {
				if (worldObj.isAirBlock(vPosition.x, vPosition.y + 1, vPosition.z)) {
					summonPlayer(entityPlayer, vPosition.x, vPosition.y, vPosition.z);
					return;
				}
				if (worldObj.isAirBlock(vPosition.x, vPosition.y - 1, vPosition.z)) {
					summonPlayer(entityPlayer, vPosition.x, vPosition.y - 1, vPosition.z);
					return;
				}
			} else if ( worldObj.isAirBlock(vPosition.x, vPosition.y - 1, vPosition.z)
			         && worldObj.isAirBlock(vPosition.x, vPosition.y - 2, vPosition.z)
			         && !worldObj.isAirBlock(vPosition.x, vPosition.y - 3, vPosition.z)  ) {
				summonPlayer(entityPlayer, vPosition.x, vPosition.y - 2, vPosition.z);
				return;
			} else if ( worldObj.isAirBlock(vPosition.x, vPosition.y + 1, vPosition.z)
			         && worldObj.isAirBlock(vPosition.x, vPosition.y + 2, vPosition.z)
			         && !worldObj.isAirBlock(vPosition.x, vPosition.y, vPosition.z)  ) {
				summonPlayer(entityPlayer, vPosition.x, vPosition.y + 1, vPosition.z);
				return;
			}
		}
		final String message = "§c" + String.format("No safe spot found to summon player %1$s", entityPlayer.getDisplayName());
		messageToAllPlayersOnShip(message);
		Commons.addChatMessage(entityPlayer, message);
	}
	
	private void summonPlayer(EntityPlayerMP player, final int x, final int y, final int z) {
		if (energy_consume(WarpDriveConfig.SHIP_TELEPORT_ENERGY_PER_ENTITY, false)) {
			if (player.dimension != worldObj.provider.dimensionId) {
				player.mcServer.getConfigurationManager().transferPlayerToDimension(
					player,
					worldObj.provider.dimensionId,
					new SpaceTeleporter(
						DimensionManager.getWorld(worldObj.provider.dimensionId),
						0,
						MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
				player.setPositionAndUpdate(x + 0.5d, y, z + 0.5d);
				player.sendPlayerAbilities();
			} else {
				player.setPositionAndUpdate(x + 0.5d, y, z + 0.5d);
			}
		}
	}
	
	public boolean validateShipSpatialParameters(final StringBuilder reason) {
		if (controller == null) {
			reason.append("TileEntityShipCore.validateShipSpatialParameters: no controller detected!");
			return false;
		}
		int shipFront, shipBack;
		int shipLeft, shipRight;
		int shipUp, shipDown;
		shipFront = controller.getFront();
		shipRight = controller.getRight();
		shipUp = controller.getUp();
		shipBack = controller.getBack();
		shipLeft = controller.getLeft();
		shipDown = controller.getDown();
		
		int x1 = 0, x2 = 0, z1 = 0, z2 = 0;
		
		if (facing.offsetX == 1) {
			x1 = xCoord - shipBack;
			x2 = xCoord + shipFront;
			z1 = zCoord - shipLeft;
			z2 = zCoord + shipRight;
		} else if (facing.offsetX == -1) {
			x1 = xCoord - shipFront;
			x2 = xCoord + shipBack;
			z1 = zCoord - shipRight;
			z2 = zCoord + shipLeft;
		} else if (facing.offsetZ == 1) {
			z1 = zCoord - shipBack;
			z2 = zCoord + shipFront;
			x1 = xCoord - shipRight;
			x2 = xCoord + shipLeft;
		} else if (facing.offsetZ == -1) {
			z1 = zCoord - shipFront;
			z2 = zCoord + shipBack;
			x1 = xCoord - shipLeft;
			x2 = xCoord + shipRight;
		}
		
		if (x1 < x2) {
			minX = x1;
			maxX = x2;
		} else {
			minX = x2;
			maxX = x1;
		}
		
		if (z1 < z2) {
			minZ = z1;
			maxZ = z2;
		} else {
			minZ = z2;
			maxZ = z1;
		}
		
		minY = yCoord - shipDown;
		maxY = yCoord + shipUp;
		
		// Ship side is too big
		if ( (shipBack + shipFront) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE
		  || (shipLeft + shipRight) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE
		  || (shipDown + shipUp) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE) {
			reason.append(String.format("Ship is too big (max is %d per side)",
				WarpDriveConfig.SHIP_MAX_SIDE_SIZE));
			return false;
		}
		
		boolean isUnlimited = false;
		final AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		final List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		for (Object object : list) {
			if (object == null || !(object instanceof EntityPlayer)) {
				continue;
			}
			
			final String displayName = ((EntityPlayer) object).getDisplayName();
			for (final String nameUnlimited : WarpDriveConfig.SHIP_VOLUME_UNLIMITED_PLAYERNAMES) {
				isUnlimited = isUnlimited || nameUnlimited.equals(displayName);
			}
		}
		
		updateShipMassAndVolume();
		if ( !isUnlimited
		  && shipMass > WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE
		  && CelestialObjectManager.isPlanet(worldObj, xCoord, zCoord) ) {
			reason.append(String.format("Ship is too big for a planet (max is %d blocks)",
				WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE));
			return false;
		}
		
		return true;
	}
	
	protected boolean validateShipMovementParameters(final StringBuilder reason) {
		shipMovementType = EnumShipMovementType.compute(worldObj, xCoord, minY, maxY, zCoord, commandCurrent, controller.getMovement().y, reason);
		if (shipMovementType == null) {
			return false;
		}
		
		// compute movement costs
		shipMovementCosts = new ShipMovementCosts(worldObj, xCoord, yCoord, zCoord, shipMass, shipMovementType, (int) Math.ceil(Math.sqrt(distanceSquared)));
		return true;
	}
	
	// Computer interface are running independently of updateTicks, hence doing local computations getMaxJumpDistance() and getEnergyRequired()
	protected int getMaxJumpDistance(final EnumShipControllerCommand command, final StringBuilder reason) {
		final EnumShipMovementType shipMovementType = EnumShipMovementType.compute(worldObj, xCoord, minY, maxY, zCoord, command, controller.getMovement().y, reason);
		if (shipMovementType == null) {
			controller.commandDone(false, reason.toString());
			return -1;
		}
		
		// compute movement costs
		final ShipMovementCosts shipMovementCosts = new ShipMovementCosts(worldObj, xCoord, yCoord, zCoord, shipMass, shipMovementType, (int) Math.ceil(Math.sqrt(distanceSquared)));
		return shipMovementCosts.maximumDistance_blocks;
	}
	
	protected int getEnergyRequired(final EnumShipControllerCommand command, final StringBuilder reason) {
		final EnumShipMovementType shipMovementType = EnumShipMovementType.compute(worldObj, xCoord, minY, maxY, zCoord, command, controller.getMovement().y, reason);
		if (shipMovementType == null) {
			controller.commandDone(false, reason.toString());
			return -1;
		}
		
		// compute movement costs
		final ShipMovementCosts shipMovementCosts = new ShipMovementCosts(worldObj, xCoord, yCoord, zCoord, shipMass, shipMovementType, (int) Math.ceil(Math.sqrt(distanceSquared)));
		return shipMovementCosts.energyRequired;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isShipInJumpgate(final Jumpgate jumpgate, final StringBuilder reason) {
		final AxisAlignedBB aabb = jumpgate.getGateAABB();
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jumpgate " + jumpgate.name + " AABB is " + aabb);
		}
		int countBlocksInside = 0;
		int countBlocksTotal = 0;
		
		if ( aabb.isVecInside(Vec3.createVectorHelper(minX, minY, minZ))
		  && aabb.isVecInside(Vec3.createVectorHelper(maxX, maxY, maxZ)) ) {
			// fully inside
			return true;
		}
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					Block block = worldObj.getBlock(x, y, z);
					
					// Skipping vanilla air & ignored blocks
					if (block == Blocks.air || Dictionary.BLOCKS_LEFTBEHIND.contains(block)) {
						continue;
					}
					if (Dictionary.BLOCKS_NOMASS.contains(block)) {
						continue;
					}
					
					if (aabb.minX <= x && aabb.maxX >= x && aabb.minY <= y && aabb.maxY >= y && aabb.minZ <= z && aabb.maxZ >= z) {
						countBlocksInside++;
					}
					countBlocksTotal++;
				}
			}
		}
		
		float percent = 0F;
		if (shipMass != 0) {
			percent = Math.round((((countBlocksInside * 1.0F) / shipMass) * 100.0F) * 10.0F) / 10.0F;
		}
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			if (shipMass != countBlocksTotal) {
				WarpDrive.logger.info(this + " Ship mass has changed from " + shipMass + " to " + countBlocksTotal + " blocks");
			}
			WarpDrive.logger.info(this + "Ship has " + countBlocksInside + " / " + shipMass + " blocks (" + percent + "%) in jumpgate '" + jumpgate.name + "'");
		}
		
		// At least 80% of ship must be inside jumpgate
		if (percent > 80F) {
			return true;
		} else if (percent <= 0.001) {
			reason.append(String.format("Ship is not inside a jumpgate. Jump rejected. Nearest jumpgate is %s",
				jumpgate.toNiceString()));
			return false;
		} else {
			reason.append(String.format("Ship is only %.1f%% inside a jumpgate. Sorry, we'll loose too much crew as is, jump rejected.",
				percent));
			return false;
		}
	}
	
	private boolean isFreePlaceForShip(int destX, int destY, int destZ) {
		int newX, newZ;
		
		if (controller == null || destY + controller.getUp() > 255 || destY - controller.getDown() < 5) {
			return false;
		}
		
		int moveX = destX - xCoord;
		int moveY = destY - yCoord;
		int moveZ = destZ - zCoord;
		
		for (int x = minX; x <= maxX; x++) {
			newX = moveX + x;
			for (int z = minZ; z <= maxZ; z++) {
				newZ = moveZ + z;
				for (int y = minY; y <= maxY; y++) {
					Block blockSource = worldObj.getBlock(x, y, z);
					Block blockTarget = worldObj.getBlock(newX, moveY + y, newZ);
					
					// not vanilla air nor ignored blocks at source
					// not vanilla air nor expandable blocks are target location
					if ( blockSource != Blocks.air
					  && !Dictionary.BLOCKS_EXPANDABLE.contains(blockSource)
					  && blockTarget != Blocks.air
					  && !Dictionary.BLOCKS_EXPANDABLE.contains(blockTarget)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private void doGateJump() {
		// Search nearest jump-gate
		final String targetName = controller.getTargetName();
		final Jumpgate targetGate = WarpDrive.jumpgates.findGateByName(targetName);
		
		if (targetGate == null) {
			controller.commandDone(false, String.format("Destination jumpgate '%s' is unknown. Check jumpgate name.", targetName));
			return;
		}
		
		// Now make jump to a beacon
		int gateX = targetGate.xCoord;
		int gateY = targetGate.yCoord;
		int gateZ = targetGate.zCoord;
		int destX = gateX;
		int destY = gateY;
		int destZ = gateZ;
		Jumpgate nearestGate = WarpDrive.jumpgates.findNearestGate(xCoord, yCoord, zCoord);
		
		final StringBuilder reason = new StringBuilder();
		if (!isShipInJumpgate(nearestGate, reason)) {
			controller.commandDone(false, reason.toString());
			return;
		}
		
		// If gate is blocked by obstacle
		if (!isFreePlaceForShip(gateX, gateY, gateZ)) {
			// Randomize destination coordinates and check for collision with obstacles around jumpgate
			// Try to find good place for ship
			int numTries = 10; // num tries to check for collision
			boolean placeFound = false;
			
			for (; numTries > 0; numTries--) {
				// randomize destination coordinates around jumpgate
				destX = gateX + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(100));
				destZ = gateZ + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(100));
				destY = gateY + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(50));
				
				// check for collision
				if (isFreePlaceForShip(destX, destY, destZ)) {
					placeFound = true;
					break;
				}
			}
			
			if (!placeFound) {
				controller.commandDone(false, "Destination gate is blocked by obstacles. Aborting...");
				return;
			}
			
			WarpDrive.logger.info("[GATE] Place found over " + (10 - numTries) + " tries.");
		}
		
		// Consume energy
		if (energy_consume(shipMovementCosts.energyRequired, false)) {
			WarpDrive.logger.info(this + " Moving ship to a place around gate '" + targetGate.name + "' (" + destX + "; " + destY + "; " + destZ + ")");
			JumpSequencer jump = new JumpSequencer(this, EnumShipMovementType.GATE_ACTIVATING, targetName, 0, 0, 0, (byte) 0, destX, destY, destZ);
			jump.enable();
		} else {
			messageToAllPlayersOnShip("Insufficient energy level");
		}
	}
	
	private void doJump() {
		
		final int requiredEnergy = shipMovementCosts.energyRequired;
		
		if (!energy_consume(requiredEnergy, true)) {
			controller.commandDone(false, String.format("Insufficient energy to jump! Core is currently charged with %d EU while jump requires %d EU",
			                                            energy_getEnergyStored(), requiredEnergy));
			return;
		}
		
		String shipInfo = "" + shipVolume + " blocks inside (" + minX + ", " + minY + ", " + minZ + ") to (" + maxX + ", " + maxY + ", " + maxZ + ") with an actual mass of " + shipMass + " blocks";
		switch (commandCurrent) {
		case GATE:
			WarpDrive.logger.info(this + " Performing gate jump of " + shipInfo);
			doGateJump();
			return;
			
		case HYPERDRIVE:
			WarpDrive.logger.info(this + " Performing hyperdrive jump of " + shipInfo);
			
			// Check ship size for hyper-space jump
			if (shipMass < WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE) {
				Jumpgate nearestGate = null;
				if (WarpDrive.jumpgates == null) {
					WarpDrive.logger.warn(this + " WarpDrive.jumpGates is NULL!");
				} else {
					nearestGate = WarpDrive.jumpgates.findNearestGate(xCoord, yCoord, zCoord);
				}
				
				final StringBuilder reason = new StringBuilder();
				if (nearestGate == null || !isShipInJumpgate(nearestGate, reason)) {
					controller.commandDone(false, String.format("Ship is too small (%d/%d).\nInsufficient ship mass to engage alcubierre drive.\nIncrease your mass or use a jumpgate to reach or exit hyperspace.",
					                                            shipMass, WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE));
					return;
				}
			}
			break;
			
		case MANUAL:
			WarpDrive.logger.info(String.format("%s Performing manual jump of %s, %s, movement %s, rotationSteps %d",
			                                    this, shipInfo, shipMovementType, controller.getMovement(), controller.getRotationSteps()));
			break;
			
		default:
			WarpDrive.logger.error(String.format("%s Aborting while trying to perform invalid jump command %s",
			                                     this, commandCurrent));
			controller.commandDone(false, "Internal error, check console for details");
			commandCurrent = EnumShipControllerCommand.IDLE;
			stateCurrent = EnumShipCoreState.DISCONNECTED;
			return;
		}
		
		if (!energy_consume(requiredEnergy, false)) {
			controller.commandDone(false, "Insufficient energy level");
			return;
		}
		
		int moveX = 0;
		int moveY = 0;
		int moveZ = 0;
		
		if (commandCurrent != EnumShipControllerCommand.HYPERDRIVE) {
			VectorI movement = controller.getMovement();
			VectorI shipSize = new VectorI(controller.getFront() + 1 + controller.getBack(),
			                               controller.getUp()    + 1 + controller.getDown(),
			                               controller.getRight() + 1 + controller.getLeft());
			final int maxDistance = shipMovementCosts.maximumDistance_blocks;
			if (Math.abs(movement.x) - shipSize.x > maxDistance) {
				movement.x = (int) Math.signum(movement.x) * (shipSize.x + maxDistance);
			}
			if (Math.abs(movement.y) - shipSize.y > maxDistance) {
				movement.y = (int) Math.signum(movement.y) * (shipSize.y + maxDistance);
			}
			if (Math.abs(movement.z) - shipSize.z > maxDistance) {
				movement.z = (int) Math.signum(movement.z) * (shipSize.z + maxDistance);
			}
			moveX = facing.offsetX * movement.x - facing.offsetZ * movement.z;
			moveY = movement.y;
			moveZ = facing.offsetZ * movement.x + facing.offsetX * movement.z;
		}
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Movement adjusted to (" + moveX + " " + moveY + " " + moveZ + ") blocks.");
		}
		final JumpSequencer jump = new JumpSequencer(this, shipMovementType, null,
				moveX, moveY, moveZ, controller.getRotationSteps(),
				0, 0, 0);
		jump.enable();
	}
	
	private static boolean isOutsideBB(AxisAlignedBB axisalignedbb, int x, int y, int z) {
		return axisalignedbb.minX > x || axisalignedbb.maxX < x
		    || axisalignedbb.minY > y || axisalignedbb.maxY < y
		    || axisalignedbb.minZ > z || axisalignedbb.maxZ < z;
	}
	
	@Override
	public String getStatus() {
		return super.getStatus()
			+ ((cooldownTime > 0) ? "\n" + StatCollector.translateToLocalFormatted("warpdrive.ship.statusLine.cooling", cooldownTime / 20) : "")
			+ ((isolationBlocksCount > 0) ? "\n" + StatCollector.translateToLocalFormatted("warpdrive.ship.statusLine.isolation", isolationBlocksCount, isolationRate * 100.0) : "");
	}
	
	private void updateShipMassAndVolume() {
		int newMass = 0;
		int newVolume = 0;
		
		try {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					for (int y = minY; y <= maxY; y++) {
						Block block = worldObj.getBlock(x, y, z);
						
						// Skipping vanilla air & ignored blocks
						if (block == Blocks.air || Dictionary.BLOCKS_LEFTBEHIND.contains(block)) {
							continue;
						}
						newVolume++;
						
						if (Dictionary.BLOCKS_NOMASS.contains(block)) {
							continue;
						}
						newMass++;
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		shipMass = newMass;
		shipVolume = newVolume;
	}
	
	private TileEntityShipController findControllerBlock() {
		TileEntity tileEntity;
		tileEntity = worldObj.getTileEntity(xCoord + 1, yCoord, zCoord);
		
		if (tileEntity != null && tileEntity instanceof TileEntityShipController) {
			facing = ForgeDirection.EAST;
			return (TileEntityShipController) tileEntity;
		}
		
		tileEntity = worldObj.getTileEntity(xCoord - 1, yCoord, zCoord);
		
		if (tileEntity != null && tileEntity instanceof TileEntityShipController) {
			facing = ForgeDirection.WEST;
			return (TileEntityShipController) tileEntity;
		}
		
		tileEntity = worldObj.getTileEntity(xCoord, yCoord, zCoord + 1);
		
		if (tileEntity != null && tileEntity instanceof TileEntityShipController) {
			facing = ForgeDirection.SOUTH;
			return (TileEntityShipController) tileEntity;
		}
		
		tileEntity = worldObj.getTileEntity(xCoord, yCoord, zCoord - 1);
		
		if (tileEntity != null && tileEntity instanceof TileEntityShipController) {
			facing = ForgeDirection.NORTH;
			return (TileEntityShipController) tileEntity;
		}
		
		return null;
	}
	
	public int getCooldown() {
		return cooldownTime;
	}
	
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.SHIP_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(ForgeDirection from) {
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		facing = ForgeDirection.getOrientation(tag.getInteger("facing"));
		uuid = new UUID(tag.getLong("uuidMost"), tag.getLong("uuidLeast"));
		if (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0) {
			uuid = UUID.randomUUID();
		}
		shipName = tag.getString("corefrequency") + tag.getString("shipName");	// coreFrequency is the legacy tag name
		isolationRate = tag.getDouble("isolationRate");
		cooldownTime = tag.getInteger("cooldownTime");
		jumpCount = tag.getInteger("jumpCount");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (facing != null) {
			tag.setInteger("facing", facing.ordinal());
		}
		if (uuid != null) {
			tag.setLong("uuidMost", uuid.getMostSignificantBits());
			tag.setLong("uuidLeast", uuid.getLeastSignificantBits());
		}
		tag.setString("shipName", shipName);
		tag.setDouble("isolationRate", isolationRate);
		tag.setInteger("cooldownTime", cooldownTime);
		tag.setInteger("jumpCount", jumpCount);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
	}
	
	@Override
	public void validate() {
		super.validate();
		
		if (worldObj.isRemote) {
			return;
		}
		
		WarpDrive.starMap.updateInRegistry(this);
	}
	
	@Override
	public void invalidate() {
		if (!worldObj.isRemote) {
			WarpDrive.starMap.removeFromRegistry(this);
		}
		super.invalidate();
	}
	
	// IStarMapRegistryTileEntity overrides
	@Override
	public String getStarMapType() {
		return EnumStarMapEntryType.SHIP.getName();
	}
	
	@Override
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public AxisAlignedBB getStarMapArea() {
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	@Override
	public int getMass() {
		return shipMass;
	}
	
	@Override
	public double getIsolationRate() {
		return isolationRate;
	}
	
	@Override
	public String getStarMapName() {
		return shipName;
	}
	
	@Override
	public void onBlockUpdatedInArea(final VectorI vector, final Block block, final int metadata) {
		// no operation
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s \'%s\' @ \'%s\' (%d %d %d)",
			getClass().getSimpleName(), shipName, worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			xCoord, yCoord, zCoord);
	}
}
