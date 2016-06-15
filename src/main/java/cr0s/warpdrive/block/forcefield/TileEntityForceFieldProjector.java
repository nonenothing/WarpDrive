package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IForceFieldShape;
import cr0s.warpdrive.config.*;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.data.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TileEntityForceFieldProjector extends TileEntityAbstractForceField {
	private static final int PROJECTOR_MAX_ENERGY_STORED = 100000;
	private static final int PROJECTOR_COOLDOWN_TICKS = 200;
	private static final int PROJECTOR_PROJECTION_UPDATE_TICKS = 8;
	private static final int PROJECTOR_SETUP_TICKS = 20;
	private static final int PROJECTOR_SOUND_UPDATE_TICKS = 200; // TODO
	private static final int PROJECTOR_SCAN_MAX_BLOCKS_PER_UPDATE = 200;
	private static final int PROJECTOR_PLACE_MAX_BLOCKS_PER_UPDATE = 100;
	
	// persistent properties
	public boolean isDoubleSided;
	private EnumForceFieldShape shape;
	
	// computed properties
	private int cooldownTicks;
	private int setupTicks;
	private int updateTicks;
	protected boolean isPowered = true;
	private boolean isMuted = false;
	private ForceFieldSetup forceFieldSetup_cache;
	
	// carry over speed to next tick, useful for slow interactions
	private float carryScanSpeed;
	private float carryPlaceSpeed;
	
	// allow only one computation at a time
	private static final AtomicBoolean isGlobalThreadRunning = new AtomicBoolean(false);
	// computation is ongoing for this specific tile
	private final AtomicBoolean isThreadRunning = new AtomicBoolean(false);
	// parameters have changed, new computation is required
	private final AtomicBoolean isDirty = new AtomicBoolean(true);
	
	private Set<VectorI> calculated_interiorField = null;
	private Set<VectorI> calculated_forceField = null;
	private Iterator<VectorI> iteratorForcefield = null;
	
	// currently placed forcefield blocks
	private final Set<VectorI> vForceFields = new HashSet<>();
	
	public TileEntityForceFieldProjector() {
		super();
		
		peripheralName = "warpdriveForceFieldProjector";
		addMethods(new String[] {
			"status"    // isConnected, isPowered, shape
		});
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		cooldownTicks = worldObj.rand.nextInt(PROJECTOR_COOLDOWN_TICKS);
		setupTicks = worldObj.rand.nextInt(PROJECTOR_SETUP_TICKS);
		updateTicks = worldObj.rand.nextInt(PROJECTOR_PROJECTION_UPDATE_TICKS);
		getForceFieldSetup();
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		// Frequency is not set
		if (!isConnected) {
			return;
		}
		
		// clear setup cache periodically
		setupTicks--;
		if (setupTicks <= 0) {
			setupTicks = PROJECTOR_SETUP_TICKS;
			forceFieldSetup_cache = null;
		}
		
		// Powered ?
		int energyRequired;
		energyRequired = getEnergyRequired();
		isPowered = getEnergyStored() >= energyRequired;
		
		boolean isEnabledAndValid = isEnabled && isValid();
		boolean isOn = isEnabledAndValid && cooldownTicks <= 0 && isPowered;
		if (isOn) {
			consumeEnergy(energyRequired, false);
			cooldownTicks = 0;
			
			updateTicks--;
			if (!worldObj.isRemote) {
				if (updateTicks <= 0) {
					updateTicks = PROJECTOR_PROJECTION_UPDATE_TICKS;
					if (!isCalculated()) {
						calculateForceField();
					} else {
						projectForceField();
					}
				}
			} else {
				// TODO add some animation
				if (updateTicks <= 0) {
					updateTicks = PROJECTOR_SOUND_UPDATE_TICKS;
					if (isPowered && !isMuted) {
						worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "projecting", 1.0F, 0.85F + 0.15F * worldObj.rand.nextFloat());
					}
				}
			}
			
		} else if (!worldObj.isRemote) {
			destroyForceField(false);
			if (cooldownTicks > 0) {
				cooldownTicks--;
			} else if (isEnabledAndValid) {
				cooldownTicks = PROJECTOR_COOLDOWN_TICKS;
				// TODO: localization
				String msg = "We're running out of power captain, reduce our consumption or get that scottish engineer to boost our power!";
				
				AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 10, yCoord - 10, zCoord - 10, xCoord + 10, yCoord + 10, zCoord + 10);
				List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
				
				for (Entity entity : list) {
					if (entity == null || (!(entity instanceof EntityPlayer)) || entity instanceof FakePlayer) {
						continue;
					}
					
					WarpDrive.addChatMessage((EntityPlayer) entity, "[Projector] " + msg);
				}
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		destroyForceField(true);
	}
	
	private int getEnergyRequired() {
		return 6 + 2 * tier;
	}
	
	public boolean isValid() {
		return getShape() != EnumForceFieldShape.NONE;
	}
	
	public boolean isCalculated() {
		return !isDirty.get() && !isThreadRunning.get();
	}
	
	private void calculateForceField() {
		if ((!worldObj.isRemote) && isValid()) {
			if (!isGlobalThreadRunning.getAndSet(true)) {
				isThreadRunning.set(true);
				isDirty.set(false);
				iteratorForcefield = null;
				calculated_interiorField = null;
				calculated_forceField = null;
				vForceFields.clear();
				
				new ThreadCalculation(this).start();
			}
		}
	}
	
	private void calculation_done(Set<VectorI> interiorField, Set<VectorI> forceField) {
		if (interiorField == null || forceField == null) {
			calculated_interiorField = new HashSet<>(0);
			calculated_forceField = new HashSet<>(0);
		} else {
			calculated_interiorField = interiorField;
			calculated_forceField = forceField;
		}
		isThreadRunning.set(false);
		isGlobalThreadRunning.set(false);
	}
	
	boolean isPartOfForceField(VectorI vector) {
		if (!isEnabled || !isValid()) {
			return false;
		}
		if (!isCalculated()) {
			return true;
		}
		// only consider the forcefield itself
		return calculated_forceField.contains(vector);
	}
	
	private Set<VectorI> getInteriorPoints() {
		if (!isCalculated()) {
			throw new ConcurrentModificationException("Calculation ongoing...");
		}
		return calculated_interiorField;
	}
	
	private void projectForceField() {
		if (worldObj.isRemote || (!isCalculated())) {
			return;
		}
		
		ForceFieldSetup forceFieldSetup = getForceFieldSetup();
		
		// compute maximum number of blocks to scan
		int countScanned = 0;
		float floatScanSpeed = Math.min(calculated_forceField.size(), PROJECTOR_SCAN_MAX_BLOCKS_PER_UPDATE);
		floatScanSpeed = Math.min(forceFieldSetup.maxScanSpeed * PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F + carryScanSpeed, floatScanSpeed);
		int countMaxScanned = (int)Math.floor(floatScanSpeed);
		carryScanSpeed = floatScanSpeed - countMaxScanned;
		
		// compute maximum number of blocks to place
		int countPlaced = 0;
		float floatPlaceSpeed = Math.min(calculated_forceField.size(), PROJECTOR_PLACE_MAX_BLOCKS_PER_UPDATE);
		floatPlaceSpeed = Math.min(forceFieldSetup.maxPlaceSpeed * PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F + carryPlaceSpeed, floatPlaceSpeed);
		int countMaxPlaced = (int)Math.floor(floatPlaceSpeed);
		carryPlaceSpeed = floatPlaceSpeed - countMaxPlaced;
		
		Set<TileEntityForceFieldProjector> projectors = new HashSet<>();
		if (forceFieldSetup.hasFusion) {
			for (TileEntity tileEntity : ForceFieldRegistry.getTileEntities(getBeamFrequency())) {
				if ( (tileEntity instanceof TileEntityForceFieldProjector)
					&& (tileEntity != this)
					&& (((TileEntityForceFieldProjector) tileEntity).worldObj == worldObj)
					&& (((TileEntityForceFieldProjector) tileEntity).isEnabled)
					&& (((TileEntityForceFieldProjector) tileEntity).isValid())
					&& (((TileEntityForceFieldProjector) tileEntity).isCalculated())) {
					projectors.add((TileEntityForceFieldProjector) tileEntity);
				}
			}
		}
		
		VectorI vector;
		Block block;
		boolean doProjectThisBlock;
		
		while (countScanned < countMaxScanned && countPlaced < countMaxPlaced) {
			if (iteratorForcefield == null || !iteratorForcefield.hasNext()) {
				iteratorForcefield = calculated_forceField.iterator();
			}
			countScanned++;
			
			vector = iteratorForcefield.next();
			
			if (!worldObj.blockExists(vector.x, vector.y, vector.z) || !worldObj.getChunkFromBlockCoords(vector.x, vector.z).isChunkLoaded) {
				continue;
			}
			
			block = vector.getBlock(worldObj);
			doProjectThisBlock = true;
			
			// skip if fusion upgrade is present and it's inside another projector area
			if (forceFieldSetup.hasFusion) {
				for (TileEntityForceFieldProjector projector : projectors) {
					if (projector.getInteriorPoints().contains(vector)) {
						doProjectThisBlock = false;
						break;
					}
				}
			}
			
			// skip if block properties prevents it
			if (doProjectThisBlock) {
				// MFR laser is unbreakable and replaceable
				// Liquid, vine and snow are replaceable
				if ((block == null) || (block == Blocks.tallgrass) || (block == Blocks.deadbush) || Dictionary.BLOCKS_EXPANDABLE.contains(block)) {
					// all good, continue
				} else if (forceFieldSetup.disintegrationLevel > 0) {
					float blockHardness = block.getBlockHardness(worldObj, vector.x, vector.y, vector.z);
					// stops on unbreakable or too hard
					if (blockHardness == -1.0F || blockHardness > forceFieldSetup.disintegrationLevel) {
						doProjectThisBlock = false;
					}
				} else {// doesn't have disintegration
					doProjectThisBlock = block.isReplaceable(worldObj, vector.x, vector.y, vector.z);
				}
			}
			
			// skip if area is protected
			if (doProjectThisBlock) {
				// TODO: check area protection
			}
			
			if (doProjectThisBlock) {
				if ((block != WarpDrive.blockForceFields[tier - 1]) && (!vector.equals(this))) {
					boolean hasConsumedEnergy = false;
					if (forceFieldSetup.disintegrationLevel > 0) {
						// TODO break the block
						// if (forceFieldSetup.attractionLevel > 10.0F) {
							// TODO store result in chest
						// } else {
							// TODO drop
						// }
					} else if (forceFieldSetup.hasStabilize) {
						// TODO collect from chest
						// TODO place block (ItemBlock.place?)
					} else if (forceFieldSetup.hasPump) {
						// TODO fluid support
					} else if (forceFieldSetup.temperatureLevel != 0.0F && forceFieldSetup.isInverted) {
						// TODO glass <> sandstone <> sand <> gravel <> cobblestone <> stone <> obsidian
						// TODO ice <> snow <> water <> air > fire
						// TODO obsidian < lava
					} else {
						hasConsumedEnergy = true;
						worldObj.setBlock(vector.x, vector.y, vector.z, WarpDrive.blockForceFields[tier - 1], 0, 2);
						
						TileEntity tileEntity = worldObj.getTileEntity(vector.x, vector.y, vector.z);
						if (tileEntity instanceof TileEntityForceField) {
							((TileEntityForceField) tileEntity).setProjector(new VectorI(this));
						}
						
						vForceFields.add(vector);
					}
					if (hasConsumedEnergy) {
						countPlaced++;
						consumeEnergy(Math.round(forceFieldSetup.placeEnergyCost), false);
					} else {
						consumeEnergy(Math.round(forceFieldSetup.scanEnergyCost), false);
					}
					
				} else {
					// scanning a valid position
					consumeEnergy(Math.round(forceFieldSetup.scanEnergyCost), false);
					
					// recover forcefield blocks from recalculation or chunk loading
					if (block == WarpDrive.blockForceFields[tier - 1] && !vForceFields.contains(vector)) {
						TileEntity tileEntity = worldObj.getTileEntity(vector.x, vector.y, vector.z);
						if (tileEntity instanceof TileEntityForceField && (((TileEntityForceField) tileEntity).getProjector() == this)) {
							vForceFields.add(vector);
						}
					}
				}
				
			} else {
				// scanning an invalid position
				consumeEnergy(Math.round(forceFieldSetup.scanEnergyCost), false);
				
				// remove our own force field block
				if (block == WarpDrive.blockForceFields[tier - 1]) {
					if (((BlockForceField) block).getProjector(worldObj, vector.x, vector.y, vector.z) == this) {
						worldObj.setBlockToAir(vector.x, vector.y, vector.z);
						vForceFields.remove(vector);
					}
				}
			}
		}
	}
	
	private void destroyForceField(boolean isChunkLoading) {
		if ((!worldObj.isRemote) && (!vForceFields.isEmpty())) {
			for (Iterator<VectorI> iterator = vForceFields.iterator(); iterator.hasNext();) {
				VectorI vector = iterator.next();
				if (!isChunkLoading) {
					if (!(worldObj.blockExists(vector.x, vector.y, vector.z))) {// chunk is not loaded, skip it
						continue;
					}
					if (!worldObj.getChunkFromBlockCoords(vector.x, vector.z).isChunkLoaded) {// chunk is unloading, skip it
						continue;
					}
				}
				Block block = vector.getBlock(worldObj);
				
				if (block == WarpDrive.blockForceFields[tier - 1]) {
					worldObj.setBlockToAir(vector.x, vector.y, vector.z);
				}
				iterator.remove();
			}
		}
		
		if ((!worldObj.isRemote) && isCalculated() && isChunkLoading) {
			for (VectorI vector : calculated_forceField) {
				Block block = vector.getBlock(worldObj);
				
				if (block == WarpDrive.blockForceFields[tier - 1]) {
					TileEntity tileEntity = worldObj.getTileEntity(vector.x, vector.y, vector.z);
					if (tileEntity instanceof TileEntityForceField && (((TileEntityForceField) tileEntity).getProjector() == this)) {
						worldObj.setBlockToAir(vector.x, vector.y, vector.z);
					}
				}
			}
		}
	}
	
	public IForceFieldShape getShapeProvider() {
		return getShape();
	}
	
	@Override
	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	@Override
	public void setBeamFrequency(final int parBeamFrequency) {
		super.setBeamFrequency(parBeamFrequency);
		isDirty.set(true);
		if (worldObj != null) {
			destroyForceField(false);
		}
	}
	
	private String getShapeStatus() {
		EnumForceFieldShape enumForceFieldShape = getShape();
		String strDisplayName = StatCollector.translateToLocalFormatted("warpdrive.forcefield.shape.statusLine." + enumForceFieldShape.unlocalizedName);
		if (enumForceFieldShape == EnumForceFieldShape.NONE) {
			return StatCollector.translateToLocalFormatted("warpdrive.forcefield.shape.statusLine.none", 
				strDisplayName);
		} else if (isDoubleSided) {
			return StatCollector.translateToLocalFormatted("warpdrive.forcefield.shape.statusLine.double",
				strDisplayName);
		} else {
			return StatCollector.translateToLocalFormatted("warpdrive.forcefield.shape.statusLine.single", 
				strDisplayName);
		}
	}
	
	public String getStatus() {
		return super.getStatus()
			+ "\n" + getShapeStatus();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		isDoubleSided = tag.getBoolean("isDoubleSided");
		setShape(EnumForceFieldShape.get(tag.getByte("shape")));
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setBoolean("isDoubleSided", isDoubleSided);
		tag.setByte("shape", (byte) getShape().ordinal());
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		tagCompound.setBoolean("isPowered", isPowered);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
		isPowered = tagCompound.getBoolean("isPowered");
	}
	
	public ForceFieldSetup getForceFieldSetup() {
		if (forceFieldSetup_cache == null) {
			forceFieldSetup_cache = new ForceFieldSetup(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, tier, beamFrequency);
			setupTicks = Math.max(setupTicks, 10);
		}
		return forceFieldSetup_cache;
	}
	
	@Override
	public int getMaxEnergyStored() {
		return PROJECTOR_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
	
	public EnumForceFieldShape getShape() {
		if (shape == null) {
			return EnumForceFieldShape.NONE;
		}
		return shape;
	}
	
	void setShape(EnumForceFieldShape shape) {
		this.shape = shape;
		isDirty.set(true);
		if (worldObj != null) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			destroyForceField(false);
		}
	}
	
	private class ThreadCalculation extends Thread {
		private final TileEntityForceFieldProjector projector;
		
		ThreadCalculation(TileEntityForceFieldProjector projector) {
			this.projector = projector;
		}
		
		@Override
		public void run() {
			Set<VectorI> vPerimeterBlocks = null;
			Set<VectorI> vInteriorBlocks = null;
			
			// calculation start is done synchronously, by caller
			try {
				if (projector.isValid()) {
					ForceFieldSetup forceFieldSetup = projector.getForceFieldSetup();
					if (WarpDriveConfig.LOGGING_FORCEFIELD) {
						WarpDrive.logger.info("Calculation started for " + projector);
					}
					
					// create HashSets
					VectorI vScale = forceFieldSetup.vMax.clone().subtract(forceFieldSetup.vMin);
					vInteriorBlocks = new HashSet<>(vScale.x * vScale.y * vScale.z);
					vPerimeterBlocks = new HashSet<>(2 * vScale.x * vScale.y + 2 * vScale.x * vScale.z + 2 * vScale.y * vScale.z);
					
					// compute interior fields to remove overlapping parts
					for (Map.Entry<VectorI, Boolean> entry : forceFieldSetup.shapeProvider.getVertexes(forceFieldSetup).entrySet()) {
						VectorI vPosition = entry.getKey();
						if (forceFieldSetup.isDoubleSided || vPosition.y >= yCoord) {
							if ((forceFieldSetup.rotationYaw != 0) || (forceFieldSetup.rotationPitch != 0)) {
								vPosition.rotateByAngle(forceFieldSetup.rotationYaw, forceFieldSetup.rotationPitch);
							}
							
							vPosition.translate(forceFieldSetup.vTranslation);
							
							if (vPosition.y > 0 && vPosition.y <= projector.worldObj.getHeight()) {
								if (entry.getValue()) {
									vPerimeterBlocks.add(vPosition);
								} else {
									vInteriorBlocks.add(vPosition);
								}
							}
						}
					}
					
					// compute forcefield itself
					if (forceFieldSetup.isInverted) {
						// inverted mode => same as interior before fusion => need to be fully cloned
						vPerimeterBlocks = new HashSet<>(vInteriorBlocks);
					}
					
					if (WarpDriveConfig.LOGGING_FORCEFIELD) {
						WarpDrive.logger.info(projector + " Calculation done: "
							+ vInteriorBlocks.size() + " blocks inside, including " + vPerimeterBlocks.size() + " blocks to place");
					}
				} else {
					if (WarpDriveConfig.LOGGING_FORCEFIELD) {
						WarpDrive.logger.info(projector + " Calculation aborted");
					}
				}
			} catch (Exception exception) {
				vInteriorBlocks = null;
				vPerimeterBlocks = null;
				exception.printStackTrace();
				WarpDrive.logger.error(projector + " Calculation failed");
			}
			
			projector.calculation_done(vInteriorBlocks, vPerimeterBlocks);
		}
	}
}
