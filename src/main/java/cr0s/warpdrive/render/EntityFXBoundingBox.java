package cr0s.warpdrive.render;

import cr0s.warpdrive.data.Vector3;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class EntityFXBoundingBox extends Particle {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("warpdrive", "textures/particle/bounding_box.png");
	
	private Vector3 min;
	private Vector3 max;
	
	public EntityFXBoundingBox(final World world, final Vector3 position, final Vector3 min, final Vector3 max,
	                           final float red, final float green, final float blue, final int age) {
		super(world, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
		this.setRBGColorF(red, green, blue);
		this.setSize(0.02F, 0.02F);
		this.isCollided = false;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.min = min;
		this.max = max;
		this.particleMaxAge = age;
		
		// kill the particle if it's too far away
		/*
		final Entity entityRender = Minecraft.getMinecraft().getRenderViewEntity();
		int visibleDistance = 300;

		if (!Minecraft.getMinecraft().gameSettings.fancyGraphics) {
			visibleDistance = 100;
		}

		if (entityRender.getDistance(posX, posY, posZ) > visibleDistance) {
			particleMaxAge = 0;
		}
		/**/
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		
		if (particleAge++ >= particleMaxAge) {
			setExpired();
		}
	}
	
	@Override
	public void renderParticle(final VertexBuffer vertexBuffer, final Entity entityIn, final float partialTick,
	                           final float rotationX, final float rotationZ, final float rotationYZ, final float rotationXY, final float rotationXZ) {
		GL11.glPushMatrix();
		
		// final float rot = (worldObj.provider.getWorldTime() % (360 / rotationSpeed) + partialTick) * rotationSpeed;
		
        // alpha starts at 50%, vanishing to 10% during last ticks
		float alpha = 0.45F;
		if (particleMaxAge - particleAge <= 2) {
			alpha = 0.35F; // 0.45F - (1 - (particleMaxAge - particleAge)) * 0.35F;
		} else if (particleAge < 1) {
			alpha = 0.10F;
		}
		
		// final double relativeTime = worldObj.getTotalWorldTime() + partialTick;
		// final double uOffset = (float) (-relativeTime * 0.3D - MathHelper.floor_double(-relativeTime * 0.15D));
		// final double vOffset = (float) (-relativeTime * 0.2D - MathHelper.floor_double(-relativeTime * 0.1D));
		
		// box position
		final double relativeTime = Math.abs(worldObj.getTotalWorldTime() % 64L + partialTick) / 64.0D;
		final double sizeOffset = 0.01F * (1.0F + (float) Math.sin(relativeTime * Math.PI * 2));
		final double xMin = min.x - posX - sizeOffset;
		final double xMax = max.x - posX + sizeOffset;
		final double yMin = min.y - posY - sizeOffset;
		final double yMax = max.y - posY + sizeOffset;
		final double zMin = min.z - posZ - sizeOffset;
		final double zMax = max.z - posZ + sizeOffset;
		
		// texture coordinates
		final double uvScale = 1.0D;
		final double uv_xMin = xMin / uvScale + 0.5D;
		final double uv_xMax = xMax / uvScale + 0.5D;
		final double uv_yMin = yMin / uvScale + 0.5D;
		final double uv_yMax = yMax / uvScale + 0.5D;
		final double uv_zMin = zMin / uvScale + 0.5D;
		final double uv_zMax = zMax / uvScale + 0.5D;
		
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);
		
		final float xx = (float)(prevPosX + (posX - prevPosX) * partialTick - interpPosX);
		final float yy = (float)(prevPosY + (posY - prevPosY) * partialTick - interpPosY);
		final float zz = (float)(prevPosZ + (posZ - prevPosZ) * partialTick - interpPosZ);
		GL11.glTranslated(xx, yy, zz);
		final int brightness = 200;
		
		final Tessellator tessellator = Tessellator.getInstance();
		
		// x planes
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		// @TODO MC1.10 tessellator.setBrightness(brightness);
		GlStateManager.color(particleRed, particleGreen, particleBlue, alpha);
		vertexBuffer.pos(xMin, yMin, zMin).tex(uv_yMin, uv_zMin).endVertex();
		vertexBuffer.pos(xMin, yMin, zMax).tex(uv_yMin, uv_zMax).endVertex();
		vertexBuffer.pos(xMin, yMax, zMax).tex(uv_yMax, uv_zMax).endVertex();
		vertexBuffer.pos(xMin, yMax, zMin).tex(uv_yMax, uv_zMin).endVertex();
		tessellator.draw();
		
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		// @TODO MC1.10 tessellator.setBrightness(brightness);
		GlStateManager.color(particleRed, particleGreen, particleBlue, alpha);
		vertexBuffer.pos(xMax, yMin, zMin).tex( uv_yMin, uv_zMin).endVertex();
		vertexBuffer.pos(xMax, yMin, zMax).tex( uv_yMin, uv_zMax).endVertex();
		vertexBuffer.pos(xMax, yMax, zMax).tex( uv_yMax, uv_zMax).endVertex();
		vertexBuffer.pos(xMax, yMax, zMin).tex( uv_yMax, uv_zMin).endVertex();
		tessellator.draw();
		
		// y planes
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		// @TODO MC1.10 tessellator.setBrightness(brightness);
		GlStateManager.color(particleRed, particleGreen, particleBlue, alpha);
		vertexBuffer.pos(xMin, yMin, zMin).tex(uv_xMin, uv_zMin).endVertex();
		vertexBuffer.pos(xMin, yMin, zMax).tex(uv_xMin, uv_zMax).endVertex();
		vertexBuffer.pos(xMax, yMin, zMax).tex(uv_xMax, uv_zMax).endVertex();
		vertexBuffer.pos(xMax, yMin, zMin).tex(uv_xMax, uv_zMin).endVertex();
		tessellator.draw();
		
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		// @TODO MC1.10 tessellator.setBrightness(brightness);
		GlStateManager.color(particleRed, particleGreen, particleBlue, alpha);
		vertexBuffer.pos(xMin, yMax, zMin).tex(uv_xMin, uv_zMin).endVertex();
		vertexBuffer.pos(xMin, yMax, zMax).tex(uv_xMin, uv_zMax).endVertex();
		vertexBuffer.pos(xMax, yMax, zMax).tex(uv_xMax, uv_zMax).endVertex();
		vertexBuffer.pos(xMax, yMax, zMin).tex(uv_xMax, uv_zMin).endVertex();
		tessellator.draw();
		
		// z planes
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		// @TODO MC1.10 tessellator.setBrightness(brightness);
		GlStateManager.color(particleRed, particleGreen, particleBlue, alpha);
		vertexBuffer.pos(xMin, yMin, zMin).tex(uv_xMin, uv_yMin).endVertex();
		vertexBuffer.pos(xMin, yMax, zMin).tex(uv_xMin, uv_yMax).endVertex();
		vertexBuffer.pos(xMax, yMax, zMin).tex(uv_xMax, uv_yMax).endVertex();
		vertexBuffer.pos(xMax, yMin, zMin).tex(uv_xMax, uv_yMin).endVertex();
		tessellator.draw();
		
		vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		// @TODO MC1.10 tessellator.setBrightness(brightness);
		GlStateManager.color(particleRed, particleGreen, particleBlue, alpha);
		vertexBuffer.pos(xMin, yMin, zMax).tex(uv_xMin, uv_yMin).endVertex();
		vertexBuffer.pos(xMin, yMax, zMax).tex(uv_xMin, uv_yMax).endVertex();
		vertexBuffer.pos(xMax, yMax, zMax).tex(uv_xMax, uv_yMax).endVertex();
		vertexBuffer.pos(xMax, yMin, zMax).tex(uv_xMax, uv_yMin).endVertex();
		tessellator.draw();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("textures/particle/particles.png"));
	}
}