package cr0s.warpdrive.api;

import net.minecraft.item.ItemStack;

public interface IParticleContainerItem {
	
	ParticleStack getParticleStack(ItemStack container);
	
	int getCapacity(ItemStack container);
	
	boolean isEmpty(ItemStack container);
	
	// fills the container and return how much could be transferred or 0 if container is empty or contains different particles
	int fill(ItemStack container, ParticleStack resource, boolean doFill);
	
	// drains the container and return how much could be transferred or null if container is empty or contains different particles
	ParticleStack drain(ItemStack container, ParticleStack resource, boolean doDrain);
	
	// called during recipe match to set amount to consume in next call to getContainerItem
	void setAmountToConsume(ItemStack container, int amount);
}