package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IStarMapRegistryTileEntity;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Jumpgate;
import cr0s.warpdrive.data.SoundEvents;
import cr0s.warpdrive.data.StarMapRegistryItem.EnumStarMapEntryType;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.event.JumpSequencer;
import cr0s.warpdrive.world.SpaceTeleporter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.common.DimensionManager;


public class TileEntityShipCore extends TileEntityAbstractEnergy implements IStarMapRegistryTileEntity {
	
	public int dx, dz;
	private int direction;
	
	public int maxX, maxY, maxZ;
	public int minX, minY, minZ;
	
	public int shipMass;
	public int shipVolume;
	private EnumShipCoreMode currentMode = EnumShipCoreMode.IDLE;
	
	public enum EnumShipCoreMode {
		IDLE(0),
		BASIC_JUMP(1),		// 0-128
		LONG_JUMP(2),		// 0-12800
		// TELEPORT(3),
		BEACON_JUMP(4),		// Jump ship by beacon
		HYPERSPACE(5),		// Jump to/from Hyperspace
		GATE_JUMP(6);		// Jump via jumpgate
		
		private final int code;
		
		EnumShipCoreMode(int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}
	
	private int warmupTime = 0;
	private int cooldownTime = 0;
	private boolean isCooldownReported = false;
	protected int randomWarmupAddition = 0;
	
	private final int registryUpdateInterval_ticks = 20 * WarpDriveConfig.STARMAP_REGISTRY_UPDATE_INTERVAL_SECONDS;
	private int registryUpdateTicks = 0;
	private int bootTicks = 20;
	private static final int logInterval_ticks = 20 * 60;
	private int logTicks = 120;
	
	public UUID uuid = null;
	public String shipName = "default";
	
	private int isolationBlocksCount = 0;
	public double isolationRate = 0.0D;
	private int isolationUpdateTicks = 0;
	
	public int jumpCount = 0;
	
	public TileEntityShipController controller;
	
	private boolean soundPlayed = false;
	
	public TileEntityShipCore() {
		super();
		peripheralName = "warpdriveShipCore";
		// methodsArray = Arrays.asList("", "");
	}
	
	@Override
	public void update() {
		super.update();
		
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
		
		// Update state
		if (cooldownTime > 0) { // cooling down (2)
			if (getBlockMetadata() != 2) {
				updateMetadata(2);
			}
		} else if (controller == null) { // not connected (0)
			if (getBlockMetadata() != 0) {
				updateMetadata(0);
			}
		} else if (controller.isJumpFlag() || controller.isSummonAllFlag() || !controller.getToSummon().isEmpty()) { // active (1)
			if (getBlockMetadata() != 1) {
				updateMetadata(1);
			}
		} else { // inactive
			if (getBlockMetadata() != 0) {
				updateMetadata(0);
			}
		}
		
		// Update warp core in cores registry
		// accelerate update ticks during boot
		if (bootTicks > 0) {
			bootTicks--;
			if (controller == null) {
				registryUpdateTicks = 1;
			}
		}
		registryUpdateTicks--;
		if (registryUpdateTicks <= 0) {
			registryUpdateTicks = registryUpdateInterval_ticks;
			if (uuid == null || (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0)) {
				uuid = UUID.randomUUID();
			}
			// recover registration, shouldn't be needed, in theory...
			WarpDrive.starMap.updateInRegistry(this);
			
			TileEntity controllerFound = findControllerBlock();
			if (controllerFound == null) {
				controller = null;
				warmupTime = 0;
				soundPlayed = false;
				return;
			}
			controller = (TileEntityShipController) controllerFound;
		}
		
		logTicks--;
		if (logTicks <= 0) {
			logTicks = logInterval_ticks;
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " controller is " + controller + ", warmupTime " + warmupTime + ", currentMode " + currentMode + ", jumpFlag "
						+ (controller == null ? "NA" : controller.isJumpFlag()) + ", cooldownTime " + cooldownTime);
			}
		}
		
		isolationUpdateTicks++;
		if (isolationUpdateTicks > WarpDriveConfig.SHIP_CORE_ISOLATION_UPDATE_INTERVAL_SECONDS * 20) {
			isolationUpdateTicks = 0;
			updateIsolationState();
		}
		
		if (controller == null) {
			return;
		}
		
		currentMode = controller.getMode();
		
		StringBuilder reason = new StringBuilder();
		
		if ((controller.isJumpFlag() && (isolationUpdateTicks == 1)) || controller.isSummonAllFlag() || !controller.getToSummon().isEmpty()) {
			if (!validateShipSpatialParameters(reason)) {
				if (controller.isJumpFlag()) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip(reason.toString());
				}
				warmupTime = 0;
				soundPlayed = false;
				return;
			}
			
			if (controller.isSummonAllFlag()) {
				summonPlayers();
				controller.setSummonAllFlag(false);
			} else if (!controller.getToSummon().isEmpty()) {
				summonSinglePlayer(controller.getToSummon());
				controller.setToSummon("");
			}
		}
		
		switch (currentMode) {
		case BASIC_JUMP:
		case LONG_JUMP:
		case BEACON_JUMP:
		case HYPERSPACE:
		case GATE_JUMP:
			if (controller.isJumpFlag()) {
				// Compute warm-up time
				int targetWarmup;
				switch (currentMode) {
				case BASIC_JUMP:
				case LONG_JUMP:
					if (controller.getDistance() < 50) {
						targetWarmup = WarpDriveConfig.SHIP_SHORTJUMP_WARMUP_SECONDS * 20;
					} else {
						targetWarmup = WarpDriveConfig.SHIP_LONGJUMP_WARMUP_SECONDS * 20;
					}
					break;
					
				case BEACON_JUMP:
				case HYPERSPACE:
				case GATE_JUMP:
				default:
					targetWarmup = WarpDriveConfig.SHIP_LONGJUMP_WARMUP_SECONDS * 20;
					break;
				}
				// Select best sound file and adjust offset
				int soundThreshold;
				SoundEvent soundEvent;
				if (targetWarmup < 10 * 20) {
					soundThreshold = targetWarmup - 4 * 20;
					soundEvent = SoundEvents.WARP_4_SECONDS;
				} else if (targetWarmup > 29 * 20) {
					soundThreshold = targetWarmup - 30 * 20;
					soundEvent = SoundEvents.WARP_30_SECONDS;
				} else {
					soundThreshold = targetWarmup - 10 * 20;
					soundEvent = SoundEvents.WARP_10_SECONDS;
				}
				// Add random duration
				soundThreshold += randomWarmupAddition;
				
				// Check cooldown time
				if (cooldownTime > 0) {
					if (cooldownTime % 20 == 0) {
						int seconds = cooldownTime / 20;
						if (!isCooldownReported || (seconds < 5) || ((seconds < 30) && (seconds % 5 == 0)) || (seconds % 10 == 0)) {
							isCooldownReported = true;
							messageToAllPlayersOnShip("Warp core is cooling down... " + seconds + "s to go...");
						}
					}
					return;
				}
				
				// Set up activated animation
				if (warmupTime == 0) {
					messageToAllPlayersOnShip("Running pre-jump checklist...");
					
					// update ship parameters
					if (!validateShipSpatialParameters(reason)) {
						controller.setJumpFlag(false);
						messageToAllPlayersOnShip(reason.toString());
						return;
					}
					if (WarpDriveConfig.SHIP_WARMUP_SICKNESS) {
						if (WarpDriveConfig.LOGGING_JUMP) {
							WarpDrive.logger.info(this + " Giving warp sickness targetWarmup " + targetWarmup + " distance " + controller.getDistance());
						}
						makePlayersOnShipDrunk(targetWarmup + WarpDriveConfig.SHIP_WARMUP_RANDOM_TICKS);
					}
				}
				
				if (!soundPlayed && (soundThreshold > warmupTime)) {
					if (WarpDriveConfig.LOGGING_JUMP) {
						WarpDrive.logger.info(this + " Playing sound effect " + soundEvent + " soundThreshold " + soundThreshold + " warmupTime " + warmupTime);
					}
					worldObj.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 4.0F, 1.0F);
					soundPlayed = true;
				}
				
				// Awaiting cool-down time
				if (warmupTime < (targetWarmup + randomWarmupAddition)) {
					warmupTime++;
					return;
				}
				
				warmupTime = 0;
				soundPlayed = false;
				
				if (!validateShipSpatialParameters(reason)) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip(reason.toString());
					return;
				}
				
				if (WarpDrive.starMap.isWarpCoreIntersectsWithOthers(this)) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip("Warp field intersects with other ship's field. Cannot jump.");
					return;
				}
				
				if (WarpDrive.cloaks.isCloaked(worldObj.provider.getDimension(), pos)) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip("Core is inside a cloaking field. Aborting. Disable cloaking field to jump!");
					return;
				}
				
				doJump();
				cooldownTime = Math.max(1, WarpDriveConfig.SHIP_COOLDOWN_INTERVAL_SECONDS * 20);
				isCooldownReported = false;
				controller.setJumpFlag(false);
				jumpCount++;
			} else {
				warmupTime = 0;
			}
			break;
		default:
			break;
		}
	}

	@Deprecated
	private void messageToAllPlayersOnShip(String string) {
		messageToAllPlayersOnShip(new TextComponentString(string));
	}
	public void messageToAllPlayersOnShip(ITextComponent textComponent) {
		AxisAlignedBB axisalignedbb = new AxisAlignedBB(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		
		WarpDrive.logger.info(this + " messageToAllPlayersOnShip: " + textComponent.getFormattedText());
		for (Object object : list) {
			if (!(object instanceof EntityPlayer)) {
				continue;
			}
			
			Commons.addChatMessage((EntityPlayer) object, new TextComponentString("[" + (!shipName.isEmpty() ? shipName : "ShipCore") + "] ").appendSibling(textComponent));
		}
	}
	
	private void updateIsolationState() {
		// Search block in cube around core
		int xMax, yMax, zMax;
		int xMin, yMin, zMin;
		xMin = pos.getX() - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		xMax = pos.getX() + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		
		zMin = pos.getZ() - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		zMax = pos.getZ() + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		
		// scan 1 block higher to encourage putting isolation block on both
		// ground and ceiling
		yMin = Math.max(  0, pos.getY() - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1);
		yMax = Math.min(255, pos.getY() + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1);
		
		int newCount = 0;
		
		// Search for warp isolation blocks
		for (int y = yMin; y <= yMax; y++) {
			for (int x = xMin; x <= xMax; x++) {
				for (int z = zMin; z <= zMax; z++) {
					if (worldObj.getBlockState(new BlockPos(x, y, z)).getBlock() == WarpDrive.blockWarpIsolation) {
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
			WarpDrive.logger.info(this + " Isolation updated to " + isolationBlocksCount + " (" + String.format("%.1f", isolationRate * 100.0) + "%)");
		}
	}
	
	private void makePlayersOnShipDrunk(int tickDuration) {
		AxisAlignedBB axisalignedbb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		
		for (Object object : list) {
			if (object == null || !(object instanceof EntityPlayer)) {
				continue;
			}
			
			// Set "drunk" effect
			((EntityPlayer) object).addPotionEffect(
					new PotionEffect(MobEffects.NAUSEA, tickDuration, 0, true, true));
		}
	}
	
	private void summonPlayers() {
		AxisAlignedBB aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		
		for (int i = 0; i < controller.players.size(); i++) {
			String nick = controller.players.get(i);
			@SuppressWarnings("ConstantConditions") EntityPlayerMP player =  worldObj.getMinecraftServer().getPlayerList().getPlayerByUsername(nick);
			
			if (player != null
			  && isOutsideBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))) {
				summonPlayer(player);
			}
		}
	}
	
	private void summonSinglePlayer(final String nickname) {
		AxisAlignedBB aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		
		for (int i = 0; i < controller.players.size(); i++) {
			String nick = controller.players.get(i);
			@SuppressWarnings("ConstantConditions") EntityPlayerMP player = worldObj.getMinecraftServer().getPlayerList().getPlayerByUsername(nick);
			
			if (player != null && nick.equals(nickname)
			    && isOutsideBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))) {
				summonPlayer(player);
				return;
			}
		}
	}
	
	public void summonOwnerOnDeploy(final String playerName) {
		@SuppressWarnings("ConstantConditions") EntityPlayerMP entityPlayerMP = worldObj.getMinecraftServer().getPlayerList().getPlayerByUsername(playerName);
		StringBuilder reason = new StringBuilder();
		if (!validateShipSpatialParameters(reason)) {
			Commons.addChatMessage(entityPlayerMP, new TextComponentString("[" + (!shipName.isEmpty() ? shipName : "ShipCore") + "] §c" + reason.toString()));
			return;
		}
		
		AxisAlignedBB aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		
		TileEntity controllerFound = findControllerBlock();
		if (controllerFound != null) {
			controller = (TileEntityShipController) controllerFound;
			controller.players.clear();
			controller.players.add(playerName);
		}
		
		if ( entityPlayerMP != null
		  && isOutsideBB(aabb, MathHelper.floor_double(entityPlayerMP.posX), MathHelper.floor_double(entityPlayerMP.posY), MathHelper.floor_double(entityPlayerMP.posZ)) ) {
			summonPlayer(entityPlayerMP);
		}
	}
	
	private static final VectorI[] vSummonOffsets = { new VectorI(2, 0, 0), new VectorI(-1, 0, 0),
		new VectorI(2, 0, 1), new VectorI(2, 0, -1), new VectorI(-1, 0, 1), new VectorI(-1, 0, -1),
		new VectorI(1, 0, 1), new VectorI(1, 0, -1), new VectorI( 0, 0, 1), new VectorI( 0, 0, -1) };
	private void summonPlayer(EntityPlayerMP entityPlayer) {
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
			Commons.addChatMessage(entityPlayer, new TextComponentString("§c" + String.format("You are to far away to be summoned aboard '%1$s' (max. is %2$d m)", shipName, WarpDriveConfig.SHIP_SUMMON_MAX_RANGE)));
			return;
		}
		
		// find a free spot
		for (VectorI vOffset : vSummonOffsets) {
			BlockPos blockPos = pos.add(
				dx * vOffset.x + dz * vOffset.z,
			    0,
			    dz * vOffset.x + dx * vOffset.z);
			if ( worldObj.isAirBlock(blockPos)
			  && worldObj.isAirBlock(blockPos.add(0, 1, 0))) {
				summonPlayer(entityPlayer, blockPos);
				return;
			}
		}
		messageToAllPlayersOnShip("§c" + String.format("No safe spot found to summon player %1$s", entityPlayer.getDisplayName()));
	}
	
	private void summonPlayer(EntityPlayerMP player, BlockPos blockPos) {
		if (energy_consume(WarpDriveConfig.SHIP_TELEPORT_ENERGY_PER_ENTITY, false)) {
			if (player.dimension != worldObj.provider.getDimension()) {
				player.mcServer.getPlayerList().transferPlayerToDimension(
					player,
					worldObj.provider.getDimension(),
					new SpaceTeleporter(
						DimensionManager.getWorld(worldObj.provider.getDimension()),
						0,
						MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
				player.setPositionAndUpdate(blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d);
				player.sendPlayerAbilities();
			} else {
				player.setPositionAndUpdate(blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d);
			}
		}
	}
	
	public boolean validateShipSpatialParameters(StringBuilder reason) {
		if (controller == null) {
			reason.append("TileEntityReactor.validateShipSpatialParameters: no controller detected!");
			return false;
		}
		direction = controller.getDirection();
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
		
		if (Math.abs(dx) > 0) {
			if (dx == 1) {
				x1 = pos.getX() - shipBack;
				x2 = pos.getX() + shipFront;
				z1 = pos.getZ() - shipLeft;
				z2 = pos.getZ() + shipRight;
			} else {
				x1 = pos.getX() - shipFront;
				x2 = pos.getX() + shipBack;
				z1 = pos.getZ() - shipRight;
				z2 = pos.getZ() + shipLeft;
			}
		} else if (Math.abs(dz) > 0) {
			if (dz == 1) {
				z1 = pos.getZ() - shipBack;
				z2 = pos.getZ() + shipFront;
				x1 = pos.getX() - shipRight;
				x2 = pos.getX() + shipLeft;
			} else {
				z1 = pos.getZ() - shipFront;
				z2 = pos.getZ() + shipBack;
				x1 = pos.getX() - shipLeft;
				x2 = pos.getX() + shipRight;
			}
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
		
		minY = pos.getY() - shipDown;
		maxY = pos.getY() + shipUp;
		
		// Ship side is too big
		if ( (shipBack + shipFront) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE
		  || (shipLeft + shipRight) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE
		  || (shipDown + shipUp) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE) {
			reason.append(String.format("Ship is too big (max is %d per side)",
				WarpDriveConfig.SHIP_MAX_SIDE_SIZE));
			return false;
		}
		
		boolean isUnlimited = false;
		AxisAlignedBB axisalignedbb = new AxisAlignedBB(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		for (Object object : list) {
			if (object == null || !(object instanceof EntityPlayer)) {
				continue;
			}
			
			String playerName = ((EntityPlayer) object).getName();
			for (String unlimitedName : WarpDriveConfig.SHIP_VOLUME_UNLIMITED_PLAYERNAMES) {
				isUnlimited = isUnlimited || unlimitedName.equals(playerName);
			}
		}
		
		updateShipMassAndVolume();
		if ( !isUnlimited
		  && shipMass > WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE
		  && WarpDrive.starMap.isPlanet(worldObj, pos.getX(), pos.getZ()) ) {
			reason.append(String.format("Ship is too big for a planet (max is %d blocks)",
				WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE));
			return false;
		}
		
		return true;
	}
	
	private void doBeaconJump() {
		// Search beacon coordinates
		String freq = controller.getBeaconFrequency();
		int beaconX = 0, beaconZ = 0;
		boolean isBeaconFound = false;
		
		for (EntityPlayerMP entityPlayerMP : worldObj.getMinecraftServer().getPlayerList().getPlayerList()) {
			
			// Skip players from other dimensions
			if (entityPlayerMP.dimension != worldObj.provider.getDimension()) {
				continue;
			}
			
			TileEntity tileEntity = worldObj.getTileEntity(new BlockPos(
					MathHelper.floor_double(entityPlayerMP.posX),
					MathHelper.floor_double(entityPlayerMP.posY) - 1,
					MathHelper.floor_double(entityPlayerMP.posZ)));
			
			if (tileEntity != null && (tileEntity instanceof TileEntityShipController)) {
				if (((TileEntityShipController) tileEntity).getBeaconFrequency().equals(freq)) {
					beaconX = tileEntity.getPos().getX();
					beaconZ = tileEntity.getPos().getZ();
					isBeaconFound = true;
					break;
				}
			}
		}
		
		// Now make jump to a beacon
		if (isBeaconFound) {
			// Consume energy
			if (energy_consume(calculateRequiredEnergy(currentMode, shipMass, controller.getDistance()), false)) {
				WarpDrive.logger.info(this + " Moving ship to beacon (" + beaconX + "; " + getPos().getY() + "; " + beaconZ + ")");
				JumpSequencer jump = new JumpSequencer(this, false, 0, 0, 0, (byte)0, true, beaconX, getPos().getY(), beaconZ);
				jump.enable();
			} else {
				messageToAllPlayersOnShip("Insufficient energy level");
			}
		} else {
			WarpDrive.logger.info(this + " Beacon '" + freq + "' is unknown.");
		}
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isShipInJumpgate(Jumpgate jumpgate, StringBuilder reason) {
		AxisAlignedBB aabb = jumpgate.getGateAABB();
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jumpgate " + jumpgate.name + " AABB is " + aabb);
		}
		int countBlocksInside = 0;
		int countBlocksTotal = 0;
		
		if ( aabb.isVecInside(new Vec3d(minX, minY, minZ))
		  && aabb.isVecInside(new Vec3d(maxX, maxY, maxZ)) ) {
			// fully inside
			return true;
		}
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					IBlockState blockState = worldObj.getBlockState(new BlockPos(x, y, z));
					
					// Skipping vanilla air & ignored blocks
					if (blockState.getBlock() == Blocks.AIR || Dictionary.BLOCKS_LEFTBEHIND.contains(blockState.getBlock())) {
						continue;
					}
					if (Dictionary.BLOCKS_NOMASS.contains(blockState.getBlock())) {
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
		
		int moveX = destX - pos.getX();
		int moveY = destY - pos.getY();
		int moveZ = destZ - pos.getZ();
		
		for (int x = minX; x <= maxX; x++) {
			newX = moveX + x;
			for (int z = minZ; z <= maxZ; z++) {
				newZ = moveZ + z;
				for (int y = minY; y <= maxY; y++) {
					Block blockSource = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
					Block blockTarget = worldObj.getBlockState(new BlockPos(newX, moveY + y, newZ)).getBlock();
					
					// not vanilla air nor ignored blocks at source
					// not vanilla air nor expandable blocks are target location
					if ( blockSource != Blocks.AIR
					  && !Dictionary.BLOCKS_EXPANDABLE.contains(blockSource)
					  && blockTarget != Blocks.AIR
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
		String gateName = controller.getTargetJumpgateName();
		Jumpgate targetGate = WarpDrive.jumpgates.findGateByName(gateName);
		
		if (targetGate == null) {
			messageToAllPlayersOnShip("Destination jumpgate '" + gateName + "' is unknown. Check jumpgate name.");
			controller.setJumpFlag(false);
			return;
		}
		
		// Now make jump to a beacon
		int gateX = targetGate.xCoord;
		int gateY = targetGate.yCoord;
		int gateZ = targetGate.zCoord;
		int destX = gateX;
		int destY = gateY;
		int destZ = gateZ;
		Jumpgate nearestGate = WarpDrive.jumpgates.findNearestGate(pos);
		
		StringBuilder reason = new StringBuilder();
		if (!isShipInJumpgate(nearestGate, reason)) {
			messageToAllPlayersOnShip(reason.toString());
			controller.setJumpFlag(false);
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
				messageToAllPlayersOnShip("Destination gate is blocked by obstacles. Aborting...");
				controller.setJumpFlag(false);
				return;
			}
			
			WarpDrive.logger.info("[GATE] Place found over " + (10 - numTries) + " tries.");
		}
		
		// Consume energy
		if (energy_consume(calculateRequiredEnergy(currentMode, shipMass, controller.getDistance()), false)) {
			WarpDrive.logger.info(this + " Moving ship to a place around gate '" + targetGate.name + "' (" + destX + "; " + destY + "; " + destZ + ")");
			JumpSequencer jump = new JumpSequencer(this, false, 0, 0, 0, (byte)0, true, destX, destY, destZ);
			jump.enable();
		} else {
			messageToAllPlayersOnShip("Insufficient energy level");
		}
	}
	
	private void doJump() {
		int distance = controller.getDistance();
		int requiredEnergy = calculateRequiredEnergy(currentMode, shipMass, distance);
		
		if (!energy_consume(requiredEnergy, true)) {
			messageToAllPlayersOnShip("Insufficient energy to jump! Core is currently charged with " + energy_getEnergyStored() + " EU while jump requires "
					+ requiredEnergy + " EU");
			controller.setJumpFlag(false);
			return;
		}
		
		String shipInfo = "" + shipVolume + " blocks inside (" + minX + ", " + minY + ", " + minZ + ") to (" + maxX + ", " + maxY + ", " + maxZ + ") with an actual mass of " + shipMass + " blocks";
		if (currentMode == EnumShipCoreMode.GATE_JUMP) {
			WarpDrive.logger.info(this + " Performing gate jump of " + shipInfo);
			doGateJump();
			return;
		} else if (currentMode == EnumShipCoreMode.BEACON_JUMP) {
			WarpDrive.logger.info(this + " Performing beacon jump of " + shipInfo);
			doBeaconJump();
			return;
		} else if (currentMode == EnumShipCoreMode.HYPERSPACE) {
			WarpDrive.logger.info(this + " Performing hyperspace jump of " + shipInfo);
			
			// Check ship size for hyper-space jump
			if (shipMass < WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE) {
				Jumpgate nearestGate = null;
				if (WarpDrive.jumpgates == null) {
					WarpDrive.logger.warn(this + " WarpDrive.instance.jumpGates is NULL!");
				} else {
					nearestGate = WarpDrive.jumpgates.findNearestGate(pos);
				}
				
				StringBuilder reason = new StringBuilder();
				if (nearestGate == null || !isShipInJumpgate(nearestGate, reason)) {
					messageToAllPlayersOnShip(new TextComponentString("Ship is too small (" + shipMass + "/" + WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE
							+ ").\nInsufficient ship mass to open hyperspace portal.\nUse a jumpgate to reach or exit hyperspace."));
					controller.setJumpFlag(false);
					return;
				}
			}
		} else if (currentMode == EnumShipCoreMode.BASIC_JUMP) {
			WarpDrive.logger.info(this + " Performing basic jump of " + shipInfo + " toward direction " + direction + " over " + distance + " blocks.");
		} else if (currentMode == EnumShipCoreMode.LONG_JUMP) {
			WarpDrive.logger.info(this + " Performing long jump of " + shipInfo + " toward direction " + direction + " over " + distance + " blocks.");
		} else {
			WarpDrive.logger.info(this + " Performing some jump #" + currentMode + " of " + shipInfo);
		}
		
		if (currentMode == EnumShipCoreMode.BASIC_JUMP || currentMode == EnumShipCoreMode.LONG_JUMP || currentMode == EnumShipCoreMode.HYPERSPACE) {
			if (!energy_consume(requiredEnergy, false)) {
				messageToAllPlayersOnShip("Insufficient energy level");
				return;
			}
			int moveX = 0;
			int moveY = 0;
			int moveZ = 0;
			
			if (currentMode != EnumShipCoreMode.HYPERSPACE) {
				VectorI movement = controller.getMovement();
				VectorI shipSize = new VectorI(controller.getFront() + 1 + controller.getBack(),
				                               controller.getUp()    + 1 + controller.getDown(),
				                               controller.getRight() + 1 + controller.getLeft());
				int maxDistance = WarpDriveConfig.SHIP_MAX_JUMP_DISTANCE;
				if (WarpDrive.starMap.isInHyperspace(worldObj, pos.getX(), pos.getZ())) {
					maxDistance *= 100;
				}
				if (Math.abs(movement.x) - shipSize.x > maxDistance) {
					movement.x = (int) Math.signum(movement.x) * (shipSize.x + maxDistance);
				}
				if (Math.abs(movement.y) - shipSize.y > maxDistance) {
					movement.y = (int) Math.signum(movement.y) * (shipSize.y + maxDistance);
				}
				if (Math.abs(movement.z) - shipSize.z > maxDistance) {
					movement.z = (int) Math.signum(movement.z) * (shipSize.z + maxDistance);
				}
				moveX = dx * movement.x - dz * movement.z;
				moveY = movement.y;
				moveZ = dz * movement.x + dx * movement.z;
				//noinspection StatementWithEmptyBody
				if (currentMode == EnumShipCoreMode.BASIC_JUMP) {
					// VectorI sizes = new VectorI(controller.getBack() + controller.getFront(), controller.getDown() + controller.getUp(), controller.getLeft() + controller.getRight());
					// moveX += Math.signum((double)moveX) * Math.abs(dx * sizes.x - dz * sizes.z);
					// moveY += Math.signum((double)moveY) * (sizes.y);
					// moveZ += Math.signum((double)moveZ) * Math.abs(dz * sizes.x + dx * sizes.z);
				} else if (currentMode == EnumShipCoreMode.LONG_JUMP) {
					moveX *= 100;
					moveZ *= 100;
				}
			}
			
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " Movement adjusted to (" + moveX + " " + moveY + " " + moveZ + ") blocks.");
			}
			JumpSequencer jump = new JumpSequencer(this, (currentMode == EnumShipCoreMode.HYPERSPACE),
					moveX, moveY, moveZ, controller.getRotationSteps(),
					false, 0, 0, 0);
			jump.enable();
		}
	}
	
	private static boolean isOutsideBB(AxisAlignedBB axisalignedbb, int x, int y, int z) {
		return axisalignedbb.minX > x || axisalignedbb.maxX < x
		    || axisalignedbb.minY > y || axisalignedbb.maxY < y
		    || axisalignedbb.minZ > z || axisalignedbb.maxZ < z;
	}
	
	@Override
	public ITextComponent getStatus() {
		return super.getStatus()
			.appendSibling(new TextComponentString("\n")).appendSibling(getEnergyStatus())
			.appendSibling((cooldownTime > 0) ? new TextComponentString("\n").appendSibling(new TextComponentTranslation("warpdrive.ship.statusLine.cooling", cooldownTime / 20)) : new TextComponentString(""))
			.appendSibling((isolationBlocksCount > 0) ? new TextComponentString("\n").appendSibling(new TextComponentTranslation("warpdrive.ship.statusLine.isolation", isolationBlocksCount, isolationRate * 100.0)) : new TextComponentString(""));
	}
	
	public static int calculateRequiredEnergy(EnumShipCoreMode enumShipCoreMode, int shipVolume, int jumpDistance) {
		switch (enumShipCoreMode) {
		case BASIC_JUMP:
			return (WarpDriveConfig.SHIP_NORMALJUMP_ENERGY_PER_BLOCK * shipVolume) + (WarpDriveConfig.SHIP_NORMALJUMP_ENERGY_PER_DISTANCE * jumpDistance);
			
		case LONG_JUMP:
			return (WarpDriveConfig.SHIP_HYPERJUMP_ENERGY_PER_BLOCK * shipVolume) + (WarpDriveConfig.SHIP_HYPERJUMP_ENERGY_PER_DISTANCE * jumpDistance);
			
		case HYPERSPACE:
			return WarpDriveConfig.SHIP_MAX_ENERGY_STORED / 10; // 10% of maximum
			
		case BEACON_JUMP:
			return WarpDriveConfig.SHIP_MAX_ENERGY_STORED / 2; // half of maximum
			
		case GATE_JUMP:
			return 2 * shipVolume;
		default:
			break;
		}
		
		return WarpDriveConfig.SHIP_MAX_ENERGY_STORED;
	}
	
	private void updateShipMassAndVolume() {
		int newMass = 0;
		int newVolume = 0;
		
		try {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					for (int y = minY; y <= maxY; y++) {
						Block block = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
						
						// Skipping vanilla air & ignored blocks
						if (block == Blocks.AIR || Dictionary.BLOCKS_LEFTBEHIND.contains(block)) {
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
	
	private TileEntity findControllerBlock() {
		TileEntity result;
		result = worldObj.getTileEntity(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = 1;
			dz = 0;
			return result;
		}
		
		result = worldObj.getTileEntity(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = -1;
			dz = 0;
			return result;
		}
		
		result = worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = 0;
			dz = 1;
			return result;
		}
		
		result = worldObj.getTileEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = 0;
			dz = -1;
			return result;
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
	public boolean energy_canInput(EnumFacing from) {
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
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
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		if (uuid != null) {
			tag.setLong("uuidMost", uuid.getMostSignificantBits());
			tag.setLong("uuidLeast", uuid.getLeastSignificantBits());
		}
		tag.setString("shipName", shipName);
		tag.setDouble("isolationRate", isolationRate);
		tag.setInteger("cooldownTime", cooldownTime);
		tag.setInteger("jumpCount", jumpCount);
		return tag;
	}
	
	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(super.getUpdateTag());    // @TODO: to be integrated with jumpCount, probable wrong ancestor
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.getNbtCompound();
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
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
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
	public void onBlockUpdatedInArea(final VectorI vector, final IBlockState blockState) {
		// no operation
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s \'%s\' @ \'%s\' (%d %d %d)",
			getClass().getSimpleName(), shipName, worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			pos.getX(), pos.getY(), pos.getZ());
	}
}
