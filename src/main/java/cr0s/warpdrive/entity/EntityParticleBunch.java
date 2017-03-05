package cr0s.warpdrive.entity;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/*
 * Created by LemADEC on 02/02/2017.
 */
public class EntityParticleBunch extends Entity {
	
	private static final int ACCELERATION_SOUND_UPDATE_TICKS = 10;
	
	private static final double[] ACCELERATOR_COLLISION_ENERGY_TO_PITCH_X = { 0.0D, 10000.0D };
	private static final double[] ACCELERATOR_COLLISION_ENERGY_TO_PITCH_Y = { 0.25D,    2.0D };
	
	// persistent properties
	public double energy = 0.0D;
	public Vector3 vectorNextPosition = new Vector3(0.0D, 0.0D, 0.0D);
	public Vector3 vectorTurningPoint = null;
	
	// computed properties
	private int lastUpdateTicks = 0;
	private static final int UPDATE_TICKS_TIMEOUT = 20;
	private int soundTicks;
	
	public EntityParticleBunch(World world) {
		super(world);
		if (WarpDriveConfig.LOGGING_ACCELERATOR) {
			WarpDrive.logger.info(this + " created in dimension '" + worldObj.getWorldInfo().getWorldName() + "'");
		}
	}
	
	public EntityParticleBunch(World world, final double x, final double y, final double z) {
		super(world);
		this.posX = x + 0.5D;
		this.posY = y + 0.5D;
		this.posZ = z + 0.5D;
		
		if (WarpDriveConfig.LOGGING_ACCELERATOR) {
			WarpDrive.logger.info(this + " created");
		}
	}
	
	// override to skip the block bounding override on client side
	@Override
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int p_70056_9_) {
	//	super.setPositionAndRotation2(x, y, z, yaw, pitch, p_70056_9_);
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}
	
	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}
	
	public void onRefreshFromSimulation(final double newEnergy, Vector3 vectorNewPosition, Vector3 vectorNewTurningPoint) {
		setPosition(vectorNextPosition.x, vectorNextPosition.y, vectorNextPosition.z);
		energy = newEnergy;
		vectorNextPosition = vectorNewPosition;
		vectorTurningPoint = vectorNewTurningPoint;
		lastUpdateTicks = 0;
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
			final float pitch = (float) Commons.interpolate(ACCELERATOR_COLLISION_ENERGY_TO_PITCH_X, ACCELERATOR_COLLISION_ENERGY_TO_PITCH_Y, energy);
			
			soundTicks = (int) Math.floor(ACCELERATION_SOUND_UPDATE_TICKS * pitch);
			worldObj.playSoundEffect(posX, posY, posZ, "warpdrive:accelerating", 1.0F, pitch);
		}
	}
	
	@Override
	protected void entityInit() {
		// no data watcher
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
	protected void readEntityFromNBT(NBTTagCompound nbtTagCompound) {
		energy = nbtTagCompound.getInteger("energy");
		vectorNextPosition = Vector3.createFromNBT(nbtTagCompound.getCompoundTag("nextPosition"));
		if (nbtTagCompound.hasKey("turningPoint")) {
			vectorTurningPoint = Vector3.createFromNBT(nbtTagCompound.getCompoundTag("turningPoint"));
		}
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTagCompound) {
		nbtTagCompound.setDouble("energy", energy);
		nbtTagCompound.setTag("nextPosition", vectorNextPosition.writeToNBT(new NBTTagCompound()));
		if (vectorTurningPoint != null) {
			nbtTagCompound.setTag("turningPoint", vectorTurningPoint.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound p_70109_1_) {
		super.writeToNBT(p_70109_1_);
	}
	
	// prevent saving entity to chunk
	@Override
	public boolean writeMountToNBT(NBTTagCompound p_98035_1_) {
		return false;
	}
	
	// prevent saving entity to chunk
	@Override
	public boolean writeToNBTOptional(NBTTagCompound p_70039_1_) {
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%s/%d @ \'%s\' %.2f %.2f %.2f",
			getClass().getSimpleName(),
			getEntityId(),
			worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			posX, posY, posZ);
	}
}