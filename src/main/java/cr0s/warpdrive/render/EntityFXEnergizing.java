package cr0s.warpdrive.render;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class EntityFXEnergizing extends AbstractEntityFX {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("warpdrive", "textures/particle/energy_grey.png");
	
	private double radius;
	private double length;
	private final int countSteps;
	private float rotYaw;
	private float rotPitch;
	private float prevYaw;
	private float prevPitch;
	
	public EntityFXEnergizing(final World world, final Vector3 position, final Vector3 target,
	                          final float red, final float green, final float blue,
	                          final int age, final float radius) {
		super(world, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
		setRBGColorF(red, green, blue);
		setSize(0.02F, 0.02F);
		noClip = true;
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		
		this.radius = radius;
		
		final float xd = (float) (posX - target.x);
		final float yd = (float) (posY - target.y);
		final float zd = (float) (posZ - target.z);
		length = new Vector3(this).distanceTo(target);
		final double lengthXZ = MathHelper.sqrt_double(xd * xd + zd * zd);
		rotYaw = (float) (Math.atan2(xd, zd) * 180.0D / Math.PI);
		rotPitch = (float) (Math.atan2(yd, lengthXZ) * 180.0D / Math.PI);
		prevYaw = rotYaw;
		prevPitch = rotPitch;
		particleMaxAge = age;
		
		// kill the particle if it's too far away
		// reduce cylinder resolution when fancy graphic are disabled
		final EntityLivingBase entityRender = Minecraft.getMinecraft().renderViewEntity;
		int visibleDistance = 300;
		
		if (!Minecraft.getMinecraft().gameSettings.fancyGraphics) {
			visibleDistance = 100;
			countSteps = 1;
		} else {
			countSteps = 6;
		}
		
		if (entityRender.getDistance(posX, posY, posZ) > visibleDistance) {
			particleMaxAge = 0;
		}
	}
	
	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		prevYaw = rotYaw;
		prevPitch = rotPitch;
		
		if (particleAge++ >= particleMaxAge) {
			setDead();
		}
	}
	
	@Override
	public void renderParticle(final Tessellator tessellator, final float partialTick,
	                           final float cosYaw, final float cosPitch, final float sinYaw, final float sinSinPitch, final float cosSinPitch) {
		tessellator.draw();
		GL11.glPushMatrix();
		
		final double factorFadeIn = Math.min((particleAge + partialTick) / 20.0F, 1.0F);
		
		// alpha starts at 50%, vanishing to 10% during last ticks
		float alpha = 0.5F;
		if (particleMaxAge - particleAge <= 4) {
			alpha = 0.5F - (4 - (particleMaxAge - particleAge)) * 0.1F;
		} else {
			// add random flickering
			final double timeAlpha = ((getSeed() ^ 0x47C8) & 0xFFFF) + particleAge + partialTick + 0.0167D;
			alpha += Math.pow(Math.sin(timeAlpha * 0.37D) + Math.sin(0.178D + timeAlpha * 0.17D), 2.0D) * 0.05D;
		}
		
		// texture clock is offset to de-synchronize particles
		final double timeTexture = (getSeed() & 0xFFFF) + particleAge + partialTick;
		
		// repeated a pixel column, changing periodically, to animate the texture
		final double uOffset = ((int) Math.floor(timeTexture * 0.5D) % 16) / 16.0D;
		
		// add vertical noise
		final double vOffset = Math.pow(Math.sin(timeTexture * 0.20D), 2.0D) * 0.005D;
		
		
		// bind our texture, repeating on both axis
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		// rendering on both sides
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		// alpha transparency, don't update depth mask
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);
		
		// animated translation
		final float xx = (float)(prevPosX + (posX - prevPosX) * partialTick - interpPosX);
		final float yy = (float)(prevPosY + (posY - prevPosY) * partialTick - interpPosY);
		final float zz = (float)(prevPosZ + (posZ - prevPosZ) * partialTick - interpPosZ);
		GL11.glTranslated(xx, yy, zz);
		
		// animated rotation
		final float rotYaw = prevYaw + (this.rotYaw - prevYaw) * partialTick;
		final float rotPitch = prevPitch + (this.rotPitch - prevPitch) * partialTick;
		final float rotSpin = 0.0F;
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(180.0F + rotYaw, 0.0F, 0.0F, -1.0F);
		GL11.glRotatef(rotPitch, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(rotSpin, 0.0F, 1.0F, 0.0F);
		
		// actual parameters
		final double radius = this.radius * factorFadeIn;
		final double yMin = length * (0.5D - factorFadeIn / 2.0D);
		final double yMax = length * (0.5D + factorFadeIn / 2.0D);
		final double uMin = uOffset;
		final double uMax = uMin + 1.0D / 32.0D;
		
		final double vMin = -1.0D + vOffset;
		final double vMax = vMin + length * factorFadeIn;
		
		// start drawing
		tessellator.startDrawingQuads();
		tessellator.setBrightness(200);
		tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, alpha);
		
		// loop covering 45 deg, using symmetry to cover a full circle
		final double angleMax = Math.PI / 4.0D;
		final double angleStep = angleMax / countSteps;
		double angle = 0.0D;
		double cosPrev = radius * Math.cos(angle);
		double sinPrev = radius * Math.sin(angle);
		for (int indexStep = 1; indexStep <= countSteps; indexStep++) {
			angle += angleStep;
			final double cosNext = radius * Math.cos(angle);
			final double sinNext = radius * Math.sin(angle);
			
			// cos sin
			tessellator.addVertexWithUV( cosPrev, yMax,  sinPrev, uMax, vMax);
			tessellator.addVertexWithUV( cosPrev, yMin,  sinPrev, uMax, vMin);
			tessellator.addVertexWithUV( cosNext, yMin,  sinNext, uMin, vMin);
			tessellator.addVertexWithUV( cosNext, yMax,  sinNext, uMin, vMax);
			
			tessellator.addVertexWithUV(-cosPrev, yMax,  sinPrev, uMax, vMax);
			tessellator.addVertexWithUV(-cosPrev, yMin,  sinPrev, uMax, vMin);
			tessellator.addVertexWithUV(-cosNext, yMin,  sinNext, uMin, vMin);
			tessellator.addVertexWithUV(-cosNext, yMax,  sinNext, uMin, vMax);
			
			tessellator.addVertexWithUV( cosPrev, yMax, -sinPrev, uMax, vMax);
			tessellator.addVertexWithUV( cosPrev, yMin, -sinPrev, uMax, vMin);
			tessellator.addVertexWithUV( cosNext, yMin, -sinNext, uMin, vMin);
			tessellator.addVertexWithUV( cosNext, yMax, -sinNext, uMin, vMax);
			
			tessellator.addVertexWithUV(-cosPrev, yMax, -sinPrev, uMax, vMax);
			tessellator.addVertexWithUV(-cosPrev, yMin, -sinPrev, uMax, vMin);
			tessellator.addVertexWithUV(-cosNext, yMin, -sinNext, uMin, vMin);
			tessellator.addVertexWithUV(-cosNext, yMax, -sinNext, uMin, vMax);
			
			// sin cos
			tessellator.addVertexWithUV( sinPrev, yMax,  cosPrev, uMax, vMax);
			tessellator.addVertexWithUV( sinPrev, yMin,  cosPrev, uMax, vMin);
			tessellator.addVertexWithUV( sinNext, yMin,  cosNext, uMin, vMin);
			tessellator.addVertexWithUV( sinNext, yMax,  cosNext, uMin, vMax);
			
			tessellator.addVertexWithUV(-sinPrev, yMax,  cosPrev, uMax, vMax);
			tessellator.addVertexWithUV(-sinPrev, yMin,  cosPrev, uMax, vMin);
			tessellator.addVertexWithUV(-sinNext, yMin,  cosNext, uMin, vMin);
			tessellator.addVertexWithUV(-sinNext, yMax,  cosNext, uMin, vMax);
			
			tessellator.addVertexWithUV( sinPrev, yMax, -cosPrev, uMax, vMax);
			tessellator.addVertexWithUV( sinPrev, yMin, -cosPrev, uMax, vMin);
			tessellator.addVertexWithUV( sinNext, yMin, -cosNext, uMin, vMin);
			tessellator.addVertexWithUV( sinNext, yMax, -cosNext, uMin, vMax);
			
			tessellator.addVertexWithUV(-sinPrev, yMax, -cosPrev, uMax, vMax);
			tessellator.addVertexWithUV(-sinPrev, yMin, -cosPrev, uMax, vMin);
			tessellator.addVertexWithUV(-sinNext, yMin, -cosNext, uMin, vMin);
			tessellator.addVertexWithUV(-sinNext, yMax, -cosNext, uMin, vMax);
			
			cosPrev = cosNext;
			sinPrev = sinNext;
		}
		
		// draw
		tessellator.draw();
		
		// restore OpenGL state
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		tessellator.startDrawingQuads();
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("textures/particle/particles.png"));
	}
}