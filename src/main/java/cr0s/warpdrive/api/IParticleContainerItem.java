package cr0s.warpdrive.api;

import net.minecraft.item.ItemStack;

public interface IParticleContainerItem {
	
	ParticleStack getParticle(ItemStack container);
	
	int getCapacity(ItemStack container);
	
	int fill(ItemStack container, ParticleStack resource, boolean doFill);
	
	ParticleStack drain(ItemStack container, int maxDrain, boolean doDrain);
	
	// called during recipe match to set amount to consume in next call to getContainerItem
	void setAmountToConsume(ItemStack container, int amount);
	
	// called during recipe creation to display 'fake' items in NEI, so our handler takes priority
	// NBT changes aren't supported by default, so you need to change item or damage.
	ItemStack getFakeVariant(ItemStack container);
}