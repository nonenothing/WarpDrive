package cr0s.warpdrive.render;

import cr0s.warpdrive.Commons;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCommons {
	
	private static final Minecraft minecraft = Minecraft.getMinecraft();
	
	protected static int colorGradient(final float gradient, final int start, final int end) {
		return Math.max(0, Math.min(255, start + Math.round(gradient * (end - start))));
	}
	
	protected static int colorARGBtoInt(final int alpha, final int red, final int green, final int blue) {
		return (Commons.clamp(0, 255, alpha) << 24)
		     + (Commons.clamp(0, 255, red  ) << 16)
			 + (Commons.clamp(0, 255, green) <<  8)
			 +  Commons.clamp(0, 255, blue );
	}
	
	// from net.minecraft.client.gui.Gui
	private static final float scaleUV = 0.00390625F;  // 1/256
	protected static void drawTexturedModalRect(final int x, final int y, final int u, final int v, final int sizeX, final int sizeY, final int zLevel) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV( x         , (y + sizeY), zLevel, scaleUV * u          , scaleUV * (v + sizeY));
		tessellator.addVertexWithUV((x + sizeX), (y + sizeY), zLevel, scaleUV * (u + sizeX), scaleUV * (v + sizeY));
		tessellator.addVertexWithUV((x + sizeX),  y         , zLevel, scaleUV * (u + sizeX), scaleUV *  v         );
		tessellator.addVertexWithUV( x         ,  y         , zLevel, scaleUV * u          , scaleUV *  v         );
		tessellator.draw();
	}
	
	public static int drawSplashAlarm(final int scaledWidth, final int scaledHeight, final String title, final String message) {
		// compute animation clock
		final double cycle = ((System.nanoTime() / 1000) % 0x200000) / (double) 0x200000;
		
		// start rendering
		GL11.glPushMatrix();
		GL11.glScalef(2.0F, 2.0F, 0.0F);
		
		int y = scaledHeight / 10;
		
		// bold title, single line, centered, with shadows
		final String textTitle = Commons.updateEscapeCodes("§l" + StatCollector.translateToLocal(title));
		minecraft.fontRenderer.drawString(textTitle,
		                                  scaledWidth / 4 - minecraft.fontRenderer.getStringWidth(textTitle) / 2,
		                                  y - minecraft.fontRenderer.FONT_HEIGHT,
		                                  colorARGBtoInt(230, 255, 32, 24),
		                                  true);
		
		// normal message, multi-lines, centered, without shadows
		final String textMessage = Commons.updateEscapeCodes(StatCollector.translateToLocal(message));
		final int alpha = 160 + (int) (85.0D * Math.sin(cycle * 2 * Math.PI));
		
		@SuppressWarnings("unchecked")
		final List<String> listMessages = minecraft.fontRenderer.listFormattedStringToWidth(textMessage, scaledWidth / 2);
		for (final String textLine : listMessages) {
			minecraft.fontRenderer.drawString(textLine,
			                                  scaledWidth / 4 - minecraft.fontRenderer.getStringWidth(textLine) / 2,
			                                  y,
			                                  colorARGBtoInt(alpha, 192, 64, 48),
			                                  false);
			y += minecraft.fontRenderer.FONT_HEIGHT;
		}
		
		// close rendering
		GL11.glPopMatrix();
		return alpha;
	}
}