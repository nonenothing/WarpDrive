package cr0s.warpdrive.entity;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.SoundEvents;
import cr0s.warpdrive.data.Vector3;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Created by LemADEC on 02/02/2017.
 */
public class EntityParticleBunch extends Entity {
	
	private static final int ACCELERATION_SOUND_UPDATE_TICKS = 10;
	
	private static final double[] PARTICLE_BUNCH_ENERGY_TO_X       = { 0.1D, 1.0D, 10.0D, 100.0D };
	private static final double[] PARTICLE_BUNCH_ENERGY_TO_SOUND_Y = { 0.0D, 1.0D,  2.0D,   3.0D };
	private static final SoundEvent[] PARTICLE_BUNCH_SOUNDS = { SoundEvents.ACCELERATING_LOW, SoundEvents.ACCELERATING_MEDIUM, SoundEvents.ACCELERATING_HIGH };
	
	// persistent properties
	private static final DataParameter<Float> DATA_PARAMETER_ENERGY = EntityDataManager.createKey(EntityParticleBunch.class, DataSerializers.FLOAT);
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
	@SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
	//	super.setPositionAndRotation(x, y, z, yaw, pitch);
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}
	
	@Override
	public boolean isEntityInvulnerable(@Nonnull DamageSource source) {
		return true;
	}
	
	public void onRefreshFromSimulation(final double newEnergy, Vector3 vectorNewPosition, Vector3 vectorNewTurningPoint) {
		setPosition(vectorNextPosition.x, vectorNextPosition.y, vectorNextPosition.z);
		setEnergy((float) newEnergy);
		vectorNextPosition = vectorNewPosition;
		vectorTurningPoint = vectorNewTurningPoint;
		lastUpdateTicks = 0;
	}
	
	public float getEnergy() {
		return this.dataManager.get(DATA_PARAMETER_ENERGY);
	}
	
	public void setEnergy(final float energy) {
		this.dataManager.set(DATA_PARAMETER_ENERGY, energy);
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
			final SoundEvent soundEvent = PARTICLE_BUNCH_SOUNDS[ Commons.clamp(0, PARTICLE_BUNCH_SOUNDS.length, indexSound) ];
			final float pitch = 0.6F + 0.4F * (float) (factor - indexSound);
			
			soundTicks = (int) Math.floor(ACCELERATION_SOUND_UPDATE_TICKS * pitch);
			worldObj.playSound((EntityPlayer) null, posX, posY, posZ, soundEvent, SoundCategory.HOSTILE, 1.0F, pitch);
		}
	}
	
	@Override
	protected void entityInit() {
		dataManager.register(DATA_PARAMETER_ENERGY, 0.0F);
		
		// entity size is used by vanilla to define render distance, so we force to a high value and adjust in render itself
		setSize(2.0F, 2.0F);
		noClip = true;
		soundTicks = 0;
	}
	
	@Override
	public float getEyeHeight() {
		return 2.0F;
	}
	
	@Override
	public void setDead() {
		super.setDead();
		if (WarpDriveConfig.LOGGING_ACCELERATOR) {
			WarpDrive.logger.info(this + " dead");
		}
	}
	
	// @TODO MC1.10
	/*
	@Override
	public void onChunkLoad() {
		super.onChunkLoad();
		// we're not supposed to reload this entity from a save
		setDead();
	}
	/**/
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTagCompound) {
		// energy = nbtTagCompound.getInteger("energy");
		vectorNextPosition = Vector3.createFromNBT(nbtTagCompound.getCompoundTag("nextPosition"));
		if (nbtTagCompound.hasKey("turningPoint")) {
			vectorTurningPoint = Vector3.createFromNBT(nbtTagCompound.getCompoundTag("turningPoint"));
		}
	}
	
	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound nbtTagCompound) {
		// nbtTagCompound.setDouble("energy", energy);
		nbtTagCompound.setTag("nextPosition", vectorNextPosition.writeToNBT(new NBTTagCompound()));
		if (vectorTurningPoint != null) {
			nbtTagCompound.setTag("turningPoint", vectorTurningPoint.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public boolean writeToNBTAtomically(@Nonnull NBTTagCompound compound) {
		return super.writeToNBTAtomically(compound);
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return super.writeToNBT(compound);
	}
	
	// prevent saving entity to chunk
	// @TODO MC1.10 check boat entity
	/*
	@Override
	public boolean writeMountToNBT(NBTTagCompound p_98035_1_) {
		return false;
	}
	/**/
	
	// prevent saving entity to chunk
	@Override
	public boolean writeToNBTOptional(@Nonnull NBTTagCompound p_70039_1_) {
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