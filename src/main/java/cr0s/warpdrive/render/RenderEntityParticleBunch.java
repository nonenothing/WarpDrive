package cr0s.warpdrive.render;

import cr0s.warpdrive.entity.EntityParticleBunch;
import org.lwjgl.opengl.GL11;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityParticleBunch extends RenderEntity {
	
	@Override
	public void doRender(Entity entity, double x, double y, double z, float rotation, float partialTick) {
		if (entity instanceof EntityParticleBunch) {
			doRender((EntityParticleBunch) entity, x, y, z, rotation, partialTick);
		}
	}
	
	@Override
	public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float rotation, float partialTick) {
		// super.doRenderShadowAndFire(entity, x, y, z, rotation, partialTick);
	}
	
	public void doRender(EntityParticleBunch entityParticleBunch, double x, double y, double z, float rotation, float partialTick) {
		// adjust render distance
		final int maxRenderDistanceSquared;
		if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
			maxRenderDistanceSquared = 128 * 128;
		} else {
			maxRenderDistanceSquared = 20 * 20;
		}
		if ((x * x + y * y + z * z) > maxRenderDistanceSquared) {
			return;
		}
		
		// translate
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		
		// compute parameters
		final float energy = (float) entityParticleBunch.energy;
		final float size = Math.min(Math.max(energy / 50000F, 0.01F), 0.07F);
		final int rayCount_base = 45; 
        
        renderStar(entityParticleBunch.ticksExisted + partialTick, entityParticleBunch.getEntityId(), rayCount_base,
            1.0F, 0.2F, 0.5F, 0.5F, 1.0F, 0.2F, size, size, size);
        
        // restore
		GL11.glPopMatrix();
	}
	
	// Loosely based on ender dragon death effect
	private static void renderStar(final float ticksExisted, final long seed, final int rayCount_base,
						   final float redIn, final float greenIn, final float blueIn,
						   final float redOut, final float greenOut, final float blueOut,
						   final float scaleX, final float scaleY, final float scaleZ) {
		Random random = new Random(seed);
		
		// compute rotation cycle
		final int tickRotationPeriod = 220 + 2 * random.nextInt(30);
		int tickRotation = (int) (ticksExisted % tickRotationPeriod);
		if (tickRotation >= tickRotationPeriod / 2) {
			tickRotation = tickRotationPeriod - tickRotation - 1;
		}
		final float cycleRotation = 2 * tickRotation / (float) tickRotationPeriod;
	    
		// compute boost pulsation cycle
		final int tickBoostPeriod = 15 + 2 * random.nextInt(10);
		int tickBoost = (int) (ticksExisted % tickBoostPeriod);
		if (tickBoost >= tickBoostPeriod / 2) {
			tickBoost = tickBoostPeriod - tickBoost - 1;
		}
		final float cycleBoost = 2 * tickBoost / (float) tickBoostPeriod;
		float boost = 0.0F;
		if (cycleBoost > 0.7F) {
			boost = (cycleBoost - 0.6F) / 0.4F;
		}
		
		// compute number of rays
		// final int rayCount = 45 + (int) ((cycleRotation + cycleRotation * cycleRotation) * 15.0F);
		final int rayCount = rayCount_base + random.nextInt(10);
		
		// drawing preparation
		Tessellator tessellator = Tessellator.instance;
		RenderHelper.disableStandardItemLighting();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT | GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);
		GL11.glPushMatrix();
		GL11.glScalef(scaleX, scaleY, scaleZ);
		
		for (int i = 0; i < rayCount; i++) {
			GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F + cycleRotation * 90F, 0.0F, 0.0F, 1.0F);
			tessellator.startDrawing(6);
			float rayLength = random.nextFloat() * 20.0F + 5.0F + boost * 10.0F;
			float rayWidth  = random.nextFloat() *  2.0F + 1.0F + boost *  2.0F;
			tessellator.setColorRGBA_F(redIn, greenIn, blueIn, (int) (255F * (1.0F - boost)));
			tessellator.addVertex(0.0D              , 0.0D, 0.0D);
			tessellator.setColorRGBA_F(redOut, greenOut, blueOut, 0);
			tessellator.addVertex(-0.866D * rayWidth, rayLength, -0.5D * rayWidth);
			tessellator.addVertex( 0.866D * rayWidth, rayLength, -0.5D * rayWidth);
			tessellator.addVertex( 0.000D           , rayLength,  1.0D * rayWidth);
			tessellator.addVertex(-0.866D * rayWidth, rayLength, -0.5D * rayWidth);
			tessellator.draw();
		}
		
		// drawing closure
		GL11.glPopMatrix();
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glPopAttrib();
		RenderHelper.enableStandardItemLighting();
	}
}