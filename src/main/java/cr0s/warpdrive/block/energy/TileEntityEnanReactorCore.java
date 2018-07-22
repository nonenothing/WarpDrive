package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.computer.IEnanReactorCore;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumReactorFace;
import cr0s.warpdrive.data.EnumReactorReleaseMode;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import net.minecraftforge.fml.common.Optional;

public class TileEntityEnanReactorCore extends TileEntityAbstractEnergy implements IEnanReactorCore {
	
	private static final int ENAN_REACTOR_SETUP_TICKS = 1200;
	
	// generation & instability is 'per tick'
	private static final double INSTABILITY_MIN = 0.004D;
	private static final double INSTABILITY_MAX = 0.060D;
	
	// laser stabilization is per shot
	// target is to consume 10% max output power every second, hence 2.5% per side
	// laser efficiency is 33% at 16% power (target spot), 50% at 24% power, 84% at 50% power, etc.
	// 10% * 20 * PR_MAX_GENERATION / (4 * 0.16) => ~200kRF => ~ max laser energy
	private static final double PR_MAX_LASER_ENERGY = 200000.0D;
	private static final double PR_MAX_LASER_EFFECT = INSTABILITY_MAX * 20 / 0.33D;
	
	private boolean hold = true; // hold updates and power output until reactor is controlled (i.e. don't explode on chunk-loading while computer is booting)
	
	// persistent properties
	private boolean isEnabled = false;
	private EnumReactorReleaseMode releaseMode = EnumReactorReleaseMode.OFF;
	private int releaseRate = 0;
	private int releaseAbove = 0;
	private double instabilityTarget = 50.0D;
	private int stabilizerEnergy = 10000;
	
	private int containedEnergy = 0;
	private double[] instabilityValues = new double[EnumReactorFace.maxInstabilities]; // no instability  = 0, explosion = 100
	
	// computed properties
	private int energyStored_max;
	private int generation_offset;
	private int generation_range;
	
	private int updateTicks = 0;
	private int setupTicks = 0;
	
	private float lasersReceived = 0;
	private int lastGenerationRate = 0;
	private int releasedThisTick = 0; // amount of energy released during current tick update
	private long releasedThisCycle = 0; // amount of energy released during current cycle
	private long releasedLastCycle = 0;
	
	public TileEntityEnanReactorCore() {
		super();
		
		peripheralName = "warpdriveEnanReactorCore";
		addMethods(new String[] {
			"enable",
			"energy",		// returns energy, max energy, energy rate
			"instability",	// returns ins0,1,2,3
			"instabilityTarget",
			"release",		// releases all energy
			"releaseRate",	// releases energy when more than arg0 is produced
			"releaseAbove",	// releases any energy above arg0 amount
			"stabilizerEnergy",
			"state"
		});
		CC_scripts = Collections.singletonList("startup");
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		
		energyStored_max  = WarpDriveConfig.ENAN_REACTOR_MAX_ENERGY_STORED_BY_TIER[enumTier.getIndex()];
		generation_offset = WarpDriveConfig.ENAN_REACTOR_GENERATION_MIN_RF_BY_TIER[enumTier.getIndex()];
		generation_range  = WarpDriveConfig.ENAN_REACTOR_GENERATION_MAX_RF_BY_TIER[enumTier.getIndex()] - generation_offset;
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
	}
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			return;
		}
		
		if (WarpDriveConfig.LOGGING_ENERGY) {
			WarpDrive.logger.info(String.format("updateTicks %d setupTicks %d releasedThisTick %6d lasersReceived %.5f releasedThisCycle %6d containedEnergy %8d",
			                                    updateTicks, setupTicks, releasedThisTick, lasersReceived, releasedThisCycle, containedEnergy));
		}
		releasedThisTick = 0;
		
		setupTicks--;
		if (setupTicks <= 0) {
			setupTicks = ENAN_REACTOR_SETUP_TICKS;
			scanSetup();
		}
		
		lasersReceived = Math.max(0.0F, lasersReceived - 0.05F);
		updateTicks--;
		if (updateTicks > 0) {
			return;
		}
		updateTicks = WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS;
		releasedLastCycle = releasedThisCycle;
		releasedThisCycle = 0;
		
		updateMetadata();
		
		if (!hold) {// still loading/booting => hold simulation
			// unstable at all time
			if (shouldExplode()) {
				explode();
			}
			increaseInstability();
			
			generateEnergy();
			
			runControlLoop();
		}
		
		sendEvent("reactorPulse", lastGenerationRate);
	}
	
	private void increaseInstability() {
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			// increase instability
			final int indexStability = reactorFace.indexStability;
			if (containedEnergy > 2000) {
				final double amountToIncrease = WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS
						* Math.max(INSTABILITY_MIN, INSTABILITY_MAX * Math.pow((world.rand.nextDouble() * containedEnergy) / energyStored_max, 0.1));
				if (WarpDriveConfig.LOGGING_ENERGY) {
					WarpDrive.logger.info(String.format("increaseInstability %.5f",
					                                    amountToIncrease));
				}
				instabilityValues[indexStability] += amountToIncrease;
			} else {
				// when charge is extremely low, reactor is naturally stabilizing, to avoid infinite decay
				final double amountToDecrease = WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS * Math.max(INSTABILITY_MIN, instabilityValues[indexStability] * 0.02D);
				instabilityValues[indexStability] = Math.max(0.0D, instabilityValues[indexStability] - amountToDecrease);
			}
		}
	}
	
	void decreaseInstability(final EnumReactorFace reactorFace, final int energy) {
		if (reactorFace.indexStability < 0) {
			return;
		}
		
		final int amount = convertInternalToRF_floor(energy);
		if (amount <= 1) {
			return;
		}
		
		lasersReceived = Math.min(10.0F, lasersReceived + 1.0F / WarpDriveConfig.ENAN_REACTOR_MAX_LASERS_PER_SECOND);
		double nospamFactor = 1.0D;
		if (lasersReceived > 1.0F) {
			nospamFactor = 0.5;
			world.newExplosion(null,
			                   pos.getX() + reactorFace.x - reactorFace.facingLaserProperty.getFrontOffsetX(),
			                   pos.getY() + reactorFace.y - reactorFace.facingLaserProperty.getFrontOffsetY(),
			                   pos.getZ() + reactorFace.z - reactorFace.facingLaserProperty.getFrontOffsetZ(),
			                   1, false, false);
		}
		final double normalisedAmount = Math.min(1.0D, Math.max(0.0D, amount / PR_MAX_LASER_ENERGY)); // 0.0 to 1.0
		final double baseLaserEffect = 0.5D + 0.5D * Math.cos( Math.PI * Math.log10(0.1D + 0.9D * normalisedAmount) ); // 0.0 to 1.0
		final double randomVariation = 0.8D + 0.4D * world.rand.nextDouble(); // ~1.0
		final double amountToRemove = PR_MAX_LASER_EFFECT * baseLaserEffect * randomVariation * nospamFactor;
		
		final int indexStability = reactorFace.indexStability;
		
		if (WarpDriveConfig.LOGGING_ENERGY) {
			if (indexStability == 3) {
				WarpDrive.logger.info(String.format("Instability on %s decreased by %.1f/%.1f after consuming %d/%.1f laserReceived is %.1f hence nospamFactor is %.3f",
				                                    reactorFace, amountToRemove, PR_MAX_LASER_EFFECT,
				                                    amount, PR_MAX_LASER_ENERGY, lasersReceived, nospamFactor));
			}
		}
		
		instabilityValues[indexStability] = Math.max(0, instabilityValues[indexStability] - amountToRemove);
	}
	
	private void generateEnergy() {
		double stabilityOffset = 0.5;
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			stabilityOffset *= Math.max(0.01D, instabilityValues[reactorFace.indexStability] / 100.0D);
		}
		
		if (isEnabled) {// producing, instability increases output, you want to take the risk
			final int amountToGenerate = (int) Math.ceil(WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS * (0.5D + stabilityOffset)
					* ( generation_offset
					  + generation_range * Math.pow(containedEnergy / (double) energyStored_max, 0.6D)));
			containedEnergy = Math.min(containedEnergy + amountToGenerate, energyStored_max);
			lastGenerationRate = amountToGenerate / WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS;
			if (WarpDriveConfig.LOGGING_ENERGY) {
				WarpDrive.logger.info(String.format("Generated %d", amountToGenerate));
			}
		} else {// decaying over 20s without producing power, you better have power for those lasers
			final int amountToDecay = (int) (WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS * (1.0D - stabilityOffset) * (generation_offset + containedEnergy * 0.01D));
			containedEnergy = Math.max(0, containedEnergy - amountToDecay);
			lastGenerationRate = 0;
			if (WarpDriveConfig.LOGGING_ENERGY) {
				WarpDrive.logger.info(String.format("Decayed %d", amountToDecay));
			}
		}
	}
	
	private void runControlLoop() {
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			if (instabilityValues[reactorFace.indexStability] > instabilityTarget) {
				final TileEntity tileEntity = world.getTileEntity(
					pos.add(reactorFace.x, reactorFace.y, reactorFace.z));
				if (tileEntity instanceof TileEntityEnanReactorLaser) {
					((TileEntityEnanReactorLaser) tileEntity).stabilize(stabilizerEnergy);
				}
			}
		}
	}
	
	private boolean shouldExplode() {
		boolean exploding = false;
		final StringBuilder laserStatus = new StringBuilder();
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			exploding = exploding || (instabilityValues[reactorFace.indexStability] >= 100);
			int laserEnergy = 0;
			final TileEntity tileEntity = world.getTileEntity(
					pos.add(reactorFace.x, reactorFace.y, reactorFace.z));
			if (tileEntity instanceof TileEntityEnanReactorLaser) {
				try {
					laserEnergy = (int) ((TileEntityEnanReactorLaser) tileEntity).energy()[0];
				} catch (final Exception exception) {
					exception.printStackTrace();
					WarpDrive.logger.error(String.format("%s tileEntity is %s", this, tileEntity));
				}
			}
			laserStatus.append(String.format("\n- face %s has reached instability %.2f while laser has %d energy available",
			                                 reactorFace.name,
			                                 instabilityValues[reactorFace.indexStability],
			                                 laserEnergy));
		}
		exploding &= (world.rand.nextInt(4) == 2);
		
		if (exploding) {
			WarpDrive.logger.info(String.format("%s Explosion triggered\nEnergy stored is %d, Laser received is %.2f, reactor is %s%s",
			                                    this,
			                                    containedEnergy,
			                                    lasersReceived,
			                                    isEnabled ? "ENABLED" : "DISABLED",
			                                    laserStatus.toString()));
			isEnabled = false;
		}
		return exploding;
	}
	
	private void explode() {
		// remove blocks randomly up to x blocks around (breaking whatever protection is there)
		final double normalizedEnergy = containedEnergy / (double) energyStored_max;
		final double factorEnergy = Math.pow(normalizedEnergy, 0.125);
		final int radius = (int) Math.round( WarpDriveConfig.ENAN_REACTOR_EXPLOSION_MAX_RADIUS_BY_TIER[enumTier.getIndex()]
		                                   * factorEnergy );
		final double chanceOfRemoval = WarpDriveConfig.ENAN_REACTOR_EXPLOSION_MAX_REMOVAL_CHANCE_BY_TIER[enumTier.getIndex()]
		                             * factorEnergy;
		if (WarpDriveConfig.LOGGING_ENERGY) {
			WarpDrive.logger.info(this + " Explosion radius is " + radius + ", Chance of removal is " + chanceOfRemoval);
		}
		if (radius > 1) {
			final MutableBlockPos mutableBlockPos = new MutableBlockPos(pos);
			final float explosionResistanceThreshold = Blocks.OBSIDIAN.getExplosionResistance(world, mutableBlockPos, null, null);
			for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
				for (int y = pos.getY() - radius; y <= pos.getY() + radius; y++) {
					for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
						if (z != pos.getZ() || y != pos.getY() || x != pos.getX()) {
							if (world.rand.nextDouble() < chanceOfRemoval) {
								mutableBlockPos.setPos(x, y, z);
								final float explosionResistanceActual = world.getBlockState(mutableBlockPos).getBlock().getExplosionResistance(world, mutableBlockPos, null, null);
								if (explosionResistanceActual >= explosionResistanceThreshold) {
									world.setBlockToAir(mutableBlockPos);
								}
							}
						}
					}
				}
			}
		}
		
		// remove reactor
		world.setBlockToAir(pos);
		
		// set a few augmented TnT around reactor core
		final int countExplosions = WarpDriveConfig.ENAN_REACTOR_EXPLOSION_COUNT_BY_TIER[enumTier.getIndex()];
		final float strengthMin = WarpDriveConfig.ENAN_REACTOR_EXPLOSION_STRENGTH_MIN_BY_TIER[enumTier.getIndex()];
		final int strengthRange = (int) Math.ceil(WarpDriveConfig.ENAN_REACTOR_EXPLOSION_STRENGTH_MAX_BY_TIER[enumTier.getIndex()] - strengthMin);
		for (int i = 0; i < countExplosions; i++) {
			world.newExplosion(null,
			                   pos.getX() + world.rand.nextInt(3) - 1.5D,
			                   pos.getY() + world.rand.nextInt(3) - 0.5D,
			                   pos.getZ() + world.rand.nextInt(3) - 1.5D,
				               strengthMin + world.rand.nextInt(strengthRange), true, true);
		}
	}
	
	private void updateMetadata() {
		double maxInstability = 0.0D;
		for (final Double instability : instabilityValues) {
			if (instability > maxInstability) {
				maxInstability = instability;
			}
		}
		final int instabilityNibble = (int) Math.max(0, Math.min(3, Math.round(maxInstability / 25.0D)));
		final int energyNibble = (int) Math.max(0, Math.min(3, Math.round(4.0D * containedEnergy / energyStored_max)));
		
		final IBlockState blockStateNew = getBlockType().getDefaultState()
		                                                .withProperty(BlockEnanReactorCore.ENERGY, energyNibble)
		                                                .withProperty(BlockEnanReactorCore.INSTABILITY, instabilityNibble);
		updateBlockState(null, blockStateNew);
	}
	
	@Override
	public void onBlockUpdateDetected() {
		super.onBlockUpdateDetected();
		
		setupTicks = 0;
	}
	
	private void scanSetup() {
		final MutableBlockPos mutableBlockPos = new MutableBlockPos(pos);
		
		// first check if we have the required 'air' blocks
		boolean isValid = true;
		for (final EnumReactorFace reactorFace : EnumReactorFace.get(enumTier)) {
			assert reactorFace.enumTier == enumTier;
			if (reactorFace.indexStability < 0) {
				mutableBlockPos.setPos(pos.getX() + reactorFace.x,
				                       pos.getY() + reactorFace.y,
				                       pos.getZ() + reactorFace.z);
				final IBlockState blockState = world.getBlockState(mutableBlockPos);
				final boolean isAir = blockState.getBlock().isAir(blockState, world, mutableBlockPos);
				if (!isAir) {
					isValid = false;
					final Vector3 vPosition = new Vector3(mutableBlockPos).translate(0.5D);
					PacketHandler.sendSpawnParticlePacket(world, "jammed", (byte) 5, vPosition,
					                                      new Vector3(0.0D, 0.0D, 0.0D),
					                                      1.0F, 1.0F, 1.0F,
					                                      1.0F, 1.0F, 1.0F,
					                                      32);
				}
			}
		}
		
		// then update the stabilization lasers accordingly
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			mutableBlockPos.setPos(pos.getX() + reactorFace.x,
			                       pos.getY() + reactorFace.y,
			                       pos.getZ() + reactorFace.z);
			final TileEntity tileEntity = world.getTileEntity(mutableBlockPos);
			if (tileEntity instanceof TileEntityEnanReactorLaser) {
				if (isValid) {
					((TileEntityEnanReactorLaser) tileEntity).setReactorFace(reactorFace, this);
				} else {
					((TileEntityEnanReactorLaser) tileEntity).setReactorFace(EnumReactorFace.UNKNOWN, null);
				}
			} else {
				final Vector3 vPosition = new Vector3(mutableBlockPos).translate(0.5D);
				PacketHandler.sendSpawnParticlePacket(world, "jammed", (byte) 5, vPosition,
				                                      new Vector3(0.0D, 0.0D, 0.0D),
				                                      1.0F, 1.0F, 1.0F,
				                                      1.0F, 1.0F, 1.0F,
				                                      32);
			}
		}
	}
	
	// Common OC/CC methods
	@Override
	public Object[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final boolean enableRequest;
			try {
				enableRequest = Commons.toBool(arguments[0]);
			} catch (final Exception exception) {
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.error(String.format("%s LUA error on enable(): Boolean expected for 1st argument %s",
					                                     this, arguments[0]));
				}
				return enable(new Object[0]);
			}
			if (isEnabled && !enableRequest) {
				sendEvent("reactorDeactivation");
			} else if (!isEnabled && enableRequest) {
				sendEvent("reactorActivation");
			}
			isEnabled = enableRequest;
		}
		return new Object[] { isEnabled };
	}
	
	@Override
	public Object[] energy() {
		return new Object[] { containedEnergy, energyStored_max, releasedLastCycle / WarpDriveConfig.ENAN_REACTOR_UPDATE_INTERVAL_TICKS };
	}
	
	@Override
	public Object[] instability() {
		// computer is alive => start updating reactor
		hold = false;
		
		final ArrayList<Double> result = new ArrayList<>(16);
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			result.add(reactorFace.indexStability, instabilityValues[reactorFace.indexStability]);
		}
		return result.toArray();
	}
	
	@Override
	public Object[] instabilityTarget(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final double instabilityTargetRequested;
			try {
				instabilityTargetRequested = Commons.toDouble(arguments[0]);
			} catch (final Exception exception) {
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.error(String.format("%s LUA error on instabilityTarget(): Double expected for 1st argument %s",
					                                     this, arguments[0]));
				}
				return new Object[] { instabilityTarget };
			}
			
			instabilityTarget = Commons.clamp(0.0D, 100.0D, instabilityTargetRequested);
		}
		return new Object[] { instabilityTarget };
	}
	
	@Override
	public Object[] release(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final boolean releaseRequested;
			try {
				releaseRequested = Commons.toBool(arguments[0]);
			} catch (final Exception exception) {
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.error(String.format("%s LUA error on release(): Boolean expected for 1st argument %s",
					                                     this, arguments[0]));
				}
				return new Object[] { releaseMode != EnumReactorReleaseMode.OFF };
			}
			
			releaseMode = releaseRequested ? EnumReactorReleaseMode.UNLIMITED : EnumReactorReleaseMode.OFF;
			releaseAbove = 0;
			releaseRate = 0;
		}
		return new Object[] { releaseMode != EnumReactorReleaseMode.OFF };
	}
	
	@Override
	public Object[] releaseRate(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final int releaseRateRequested;
			try {
				releaseRateRequested = Commons.toInt(arguments[0]);
			} catch (final Exception exception) {
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.error(String.format("%s LUA error on releaseRate(): Integer expected for 1st argument %s",
					                                     this, arguments[0]));
				}
				return new Object[] { releaseMode.getName(), releaseRate };
			}
			
			if (releaseRateRequested <= 0) {
				releaseMode = EnumReactorReleaseMode.OFF;
				releaseRate = 0;
			} else {
				// player has to adjust it
				releaseRate = releaseRateRequested;
				releaseMode = EnumReactorReleaseMode.AT_RATE;
			}
		}
		return new Object[] { releaseMode.getName(), releaseRate };
	}
	
	@Override
	public Object[] releaseAbove(final Object[] arguments) {
		final int releaseAboveRequested;
		try {
			releaseAboveRequested = Commons.toInt(arguments[0]);
		} catch (final Exception exception) {
			if (WarpDriveConfig.LOGGING_LUA) {
				WarpDrive.logger.error(String.format("%s LUA error on releaseAbove(): Integer expected for 1st argument %s",
				                                     this, arguments[0]));
			}
			return new Object[] { releaseMode.getName(), releaseAbove };
		}
		
		if (releaseAboveRequested <= 0) {
			releaseMode = EnumReactorReleaseMode.OFF;
			releaseAbove = 0;
		} else {
			releaseMode = EnumReactorReleaseMode.ABOVE;
			releaseAbove = releaseAboveRequested;
		}
		
		return new Object[] { releaseMode.getName(), releaseAbove };
	}
	
	@Override
	public Object[] stabilizerEnergy(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final int stabilizerEnergyRequested;
			try {
				stabilizerEnergyRequested = Commons.toInt(arguments[0]);
			} catch (final Exception exception) {
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.error(String.format("%s LUA error on stabilizerEnergy(): Integer expected for 1st argument %s",
					                                     this, arguments[0]));
				}
				return new Object[] { stabilizerEnergy };
			}
			
			stabilizerEnergy = Commons.clamp(0, Integer.MAX_VALUE, stabilizerEnergyRequested);
		}
		return new Object[] { stabilizerEnergy };
	}
	
	@Override
	public Object[] state() {
		final String status = getStatusHeaderInPureText();
		if (releaseMode == EnumReactorReleaseMode.OFF || releaseMode == EnumReactorReleaseMode.UNLIMITED) {
			return new Object[] { status, isEnabled, containedEnergy, releaseMode.getName(), 0 };
		} else if (releaseMode == EnumReactorReleaseMode.ABOVE) {
			return new Object[] { status, isEnabled, containedEnergy, releaseMode.getName(), releaseAbove };
		} else {
			return new Object[] { status, isEnabled, containedEnergy, releaseMode.getName(), releaseRate };
		}
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] enable(final Context context, final Arguments arguments) {
		return enable(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Override
	@Optional.Method(modid = "opencomputers")
	public Object[] energy(final Context context, final Arguments arguments) {
		return energy();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] instability(final Context context, final Arguments arguments) {
		return instability();
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] instabilityTarget(final Context context, final Arguments arguments) {
		return instabilityTarget(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] release(final Context context, final Arguments arguments) {
		return release(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] releaseRate(final Context context, final Arguments arguments) {
		return releaseRate(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] releaseAbove(final Context context, final Arguments arguments) {
		return releaseAbove(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] stabilizerEnergy(final Context context, final Arguments arguments) {
		return stabilizerEnergy(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] state(final Context context, final Arguments arguments) {
		return state();
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "computercraft")
	public Object[] callMethod(@Nonnull final IComputerAccess computer, @Nonnull final ILuaContext context, final int method, @Nonnull final Object[] arguments) {
		// computer is alive => start updating reactor
		hold = false;
		
		final String methodName = CC_getMethodNameAndLogCall(method, arguments);
		
		try {
			switch (methodName) {
			case "enable":
				return enable(arguments);
				
			case "energy":
				return energy();
				
			case "instability":
				return instability();
				
			case "instabilityTarget":
				return instabilityTarget(arguments);
				
			case "release":
				return release(arguments);
				
			case "releaseRate":
				return releaseRate(arguments);
				
			case "releaseAbove":
				return releaseAbove(arguments);
				
			case "stabilizerEnergy":
				return stabilizerEnergy(arguments);
				
			case "state":
				return state();
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
			return new String[] { exception.getMessage() };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	// POWER INTERFACES
	@Override
	public int energy_getPotentialOutput() {
		if (hold) {// still loading/booting => hold output
			return 0;
		}
		int result = 0;
		final int capacity = Math.max(0, 2 * lastGenerationRate - releasedThisTick);
		if (releaseMode == EnumReactorReleaseMode.UNLIMITED) {
			result = Math.min(Math.max(0, containedEnergy), capacity);
			if (WarpDriveConfig.LOGGING_ENERGY) {
				WarpDrive.logger.info(String.format("PotentialOutput Manual %d RF (%d internal) capacity %d",
				                                    result, convertRFtoInternal_floor(result), capacity));
			}
		} else if (releaseMode == EnumReactorReleaseMode.ABOVE) {
			result = Math.min(Math.max(0, containedEnergy - releaseAbove), capacity);
			if (WarpDriveConfig.LOGGING_ENERGY) {
				WarpDrive.logger.info(String.format("PotentialOutput Above %d RF (%d internal) capacity %d",
				                                    result, convertRFtoInternal_floor(result), capacity));
			}
		} else if (releaseMode == EnumReactorReleaseMode.AT_RATE) {
			final int remainingRate = Math.max(0, releaseRate - releasedThisTick);
			result = Math.min(Math.max(0, containedEnergy), Math.min(remainingRate, capacity));
			if (WarpDriveConfig.LOGGING_ENERGY) {
				WarpDrive.logger.info(String.format("PotentialOutput Rated %d RF (%d internal) remainingRate %d RF/t capacity %d",
				                                    result, convertRFtoInternal_floor(result), remainingRate, capacity));
			}
		}
		return (int) convertRFtoInternal_floor(result);
	}
	
	@Override
	public boolean energy_canOutput(final EnumFacing from) {
		return from.equals(EnumFacing.UP) || from.equals(EnumFacing.DOWN);
	}
	
	@Override
	protected void energy_outputDone(final long energyOutput_internal) {
		final long energyOutput_RF = convertInternalToRF_ceil(energyOutput_internal);
		containedEnergy -= energyOutput_RF;
		if (containedEnergy < 0) {
			containedEnergy = 0;
		}
		releasedThisTick += energyOutput_RF;
		releasedThisCycle += energyOutput_RF;
		if (WarpDriveConfig.LOGGING_ENERGY) {
			WarpDrive.logger.info(String.format("OutputDone %d (%d RF)",
			                                    energyOutput_internal, energyOutput_RF));
		}
	}
	
	@Override
	public int energy_getEnergyStored() {
		return (int) Commons.clamp(0L, energy_getMaxStorage(), convertRFtoInternal_floor(containedEnergy));
	}
	
	@Override
	public int energy_getMaxStorage() {
		return (int) convertRFtoInternal_floor(WarpDriveConfig.ENAN_REACTOR_MAX_ENERGY_STORED_BY_TIER[enumTier.getIndex()]);
	}
	
	// Forge overrides
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		
		tagCompound.setBoolean("isEnabled", isEnabled);
		tagCompound.setInteger("releaseMode", releaseMode.ordinal());
		tagCompound.setInteger("releaseRate", releaseRate);
		tagCompound.setInteger("releaseAbove", releaseAbove);
		tagCompound.setDouble("instabilityTarget", instabilityTarget);
		tagCompound.setInteger("stabilizerEnergy", stabilizerEnergy);
		
		tagCompound.setInteger("energy", containedEnergy);
		final NBTTagCompound tagCompoundInstability = new NBTTagCompound();
		for (final EnumReactorFace reactorFace : EnumReactorFace.getLasers(enumTier)) {
			tagCompoundInstability.setDouble(reactorFace.name, instabilityValues[reactorFace.indexStability]);
		}
		tagCompound.setTag("instability", tagCompoundInstability);
		return tagCompound;
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		isEnabled = tagCompound.getBoolean("isEnabled");
		releaseMode = EnumReactorReleaseMode.get(tagCompound.getInteger("releaseMode"));
		releaseRate = tagCompound.getInteger("releaseRate");
		releaseAbove = tagCompound.getInteger("releaseAbove");
		instabilityTarget = tagCompound.getDouble("instabilityTarget");
		stabilizerEnergy = tagCompound.getInteger("stabilizerEnergy");
		
		containedEnergy = tagCompound.getInteger("energy");
		final NBTTagCompound tagCompoundInstability = tagCompound.getCompoundTag("instability");
		// tier isn't defined yet, so we check all candidates
		for (final EnumReactorFace reactorFace : EnumReactorFace.values()) {
			if (reactorFace.indexStability < 0) {
				continue;
			}
			if (tagCompoundInstability.hasKey(reactorFace.name)) {
				instabilityValues[reactorFace.indexStability] = tagCompoundInstability.getDouble(reactorFace.name);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeItemDropNBT(tagCompound);
		tagCompound.removeTag("isEnabled");
		tagCompound.removeTag("releaseMode");
		tagCompound.removeTag("releaseRate");
		tagCompound.removeTag("releaseAbove");
		tagCompound.removeTag("instabilityTarget");
		tagCompound.removeTag("stabilizerEnergy");
		
		tagCompound.removeTag("energy");
		tagCompound.removeTag("instability");
		return tagCompound;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s",
		                     getClass().getSimpleName(), 
		                     connectedComputers == null ? "~NULL~" : connectedComputers,
		                     Commons.format(world, pos));
	}
}