package cr0s.warpdrive.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public abstract class AbstractEntityFX extends EntityFX {
	
	public AbstractEntityFX(final World world, final double x, final double y, final double z,
	                        final double xSpeed, final double ySpeed, final double zSpeed) {
		super(world, x, y, z, xSpeed, ySpeed, zSpeed);
	}
	
	// extend current life
	public void refresh() {
		particleMaxAge = Math.max(particleMaxAge, particleAge + 20);
	}
	
	// get seed
	protected int getSeed() { return getEntityId(); }
}