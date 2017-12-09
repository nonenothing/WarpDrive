package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.StarMapRegistry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SpaceWorldProvider extends AbstractWorldProvider {
	
	public SpaceWorldProvider() {
		worldChunkMgr = new WorldChunkManagerHell(WarpDrive.spaceBiome, 0.0F);
		hasNoSky = false;
	}
	
	@Override
	public boolean canRespawnHere() {
		return true;
	}
	
	@Override
	public boolean isSurfaceWorld() {
		return true;
	}
	
	@Override
	public int getAverageGroundLevel() {
		return 1;
	}
	
	@Override
	public double getHorizon() {
		return -256;
	}
	
	@Override
	public void updateWeather() {
		super.resetRainAndThunder();
	}
	
	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return WarpDrive.spaceBiome;
	}
	
	@Override
	public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
		super.setAllowedSpawnTypes(true, true);
	}
	
	@Override
	public float calculateCelestialAngle(long time, float partialTick) {
		return 0.0F;
	}
	
	@Override
	protected void generateLightBrightnessTable() {
		float f = 0.0F;
		
		for (int i = 0; i <= 15; ++i) {
			float f1 = 1.0F - i / 15.0F;
			lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean isSkyColored() {
		return false;
	}
	
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		if (player == null || player.worldObj == null) {
			WarpDrive.logger.error("Invalid player passed to getRespawnDimension: " + player);
			return 0;
		}
		return StarMapRegistry.getSpaceDimensionId(player.worldObj, (int) player.posX, (int) player.posZ);
	}
	
	@Override
	public IChunkProvider createChunkGenerator() {
		return new SpaceChunkProvider(worldObj, 45);
	}
	
	@Override
	public boolean canBlockFreeze(int x, int y, int z, boolean byWater) {
		return false;
	}
	
	@Override
	public boolean getWorldHasVoidParticles() {
		return false;
	}
	
	@Override
	public boolean isDaytime() {
		return true;
	}
	
	@Override
	public boolean canDoLightning(Chunk chunk) {
		return false;
	}
	
	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}
}