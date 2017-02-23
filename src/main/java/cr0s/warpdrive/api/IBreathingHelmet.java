package cr0s.warpdrive.api;

import net.minecraft.entity.Entity;

public interface IBreathingHelmet {
	
	boolean canBreath(Entity player);
	
	boolean removeAir(Entity player);
	
	int ticksPerCanDamage();
} 