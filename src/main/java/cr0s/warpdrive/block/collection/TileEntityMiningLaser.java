package cr0s.warpdrive.block.collection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.EnumMiningLaserMode;
import cr0s.warpdrive.data.SoundEvents;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Optional;

public class TileEntityMiningLaser extends TileEntityAbstractMiner {
	
	private final boolean canSilktouch = (WarpDriveConfig.MINING_LASER_SILKTOUCH_DEUTERIUM_L <= 0 || FluidRegistry.isFluidRegistered("deuterium"));
	
	private boolean isActive() {
		return currentState != STATE_IDLE;
	}
	
	private int layerOffset = 1;
	private boolean mineAllBlocks = true;
	
	private int delayTicks = 0;
	
	private static final int STATE_IDLE = 0;
	private static final int STATE_WARMUP = 1;
	private static final int STATE_SCANNING = 2;
	private static final int STATE_MINING = 3;
	private int currentState = 0;
	
	private boolean enoughPower = false;
	private int currentLayer;
	
	private int radiusCapacity = WarpDriveConfig.MINING_LASER_RADIUS_NO_LASER_MEDIUM;
	private final ArrayList<BlockPos> valuablesInLayer = new ArrayList<>();
	private int valuableIndex = 0;
	
	public TileEntityMiningLaser() {
		super();
		laserOutputSide = EnumFacing.DOWN;
		peripheralName = "warpdriveMiningLaser";
		addMethods(new String[] {
				"start",
				"stop",
				"state",
				"offset",
				"onlyOres",
				"silktouch"
		});
		CC_scripts = Arrays.asList("mine", "stop");
		laserMedium_maxCount = WarpDriveConfig.MINING_LASER_MAX_MEDIUMS_COUNT;
	}
	
	@SuppressWarnings("UnnecessaryReturnStatement")
	@Override
	public void update() {
		super.update();
		
		if (worldObj.isRemote) {
			return;
		}
		
		IBlockState blockState = worldObj.getBlockState(pos);
		if (currentState == STATE_IDLE) {
			delayTicks = WarpDriveConfig.MINING_LASER_WARMUP_DELAY_TICKS;
			updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.INACTIVE);
			
			// force start if no computer control is available
			if (!WarpDriveConfig.isComputerCraftLoaded && !WarpDriveConfig.isOpenComputersLoaded) {
				enableSilktouch = false;
				layerOffset = 1;
				mineAllBlocks = true;
				start();
			}
			return;
		}
		
		final boolean isOnPlanet = CelestialObjectManager.hasAtmosphere(worldObj, pos.getX(), pos.getZ());
		radiusCapacity = WarpDriveConfig.MINING_LASER_RADIUS_NO_LASER_MEDIUM
		               + cache_laserMedium_count * WarpDriveConfig.MINING_LASER_RADIUS_PER_LASER_MEDIUM;
		
		delayTicks--;
		if (currentState == STATE_WARMUP) {
			updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_LOW_POWER);
			if (delayTicks < 0) {
				delayTicks = WarpDriveConfig.MINING_LASER_SCAN_DELAY_TICKS;
				currentState = STATE_SCANNING;
				updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_LOW_POWER);
				return;
			}
			
		} else if (currentState == STATE_SCANNING) {
			if (delayTicks == WarpDriveConfig.MINING_LASER_SCAN_DELAY_TICKS - 1) {
				// check power level
				enoughPower = laserMedium_consumeExactly(isOnPlanet ? WarpDriveConfig.MINING_LASER_PLANET_ENERGY_PER_LAYER : WarpDriveConfig.MINING_LASER_SPACE_ENERGY_PER_LAYER, true);
				if (!enoughPower) {
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_LOW_POWER);
					delayTicks = WarpDriveConfig.MINING_LASER_WARMUP_DELAY_TICKS;
					return;
				} else {
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_POWERED);
				}
				
				// show current layer
				int age = Math.max(40, 5 * WarpDriveConfig.MINING_LASER_SCAN_DELAY_TICKS);
				double xMax = pos.getX() + radiusCapacity + 1.0D;
				double xMin = pos.getX() - radiusCapacity + 0.0D;
				double zMax = pos.getZ() + radiusCapacity + 1.0D;
				double zMin = pos.getZ() - radiusCapacity + 0.0D;
				double y = currentLayer + 1.0D;
				PacketHandler.sendBeamPacket(worldObj, new Vector3(xMin, y, zMin), new Vector3(xMax, y, zMin), 0.3F, 0.0F, 1.0F, age, 0, 50);
				PacketHandler.sendBeamPacket(worldObj, new Vector3(xMax, y, zMin), new Vector3(xMax, y, zMax), 0.3F, 0.0F, 1.0F, age, 0, 50);
				PacketHandler.sendBeamPacket(worldObj, new Vector3(xMax, y, zMax), new Vector3(xMin, y, zMax), 0.3F, 0.0F, 1.0F, age, 0, 50);
				PacketHandler.sendBeamPacket(worldObj, new Vector3(xMin, y, zMax), new Vector3(xMin, y, zMin), 0.3F, 0.0F, 1.0F, age, 0, 50);
				
			} else if (delayTicks < 0) {
				delayTicks = WarpDriveConfig.MINING_LASER_SCAN_DELAY_TICKS;
				if (currentLayer <= 0) {
					stop();
					return;
				}
				
				// consume power
				enoughPower = laserMedium_consumeExactly(isOnPlanet ? WarpDriveConfig.MINING_LASER_PLANET_ENERGY_PER_LAYER : WarpDriveConfig.MINING_LASER_SPACE_ENERGY_PER_LAYER, false);
				if (!enoughPower) {
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_LOW_POWER);
					delayTicks = WarpDriveConfig.MINING_LASER_WARMUP_DELAY_TICKS;
					return;
				} else {
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_POWERED);
				}
				
				// scan
				scanLayer();
				if (!valuablesInLayer.isEmpty()) {
					int r = (int) Math.ceil(radiusCapacity / 2.0D);
					int offset = (pos.getY() - currentLayer) % (2 * r);
					int age = Math.max(20, Math.round(2.5F * WarpDriveConfig.MINING_LASER_SCAN_DELAY_TICKS));
					double y = currentLayer + 1.0D;
					PacketHandler.sendBeamPacket(worldObj, laserOutput, new Vector3(pos.getX() - r + offset, y, pos.getZ() + r).translate(0.3D),
							0.0F, 0.0F, 1.0F, age, 0, 50);
					PacketHandler.sendBeamPacket(worldObj, laserOutput, new Vector3(pos.getX() + r, y, pos.getZ() + r - offset).translate(0.3D),
							0.0F, 0.0F, 1.0F, age, 0, 50);
					PacketHandler.sendBeamPacket(worldObj, laserOutput, new Vector3(pos.getX() + r - offset, y, pos.getZ() - r).translate(0.3D),
							0.0F, 0.0F, 1.0F, age, 0, 50);
					PacketHandler.sendBeamPacket(worldObj, laserOutput, new Vector3(pos.getX() - r, y, pos.getZ() - r + offset).translate(0.3D),
							0.0F, 0.0F, 1.0F, age, 0, 50);
					worldObj.playSound(null, pos, SoundEvents.LASER_HIGH, SoundCategory.BLOCKS, 4F, 1F);
					delayTicks = WarpDriveConfig.MINING_LASER_MINE_DELAY_TICKS;
					currentState = STATE_MINING;
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.MINING_POWERED);
					return;
					
				} else {
					worldObj.playSound(null, pos, SoundEvents.LASER_LOW, SoundCategory.BLOCKS, 4F, 1F);
					currentLayer--;
				}
			}
		} else if (currentState == STATE_MINING) {
			if (delayTicks < 0) {
				delayTicks = WarpDriveConfig.MINING_LASER_MINE_DELAY_TICKS;
				
				if (valuableIndex < 0 || valuableIndex >= valuablesInLayer.size()) {
					delayTicks = WarpDriveConfig.MINING_LASER_SCAN_DELAY_TICKS;
					currentState = STATE_SCANNING;
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.SCANNING_POWERED);
					
					// rescan same layer
					scanLayer();
					if (valuablesInLayer.size() <= 0) {
						currentLayer--;
					}
					return;
				}
				
				// consume power
				int requiredPower = isOnPlanet ? WarpDriveConfig.MINING_LASER_PLANET_ENERGY_PER_BLOCK : WarpDriveConfig.MINING_LASER_SPACE_ENERGY_PER_BLOCK;
				if (!mineAllBlocks) {
					requiredPower *= WarpDriveConfig.MINING_LASER_ORESONLY_ENERGY_FACTOR;
				}
				if (enableSilktouch) {
					requiredPower *= WarpDriveConfig.MINING_LASER_SILKTOUCH_ENERGY_FACTOR;
				}
				enoughPower = laserMedium_consumeExactly(requiredPower, false);
				if (!enoughPower) {
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.MINING_LOW_POWER);
					return;
				} else {
					updateBlockState(blockState, BlockMiningLaser.MODE, EnumMiningLaserMode.MINING_POWERED);
				}
				
				BlockPos valuable = valuablesInLayer.get(valuableIndex);
				valuableIndex++;
				
				// Mine valuable ore
				IBlockState blockStateValuable = worldObj.getBlockState(valuable);
				
				// Skip if block is too hard or its empty block (check again in case it changed)
				if (!canDig(blockStateValuable, valuable)) {
					delayTicks = Math.round(WarpDriveConfig.MINING_LASER_MINE_DELAY_TICKS * 0.2F);
					return;
				}
				int age = Math.max(10, Math.round((4 + worldObj.rand.nextFloat()) * WarpDriveConfig.MINING_LASER_MINE_DELAY_TICKS));
				PacketHandler.sendBeamPacket(worldObj, laserOutput, new Vector3(valuable).translate(0.5D),
						1.0F, 1.0F, 0.0F, age, 0, 50);
				worldObj.playSound(null, pos, SoundEvents.LASER_LOW, SoundCategory.BLOCKS, 4F, 1F);
				harvestBlock(valuable);
			}
		}
	}
	
	@Override
	protected void stop() {
		super.stop();
		currentState = STATE_IDLE;
		updateBlockState(null, BlockMiningLaser.MODE, EnumMiningLaserMode.INACTIVE);
	}
	
	private boolean canDig(IBlockState blockState, final BlockPos blockPos) {
		// ignore air
		if (worldObj.isAirBlock(blockPos)) {
			return false;
		}
		// check blacklists
		if (Dictionary.BLOCKS_SKIPMINING.contains(blockState.getBlock())) {
			return false;
		}
		if (Dictionary.BLOCKS_STOPMINING.contains(blockState.getBlock())) {
			stop();
			if (WarpDriveConfig.LOGGING_COLLECTION) {
				WarpDrive.logger.info(this + " Mining stopped by " + blockState + " at (" + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ() + ")");
			}
			return false;
		}
		// check whitelist
		if ( Dictionary.BLOCKS_MINING.contains(blockState.getBlock())
		  || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {
			return true;
		}
		// check area protection
		if (isBlockBreakCanceled(null, worldObj, blockPos)) {
			stop();
			if (WarpDriveConfig.LOGGING_COLLECTION) {
				WarpDrive.logger.info(this + " Mining stopped by cancelled event at (" + blockPos.getX() + " " + blockPos.getY()+ " " + blockPos.getZ() + ")");
			}
			return false;
		}
		// check default (explosion resistance is used to test for force fields and reinforced blocks, basically preventing mining a base or ship) 
		if (blockState.getBlock().getExplosionResistance(null) <= Blocks.OBSIDIAN.getExplosionResistance(null)) {
			return true;
		}
		if (WarpDriveConfig.LOGGING_COLLECTION) {
			WarpDrive.logger.info(this + " Rejecting " + blockState + " at (" + blockPos.getX() + " " + blockPos.getY()+ " " + blockPos.getZ() + ")");
		}
		return false;
	}
	
	private void scanLayer() {
		// WarpDrive.logger.info("Scanning layer");
		IBlockState blockState;
		for (int y = pos.getY() - 1; y > currentLayer; y --) {
			blockState = worldObj.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
			if (Dictionary.BLOCKS_STOPMINING.contains(blockState.getBlock())) {
				stop();
				if (WarpDriveConfig.LOGGING_COLLECTION) {
					WarpDrive.logger.info(this + " Mining stopped by " + blockState + " at (" + pos.getX() + " " + y + " " + pos.getZ() + ")");
				}
				return;
			}
		}

		valuablesInLayer.clear();
		valuableIndex = 0;
		int radius, x, z;
		int xMax, zMax;
		int xMin, zMin;
		BlockPos blockPos;
		
		// Search for valuable blocks
		x = pos.getX();
		z = pos.getZ();
		blockPos = new BlockPos(x, currentLayer, z);
		blockState = worldObj.getBlockState(blockPos);
		if (canDig(blockState, blockPos)) {
			if (mineAllBlocks || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {// Quarry collects all blocks or only collect valuables blocks
				valuablesInLayer.add(blockPos);
			}
		}
		for (radius = 1; radius <= radiusCapacity; radius++) {
			xMax = pos.getX() + radius;
			xMin = pos.getX() - radius;
			zMax = pos.getZ() + radius;
			zMin = pos.getZ() - radius;
			x = pos.getX();
			z = zMin;
			for (; x <= xMax; x++) {
				blockPos = new BlockPos(x, currentLayer, z);
				blockState = worldObj.getBlockState(blockPos);
				if (canDig(blockState, blockPos)) {
					if (mineAllBlocks || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(blockPos);
					}
				}
			}
			x = xMax;
			z++;
			for (; z <= zMax; z++) {
				blockPos = new BlockPos(x, currentLayer, z);
				blockState = worldObj.getBlockState(blockPos);
				if (canDig(blockState, blockPos)) {
					if (mineAllBlocks || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(blockPos);
					}
				}
			}
			x--;
			z = zMax;
			for (; x >= xMin; x--) {
				blockPos = new BlockPos(x, currentLayer, z);
				blockState = worldObj.getBlockState(blockPos);
				if (canDig(blockState, blockPos)) {
					if (mineAllBlocks || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(blockPos);
					}
				}
			}
			x = xMin;
			z--;
			for (; z > zMin; z--) {
				blockPos = new BlockPos(x, currentLayer, z);
				blockState = worldObj.getBlockState(blockPos);
				if (canDig(blockState, blockPos)) {
					if (mineAllBlocks || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(blockPos);
					}
				}
			}
			x = xMin;
			z = zMin;
			for (; x < pos.getX(); x++) {
				blockPos = new BlockPos(x, currentLayer, z);
				blockState = worldObj.getBlockState(blockPos);
				if (canDig(blockState, blockPos)) {
					if (mineAllBlocks || Dictionary.BLOCKS_ORES.contains(blockState.getBlock())) {// Quarry collects all blocks or only collect valuables blocks
						valuablesInLayer.add(blockPos);
					}
				}
			}
		}

		if (WarpDriveConfig.LOGGING_COLLECTION) {
			WarpDrive.logger.info(this + " Found " + valuablesInLayer.size() + " valuables");
		}
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		layerOffset = tagCompound.getInteger("layerOffset");
		mineAllBlocks = tagCompound.getBoolean("mineAllBlocks");
		currentState = tagCompound.getInteger("currentState");
		currentLayer = tagCompound.getInteger("currentLayer");
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		tagCompound.setInteger("layerOffset", layerOffset);
		tagCompound.setBoolean("mineAllBlocks", mineAllBlocks);
		tagCompound.setInteger("currentState", currentState);
		tagCompound.setInteger("currentLayer", currentLayer);
		return tagCompound;
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] start(Context context, Arguments arguments) {
		return start();
	}
	
	@SuppressWarnings("SameReturnValue")
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] stop(Context context, Arguments arguments) {
		stop();
		return null;
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] state(Context context, Arguments arguments) {
		return state();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] offset(Context context, Arguments arguments) {
		return offset(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] onlyOres(Context context, Arguments arguments) {
		return onlyOres(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] silktouch(Context context, Arguments arguments) {
		return silktouch(argumentsOCtoCC(arguments));
	}
	
	// Common OC/CC methods
	private Object[] start() {
		if (isActive()) {
			return new Object[] { false, "Already started" };
		}
		
		enableSilktouch &= canSilktouch;
		delayTicks = WarpDriveConfig.MINING_LASER_WARMUP_DELAY_TICKS;
		currentState = STATE_WARMUP;
		currentLayer = pos.getY() - layerOffset - 1;
		if (WarpDriveConfig.LOGGING_LUA) {
			WarpDrive.logger.info(this + " Starting from Y " + currentLayer + " with silktouch " + enableSilktouch);
		}
		return new Boolean[] { true };
	}
	
	private Object[] state() {
		final int energy = laserMedium_getEnergyStored();
		final String status = getStatusHeaderInPureText();
		final Integer retValuablesInLayer, retValuablesMined;
		if (isActive()) {
			retValuablesInLayer = valuablesInLayer.size();
			retValuablesMined = valuableIndex;
			
			return new Object[] { status, isActive(), energy, currentLayer, retValuablesMined, retValuablesInLayer };
		}
		return new Object[] { status, isActive(), energy, currentLayer, 0, 0 };
	}
	
	private Object[] onlyOres(Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			try {
				mineAllBlocks = ! Commons.toBool(arguments[0]);
				markDirty();
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.info(this + " onlyOres set to " + !mineAllBlocks);
				}
			} catch (Exception exception) {
				return new Object[] { !mineAllBlocks };
			}
		}
		return new Object[] { !mineAllBlocks };
	}
	
	private Object[] offset(Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			try {
				layerOffset = Math.min(256, Math.abs(Commons.toInt(arguments[0])));
				markDirty();
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.info(this + " offset set to " + layerOffset);
				}
			} catch (Exception exception) {
				return new Integer[] { layerOffset };
			}
		}
		return new Integer[] { layerOffset };
	}
	
	private Object[] silktouch(Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			try {
				enableSilktouch = Commons.toBool(arguments[0]);
				markDirty();
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.info(this + " silktouch set to " + enableSilktouch);
				}
			} catch (Exception exception) {
				return new Object[] { enableSilktouch };
			}
		}
		return new Object[] { enableSilktouch };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "start":
			return start();
			
		case "stop":
			stop();
			return null;
			
		case "state":
			return state();
			
		case "offset":
			return offset(arguments);
			
		case "onlyOres":
			return onlyOres(arguments);
			
		case "silktouch":
			return silktouch(arguments);
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public ITextComponent getStatusHeader() {
		final int energy = laserMedium_getEnergyStored();
		ITextComponent state = new TextComponentTranslation("IDLE (not mining)");
		if (currentState == STATE_IDLE) {
			state = new TextComponentTranslation("IDLE (not mining)");
		} else if (currentState == STATE_WARMUP) {
			state = new TextComponentTranslation("Warming up...");
		} else if (currentState == STATE_SCANNING) {
			if (mineAllBlocks) {
				state = new TextComponentTranslation("Scanning all");
			} else {
				state = new TextComponentTranslation("Scanning ores");
			}
		} else if (currentState == STATE_MINING) {
			if (mineAllBlocks) {
				state = new TextComponentTranslation("Mining all");
			} else {
				state = new TextComponentTranslation("Mining ores");
			}
			if (enableSilktouch) {
				state.appendSibling(new TextComponentTranslation(" with silktouch"));
			}
		}
		if (energy <= 0) {
			state.appendSibling(new TextComponentTranslation(" - Out of energy"));
		} else if (((currentState == STATE_SCANNING) || (currentState == STATE_MINING)) && !enoughPower) {
			state.appendSibling(new TextComponentTranslation(" - Not enough power"));
		}
		return state;
	}
	
	@Override
	public String toString() {
		return String.format("%s @ \'%s\' %d, %d, %d",
		                     getClass().getSimpleName(), 
		                     worldObj == null ? "~NULL~" : worldObj.provider.getSaveFolder(), 
		                     pos.getX(), pos.getY(), pos.getZ());
	}
}
