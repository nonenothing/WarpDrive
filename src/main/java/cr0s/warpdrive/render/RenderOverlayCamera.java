package cr0s.warpdrive.render;

import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumCameraType;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

@SideOnly(Side.CLIENT)
public class RenderOverlayCamera {
	
	private static final int ANIMATION_FRAMES = 200;
	
	private Minecraft minecraft = Minecraft.getMinecraft();
	private int frameCount = 0;
	
	private void renderOverlay(final int scaledWidth, final int scaledHeight) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		
		try {
			final String strHelp;
			if (ClientCameraHandler.overlayType == EnumCameraType.SIMPLE_CAMERA) {
				minecraft.getTextureManager().bindTexture(new ResourceLocation("warpdrive", "textures/blocks/detection/camera-overlay.png"));
				strHelp = "Left click to zoom / Right click to exit";
			} else {
				minecraft.getTextureManager().bindTexture(new ResourceLocation("warpdrive", "textures/blocks/weapon/laser_camera-overlay.png"));
				strHelp = "Left click to zoom / Right click to exit / Space to fire";
			}
			
			final Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(       0.0D, scaledHeight, -90.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV(scaledWidth, scaledHeight, -90.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV(scaledWidth,         0.0D, -90.0D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(       0.0D,         0.0D, -90.0D, 0.0D, 0.0D);
			tessellator.draw();
			
			frameCount++;
			if (frameCount >= ANIMATION_FRAMES) {
				frameCount = 0;
			}
			final float time = Math.abs(frameCount * 2.0F / ANIMATION_FRAMES - 1.0F);
			final int color = (RenderCommons.colorGradient(time, 0x40, 0xA0) << 16)
			                + (RenderCommons.colorGradient(time, 0x80, 0x00) << 8)
			                +  RenderCommons.colorGradient(time, 0x80, 0xFF);
			minecraft.fontRenderer.drawString(strHelp,
			                                  (scaledWidth - minecraft.fontRenderer.getStringWidth(strHelp)) / 2,
			                                  (int)(scaledHeight * 0.19) - minecraft.fontRenderer.FONT_HEIGHT,
			                                  color, true);
			
			final String strZoom = "Zoom " + (ClientCameraHandler.originalFOV / minecraft.gameSettings.fovSetting) + "x";
			minecraft.fontRenderer.drawString(strZoom,
			                                  (int) (scaledWidth * 0.91) - minecraft.fontRenderer.getStringWidth(strZoom),
			                                  (int) (scaledHeight * 0.81),
			                                  0x40A080, true);
			
			if (WarpDriveConfig.LOGGING_CAMERA) {
				minecraft.fontRenderer.drawString(ClientCameraHandler.overlayLoggingMessage,
				                                  (scaledWidth - minecraft.fontRenderer.getStringWidth(ClientCameraHandler.overlayLoggingMessage)) / 2,
				                                  (int)(scaledHeight * 0.19),
				                                  0xFF008F, true);
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@SubscribeEvent
	public void onRender(final RenderGameOverlayEvent.Pre event) {
		if (ClientCameraHandler.isOverlayEnabled) {
			if (event.type == ElementType.HELMET) {
				renderOverlay(event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
			} else if (event.type == ElementType.AIR
					|| event.type == ElementType.ARMOR
					|| event.type == ElementType.BOSSHEALTH
					|| event.type == ElementType.CROSSHAIRS
					|| event.type == ElementType.EXPERIENCE
					|| event.type == ElementType.FOOD
					|| event.type == ElementType.HEALTH
					|| event.type == ElementType.HEALTHMOUNT
					|| event.type == ElementType.HOTBAR
					|| event.type == ElementType.TEXT) {
				// Don't render other GUI parts
				if (event.isCancelable()) {
					event.setCanceled(true);
				}
			}
		}
	}
}