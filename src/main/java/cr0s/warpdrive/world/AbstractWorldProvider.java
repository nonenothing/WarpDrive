package cr0s.warpdrive.world;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.render.RenderBlank;
import cr0s.warpdrive.render.RenderSpaceSky;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractWorldProvider extends WorldProvider {
	
	protected CelestialObject celestialObjectDimension = null;
	protected boolean isRemote;
	
	protected void updateCelestialObject() throws RuntimeException {
		if (dimensionId == 0) {
			throw new RuntimeException("Critical error: you can't use a WorldProvider before settings its dimension id!");
		}
		if (celestialObjectDimension == null) {
			isRemote = FMLCommonHandler.instance().getEffectiveSide().isClient();
			celestialObjectDimension = CelestialObjectManager.get(isRemote, dimensionId, 0, 0);
		}
	}
	
	@Override
	public String getSaveFolder() {
		updateCelestialObject();
		if (celestialObjectDimension == null) {
			throw new RuntimeException(String.format("Critical error: there's no celestial object defining %s dimension id %d, unable to proceed further",
			                                         isRemote ? "client" : "server", dimensionId));
		}
		return celestialObjectDimension.id;
	}
	
	@Override
	public String getDimensionName() {
		updateCelestialObject();
		if (celestialObjectDimension == null) {
			if (isRemote) {
				return String.format("DIM%d", dimensionId);
			} else {
				throw new RuntimeException(String.format("Critical error: there's no celestial object defining %s dimension id %d, unable to proceed further",
				                                         "server", dimensionId));
			}
		}
		return celestialObjectDimension.id;
	}
	
	@Override
	public boolean canCoordinateBeSpawn(int x, int z) {
		int y = worldObj.getTopSolidOrLiquidBlock(x, z);
		return y != 0;
	}
	
	@Override
	public ChunkCoordinates getEntrancePortalLocation() {
		return null;
	}
	
	/*
	@Override
	public ChunkCoordinates getRandomizedSpawnPoint() {
		ChunkCoordinates position = new ChunkCoordinates(worldObj.getSpawnPoint());
		// boolean isAdventure = worldObj.getWorldInfo().getGameType() == EnumGameType.ADVENTURE;
		int spawnFuzz = 100;
		int spawnFuzzHalf = spawnFuzz / 2;
		{
			position.posX += worldObj.rand.nextInt(spawnFuzz) - spawnFuzzHalf;
			position.posZ += worldObj.rand.nextInt(spawnFuzz) - spawnFuzzHalf;
			position.posY = 200;
		}
		
		if (worldObj.isAirBlock(position.posX, position.posY, position.posZ)) {
			worldObj.setBlock(position.posX, position.posY, position.posZ, Blocks.stone, 0, 2);
			worldObj.setBlock(position.posX + 1, position.posY + 1, position.posZ, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX + 1, position.posY + 2, position.posZ, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX - 1, position.posY + 1, position.posZ, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX - 1, position.posY + 2, position.posZ, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX, position.posY + 1, position.posZ + 1, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX, position.posY + 2, position.posZ + 1, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX, position.posY + 1, position.posZ - 1, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX, position.posY + 2, position.posZ - 1, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX, position.posY + 3, position.posZ, Blocks.glass, 0, 2);
			worldObj.setBlock(position.posX, position.posY, position.posZ, WarpDrive.blockAir, 15, 2);
			worldObj.setBlock(position.posX, position.posY + 1, position.posZ, WarpDrive.blockAir, 15, 2);
		}
		
		return position;
	}
	/**/
	
	// shared for getFogColor(), getStarBrightness()
	// @SideOnly(Side.CLIENT)
	protected static CelestialObject celestialObject = null;
	
	@SideOnly(Side.CLIENT)
	@Override
	public Vec3 getSkyColor(Entity cameraEntity, float partialTicks) {
		if (getCloudRenderer() == null) {
			setCloudRenderer(RenderBlank.getInstance());
		}
		if (getSkyRenderer() == null) {
			setSkyRenderer(RenderSpaceSky.getInstance());
		}
		
		celestialObject = cameraEntity.worldObj == null ? null : CelestialObjectManager.get(
		cameraEntity.worldObj,
		MathHelper.floor_double(cameraEntity.posX), MathHelper.floor_double(cameraEntity.posZ));
		if (celestialObject == null) {
			return Vec3.createVectorHelper(1.0D, 0.0D, 0.0D);
		} else {
			return Vec3.createVectorHelper(celestialObject.backgroundColor.red, celestialObject.backgroundColor.green, celestialObject.backgroundColor.blue);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Vec3 getFogColor(float celestialAngle, float par2) {
		final float factor = Commons.clamp(0.0F, 1.0F, MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F) * 2.0F + 0.5F);
		
		float red   = celestialObject == null ? 0.0F : celestialObject.colorFog.red;
		float green = celestialObject == null ? 0.0F : celestialObject.colorFog.green;
		float blue  = celestialObject == null ? 0.0F : celestialObject.colorFog.blue;
		float factorRed   = celestialObject == null ? 0.0F : celestialObject.factorFog.red;
		float factorGreen = celestialObject == null ? 0.0F : celestialObject.factorFog.green;
		float factorBlue  = celestialObject == null ? 0.0F : celestialObject.factorFog.blue;
		red   *= factor * factorRed   + (1.0F - factorRed  );
		green *= factor * factorGreen + (1.0F - factorGreen);
		blue  *= factor * factorBlue  + (1.0F - factorBlue );
		return Vec3.createVectorHelper(red, green, blue);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public float getStarBrightness(float partialTicks) {
		if (celestialObject == null) {
			return 0.0F;
		}
		final float starBrightnessVanilla = super.getStarBrightness(partialTicks);
		return celestialObject.baseStarBrightness + celestialObject.vanillaStarBrightness * starBrightnessVanilla;
	}
}