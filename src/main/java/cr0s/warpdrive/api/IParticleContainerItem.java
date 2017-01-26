package cr0s.warpdrive.api;

import net.minecraft.item.ItemStack;

public interface IParticleContainerItem {
	ParticleStack getParticle(ItemStack container);
	
	int getCapacity(ItemStack container);
	
	int fill(ItemStack container, ParticleStack resource, boolean doFill);
	
	ParticleStack drain(ItemStack container, int maxDrain, boolean doDrain);
}
	
	
	