package cr0s.warpdrive.damage;

import net.minecraft.util.DamageSource;

public class DamageIrradiation extends DamageSource {

	public DamageIrradiation() {
		super("warpdrive.irradiation");
		setDamageBypassesArmor();
		setDamageIsAbsolute();
	}
	
}
