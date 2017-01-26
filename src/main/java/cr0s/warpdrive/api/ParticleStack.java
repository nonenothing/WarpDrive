package cr0s.warpdrive.api;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public class ParticleStack {
	public final Particle particle;
	public int amount;
	public NBTTagCompound tag;
	
	public ParticleStack(@Nonnull final Particle particle, final int amount) {
		if (!ParticleRegistry.isParticleRegistered(particle)) {
			FMLLog.bigWarning("Failed attempt to create a particleStack for an unregistered Particle %s (type %s)", particle.getRegistryName(), particle.getClass().getName());
			throw new IllegalArgumentException("Cannot create a particleStack from an unregistered particle");
		}
		this.amount = amount;
		this.particle = particle;
	}
	
	public ParticleStack(final Particle particle, final int amount, final NBTTagCompound nbt) {
		this(particle, amount);
		
		if (nbt != null) {
			tag = (NBTTagCompound) nbt.copy();
		}
	}
	
	public ParticleStack(ParticleStack stack, int amount) {
		this(stack.getParticle(), amount, stack.tag);
	}
	
	/**
	 * Return null if stack is invalid.
	 */
	public static ParticleStack loadFromNBT(NBTTagCompound nbt) {
		if (nbt == null) {
			return null;
		}
		String particleName = nbt.getString("name");
		
		if (particleName == null || ParticleRegistry.getParticle(particleName) == null) {
			return null;
		}
		ParticleStack stack = new ParticleStack(ParticleRegistry.getParticle(particleName), nbt.getInteger("amount"));
		
		if (nbt.hasKey("tag")) {
			stack.tag = nbt.getCompoundTag("tag");
		}
		return stack;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setString("name", ParticleRegistry.getParticleName(getParticle()));
		nbt.setInteger("amount", amount);
		
		if (tag != null) {
			nbt.setTag("tag", tag);
		}
		return nbt;
	}
	
	public final Particle getParticle() {
		return particle;
	}
	
	public String getLocalizedName() {
		return this.getParticle().getLocalizedName();
	}
	
	public String getUnlocalizedName() {
		return this.getParticle().getUnlocalizedName();
	}
	
	public ParticleStack copy()
	{
		return new ParticleStack(getParticle(), amount, tag);
	}
	
	public boolean isParticleEqual(ParticleStack other) {
		return other != null && getParticle() == other.getParticle() && isParticleStackTagEqual(other);
	}
	
	private boolean isParticleStackTagEqual(ParticleStack other) {
		return tag == null ? other.tag == null : other.tag != null && tag.equals(other.tag);
	}
	
	public static boolean areParticleStackTagsEqual(ParticleStack stack1, ParticleStack stack2) {
		return stack1 == null && stack2 == null || (!(stack1 == null || stack2 == null) && stack1.isParticleStackTagEqual(stack2));
	}
	
	public boolean containsParticle(ParticleStack other) {
		return isParticleEqual(other) && amount >= other.amount;
	}
	
	public boolean isParticleStackIdentical(ParticleStack other) {
		return isParticleEqual(other) && amount == other.amount;
	}
	
	public boolean isParticleEqual(ItemStack other) {
		if (other == null) {
			return false;
		}
		
		if (other.getItem() instanceof IParticleContainerItem) {
			return isParticleEqual(((IParticleContainerItem) other.getItem()).getParticle(other));
		}
		
		return false;
	}
	
	@Override
	public final int hashCode() {
		int code = 1;
		code = 31 * code + getParticle().hashCode();
		code = 31 * code + amount;
		if (tag != null) {
			code = 31 * code + tag.hashCode();
		}
		return code;
	}
	
	@Override
	public final boolean equals(Object object) {
		return object instanceof ParticleStack && isParticleEqual((ParticleStack) object);
	}
}
