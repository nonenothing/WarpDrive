package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IForceFieldShape;
import cr0s.warpdrive.config.*;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.data.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStaticLiquid;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TileEntityForceFieldProjector extends TileEntityAbstractForceField {
	private static final int PROJECTOR_MAX_ENERGY_STORED = 10000;
	private static final int PROJECTOR_COOLDOWN_TICKS = 20;
	public static final int PROJECTOR_PROJECTION_UPDATE_TICKS = 8;
	private static final int PROJECTOR_SETUP_TICKS = 20;
	private static final int PROJECTOR_SOUND_UPDATE_TICKS = 100;
	private int maxEnergyStored;
	
	// persistent properties
	public boolean isDoubleSided;
	private EnumForceFieldShape shape;
	
	// computed properties
	private int cooldownTicks;
	private int setupTicks;
	private int updateTicks;
	private int soundTicks;
	protected boolean isPowered = true;
	private ForceFieldSetup cache_forceFieldSetup;
	private ForceFieldSetup legacy_forceFieldSetup;
	private boolean legacy_isOn = true;     // we assume it's on so we don't consume startup energy on chunk loading
	
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
		
		for (EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			if (enumForceFieldUpgrade.maxCountOnProjector > 0) {
				setUpgradeMaxCount(enumForceFieldUpgrade, enumForceFieldUpgrade.maxCountOnProjector);
			}
		}
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		maxEnergyStored = PROJECTOR_MAX_ENERGY_STORED * (1 + 2 * tier);
		cooldownTicks = worldObj.rand.nextInt(PROJECTOR_COOLDOWN_TICKS);
		setupTicks = worldObj.rand.nextInt(PROJECTOR_SETUP_TICKS);
		updateTicks = worldObj.rand.nextInt(PROJECTOR_PROJECTION_UPDATE_TICKS);
		getForceFieldSetup();
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		// Frequency is not set
		if (!isConnected) {
			return;
		}
		
		// clear setup cache periodically
		setupTicks--;
		if (setupTicks <= 0) {
			setupTicks = PROJECTOR_SETUP_TICKS;
			if (cache_forceFieldSetup != null) {
				legacy_forceFieldSetup = cache_forceFieldSetup;
				cache_forceFieldSetup = null;
			}
		}
		
		// Powered ?
		ForceFieldSetup forceFieldSetup = getForceFieldSetup();
		int energyRequired;
		if (!legacy_isOn) {
			energyRequired = forceFieldSetup.startupEnergyCost;
			energyRequired += Math.round(forceFieldSetup.placeEnergyCost * forceFieldSetup.placeSpeed * PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F);
		} else {
			energyRequired = Math.round(forceFieldSetup.scanEnergyCost * forceFieldSetup.scanSpeed * PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F);
		}
		if (energyRequired > getMaxEnergyStored()) {
			WarpDrive.logger.error("Force field projector requires " + energyRequired + " to get started but can only store " + getMaxEnergyStored());
		}
		isPowered = getEnergyStored() >= energyRequired;
		
		boolean isEnabledAndValid = isEnabled && isValid();
		boolean isOn = isEnabledAndValid && cooldownTicks <= 0 && isPowered;
		if (isOn) {
			if (!legacy_isOn) {
				consumeEnergy(forceFieldSetup.startupEnergyCost, false);
				legacy_isOn = true;
			}
			cooldownTicks = 0;
			
			updateTicks--;
			if (updateTicks <= 0) {
				updateTicks = PROJECTOR_PROJECTION_UPDATE_TICKS;
				if (!isCalculated()) {
					calculateForceField();
				} else {
					projectForceField();
				}
			}
			
			// TODO add some animation
			soundTicks--;
			if (soundTicks <= 0) {
				soundTicks = PROJECTOR_SOUND_UPDATE_TICKS;
				if (!hasUpgrade(EnumForceFieldUpgrade.SILENCER)) {
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "warpdrive:projecting", 1.0F, 0.85F + 0.15F * worldObj.rand.nextFloat());
				}
			}
			
		} else {
			legacy_isOn = false;
			destroyForceField(false);
			if (cooldownTicks > 0) {
				cooldownTicks--;
			} else if (isEnabledAndValid) {
				cooldownTicks = PROJECTOR_COOLDOWN_TICKS;
				String msg = StatCollector.translateToLocalFormatted("warpdrive.guide.prefix", getBlockType().getLocalizedName()) 
				           + StatCollector.translateToLocalFormatted("warpdrive.forcefield.guide.lowPower");
				
				AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 10, yCoord - 10, zCoord - 10, xCoord + 10, yCoord + 10, zCoord + 10);
				List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
				
				for (Entity entity : list) {
					if (entity == null || (!(entity instanceof EntityPlayer)) || entity instanceof FakePlayer) {
						continue;
					}
					
					WarpDrive.addChatMessage((EntityPlayer) entity, msg);
				}
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		destroyForceField(true);
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
				if (WarpDriveConfig.LOGGING_FORCEFIELD) {
					WarpDrive.logger.info("Calculation initiated for " + this);
				}
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
		if (WarpDriveConfig.LOGGING_FORCEFIELD) {
			WarpDrive.logger.info("Calculation done for " + this);
		}
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
		assert(!worldObj.isRemote && isCalculated());
		
		ForceFieldSetup forceFieldSetup = getForceFieldSetup();
		
		// compute maximum number of blocks to scan
		int countScanned = 0;
		float floatScanSpeed = Math.min(forceFieldSetup.scanSpeed * PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F + carryScanSpeed, calculated_forceField.size());
		int countMaxScanned = (int)Math.floor(floatScanSpeed);
		carryScanSpeed = floatScanSpeed - countMaxScanned;
		
		// compute maximum number of blocks to place
		int countPlaced = 0;
		float floatPlaceSpeed = Math.min(forceFieldSetup.placeSpeed * PROJECTOR_PROJECTION_UPDATE_TICKS / 20.0F + carryPlaceSpeed, calculated_forceField.size());
		int countMaxPlaced = (int)Math.floor(floatPlaceSpeed);
		carryPlaceSpeed = floatPlaceSpeed - countMaxPlaced;
		
		// evaluate force field block metadata
		int metadataForceField = Math.min(15, (beamFrequency * 16) / IBeamFrequency.BEAM_FREQUENCY_MAX);
		if (forceFieldSetup.getCamouflageBlock() != null) {
			metadataForceField = forceFieldSetup.getCamouflageMetadata();
		}
		
		VectorI vector;
		Block block;
		boolean doProjectThisBlock;
		
		while (countScanned < countMaxScanned && countPlaced < countMaxPlaced
			 && consumeEnergy(Math.max(forceFieldSetup.scanEnergyCost, forceFieldSetup.placeEnergyCost), true)) {
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
				for (TileEntityForceFieldProjector projector : forceFieldSetup.projectors) {
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
					
				} else if (block instanceof BlockLiquid) {
					Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
					doProjectThisBlock = fluid == null || forceFieldSetup.pumping_maxViscosity >= fluid.getViscosity();
					
				} else if (forceFieldSetup.breaking_maxHardness > 0) {
					float blockHardness = block.getBlockHardness(worldObj, vector.x, vector.y, vector.z);
					// stops on unbreakable or too hard
					if (blockHardness == -1.0F || blockHardness > forceFieldSetup.breaking_maxHardness) {
						doProjectThisBlock = false;
					}
					
				} else {// doesn't have disintegration, not a liquid
					
					// recover force field blocks
					if (block instanceof BlockForceField) {
						// remove block if its missing a valid tile entity
						TileEntity tileEntity = vector.getTileEntity(worldObj);
						if (!(tileEntity instanceof TileEntityForceField)) {
							worldObj.setBlockToAir(vector.x, vector.y, vector.z);
							block = Blocks.air;
							
						} else {
							TileEntityForceField tileEntityForceField = ((TileEntityForceField)tileEntity);
							TileEntityForceFieldProjector tileEntityForceFieldProjector = tileEntityForceField.getProjector();
							if (tileEntityForceFieldProjector == null) {
								// orphan force field, probably from an explosion => recover it
								tileEntityForceField.setProjector(new VectorI(this));
								tileEntityForceField.cache_blockCamouflage = forceFieldSetup.getCamouflageBlock();
								tileEntityForceField.cache_metadataCamouflage = forceFieldSetup.getCamouflageMetadata();
								worldObj.setBlockMetadataWithNotify(vector.x, vector.y, vector.z, tileEntityForceField.cache_metadataCamouflage, 2);
								
							} else if ( tileEntityForceFieldProjector == this 
							         && ( tileEntityForceField.cache_blockCamouflage != forceFieldSetup.getCamouflageBlock()
								       || tileEntityForceField.cache_metadataCamouflage != forceFieldSetup.getCamouflageMetadata() ) ) {
								// camouflage changed while chunk was loaded or de-synchronisation => force a new placement
								worldObj.setBlockToAir(vector.x, vector.y, vector.z);
								block = Blocks.air;
							}
						}
					}
					
					doProjectThisBlock = block.isReplaceable(worldObj, vector.x, vector.y, vector.z) || (block == WarpDrive.blockForceFields[tier - 1]);
				}
			}
			
			// skip if area is protected
			if (doProjectThisBlock) {
				if (forceFieldSetup.breaking_maxHardness > 0) {
					doProjectThisBlock = ! isBlockBreakCanceled(null, worldObj, vector.x, vector.y, vector.z);
				} else {
					doProjectThisBlock = ! isBlockPlaceCanceled(null, worldObj, vector.x, vector.y, vector.z, WarpDrive.blockForceFields[tier - 1], metadataForceField);
				}
			}
			
			if (doProjectThisBlock) {
				if ((block != WarpDrive.blockForceFields[tier - 1]) && (!vector.equals(this))) {
					boolean hasPlaced = false;
					if (block instanceof BlockLiquid) {
						hasPlaced = true;
						if (block instanceof BlockStaticLiquid) {// it's a source block
							// TODO collect fluid
						}
						
						// TODO add fluid repealing block, temporary work around follows
						if (forceFieldSetup.isInverted || forceFieldSetup.breaking_maxHardness > 0) {
							worldObj.setBlockToAir(vector.x, vector.y, vector.z);
						} else {
							worldObj.setBlock(vector.x, vector.y, vector.z, WarpDrive.blockForceFields[tier - 1], metadataForceField, 2);
							
							TileEntity tileEntity = worldObj.getTileEntity(vector.x, vector.y, vector.z);
							if (tileEntity instanceof TileEntityForceField) {
								((TileEntityForceField) tileEntity).setProjector(new VectorI(this));
							}
							
							vForceFields.add(vector);
						}
						
					} else if (forceFieldSetup.breaking_maxHardness > 0) {
						// TODO break the block
						// if (forceFieldSetup.attractionLevel > 10.0F) {
							// TODO store result in chest
						// } else {
							// TODO drop
						// }
						
					} else if (forceFieldSetup.hasStabilize) {
						// TODO collect from chest
						// TODO place block (ItemBlock.place?)
						
					} else if (forceFieldSetup.isInverted && (forceFieldSetup.temperatureLevel < 295.0F || forceFieldSetup.temperatureLevel > 305.0F)) {
						if (forceFieldSetup.temperatureLevel > 300.0F) {
							
						} else {
							
						}
						// TODO glass <> sandstone <> sand <> gravel <> cobblestone <> stone <> obsidian
						// TODO ice <> snow <> water <> air > fire
						// TODO obsidian < lava
						
					} else {
						hasPlaced = true;
						worldObj.setBlock(vector.x, vector.y, vector.z, WarpDrive.blockForceFields[tier - 1], metadataForceField, 2);
						
						TileEntity tileEntity = worldObj.getTileEntity(vector.x, vector.y, vector.z);
						if (tileEntity instanceof TileEntityForceField) {
							((TileEntityForceField) tileEntity).setProjector(new VectorI(this));
						}
						
						vForceFields.add(vector);
					}
					if (hasPlaced) {
						countPlaced++;
						consumeEnergy(forceFieldSetup.placeEnergyCost, false);
					} else {
						consumeEnergy(forceFieldSetup.scanEnergyCost, false);
					}
					
				} else {
					// scanning a valid position
					consumeEnergy(forceFieldSetup.scanEnergyCost, false);
					
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
				consumeEnergy(forceFieldSetup.scanEnergyCost, false);
				
				// remove our own force field block
				if (block == WarpDrive.blockForceFields[tier - 1]) {
					assert(block instanceof BlockForceField);
					if (((BlockForceField) block).getProjector(worldObj, vector.x, vector.y, vector.z) == this) {
						worldObj.setBlockToAir(vector.x, vector.y, vector.z);
						vForceFields.remove(vector);
					}
				}
			}
		}
	}
	
	private void destroyForceField(boolean isChunkLoading) {
		if (worldObj.isRemote) {
			return;
		}
		
		legacy_isOn = false;
		if (!vForceFields.isEmpty()) {
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
		
		if (isCalculated() && isChunkLoading) {
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
		cache_forceFieldSetup = null;
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
	
	private String getUpgradeStatus() {
		String strUpgrades = getUpgradesAsString();
		if (strUpgrades.isEmpty()) {
			return StatCollector.translateToLocalFormatted("warpdrive.forcefield.upgrade.statusLine.none",
				strUpgrades);
		} else {
			return StatCollector.translateToLocalFormatted("warpdrive.forcefield.upgrade.statusLine.valid",
				strUpgrades);
		}
	}
	
	public String getStatus() {
		return super.getStatus()
			+ "\n" + getShapeStatus()
			+ "\n" + getUpgradeStatus();
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
		if (cache_forceFieldSetup == null) {
			cache_forceFieldSetup = new ForceFieldSetup(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, tier, beamFrequency);
			setupTicks = Math.max(setupTicks, 10);
			
			// reset field in case of major changes
			if (legacy_forceFieldSetup != null) {
				int energyRequired = cache_forceFieldSetup.startupEnergyCost - legacy_forceFieldSetup.startupEnergyCost;
				if (legacy_forceFieldSetup.getCamouflageBlock() != cache_forceFieldSetup.getCamouflageBlock()
				  || legacy_forceFieldSetup.getCamouflageMetadata() != cache_forceFieldSetup.getCamouflageMetadata()
				  || legacy_forceFieldSetup.beamFrequency != cache_forceFieldSetup.beamFrequency
				  || !consumeEnergy(energyRequired, false)) {
					destroyForceField(true);
					
				} else if (legacy_forceFieldSetup.isInverted != cache_forceFieldSetup.isInverted
				         || legacy_forceFieldSetup.shapeProvider != cache_forceFieldSetup.shapeProvider
				         || legacy_forceFieldSetup.thickness != cache_forceFieldSetup.thickness) {
					destroyForceField(true);
					isDirty.set(true);
				}
			}
		}
		return cache_forceFieldSetup;
	}
	
	@Override
	public int getMaxEnergyStored() {
		return maxEnergyStored;
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
		cache_forceFieldSetup = null;
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
						WarpDrive.logger.debug(this + " Calculation started for " + projector);
					}
					
					// create HashSets
					VectorI vScale = forceFieldSetup.vMax.clone().subtract(forceFieldSetup.vMin);
					vInteriorBlocks = new HashSet<>(vScale.x * vScale.y * vScale.z);
					vPerimeterBlocks = new HashSet<>(2 * vScale.x * vScale.y + 2 * vScale.x * vScale.z + 2 * vScale.y * vScale.z);
					
					// compute interior fields to remove overlapping parts
					for (Map.Entry<VectorI, Boolean> entry : forceFieldSetup.shapeProvider.getVertexes(forceFieldSetup).entrySet()) {
						VectorI vPosition = entry.getKey();
						if (forceFieldSetup.isDoubleSided || vPosition.y >= 0) {
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
						WarpDrive.logger.debug(this + " Calculation done: "
							+ vInteriorBlocks.size() + " blocks inside, including " + vPerimeterBlocks.size() + " blocks to place");
					}
				} else {
					if (WarpDriveConfig.LOGGING_FORCEFIELD) {
						WarpDrive.logger.error(this + " Calculation aborted");
					}
				}
			} catch (Exception exception) {
				vInteriorBlocks = null;
				vPerimeterBlocks = null;
				exception.printStackTrace();
				WarpDrive.logger.error(this + " Calculation failed");
			}
			
			projector.calculation_done(vInteriorBlocks, vPerimeterBlocks);
		}
	}
}
