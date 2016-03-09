package cr0s.warpdrive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.JumpBlock;
import cr0s.warpdrive.data.JumpShip;
import cr0s.warpdrive.data.MovingEntity;
import cr0s.warpdrive.data.Planet;
import cr0s.warpdrive.data.Transformation;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.world.SpaceTeleporter;

public class EntityJump extends Entity {
	// Jump vector
	private Transformation transformation;
	
	private int distance;
	public int shipLength;
	private boolean isHyperspaceJump;
	
	private World targetWorld;
	private Ticket sourceWorldTicket;
	private Ticket targetWorldTicket;
	
	private boolean collisionDetected = false;
	private ArrayList<Vector3> collisionAtSource;
	private ArrayList<Vector3> collisionAtTarget;
	private float collisionStrength = 0;
	
	public boolean on = false;
	private final static int STATE_IDLE = 0;
	private final static int STATE_BLOCKS = 1;
	private final static int STATE_EXTERNALS = 2;
	private final static int STATE_ENTITIES = 3;
	private final static int STATE_REMOVING = 4;
	private int state = STATE_IDLE;
	private int currentIndexInShip = 0;
	
	public JumpShip ship = new JumpShip();
	private boolean betweenWorlds;
	
	private int destX, destY, destZ;
	private boolean isCoordJump;
	
	private long msCounter = 0;
	private int ticks = 0;
	
	public EntityJump(World world) {
		super(world);
		targetWorld = worldObj;
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Entity created (empty) in dimension " + worldObj.getProviderName() + " - " + worldObj.getWorldInfo().getWorldName());
		}
	}
	
	public EntityJump(World world, int x, int y, int z, int _dx, int _dz, TileEntityShipCore _shipCore, boolean _isHyperspaceJump, int _distance, int _direction,
			boolean _isCoordJump, int _destX, int _destY, int _destZ) {
		super(world);
		this.posX = x + 0.5D;
		this.posY = y + 0.5D;
		this.posZ = z + 0.5D;
		this.ship.worldObj = worldObj;
		this.ship.coreX = x;
		this.ship.coreY = y;
		this.ship.coreZ = z;
		this.ship.dx = _dx;
		this.ship.dz = _dz;
		this.ship.shipCore = _shipCore;
		this.isHyperspaceJump = _isHyperspaceJump;
		this.distance = _distance;
		this.ship.direction = _direction;
		this.isCoordJump = _isCoordJump;
		this.destX = _destX;
		this.destY = _destY;
		this.destZ = _destZ;
		
		// set by reactor
		ship.maxX = ship.maxZ = ship.maxY = ship.minX = ship.minZ = ship.minY = 0;
		shipLength = 0;
		
		// set when preparing jump
		targetWorld = null;
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Entity created");
		}
	}
	
	public void killEntity(String reason) {
		if (!on) {
			return;
		}
		
		on = false;
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			if (reason == null || reason.isEmpty()) {
				WarpDrive.logger.info(this + " Killing jump entity...");
			} else {
				WarpDrive.logger.info(this + " Killing jump entity... (" + reason + ")");
			}
		}
		
		unforceChunks();
		worldObj.removeEntity(this);
	}
	
	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}
	
	@SuppressWarnings("unused")
	@Override
	public void onUpdate() {
		if (worldObj.isRemote) {
			return;
		}
		
		if (!on) {
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " Removing from onUpdate...");
			}
			worldObj.removeEntity(this);
			return;
		}
		
		if (ship.minY < 0 || ship.maxY > 255) {
			String msg = "Invalid Y coordinate(s), check ship dimensions...";
			ship.messageToAllPlayersOnShip(this, msg);
			killEntity(msg);
			return;
		}
		
		ticks++;
		if (state == STATE_IDLE) {
			prepareToJump();
			if (on) {
				state = STATE_BLOCKS;
			}
		} else if (state == STATE_BLOCKS) {
			moveBlocks();
			if (currentIndexInShip >= ship.jumpBlocks.length - 1) {
				currentIndexInShip = 0;
				state = STATE_EXTERNALS;
			}
		} else if (state == STATE_EXTERNALS) {
			moveExternals();
			if (currentIndexInShip >= ship.jumpBlocks.length - 1) {
				state = STATE_ENTITIES;
			}
		} else if (state == STATE_ENTITIES) {
			moveEntities();
			currentIndexInShip = 0;
			state = STATE_REMOVING;
		} else if (state == STATE_REMOVING) {
			if (false) {
				restoreEntitiesPosition();
			}
			removeBlocks();
			
			if (currentIndexInShip >= ship.jumpBlocks.length - 1) {
				finishJump();
				state = STATE_IDLE;
			}
		} else {
			String msg = "Invalid state, aborting jump...";
			ship.messageToAllPlayersOnShip(this, msg);
			killEntity(msg);
			return;
		}
	}
	
	private boolean forceChunks(StringBuilder reason) {
		LocalProfiler.start("EntityJump.forceChunks");
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Forcing chunks in " + worldObj.provider.getDimensionName() + " and " + targetWorld.provider.getDimensionName());
		}
		sourceWorldTicket = ForgeChunkManager.requestTicket(WarpDrive.instance, worldObj, Type.NORMAL); // Type.ENTITY);
		if (sourceWorldTicket == null) {
			reason.append("Chunkloading rejected in source world " + worldObj.getWorldInfo().getWorldName() + ". Aborting.");
			return false;
		}
		targetWorldTicket = ForgeChunkManager.requestTicket(WarpDrive.instance, targetWorld, Type.NORMAL);
		if (targetWorldTicket == null) {
			reason.append("Chunkloading rejected in target world " + worldObj.getWorldInfo().getWorldName() + ". Aborting.");
			return false;
		}
		// sourceWorldTicket.bindEntity(this);
		int x1 = ship.minX >> 4;
		int x2 = ship.maxX >> 4;
		int z1 = ship.minZ >> 4;
		int z2 = ship.maxZ >> 4;
		int chunkCount = 0;
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				chunkCount++;
				if (chunkCount > sourceWorldTicket.getMaxChunkListDepth()) {
					reason.append("Ship is extending over too many chunks in source world. Max is currently set to " + sourceWorldTicket.getMaxChunkListDepth() + " in forgeChunkLoading.cfg. Aborting.");
					return false;
				}
				ForgeChunkManager.forceChunk(sourceWorldTicket, new ChunkCoordIntPair(x, z));
			}
		}
		
		ChunkCoordinates targetMin = transformation.apply(ship.minX, ship.minY, ship.minZ);
		ChunkCoordinates targetMax = transformation.apply(ship.maxX, ship.maxY, ship.maxZ);
		x1 = Math.min(targetMin.posX, targetMax.posX) >> 4;
		x2 = Math.max(targetMin.posX, targetMax.posX) >> 4;
		z1 = Math.min(targetMin.posZ, targetMax.posZ) >> 4;
		z2 = Math.max(targetMin.posZ, targetMax.posZ) >> 4;
		chunkCount = 0;
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				chunkCount++;
				if (chunkCount > targetWorldTicket.getMaxChunkListDepth()) {
					reason.append("Ship is extending over too many chunks in target world. Max is currently set to " + targetWorldTicket.getMaxChunkListDepth() + " in forgeChunkLoading.cfg. Aborting.");
					return false;
				}
				ForgeChunkManager.forceChunk(targetWorldTicket, new ChunkCoordIntPair(x, z));
			}
		}
		LocalProfiler.stop();
		return true;
	}
	
	private void unforceChunks() {
		LocalProfiler.start("EntityJump.unforceChunks");
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Unforcing chunks");
		}
		
		int x1, x2, z1, z2;
		if (sourceWorldTicket != null) {
			x1 = ship.minX >> 4;
			x2 = ship.maxX >> 4;
			z1 = ship.minZ >> 4;
			z2 = ship.maxZ >> 4;
			for (int x = x1; x <= x2; x++) {
				for (int z = z1; z <= z2; z++) {
					ForgeChunkManager.unforceChunk(sourceWorldTicket, new ChunkCoordIntPair(x, z));
				}
			}
			ForgeChunkManager.releaseTicket(sourceWorldTicket);
			sourceWorldTicket = null;
		}
		
		if (targetWorldTicket != null) {
			ChunkCoordinates targetMin = transformation.apply(ship.minX, ship.minY, ship.minZ);
			ChunkCoordinates targetMax = transformation.apply(ship.maxX, ship.maxY, ship.maxZ);
			x1 = Math.min(targetMin.posX, targetMax.posX) >> 4;
			x2 = Math.max(targetMin.posX, targetMax.posX) >> 4;
			z1 = Math.min(targetMin.posZ, targetMax.posZ) >> 4;
			z2 = Math.max(targetMin.posZ, targetMax.posZ) >> 4;
			for (int x = x1; x <= x2; x++) {
				for (int z = z1; z <= z2; z++) {
					ForgeChunkManager.unforceChunk(targetWorldTicket, new ChunkCoordIntPair(x, z));
				}
			}
			ForgeChunkManager.releaseTicket(targetWorldTicket);
			targetWorldTicket = null;
		}
		
		LocalProfiler.stop();
	}
	
	public static String getDirectionLabel(int direction) {
		switch (direction) {
		case -1:
			return "UP";
		case -2:
			return "DOWN";
		case 0:
			return "FRONT";
		case 180:
			return "BACK";
		case 90:
			return "LEFT";
		case 255:
			return "RIGHT";
		default:
			return direction + " degrees";
		}
	}
	
	private void prepareToJump() {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Preparing to jump...");
		}
		LocalProfiler.start("EntityJump.prepareToJump");
		
		StringBuilder reason = new StringBuilder();
		
		boolean isInSpace = (worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID);
		boolean isInHyperSpace = (worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
		
		boolean toSpace = (ship.direction == -1) && (ship.maxY + distance > 255) && (!isInSpace) && (!isInHyperSpace);
		boolean fromSpace = (ship.direction == -2) && (ship.minY - distance < 0) && isInSpace;
		betweenWorlds = fromSpace || toSpace || isHyperspaceJump;
		int moveX = 0;
		int moveY = 0;
		int moveZ = 0;
		
		if (!isHyperspaceJump && toSpace) {
			Boolean planetFound = false;
			Boolean planetValid = false;
			int closestPlanetDistance = Integer.MAX_VALUE;
			Planet closestPlanet = null;
			for (int iPlane = 0; (!planetValid) && iPlane < WarpDriveConfig.PLANETS.length; iPlane++) {
				Planet planet = WarpDriveConfig.PLANETS[iPlane];
				if (worldObj.provider.dimensionId == planet.dimensionId) {
					planetFound = true;
					int planetDistance = planet.isValidToSpace(new VectorI(this));
					if (planetDistance == 0) {
						planetValid = true;
						moveX = planet.spaceCenterX - planet.dimensionCenterX;
						moveZ = planet.spaceCenterZ - planet.dimensionCenterZ;
						targetWorld = MinecraftServer.getServer().worldServerForDimension(WarpDriveConfig.G_SPACE_DIMENSION_ID);
						if (targetWorld == null) {
							LocalProfiler.stop();
							String msg = "Unable to load Space dimension " + WarpDriveConfig.G_SPACE_DIMENSION_ID + ", aborting jump.";
							ship.messageToAllPlayersOnShip(this, msg);
							killEntity(msg);
							return;
						}
					} else if (closestPlanetDistance > planetDistance) {
						closestPlanetDistance = planetDistance;
						closestPlanet = planet;
					}
				}
			}
			if (!planetFound) {
				LocalProfiler.stop();
				String msg = "Unable to reach space!\nThere's no valid transition plane for current dimension " + worldObj.provider.getDimensionName() + " ("
						+ worldObj.provider.dimensionId + ")";
				ship.messageToAllPlayersOnShip(this, msg);
				killEntity(msg);
				return;
			}
			if (!planetValid) {
				LocalProfiler.stop();
				assert(closestPlanet != null);
				@SuppressWarnings("null") // Eclipse derp, don't remove
				String msg = "Ship is outside border, unable to reach space!\nClosest transition plane is ~" + closestPlanetDistance + " m away ("
						+ (closestPlanet.dimensionCenterX - closestPlanet.borderSizeX) + ", 250,"
						+ (closestPlanet.dimensionCenterZ - closestPlanet.borderSizeZ) + ") to ("
						+ (closestPlanet.dimensionCenterX + closestPlanet.borderSizeX) + ", 255,"
						+ (closestPlanet.dimensionCenterZ + closestPlanet.borderSizeZ) + ")";
				ship.messageToAllPlayersOnShip(this, msg);
				killEntity(msg);
				return;
			}
		} else if (!isHyperspaceJump && fromSpace) {
			Boolean planetFound = false;
			int closestPlaneDistance = Integer.MAX_VALUE;
			Planet closestTransitionPlane = null;
			for (int iPlanet = 0; (!planetFound) && iPlanet < WarpDriveConfig.PLANETS.length; iPlanet++) {
				Planet planet = WarpDriveConfig.PLANETS[iPlanet];
				int planeDistance = planet.isValidFromSpace(new VectorI(this));
				if (planeDistance == 0) {
					planetFound = true;
					moveX = planet.dimensionCenterX - planet.spaceCenterX;
					moveZ = planet.dimensionCenterZ - planet.spaceCenterZ;
					targetWorld = MinecraftServer.getServer().worldServerForDimension(planet.dimensionId);
					if (targetWorld == null) {
						LocalProfiler.stop();
						String msg = "Undefined dimension " + planet.dimensionId + ", aborting jump. Check your server configuration!";
						ship.messageToAllPlayersOnShip(this, msg);
						killEntity(msg);
						return;
					}
				} else if (closestPlaneDistance > planeDistance) {
					closestPlaneDistance = planeDistance;
					closestTransitionPlane = planet;
				}
			}
			if (!planetFound) {
				LocalProfiler.stop();
				String msg = "";
				if (closestTransitionPlane == null) {
					msg = "No planet defined, unable to enter atmosphere!";
				} else {
					msg = "No planet in range, unable to enter atmosphere!\nClosest transition plane is " + closestPlaneDistance + " m away ("
							+ (closestTransitionPlane.spaceCenterX - closestTransitionPlane.borderSizeX) + ", 250,"
							+ (closestTransitionPlane.spaceCenterZ - closestTransitionPlane.borderSizeZ) + ") to ("
							+ (closestTransitionPlane.spaceCenterX + closestTransitionPlane.borderSizeX) + ", 255,"
							+ (closestTransitionPlane.spaceCenterZ + closestTransitionPlane.borderSizeZ) + ")";
				}
				ship.messageToAllPlayersOnShip(this, msg);
				killEntity(msg);
				return;
			}
		} else if (isHyperspaceJump && isInHyperSpace) {
			targetWorld = MinecraftServer.getServer().worldServerForDimension(WarpDriveConfig.G_SPACE_DIMENSION_ID);
			if (targetWorld == null) {
				LocalProfiler.stop();
				String msg = "Unable to load Space dimension " + WarpDriveConfig.G_SPACE_DIMENSION_ID + ", aborting jump.";
				ship.messageToAllPlayersOnShip(this, msg);
				killEntity(msg);
				return;
			}
		} else if (isHyperspaceJump && isInSpace) {
			targetWorld = MinecraftServer.getServer().worldServerForDimension(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
			if (targetWorld == null) {
				LocalProfiler.stop();
				String msg = "Unable to load Hyperspace dimension " + WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID + ", aborting jump.";
				ship.messageToAllPlayersOnShip(this, msg);
				killEntity(msg);
				return;
			}
		} else {
			targetWorld = worldObj;
		}
		
		// Calculate jump vector
		if (isCoordJump) {
			moveX = destX - ship.coreX;
			moveZ = destZ - ship.coreZ;
			moveY = destY - ship.coreY;
			distance = 0; // FIXME: check collision in straight path, starting with getPossibleJumpDistance() ?
		} else if (isHyperspaceJump) {
			distance = 0;
			if (!isInSpace && !isInHyperSpace) {
				String msg = "Unable to reach hyperspace from a planet";
				killEntity(msg);
				ship.messageToAllPlayersOnShip(this, msg);
				LocalProfiler.stop();
				return;
			}
		} else {
			if (toSpace) {
				// enter space at current altitude
				moveY = 0;
			} else if (fromSpace) {
				// re-enter atmosphere at max altitude
				moveY = 245 - ship.maxY;
			} else {
				// Do not check in long jumps
				if (distance < 256) {
					distance = getPossibleJumpDistance();
				}
				
				int movementVector[] = getVector(ship.direction);
				moveX = movementVector[0] * distance;
				moveY = movementVector[1] * distance;
				moveZ = movementVector[2] * distance;
				
				if ((ship.maxY + moveY) > 255) {
					moveY = 255 - ship.maxY;
				}
				
				if ((ship.minY + moveY) < 5) {
					moveY = 5 - ship.minY;
				}
			}
		}
		transformation = new Transformation(ship, targetWorld, moveX, moveY, moveZ, (byte) 0);
		
		if (betweenWorlds && WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " From world " + worldObj.provider.getDimensionName() + " to " + targetWorld.provider.getDimensionName());
		}
		
		// Validate positions aren't overlapping
		if (!betweenWorlds) {
			if (Math.abs(moveX) <= (ship.maxX - ship.minX + 1) && Math.abs(moveY) <= (ship.maxY - ship.minY + 1) && Math.abs(moveZ) <= (ship.maxZ - ship.minZ + 1)) {
				// render fake explosions
				doCollisionDamage(false);
				
				// cancel jump
				String msg = "Not enough space for jump!";
				killEntity(msg);
				ship.messageToAllPlayersOnShip(this, msg);
				LocalProfiler.stop();
				return;
			}
		}
		
		if (!forceChunks(reason)) {
			String msg = reason.toString();
			killEntity(msg);
			ship.messageToAllPlayersOnShip(this, msg);
			LocalProfiler.stop();
			return;
		}
		
		{
			String msg = ship.saveEntities(this);
			if (msg != null) {
				killEntity(msg);
				ship.messageToAllPlayersOnShip(this, msg);
				LocalProfiler.stop();
				return;
			}
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " Saved " + ship.entitiesOnShip.size() + " entities from ship");
			}
		}
		
		if (isHyperspaceJump && isInSpace) {
			ship.messageToAllPlayersOnShip(this, "Entering HYPERSPACE...");
		} else if (isHyperspaceJump && isInHyperSpace) {
			ship.messageToAllPlayersOnShip(this, "Leaving HYPERSPACE..");
		} else if (isCoordJump) {
			ship.messageToAllPlayersOnShip(this, "Jumping to coordinates (" + destX + "; " + ship.coreY + "; " + destZ + ")!");
		} else {
			ship.messageToAllPlayersOnShip(this, "Jumping " + getDirectionLabel(ship.direction) + " by " + distance + " blocks");
		}
		
		// validate ship content
		int shipVolume = ship.getRealShipVolume_checkBedrock(this, reason);
		if (shipVolume == -1) {
			String msg = reason.toString();
			killEntity(msg);
			ship.messageToAllPlayersOnShip(this, msg);
			LocalProfiler.stop();
			return;
		}
		
		saveShip(shipVolume);
		this.currentIndexInShip = 0;
		msCounter = System.currentTimeMillis();
		LocalProfiler.stop();
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info("Removing TE duplicates: tileEntities in target world before jump: " + targetWorld.loadedTileEntityList.size());
		}
	}
	
	/**
	 * Saving ship to memory
	 *
	 * @param shipVolume
	 */
	private void saveShip(int shipVolume) {
		LocalProfiler.start("EntityJump.saveShip");
		try {
			JumpBlock[][] placeTimeJumpBlocks = { new JumpBlock[shipVolume], new JumpBlock[shipVolume], new JumpBlock[shipVolume], new JumpBlock[shipVolume], new JumpBlock[shipVolume] };
			int[] placeTimeIndexes = { 0, 0, 0, 0, 0 }; 
			
			int xc1 = ship.minX >> 4;
			int xc2 = ship.maxX >> 4;
			int zc1 = ship.minZ >> 4;
			int zc2 = ship.maxZ >> 4;
			
			for (int xc = xc1; xc <= xc2; xc++) {
				int x1 = Math.max(ship.minX, xc << 4);
				int x2 = Math.min(ship.maxX, (xc << 4) + 15);
				
				for (int zc = zc1; zc <= zc2; zc++) {
					int z1 = Math.max(ship.minZ, zc << 4);
					int z2 = Math.min(ship.maxZ, (zc << 4) + 15);
					
					for (int y = ship.minY; y <= ship.maxY; y++) {
						for (int x = x1; x <= x2; x++) {
							for (int z = z1; z <= z2; z++) {
								Block block = worldObj.getBlock(x, y, z);
								
								// Skipping vanilla air & ignored blocks
								if (block == Blocks.air || Dictionary.BLOCKS_LEFTBEHIND.contains(block)) {
									continue;
								}
								
								int blockMeta = worldObj.getBlockMetadata(x, y, z);
								TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
								JumpBlock jumpBlock = new JumpBlock(block, blockMeta, tileEntity, x, y, z);
								
								// default priority is 2 for block, 3 for tile entities
								Integer placeTime = Dictionary.BLOCKS_PLACE.get(block);
								if (placeTime == null) {
									if (tileEntity == null) {
										placeTime = 2;
									} else {
										placeTime = 3;
									}
								}
								
								placeTimeJumpBlocks[placeTime][placeTimeIndexes[placeTime]] = jumpBlock;
								placeTimeIndexes[placeTime]++;
							}
						}
					}
				}
			}
			
			ship.jumpBlocks = new JumpBlock[shipVolume];
			int indexShip = 0;
			for (int placeTime = 0; placeTime < 5; placeTime++) {
				for (int placeTimeIndex = 0; placeTimeIndex < placeTimeIndexes[placeTime]; placeTimeIndex++) {
					ship.jumpBlocks[indexShip] = placeTimeJumpBlocks[placeTime][placeTimeIndex];
					indexShip++;
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			killEntity("Exception during jump preparation (saveShip)!");
			LocalProfiler.stop();
			return;
		}
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Ship saved as " + ship.jumpBlocks.length + " blocks");
		}
		LocalProfiler.stop();
	}
	
	/**
	 * Ship moving
	 */
	private void moveBlocks() {
		LocalProfiler.start("EntityJump.moveShip");
		int blocksToMove = Math.min(WarpDriveConfig.G_BLOCKS_PER_TICK, ship.jumpBlocks.length - currentIndexInShip);
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Moving ship blocks " + currentIndexInShip + " to " + (currentIndexInShip + blocksToMove - 1) + " / " + (ship.jumpBlocks.length - 1));
		}
		
		for (int index = 0; index < blocksToMove; index++) {
			if (currentIndexInShip >= ship.jumpBlocks.length) {
				break;
			}
			
			JumpBlock jumpBlock = ship.jumpBlocks[currentIndexInShip];
			if (jumpBlock != null) {
				if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
					WarpDrive.logger.info("Deploying from " + jumpBlock.x + ", " + jumpBlock.y + ", " + jumpBlock.z + " of " + jumpBlock.block + "@" + jumpBlock.blockMeta);
				}
				jumpBlock.deploy(targetWorld, transformation);
				
				worldObj.removeTileEntity(jumpBlock.x, jumpBlock.y, jumpBlock.z);
			}
			currentIndexInShip++;
		}
		
		LocalProfiler.stop();
	}
	
	/**
	 * Removing ship from world
	 */
	private void moveExternals() {
		LocalProfiler.start("EntityJump.moveExternals");
		int blocksToMove = Math.min(WarpDriveConfig.G_BLOCKS_PER_TICK, ship.jumpBlocks.length - currentIndexInShip);
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Removing ship externals from " + currentIndexInShip + " / " + (ship.jumpBlocks.length - 1));
		}
		int index = 0;
		while (index < blocksToMove && currentIndexInShip < ship.jumpBlocks.length) {
			JumpBlock jumpBlock = ship.jumpBlocks[ship.jumpBlocks.length - currentIndexInShip - 1];
			if (jumpBlock == null) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info(this + " Removing ship externals: unexpected null found at ship[" + currentIndexInShip + "]");
				}
				currentIndexInShip++;
				continue;
			}
			
			if (jumpBlock.blockTileEntity != null && jumpBlock.externals != null) {
				if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
					WarpDrive.logger.info("Removing externals for block " + jumpBlock.block + "@" + jumpBlock.blockMeta + " at " + jumpBlock.x + " " + jumpBlock.y + " " + jumpBlock.z);
				}
				for (Entry<String, NBTBase> external : jumpBlock.externals.entrySet()) {
					IBlockTransformer blockTransformer = WarpDriveConfig.blockTransformers.get(external.getKey());
					if (blockTransformer != null) {
						blockTransformer.remove(jumpBlock.blockTileEntity);
						
						ChunkCoordinates target = transformation.apply(jumpBlock.x, jumpBlock.y, jumpBlock.z);
						TileEntity newTileEntity = targetWorld.getTileEntity(target.posX, target.posY, target.posZ);
						blockTransformer.restoreExternals(newTileEntity, transformation, external.getValue());
					}
				}
				index++;
			}
			currentIndexInShip++;
		}
		LocalProfiler.stop();
	}
	private void removeBlocks() {
		LocalProfiler.start("EntityJump.removeShip");
		int blocksToMove = Math.min(WarpDriveConfig.G_BLOCKS_PER_TICK, ship.jumpBlocks.length - currentIndexInShip);
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Removing ship blocks " + currentIndexInShip + " to " + (currentIndexInShip + blocksToMove - 1) + " / " + (ship.jumpBlocks.length - 1));
		}
		for (int index = 0; index < blocksToMove; index++) {
			if (currentIndexInShip >= ship.jumpBlocks.length) {
				break;
			}
			JumpBlock jumpBlock = ship.jumpBlocks[ship.jumpBlocks.length - currentIndexInShip - 1];
			if (jumpBlock == null) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info(this + " Removing ship part: unexpected null found at ship[" + currentIndexInShip + "]");
				}
				currentIndexInShip++;
				continue;
			}
			if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
				WarpDrive.logger.info("Removing block " + jumpBlock.block + "@" + jumpBlock.blockMeta + " at " + jumpBlock.x + " " + jumpBlock.y + " " + jumpBlock.z);
			}
			
			ChunkCoordinates target = transformation.apply(jumpBlock.x, jumpBlock.y, jumpBlock.z); 
			if (jumpBlock.blockTileEntity != null) {
				if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
					WarpDrive.logger.info("Removing tile entity at " + jumpBlock.x + " " + jumpBlock.y + " " + jumpBlock.z);
				}
				worldObj.removeTileEntity(jumpBlock.x, jumpBlock.y, jumpBlock.z);
				
				if (jumpBlock.externals != null) {
					for (Entry<String, NBTBase> external : jumpBlock.externals.entrySet()) {
						if (external.getValue() == null) {
							continue;
						}
						IBlockTransformer blockTransformer = WarpDriveConfig.blockTransformers.get(external.getKey());
						if (blockTransformer != null) {
							TileEntity newTileEntity = targetWorld.getTileEntity(target.posX, target.posY, target.posZ);
							blockTransformer.restoreExternals(newTileEntity, transformation, external.getValue());
						}
					}
				}
			}
			worldObj.setBlock(jumpBlock.x, jumpBlock.y, jumpBlock.z, Blocks.air, 0, 2);
			
			JumpBlock.refreshBlockStateOnClient(targetWorld, target.posX, target.posY, target.posZ);
			
			currentIndexInShip++;
		}
		LocalProfiler.stop();
	}
	
	/**
	 * Finish jump: move entities, unlock worlds and delete self
	 */
	private void finishJump() {
		// FIXME TileEntity duplication workaround
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jump done in " + ((System.currentTimeMillis() - msCounter) / 1000F) + " seconds and " + ticks + " ticks");
		}
		if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
			WarpDrive.logger.info("Removing TE duplicates: tileEntities in target world after jump, before cleanup: " + targetWorld.loadedTileEntityList.size());
		}
		LocalProfiler.start("EntityJump.removeDuplicates()");
		
		try {
			targetWorld.loadedTileEntityList = this.removeDuplicates(targetWorld.loadedTileEntityList);
		} catch (Exception exception) {
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info("TE Duplicates removing exception: " + exception.getMessage());
				exception.printStackTrace();
			}
		}
		
		doCollisionDamage(true);
		
		LocalProfiler.stop();
		if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
			WarpDrive.logger.info("Removing TE duplicates: tileEntities in target world after jump, after cleanup: " + targetWorld.loadedTileEntityList.size());
		}
		killEntity("Jump done");
	}
	
	/**
	 * Checking jump possibility
	 *
	 * @return possible jump distance or -1
	 */
	private int getPossibleJumpDistance() {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Calculating possible jump distance...");
		}
		int testDistance = this.distance;
		int blowPoints = 0;
		collisionDetected = false;
		
		CheckMovementResult result = null;
		while (testDistance >= 0) {
			// Is there enough space in destination point?
			result = checkMovement(testDistance, false);
			
			if (result == null) {
				break;
			}
			
			if (result.isCollision) {
				blowPoints++;
			}
			testDistance--;
		}
		
		if (distance != testDistance && WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jump distance adjusted to " + testDistance + " after " + blowPoints + " collisions");
		}
		
		// Register explosion(s) at collision point
		if (blowPoints > WarpDriveConfig.SHIP_COLLISION_TOLERANCE_BLOCKS) {
			result = checkMovement(Math.max(1, testDistance + 1), true);
			if (result != null) {
				/*
				 * Strength scaling:
				 * Wither skull = 1
				 * Creeper = 3 or 6
				 * TNT = 4
				 * TNTcart = 4 to 11.5
				 * Wither boom = 5
				 * Endercrystal = 6
				 */
				float massCorrection = 0.5F
						+ (float) Math.sqrt(Math.min(1.0D, Math.max(0.0D, ship.shipCore.shipMass - WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE)
								/ WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE));
				collisionDetected = true;
				collisionStrength = (4.0F + blowPoints - WarpDriveConfig.SHIP_COLLISION_TOLERANCE_BLOCKS) * massCorrection;
				collisionAtSource = result.atSource;
				collisionAtTarget = result.atTarget;
				WarpDrive.logger.info(this + " Reporting " + collisionAtTarget.size() + " collisions coordinates " + blowPoints
							+ " blowPoints with massCorrection of " + String.format("%.2f", massCorrection) + " => strength "
							+ String.format("%.2f", collisionStrength));
			} else {
				WarpDrive.logger.error("WarpDrive error: unable to compute collision points, ignoring...");
			}
		}
		
		return testDistance;
	}
	
	private void doCollisionDamage(boolean atTarget) {
		if (!collisionDetected) {
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " doCollisionDamage No collision detected...");
			}
			return;
		}
		ArrayList<Vector3> collisionPoints = atTarget ? collisionAtTarget : collisionAtSource;
		Vector3 min = collisionPoints.get(0);
		Vector3 max = collisionPoints.get(0);
		for (Vector3 v : collisionPoints) {
			if (min.x > v.x) {
				min.x = v.x;
			} else if (max.x < v.x) {
				max.x = v.x;
			}
			if (min.y > v.y) {
				min.y = v.y;
			} else if (max.y < v.y) {
				max.y = v.y;
			}
			if (min.z > v.z) {
				min.z = v.z;
			} else if (max.z < v.z) {
				max.z = v.z;
			}
		}
		
		// inform players on board
		double rx = Math.round(min.x + worldObj.rand.nextInt(Math.max(1, (int) (max.x - min.x))));
		double ry = Math.round(min.y + worldObj.rand.nextInt(Math.max(1, (int) (max.y - min.y))));
		double rz = Math.round(min.z + worldObj.rand.nextInt(Math.max(1, (int) (max.z - min.z))));
		ship.messageToAllPlayersOnShip(this, "Ship collision detected around " + (int) rx + ", " + (int) ry + ", " + (int) rz + ". Damage report pending...");
		
		// randomize if too many collision points
		int nbExplosions = Math.min(5, collisionPoints.size());
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info("doCollisionDamage nbExplosions " + nbExplosions + "/" + collisionPoints.size());
		}
		for (int i = 0; i < nbExplosions; i++) {
			// get location
			Vector3 current;
			if (nbExplosions < collisionPoints.size()) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("doCollisionDamage random #" + i);
				}
				current = collisionPoints.get(worldObj.rand.nextInt(collisionPoints.size()));
			} else {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("doCollisionDamage get " + i);
				}
				current = collisionPoints.get(i);
			}
			
			// compute explosion strength with a jitter, at least 1 TNT
			float strength = Math.max(4.0F, collisionStrength / nbExplosions - 2.0F + 2.0F * worldObj.rand.nextFloat());
			
			(atTarget ? targetWorld : worldObj).newExplosion((Entity) null, current.x, current.y, current.z, strength, atTarget, atTarget);
			WarpDrive.logger.info("Ship collision caused explosion at " + current.x + ", " + current.y + ", " + current.z + " with strength " + strength);
		}
	}
	
	private void restoreEntitiesPosition() {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Restoring entities position");
		}
		LocalProfiler.start("EntityJump.restoreEntitiesPosition");
		
		if (ship.entitiesOnShip != null) {
			for (MovingEntity movingEntity : ship.entitiesOnShip) {
				Entity entity = movingEntity.entity;
				
				if (entity == null) {
					continue;
				}
				
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("Entity restoring position at (" + movingEntity.oldX + " " + movingEntity.oldY + " " + movingEntity.oldZ + ")");
				}
				
				// Update position
				if (entity instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) entity;
					
					player.setPositionAndUpdate(movingEntity.oldX, movingEntity.oldY, movingEntity.oldZ);
				} else {
					entity.setPosition(movingEntity.oldX, movingEntity.oldY, movingEntity.oldZ);
				}
			}
		}
		
		LocalProfiler.stop();
	}
		
	private boolean moveEntities() {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Moving entities");
		}
		LocalProfiler.start("EntityJump.moveEntities");
		
		if (ship.entitiesOnShip != null) {
			for (MovingEntity me : ship.entitiesOnShip) {
				Entity entity = me.entity;
				
				if (entity == null) {
					continue;
				}
				
				double oldEntityX = me.oldX;
				double oldEntityY = me.oldY;
				double oldEntityZ = me.oldZ;
				Vec3 target = transformation.apply(oldEntityX, oldEntityY, oldEntityZ);
				double newEntityX = target.xCoord;
				double newEntityY = target.yCoord;
				double newEntityZ = target.zCoord;
				
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);
				}
				
				// Travel to another dimension if needed
				if (betweenWorlds) {
					MinecraftServer server = MinecraftServer.getServer();
					WorldServer from = server.worldServerForDimension(worldObj.provider.dimensionId);
					WorldServer to = server.worldServerForDimension(targetWorld.provider.dimensionId);
					SpaceTeleporter teleporter = new SpaceTeleporter(to, 0,
							MathHelper.floor_double(newEntityX),
							MathHelper.floor_double(newEntityY),
							MathHelper.floor_double(newEntityZ));
					
					if (entity instanceof EntityPlayerMP) {
						EntityPlayerMP player = (EntityPlayerMP) entity;
						server.getConfigurationManager().transferPlayerToDimension(player, targetWorld.provider.dimensionId, teleporter);
						player.sendPlayerAbilities();
					} else {
						server.getConfigurationManager().transferEntityToWorld(entity, worldObj.provider.dimensionId, from, to, teleporter);
					}
				}
				
				// Update position
				transformation.rotate(entity);
				if (entity instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) entity;
					
					ChunkCoordinates bedLocation = player.getBedLocation(player.worldObj.provider.dimensionId);
					
					if (bedLocation != null && ship.minX <= bedLocation.posX && ship.maxX >= bedLocation.posX && ship.minY <= bedLocation.posY && ship.maxY >= bedLocation.posY
							&& ship.minZ <= bedLocation.posZ && ship.maxZ >= bedLocation.posZ) {
						bedLocation = transformation.apply(bedLocation);
						player.setSpawnChunk(bedLocation, false);
					}
					player.setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
				} else {
					entity.setPosition(newEntityX, newEntityY, newEntityZ);
				}
			}
		}
		
		LocalProfiler.stop();
		return true;
	}
	
	public int[] getVector(int i) {
		int v[] = { 0, 0, 0 };
		
		switch (i) {
		case -1:
			v[1] = 1;
			break;
			
		case -2:
			v[1] = -1;
			break;
			
		case 0:
			v[0] = ship.dx;
			v[2] = ship.dz;
			break;
			
		case 180:
			v[0] = -ship.dx;
			v[2] = -ship.dz;
			break;
			
		case 90:
			v[0] = ship.dz;
			v[2] = -ship.dx;
			break;
			
		case 270:
			v[0] = -ship.dz;
			v[2] = ship.dx;
			break;
			
		default:
			WarpDrive.logger.error(this + " Invalid direction " + i);
			break;
		}
		
		return v;
	}
	
	class CheckMovementResult {
		public ArrayList<Vector3> atSource;
		public ArrayList<Vector3> atTarget;
		public boolean isCollision = false;
		public String reason = "";
		
		CheckMovementResult() {
			this.atSource = new ArrayList<Vector3>(1);
			this.atTarget = new ArrayList<Vector3>(1);
			this.isCollision = false;
			this.reason = "Unknown reason";
		}
		
		public void add(double sx, double sy, double sz, double tx, double ty, double tz, boolean pisCollision, String preason) {
			atSource.add(new Vector3(sx, sy, sz));
			atTarget.add(new Vector3(tx, ty, tz));
			isCollision = isCollision || pisCollision;
			reason = preason;
			if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
				WarpDrive.logger.info("CheckMovementResult " + sx + ", " + sy + ", " + sz + " -> " + tx + ", " + ty + ", " + tz + " " + isCollision + " '" + reason + "'");
			}
		}
	};
	
	private CheckMovementResult checkMovement(int testDistance, boolean fullCollisionDetails) {
		CheckMovementResult result = new CheckMovementResult();
		if ((ship.direction == -1 && ship.maxY + testDistance > 255) && !betweenWorlds) {
			result.add(ship.coreX, ship.maxY + testDistance, ship.coreZ, ship.coreX + 0.5D, ship.maxY + testDistance + 1.0D, ship.coreZ + 0.5D, false,
					"Reactor will blow due +high limit");
			return result;
		}
		
		if ((ship.direction == -2 && ship.minY - testDistance <= 8) && !betweenWorlds) {
			result.add(ship.coreX, ship.minY - testDistance, ship.coreZ, ship.coreX + 0.5D, ship.maxY - testDistance, ship.coreZ + 0.5D, false, "Reactor will blow due -low limit");
			return result;
		}
		
		int movementVector[] = getVector(ship.direction);
		int lmoveX = movementVector[0] * testDistance;
		int lmoveY = movementVector[1] * testDistance;
		int lmoveZ = movementVector[2] * testDistance;
		
		int x, y, z, newX, newY, newZ;
		Block blockSource;
		Block blockTarget;
		for (y = ship.minY; y <= ship.maxY; y++) {
			newY = y + lmoveY;
			for (x = ship.minX; x <= ship.maxX; x++) {
				newX = x + lmoveX;
				for (z = ship.minZ; z <= ship.maxZ; z++) {
					newZ = z + lmoveZ;
					
					blockSource = worldObj.getBlock(x, y, z);
					blockTarget = worldObj.getBlock(newX, newY, newZ);
					if (Dictionary.BLOCKS_ANCHOR.contains(blockTarget)) {
						result.add(x, y, z,
							newX + 0.5D - movementVector[0] * 1.0D,
							newY + 0.5D - movementVector[1] * 1.0D,
							newZ + 0.5D - movementVector[2] * 1.0D,
							true, "Unpassable block " + blockTarget + " detected at destination (" + newX + ";" + newY + ";" + newZ + ")");
						if (!fullCollisionDetails) {
							return result;
						}
					}
					
					if ( blockSource != Blocks.air
					  && !Dictionary.BLOCKS_EXPANDABLE.contains(blockSource)
					  && blockTarget != Blocks.air
					  && !Dictionary.BLOCKS_EXPANDABLE.contains(blockTarget)) {
						result.add(x, y, z,
							newX + 0.5D + movementVector[0] * 0.1D,
							newY + 0.5D + movementVector[1] * 0.1D,
							newZ + 0.5D + movementVector[2]	* 0.1D,
							true, "Obstacle block #" + blockTarget + " detected at (" + newX + ", " + newY + ", " + newZ + ")");
						if (!fullCollisionDetails) {
							return result;
						}
					}
				}
			}
		}
		
		if (fullCollisionDetails && result.isCollision) {
			return result;
		} else {
			return null;
		}
	}
	
	private static ArrayList<Object> removeDuplicates(List<TileEntity> l) {
		Set<TileEntity> s = new TreeSet<TileEntity>(new Comparator<TileEntity>() {
			@Override
			public int compare(TileEntity o1, TileEntity o2) {
				if (o1.xCoord == o2.xCoord && o1.yCoord == o2.yCoord && o1.zCoord == o2.zCoord) {
					if (WarpDriveConfig.LOGGING_JUMPBLOCKS) {
						WarpDrive.logger.info("Removed duplicated TE: " + o1 + ", " + o2);
					}
					return 0;
				} else {
					return 1;
				}
			}
		});
		s.addAll(l);
		return new ArrayList<Object>(Arrays.asList(s.toArray()));
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		WarpDrive.logger.error(this + " readEntityFromNBT()");
	}
	
	@Override
	protected void entityInit() {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.warn(this + " entityInit()");
		}
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound var1) {
		WarpDrive.logger.error(this + " writeEntityToNBT()");
	}
	
	@Override
	public String toString() {
		return String.format("%s/%d \'%s\' @ \'%s\' (%.2f %.2f %.2f) #%d",
			getClass().getSimpleName(), Integer.valueOf(getEntityId()),
			(ship == null || ship.shipCore == null) ? "~NULL~" : (ship.shipCore.uuid + ":" + ship.shipCore.shipName),
			worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ),
			Integer.valueOf(ticks));
	}
}
