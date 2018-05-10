package cr0s.warpdrive.render;

import cr0s.warpdrive.data.Vector3;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFXDot extends AbstractEntityFX {
	
	private Vector3 v3Acceleration;
	private double friction;
	private int layer = 0;  // 0 = particles, 1 = blocks, 2 = items
	
	public EntityFXDot(final World world, final Vector3 v3Position,
	                   final Vector3 v3Motion, final Vector3 v3Acceleration, final double friction,
	                   final int age) {
		super(world, v3Position.x, v3Position.y, v3Position.z, 0.0D, 0.0D, 0.0D);
		this.setSize(0.02F, 0.02F);
		this.noClip = true;
		this.motionX = v3Motion.x;
		this.motionY = v3Motion.y;
		this.motionZ = v3Motion.z;
		this.v3Acceleration = v3Acceleration;
		this.friction = friction;
		this.particleMaxAge = age;
		
		// defaults to vanilla water drip
		setParticleTextureIndex(113);
		
		// refresh bounding box
		setPosition(v3Position.x, v3Position.y, v3Position.z);
	}
	
	public void setParticleFromBlockIcon(final IIcon icon) {
		layer = 1;
		setParticleIcon(icon);
	}
	
	public void setParticleFromItemIcon(final IIcon icon) {
		layer = 2;
		setParticleIcon(icon);
	}
	
	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		
		if (particleAge++ >= particleMaxAge) {
			setDead();
		}
		
		moveEntity(motionX, motionY, motionZ);
		motionX = (motionX + v3Acceleration.x) * friction;
		motionY = (motionY + v3Acceleration.y) * friction;
		motionZ = (motionZ + v3Acceleration.z) * friction;
	}
	
	@Override
	public int getFXLayer() {
		return layer;
	}
	
	@Override
	public int getBrightnessForRender(final float p_70070_1_) {
		return 0xF00000;
	}
	
	@Override
	public void renderParticle(final Tessellator tessellator, final float partialTick,
	                           final float cosYaw, final float cosPitch, final float sinYaw, final float sinSinPitch, final float cosSinPitch) {
		double minU = particleTextureIndexX / 16.0F;
		double maxU = minU + 0.0624375F;
		double minV = particleTextureIndexY / 16.0F;
		double maxV = minV + 0.0624375F;
		final float scale = 0.1F * particleScale;
		
		if (particleIcon != null) {
			minU = particleIcon.getMinU();
			maxU = particleIcon.getMaxU();
			minV = particleIcon.getMinV();
			maxV = particleIcon.getMaxV();
		}
		
		final double x = prevPosX + (posX - prevPosX) * partialTick - interpPosX;
		final double y = prevPosY + (posY - prevPosY) * partialTick - interpPosY;
		final double z = prevPosZ + (posZ - prevPosZ) * partialTick - interpPosZ;
		
		// alpha increase during first tick and decays during last 2 ticks
		float alpha = particleAlpha;
		final int ageLeft = particleMaxAge - particleAge;
		if (particleAge < 1) {
			alpha = particleAlpha * partialTick;
		} else if (ageLeft < 2) {
			if (ageLeft < 1) {
				alpha = particleAlpha * (0.5F - partialTick / 2.0F);
			} else {
				alpha = particleAlpha * (1.0F - partialTick / 2.0F);
			}
		}
		
		tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, alpha);
		tessellator.addVertexWithUV(x - cosYaw * scale - sinSinPitch * scale, y - cosPitch * scale, z - sinYaw * scale - cosSinPitch * scale, maxU, maxV);
		tessellator.addVertexWithUV(x - cosYaw * scale + sinSinPitch * scale, y + cosPitch * scale, z - sinYaw * scale + cosSinPitch * scale, maxU, minV);
		tessellator.addVertexWithUV(x + cosYaw * scale + sinSinPitch * scale, y + cosPitch * scale, z + sinYaw * scale + cosSinPitch * scale, minU, minV);
		tessellator.addVertexWithUV(x + cosYaw * scale - sinSinPitch * scale, y - cosPitch * scale, z + sinYaw * scale - cosSinPitch * scale, minU, maxV);
	}
}