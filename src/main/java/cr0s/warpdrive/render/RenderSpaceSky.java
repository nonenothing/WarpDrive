package cr0s.warpdrive.render;

import java.util.Random;

import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Planet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class RenderSpaceSky extends IRenderHandler {
	private static RenderSpaceSky INSTANCE = null;
	
	public static RenderSpaceSky getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RenderSpaceSky();
		}
		return INSTANCE;
	}
	
	private static final ResourceLocation[] texturePlanets = {
		new ResourceLocation("warpdrive:textures/celestial/planet_green.png"),
		new ResourceLocation("warpdrive:textures/celestial/planet_yellow.png"),
		new ResourceLocation("warpdrive:textures/celestial/planet_red.png")
		};
	private static final ResourceLocation textureStar = new ResourceLocation("warpdrive:textures/celestial/star_yellow.png");
	
	public static final int callListStars = GLAllocation.generateDisplayLists(3);
	public static final int callListUpperSkyBox = callListStars + 1;
	public static final int callListBottomSkyBox = callListStars + 2;
	
	{
		// pre-generate the starfield
		GL11.glPushMatrix();
		GL11.glNewList(callListStars, GL11.GL_COMPILE);
		renderStars();
		GL11.glEndList();
		GL11.glPopMatrix();
		
		// pre-generate skyboxes
		final Tessellator tessellator = Tessellator.instance;
		
		GL11.glNewList(callListUpperSkyBox, GL11.GL_COMPILE);
		final int stepSize = 64;
		final int nbSteps = 256 / stepSize + 2;
		float y = 16F;
		for (int x = -stepSize * nbSteps; x <= stepSize * nbSteps; x += stepSize) {
			for (int z = -stepSize * nbSteps; z <= stepSize * nbSteps; z += stepSize) {
				tessellator.startDrawingQuads();
				tessellator.addVertex(x, y, z);
				tessellator.addVertex(x + stepSize, y, z);
				tessellator.addVertex(x + stepSize, y, z + stepSize);
				tessellator.addVertex(x, y, z + stepSize);
				tessellator.draw();
			}
		}
		GL11.glEndList();
		
		GL11.glNewList(callListBottomSkyBox, GL11.GL_COMPILE);
		y = -16F;
		tessellator.startDrawingQuads();
		for (int x = -stepSize * nbSteps; x <= stepSize * nbSteps; x += stepSize) {
			for (int z = -stepSize * nbSteps; z <= stepSize * nbSteps; z += stepSize) {
				tessellator.addVertex(x + stepSize, y, z);
				tessellator.addVertex(x, y, z);
				tessellator.addVertex(x, y, z + stepSize);
				tessellator.addVertex(x + stepSize, y, z + stepSize);
			}
		}
		tessellator.draw();
		GL11.glEndList();
	}
	
	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		boolean isSpace = world.provider == null || world.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID;
		
		final Tessellator tessellator = Tessellator.instance;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);
		
		// draw upper skybox
		/*
		final Vec3 skyColor = getCustomSkyColor();
		float skyColorRed   = (float) skyColor.xCoord * (1 - world.getStarBrightness(partialTicks) * 2);
		float skyColorGreen = (float) skyColor.yCoord * (1 - world.getStarBrightness(partialTicks) * 2);
		float skyColorBlue  = (float) skyColor.zCoord * (1 - world.getStarBrightness(partialTicks) * 2);
		float var8;

		if (mc.gameSettings.anaglyph) {
			final float var6 = (skyColorRed * 30.0F + skyColorGreen * 59.0F + skyColorBlue * 11.0F) / 100.0F;
			final float var7 = (skyColorRed * 30.0F + skyColorGreen * 70.0F) / 100.0F;
			var8 = (skyColorRed * 30.0F + skyColorBlue * 70.0F) / 100.0F;
			skyColorRed = var6;
			skyColorGreen = var7;
			skyColorBlue = var8;
		}
		
		// GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);
		GL11.glCallList(callListUpperSkyBox);
		// GL11.glDisable(GL11.GL_FOG);
		/**/
		
		// draw star systems
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		float starBrightness = 0.2F;
		if (world.provider != null) {
			starBrightness = world.provider.getStarBrightness(partialTicks);
		}
		if (starBrightness > 0.0F) {
			if (isSpace) {
				GL11.glColor4f(1.0F, 1.0F, 0.9F, starBrightness);
			} else {
				GL11.glColor4f(0.5F, 0.6F, 0.4F, starBrightness);
			}
			GL11.glCallList(callListStars);
		}
		
		// enable texture with alpha blending
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		
		// Star
		{
			GL11.glPushMatrix();
			final double starScale = isSpace ? 30.0D : 40.0D;
			final double starRange = 150.0D;    // max 190
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(0.3F * 360.0F, 1.0F, 0.0F, 0.0F);    // Vanilla is world.getCelestialAngle(partialTicks) * 360.0F
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, isSpace ? 1.0F : 0.3F);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(textureStar);
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-starScale, starRange, -starScale, 0.0D, 0.0D);
			tessellator.addVertexWithUV( starScale, starRange, -starScale, 1.0D, 0.0D);
			tessellator.addVertexWithUV( starScale, starRange,  starScale, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-starScale, starRange,  starScale, 0.0D, 1.0D);
			tessellator.draw();
			GL11.glPopMatrix();
		}
		
		// Planet
		/*
		{
			GL11.glPushMatrix();
			final double planetScale = 10.0D;
			final double planetRange = 140.0D;
			final float planetRotation = (float) (world.getSpawnPoint().posZ - mc.thePlayer.posZ) * 0.1F;
			GL11.glScalef(0.6F, 0.6F, 0.6F);
			GL11.glRotatef(planetRotation, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(190F, 1.0F, 0.0F, 0.0F);
			
			GL11.glColor4f(1.0F, 0.0F, 1.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(texturePlanet);
			
			// world.getMoonPhase();
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-planetScale, planetRange, -planetScale, 0, 1);
			tessellator.addVertexWithUV( planetScale, planetRange, -planetScale, 1, 1);
			tessellator.addVertexWithUV( planetScale, planetRange,  planetScale, 1, 0);
			tessellator.addVertexWithUV(-planetScale, planetRange,  planetScale, 0, 0);
			tessellator.draw();
			GL11.glScalef(1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
		/**/
		
		// Planets
		Vec3 playerCoordinates = mc.thePlayer.getPosition(partialTicks);
		for(Planet planet : WarpDriveConfig.PLANETS) {
			renderPlanet(mc, tessellator, planet, isSpace, playerCoordinates);
		}
		
		// final double playerAltitude = mc.thePlayer.getPosition(partialTicks).yCoord - world.getHorizon();
		
		// stratosphere box
		/*
		float var10;
		float var11;
		float var12;
		if (playerAltitude < 0.0D) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 12.0F, 0.0F);
			GL11.glCallList(callListBottomSkyBox);
			GL11.glPopMatrix();
			var10 = 1.0F;
			var11 = -((float) (playerAltitude + 65.0D));
			var12 = -var10;
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_I(0xFF800, 255);
			tessellator.addVertex(-var10, var11,  var10);
			tessellator.addVertex( var10, var11,  var10);
			tessellator.addVertex( var10, var12,  var10);
			tessellator.addVertex(-var10, var12,  var10);
			tessellator.addVertex(-var10, var12, -var10);
			tessellator.addVertex( var10, var12, -var10);
			tessellator.addVertex( var10, var11, -var10);
			tessellator.addVertex(-var10, var11, -var10);
			tessellator.addVertex( var10, var12, -var10);
			tessellator.addVertex( var10, var12,  var10);
			tessellator.addVertex( var10, var11,  var10);
			tessellator.addVertex( var10, var11, -var10);
			tessellator.addVertex(-var10, var11, -var10);
			tessellator.addVertex(-var10, var11,  var10);
			tessellator.addVertex(-var10, var12,  var10);
			tessellator.addVertex(-var10, var12, -var10);
			tessellator.addVertex(-var10, var12, -var10);
			tessellator.addVertex(-var10, var12,  var10);
			tessellator.addVertex( var10, var12,  var10);
			tessellator.addVertex( var10, var12, -var10);
			tessellator.draw();
		}
		/**/
		/*
		// draw bottom skybox relative to horizon
		GL11.glPushMatrix();
		GL11.glColor3f(0.30F, 0.30F, 0.30F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(texturePlanet);
		
		// GL11.glTranslatef(0.0F, (float)(16.0D - playerAltitude), 0.0F);
		GL11.glCallList(callListBottomSkyBox);
		GL11.glPopMatrix();
		/**/
		
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
	}
	
	static final double PLANET_FAR = 512.0D;
	static final double PLANET_ORBIT = 128.0D;
	private static void renderPlanet(Minecraft mc, Tessellator tessellator, final Planet planet, final boolean isSpace, final Vec3 playerCoordinates) {
		final double planetX = planet.spaceCenterX - playerCoordinates.xCoord;
		final double planetZ = planet.spaceCenterZ - playerCoordinates.zCoord;
		final double distanceToBorder = planet.isValidFromSpace((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
		final double distanceToCenter = Math.sqrt(planetX * planetX + planetZ * planetZ);
		
		final double transitionFar         = (Math.max(PLANET_FAR, Math.min(WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS, distanceToBorder)) - PLANET_FAR) / (WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS - PLANET_FAR);
		final double transitionApproaching = (Math.max(PLANET_ORBIT, Math.min(PLANET_FAR, distanceToBorder)) - PLANET_ORBIT) / (PLANET_FAR - PLANET_ORBIT);
		final double transitionOrbit       = Math.max(0.0D, Math.min(PLANET_ORBIT, distanceToBorder)) / PLANET_ORBIT;
		
		// simulating a non-planar universe...
		final double planetY_far = (planet.dimensionId + 99 % 100 - 50) * Math.log(distanceToCenter) / 4.0D;
		final double planetY = planetY_far * transitionApproaching;
		
		// render range is only used for Z-ordering
		final double renderRange = 180.0D + 10.0D * (distanceToCenter / WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS);
		
		// render size is 1 at space border range
		// render size is 10 at approaching range
		// render size is 90 at orbit range
		// render size is min(1000, planet border) at orbit range
		final double renderSize = Math.min(1000.0D, Math.max(planet.borderSizeX, planet.borderSizeZ)) * (1.0D - transitionOrbit)
								+ 90.0D * (transitionOrbit < 1.0D ? transitionOrbit : (1.0D - transitionApproaching))
								+ 5.0D * (transitionApproaching < 1.0D ? transitionApproaching : (1.0D - transitionFar))
								+ 1.0D * transitionFar;
		
		// angles
		@SuppressWarnings("SuspiciousNameCombination")
		final double angleH = Math.atan2(planetX, planetZ);
		@SuppressWarnings("SuspiciousNameCombination")
		final double angleV_far = Math.atan2(Math.sqrt(planetX * planetX + planetZ * planetZ), planetY);
		final double angleV = Math.PI * (1.0D - transitionOrbit) + angleV_far * transitionOrbit;
		final double angleS = 0.15D * planet.dimensionId; // + (world.getTotalWorldTime() + partialTicks) * Math.PI / 6000.0D;
		
		// pre-computations
		final double sinH = Math.sin(angleH);
		final double cosH = Math.cos(angleH);
		final double sinV = Math.sin(angleV);
		final double cosV = Math.cos(angleV);
		final double sinS = Math.sin(angleS);
		final double cosS = Math.cos(angleS);
		
		GL11.glPushMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, isSpace ? 1.0F : 0.2F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(texturePlanets[Math.abs(planet.dimensionId) % 3]);
		tessellator.startDrawingQuads();
		for (int indexVertex = 0; indexVertex < 4; indexVertex++) {
			final double offset1 = ((indexVertex     & 2) - 1) * renderSize;
			final double offset2 = ((indexVertex + 1 & 2) - 1) * renderSize;
			final double valV = offset1 * cosS - offset2 * sinS;
			final double valH = offset2 * cosS + offset1 * sinS;
			final double y = valV * sinV + renderRange * cosV;
			final double valD = renderRange * sinV - valV * cosV;
			final double x = valD * sinH - valH * cosH;
			final double z = valH * sinH + valD * cosH;
			tessellator.addVertexWithUV(x, y, z, (indexVertex & 2) / 2, (indexVertex + 1 & 2) / 2);
		}
		tessellator.draw();
		
		GL11.glPopMatrix();
	}
	
	private void renderStars() {
		final Random rand = new Random(10842L);
		final boolean hasMoreStars = rand.nextBoolean() || rand.nextBoolean();
		final Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		
		for (int indexStars = 0; indexStars < (hasMoreStars ? 20000 : 6000); indexStars++) {
			double randomX = rand.nextDouble() * 2.0D - 1.0D;
			double randomY = rand.nextDouble() * 2.0D - 1.0D;
			double randomZ = rand.nextDouble() * 2.0D - 1.0D;
			final double lambda = 1.2D;
			final double renderSize = 0.10F + 0.03F * Math.log(1.0D - rand.nextDouble()) / (-lambda); // random.nextFloat() * 0.5F;
			double randomLength = randomX * randomX + randomY * randomY + randomZ * randomZ;
			
			if (randomLength < 1.0D && randomLength > 0.01D) {
				// forcing Z-order
				randomLength = 1.0D / Math.sqrt(randomLength);
				randomX *= randomLength;
				randomY *= randomLength;
				randomZ *= randomLength;
				
				// scaling
				final double x0 = randomX * 100.0D;
				final double y0 = randomY * 100.0D;
				final double z0 = randomZ * 100.0D;
				
				// angles
				@SuppressWarnings("SuspiciousNameCombination")
				final double angleH = Math.atan2(randomX, randomZ);
				@SuppressWarnings("SuspiciousNameCombination")
				final double angleV = Math.atan2(Math.sqrt(randomX * randomX + randomZ * randomZ), randomY);
				final double angleS = rand.nextDouble() * Math.PI * 2.0D;
				
				// pre-computations
				final double sinH = Math.sin(angleH);
				final double cosH = Math.cos(angleH);
				final double sinV = Math.sin(angleV);
				final double cosV = Math.cos(angleV);
				final double sinS = Math.sin(angleS);
				final double cosS = Math.cos(angleS);
				
				for (int indexVertex = 0; indexVertex < 4; indexVertex++) {
					final double valZero = 0.0D;
					final double offset1 = ((indexVertex     & 2) - 1) * renderSize;
					final double offset2 = ((indexVertex + 1 & 2) - 1) * renderSize;
					final double valV = offset1 * cosS - offset2 * sinS;
					final double valH = offset2 * cosS + offset1 * sinS;
					final double y1 = valV * sinV + valZero * cosV;
					final double valD = valZero * sinV - valV * cosV;
					final double x1 = valD * sinH - valH * cosH;
					final double z1 = valH * sinH + valD * cosH;
					tessellator.addVertex(x0 + x1, y0 + y1, z0 + z1);
				}
			}
		}
		
		tessellator.draw();
	}
	
	private static Vec3 getCustomSkyColor() {
		return Vec3.createVectorHelper(0.26796875D, 0.1796875D, 0.0D);
	}
	
	public static float getSkyBrightness(float par1) {
		final float var2 = FMLClientHandler.instance().getClient().theWorld.getCelestialAngle(par1);
		float var3 = 1.0F - (MathHelper.sin(var2 * (float) Math.PI * 2.0F) * 2.0F + 0.25F);

		if (var3 < 0.0F) {
			var3 = 0.0F;
		}

		if (var3 > 1.0F) {
			var3 = 1.0F;
		}

		return var3 * var3 * 1F;
	}
}
