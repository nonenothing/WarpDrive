package cr0s.warpdrive.entity;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*
 * Created by LemADEC on 02/02/2017.
 */
public class EntityParticleBunch extends Entity {
	
	private static final int ACCELERATION_SOUND_UPDATE_TICKS = 10;
	
	private static final double[] PARTICLE_BUNCH_ENERGY_TO_X       = { 0.1D, 1.0D, 10.0D, 100.0D };
	private static final double[] PARTICLE_BUNCH_ENERGY_TO_SOUND_Y = { 0.0D, 1.0D,  2.0D,   3.0D };
	private static final String[] PARTICLE_BUNCH_SOUNDS = { "warpdrive:accelerating_low", "warpdrive:accelerating_medium", "warpdrive:accelerating_high" };
	
	// persistent properties
	private static final int DATA_WATCHER_ENERGY = 2;   // 0 and 1 already taken on all vanilla entities, max is 31
	public Vector3 vectorNextPosition = new Vector3(0.0D, 0.0D, 0.0D);
	public Vector3 vectorTurningPoint = null;
	
	// computed properties
	private int lastUpdateTicks = 0;
	private static final int UPDATE_TICKS_TIMEOUT = 20;
	private int soundTicks;
	
	public EntityParticleBunch(final World world) {
		super(world);
		if (WarpDriveConfig.LOGGING_ACCELERATOR) {
			WarpDrive.logger.info(this + " created in dimension " + worldObj.provider.getDimensionName());
		}
	}
	
	public EntityParticleBunch(final World world, final double x, final double y, final double z) {
		super(world);
		this.posX = x + 0.5D;
		this.posY = y + 0.5D;
		this.posZ = z + 0.5D;
		
		if (WarpDriveConfig.LOGGING_ACCELERATOR) {
			WarpDrive.logger.info(this + " created");
		}
	}
	
	// override to skip the block bounding override on client side
	@SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotation2(final double x, final double y, final double z, final float yaw, final float pitch, final int p_70056_9_) {
	//	super.setPositionAndRotation2(x, y, z, yaw, pitch, p_70056_9_);
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}
	
	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}
	
	public void onRefreshFromSimulation(final double newEnergy, final Vector3 vectorNewPosition, final Vector3 vectorNewTurningPoint) {
		setPosition(vectorNextPosition.x, vectorNextPosition.y, vectorNextPosition.z);
		setEnergy((float) newEnergy);
		vectorNextPosition = vectorNewPosition;
		vectorTurningPoint = vectorNewTurningPoint;
		lastUpdateTicks = 0;
	}
	
	public float getEnergy() {
		return this.dataWatcher.getWatchableObjectFloat(DATA_WATCHER_ENERGY);
	}
	
	public void setEnergy(final float energy) {
		this.dataWatcher.updateObject(DATA_WATCHER_ENERGY, energy);
	}
	
	@Override
	public void onUpdate() {
		if (worldObj.isRemote) {
			return;
		}
		
		lastUpdateTicks++;
		if (lastUpdateTicks > UPDATE_TICKS_TIMEOUT) {
			setDead();
		}
		
		// apply sound effects
		soundTicks--;
		if (soundTicks < 0) {
			final double factor = Commons.interpolate(PARTICLE_BUNCH_ENERGY_TO_X, PARTICLE_BUNCH_ENERGY_TO_SOUND_Y, getEnergy());
			final int indexSound = (int) Math.floor(factor);
			final String sound = PARTICLE_BUNCH_SOUNDS[ Commons.clamp(0, PARTICLE_BUNCH_SOUNDS.length - 1, indexSound) ];
			final float pitch = 0.6F + 0.4F * (float) (factor - indexSound);
			
			soundTicks = (int) Math.floor(ACCELERATION_SOUND_UPDATE_TICKS * pitch);
			worldObj.playSoundEffect(posX, posY, posZ, sound, 1.0F, pitch);
		}
	}
	
	@Override
	protected void entityInit() {
		dataWatcher.addObject(DATA_WATCHER_ENERGY, 0.0F);
		
		// entity size is used by vanilla to define render distance, so we force to a high value and adjust in render itself
		setSize(2.0F, 2.0F);
		yOffset = 2.0F;
		noClip = true;
		soundTicks = 0;
	}
	
	@Override
	public void setDead() {
		super.setDead();
		if (WarpDriveConfig.LOGGING_ACCELERATOR) {
			WarpDrive.logger.info(this + " dead");
		}
	}
	
	@Override
	public void onChunkLoad() {
		super.onChunkLoad();
		// we're not supposed to reload this entity from a save
		setDead();
	}
	
	@Override
	protected void readEntityFromNBT(final NBTTagCompound tagCompound) {
		// energy = tagCompound.getInteger("energy");
		vectorNextPosition = Vector3.createFromNBT(tagCompound.getCompoundTag("nextPosition"));
		if (tagCompound.hasKey("turningPoint")) {
			vectorTurningPoint = Vector3.createFromNBT(tagCompound.getCompoundTag("turningPoint"));
		}
	}
	
	@Override
	protected void writeEntityToNBT(final NBTTagCompound tagCompound) {
		// tagCompound.setDouble("energy", energy);
		tagCompound.setTag("nextPosition", vectorNextPosition.writeToNBT(new NBTTagCompound()));
		if (vectorTurningPoint != null) {
			tagCompound.setTag("turningPoint", vectorTurningPoint.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
	}
	
	// prevent saving entity to chunk
	@Override
	public boolean writeMountToNBT(final NBTTagCompound tagCompound) {
		return false;
	}
	
	// prevent saving entity to chunk
	@Override
	public boolean writeToNBTOptional(final NBTTagCompound tagCompound) {
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%s/%d @ \'%s\' %.2f %.2f %.2f",
			getClass().getSimpleName(),
			getEntityId(),
			worldObj == null ? "~NULL~" : worldObj.provider.getDimensionName(),
			posX, posY, posZ);
	}
}